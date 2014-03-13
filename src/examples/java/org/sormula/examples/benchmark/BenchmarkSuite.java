/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2012 Jeff Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sormula.examples.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;
import org.sormula.examples.benchmark.SormulaBenchmarkThread.CacheType;
import org.sormula.log.ClassLogger;
import org.sormula.operation.monitor.ElapsedTime;


public class BenchmarkSuite extends ExampleBase
{
    private static final ClassLogger log = new ClassLogger();
    long seed;
    AtomicInteger idGenerator; // assumes this is only application that is inserting
    int maximumOperations;
    int maximumRowsPerOperation;
    
    
    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            new BenchmarkSuite(100, 200);
        }
        else
        {
            new BenchmarkSuite(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
    }
    
    
    public BenchmarkSuite(int maximumOperations, int maximumRowsPerOperation) throws Exception
    {
        this.maximumOperations = maximumOperations;
        this.maximumRowsPerOperation = maximumRowsPerOperation;
        
        openDatabase();
        initTable();
        initSeed();
        idGenerator = new AtomicInteger(1001);
        runBenchmarks();
        closeDatabase();        
    }
    
    
    @Override
    public String getSchema() // protected to public interface
    {
        return super.getSchema();
    }
    
    
    @Override
    public String getSchemaPrefix() // protected to public interface
    {
        return super.getSchemaPrefix();
    }


    public long getSeed()
    {
        return seed;
    }

    
    public int nextId()
    {
        return idGenerator.getAndIncrement();
    }
    
    
    public int getMaximumOperations()
    {
        return maximumOperations;
    }
    
    
    public int getMaximumRowsPerOperation()
    {
        return maximumRowsPerOperation;
    }
    
    
    protected void initTable() throws Exception
    {
        String tableName = getSchemaPrefix() + Benchmark.class.getSimpleName();
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER PRIMARY KEY," +
                " description VARCHAR(60)," +
                " integer2 INTEGER," +
                " boolean1 " + getBooleanDDL() + "," + 
                " boolean2 " + getBooleanDDL() + "," + 
                " float1 DECIMAL(6,2)," +
                " float2 DECIMAL(6,2)," +
                " double1 DECIMAL(8,3)," +
                " double2 DECIMAL(8,3)," +
                " short1 SMALLINT," +
                " short2 SMALLINT," +
                " javaDate TIMESTAMP," +
                " sqlDate DATE," +
                " sqlTimestamp TIMESTAMP)" 
        );
    }
    
    
    protected void initSeed()
    {
        // allow seed to be supplied for repeatable benchmarks
        String seedProperty = System.getProperty("seed", "");
        if (seedProperty.length() == 0)
        {
            seed = System.currentTimeMillis();
            log.info("using random seed=" + seed);
        }
        else
        {
            seed = Long.parseLong(seedProperty);
            log.info("using property seed=" + seed);
        }
    }
    
    
    protected void runBenchmarks() throws Exception
    {
        // NOTE: each thread will use same seed so each will perform the same 
        // operations in the same order for the same quantities
        
        log.info("create benchmark threads");
        List<BenchmarkThread> benchmarkThreads = new ArrayList<BenchmarkThread>();
        benchmarkThreads.add(new SormulaBenchmarkMultiResource(this, CacheType.NONE));
        benchmarkThreads.add(new SormulaBenchmarkMultiResource(this, CacheType.READ_ONLY));
        benchmarkThreads.add(new SormulaBenchmarkMultiResource(this, CacheType.READ_WRITE)); 
        benchmarkThreads.add(new JdbcBenchmarkMultiResource(this));
        benchmarkThreads.add(new SormulaBenchmarkSingleResource(this, CacheType.NONE));
        benchmarkThreads.add(new SormulaBenchmarkSingleResource(this, CacheType.READ_ONLY));
        benchmarkThreads.add(new SormulaBenchmarkSingleResource(this, CacheType.READ_WRITE));
        benchmarkThreads.add(new JdbcBenchmarkSingleResource(this));
        Collections.shuffle(benchmarkThreads); // random order
        
        // perform in separate loops so that similar things occur at the similar times  
        for (BenchmarkThread bt : benchmarkThreads)
        {
            bt.open();
            bt.normalize();
        }

        log.info("start benchmark threads");
        for (BenchmarkThread bt : benchmarkThreads) bt.start();

        log.info("wait for benchmark threads");
        for (BenchmarkThread bt : benchmarkThreads) bt.join();
        
        for (BenchmarkThread bt : benchmarkThreads) bt.close();
        
        // verify all benchmarks are the same
        boolean sameCounts = true;
        int remainingCheck = -1;
        int updatedCheck = -1;
        Database database = new Database(getConnection(), getSchema());
        Table<Benchmark> benchmarkTable = new Table<Benchmark>(database, Benchmark.class);
        for (BenchmarkThread bt : benchmarkThreads)
        {
            int remaining = benchmarkTable.<Integer>selectCount("id", "forDescription", bt.getBenchmarkName());
            int updated   = benchmarkTable.<Integer>selectCount("id", "forUpdateMarker", 
                    bt.getBenchmarkName(), BenchmarkThread.UPDATE_MARKER);
            log.info("remaining=" + remaining + " updated=" + updated + " " + bt.getBenchmarkName());
            
            if (remainingCheck == -1)
            {
                // first
                remainingCheck = remaining;
                updatedCheck = updated;
            }
            else if (remainingCheck != remaining || updatedCheck != updated)
            {
                sameCounts = false;
            }
        }
        database.close();
        if (!sameCounts) log.warn("row counts are not the same; benchmarks did not perform same operations?");

        // show fastest to slowest elapsed time
        Collections.sort(benchmarkThreads, new Comparator<BenchmarkThread>() {
            public int compare(BenchmarkThread o1, BenchmarkThread o2)
            {
                long t1 = o1.getElapsedTime().getTime();
                long t2 = o2.getElapsedTime().getTime();
                if (t1 > t2) return 1;
                if (t1 < t2) return -1;
                return 0;
            }
        });
        
        log.info("fastest to slowest:");
        for (BenchmarkThread bt : benchmarkThreads)
        {
            ElapsedTime elapsedTime = bt.getElapsedTime();
            log.info(elapsedTime.getFormattedTime() + " " + elapsedTime.getName());
        }
    }
}
