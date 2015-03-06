/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2014 Jeff Miller
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.log.ClassLogger;
import org.sormula.operation.monitor.ElapsedTime;


/** 
 * Base thread for all benchmarks. The {@link #run()} method contains the algorithm 
 * for random database operations.
 */
public abstract class BenchmarkThread extends Thread
{
    private static final ClassLogger log = new ClassLogger();
    static final Integer UPDATE_MARKER = 1234567890;
    
    BenchmarkSuite benchmarkSuite;
    String benchmarkName;
    Random suiteRandom;
    Random threadRandom;
    Connection connection;
    ElapsedTime elapsedTime;
    List<Integer> insertedIds;
    int remainingCount;
    int updatedCount;
    
    
    public BenchmarkThread(BenchmarkSuite benchmarkSuite, String benchmarkName)
    {
        this.benchmarkSuite = benchmarkSuite;
        this.benchmarkName = benchmarkName;                
        suiteRandom = new Random(benchmarkSuite.getSeed()); // same for all threads in suite
        threadRandom = new Random();                        // different for each thread
        elapsedTime = new ElapsedTime(benchmarkName);
        elapsedTime.setIgnoreFirst(false);
        insertedIds = new ArrayList<>(benchmarkSuite.getMaximumOperations() * 
                benchmarkSuite.getMaximumRowsPerOperation() / 10);
    }
    
    
    abstract protected void beginOperation() throws Exception;
    abstract protected void endOperation() throws Exception;
    abstract protected void select(int quantity) throws Exception;
    abstract protected void insert(int quantity) throws Exception;
    abstract protected void update(int quantity) throws Exception;
    abstract protected void delete(int quantity) throws Exception;


    @Override
    public void run()
    {
        try
        {
            int maximumOperations = benchmarkSuite.getMaximumOperations();
            for (int i = 1; i <= maximumOperations; ++i)
            {
                int quantity = suiteRandom.nextInt(benchmarkSuite.getMaximumRowsPerOperation()); 
            
                // random operation
                switch (suiteRandom.nextInt(4))
                {
                    case 0:
                        if (log.isDebugEnabled()) log.debug(i + " select " + quantity + " " + benchmarkName);
                        select(quantity); 
                        break;
                        
                    case 1:
                        if (log.isDebugEnabled()) log.debug(i + " insert " + quantity + " " + benchmarkName);
                        insert(quantity);
                        break;
                        
                    case 2:
                        if (log.isDebugEnabled()) log.debug(i + " update " + quantity + " " + benchmarkName);
                        update(quantity);
                        break;
                        
                    case 3:
                        if (log.isDebugEnabled()) log.debug(i + " delete " + quantity + " " + benchmarkName);
                        delete(quantity);
                        break;
                }
                
                // attempt random distribution of thread execution 
                //Thread.sleep(threadRandom.nextInt(1000));
                Thread.yield();
                
                if (i % 50 == 0) log.info("completed " + i + " " + benchmarkName);
            }
        }
        catch (Exception e)
        {
            log.error("benchmark error", e);
        }
    }
    
    
    public String getBenchmarkName()
    {
        return benchmarkName;
    }


    public ElapsedTime getElapsedTime()
    {
        return elapsedTime;
    }

    
    public void selectCounts() throws Exception
    {
        Database database = new Database(getConnection(), benchmarkSuite.getSchema());
        Table<Benchmark> benchmarkTable = new Table<>(database, Benchmark.class);
        remainingCount = benchmarkTable.<Integer>selectCount("id", "forDescription", benchmarkName);
        updatedCount   = benchmarkTable.<Integer>selectCount("id", "forUpdateMarker", benchmarkName, UPDATE_MARKER);
        database.close();
    }
    

    public int getRemainingCount()
    {
        return remainingCount;
    }


    public int getUpdatedCount()
    {
        return updatedCount;
    }


    public void open() throws Exception
    {
        if (log.isDebugEnabled()) log.debug("open() " + benchmarkName);
        connection = benchmarkSuite.createConnection();
    }
    
    
    public void close() throws Exception
    {
        if (log.isDebugEnabled()) log.debug("close() " + benchmarkName);
        connection.close();
    }
    
    
    public void normalize() throws Exception
    {
        // do 1 each operation without timing to avoid skewing time from
        // classloading for threads that are the first to use classes common to all threads
        if (log.isDebugEnabled()) log.debug("normalize() " + benchmarkName);
        insert(1);
        select(1);
        update(1);
        delete(1);
        
        // ignore time used so far
        elapsedTime = new ElapsedTime(benchmarkName);
    }
    

    protected Connection getConnection()
    {
        return connection;
    }
    
    
    protected Benchmark newBenchmark()
    {
        int id = benchmarkSuite.nextId();
        insertedIds.add(id); // remember for random selects
        
        Benchmark benchmark = new Benchmark();
        benchmark.setId(id);
        benchmark.setDescription(benchmarkName);
        benchmark.setInteger2(suiteRandom.nextInt());
        benchmark.setBoolean1(suiteRandom.nextBoolean());
        benchmark.setBoolean2(suiteRandom.nextBoolean());
        benchmark.setFloat1(suiteRandom.nextFloat());
        benchmark.setFloat2(suiteRandom.nextFloat());
        benchmark.setDouble1(suiteRandom.nextDouble());
        benchmark.setDouble2(suiteRandom.nextDouble());
        benchmark.setShort1((short)suiteRandom.nextInt());
        benchmark.setShort2((short)suiteRandom.nextInt());
        
        long randomMilliSeconds = suiteRandom.nextInt(Integer.MAX_VALUE) * 1511L;
        benchmark.setJavaDate(new java.util.Date(randomMilliSeconds));
        benchmark.setSqlDate(new java.sql.Date(randomMilliSeconds));
        benchmark.setSqlTimestamp(new java.sql.Timestamp(randomMilliSeconds));
        
        return benchmark;
    }
    
    
    protected List<Benchmark> newBenchmarks(int quantity)
    {
        List<Benchmark> benchmarks = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; ++i) benchmarks.add(newBenchmark());
        return benchmarks;
    }
    
    
    protected List<Integer> getRandomIds(int quantity)
    {
        List<Integer> randomIds = new ArrayList<>(quantity);
        Collections.shuffle(insertedIds, suiteRandom);
        
        int max = Math.min(insertedIds.size(), quantity);
        for (int i = 0; i < max; ++i) randomIds.add(insertedIds.get(i));
        
        return randomIds;
    }
    
    
    protected List<Integer> removeRandomIds(int quantity)
    {
        List<Integer> randomIds = getRandomIds(quantity);
        insertedIds.removeAll(randomIds);
        return randomIds;
    }
    
    
    /**
     * Set a specific value for integer2 member as a way to know which rows were updated.
     * <p>
     * This is used to check that all rows have the same number of updated rows. There is no
     * problem if a new benchmark is created with a random value for integer2 that is the same as
     * the update marker since all threads will have the same anomolly.
     * 
     * @param benchmarks benchmark rows to affect
     */
    protected void setUpdateMarker(List<Benchmark> benchmarks)
    {
        elapsedTime.pause(); // don't include in time
        for (Benchmark b : benchmarks) b.setInteger2(UPDATE_MARKER);
        elapsedTime.resume();
    }
}
