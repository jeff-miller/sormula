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

import java.util.List;

import org.sormula.Table;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.UpdateOperation;


/** 
 * {@link BenchmarkThread} that uses Sormula operations. The same resources are
 * used for all operations.
 */
public class SormulaBenchmarkSingleResource extends SormulaBenchmarkThread
{
    ListSelectOperation<Benchmark> selectOperation;
    InsertOperation<Benchmark> insertOperation;
    UpdateOperation<Benchmark> updateOperation;
    DeleteOperation<Benchmark> deleteOperation;
    
    
    public SormulaBenchmarkSingleResource(BenchmarkSuite benchmarkSuite, CacheType cacheType)
    {
        super(benchmarkSuite, "Sormula single resource", cacheType);
    }
    

    @Override
    public void open() throws Exception
    {
        super.open();
        
        elapsedTime.start();
        createDatabase();
        Table<Benchmark> table = database.getTable(Benchmark.class);
        insertOperation = new InsertOperation<>(table);
        selectOperation = createSelectForDescription();
        updateOperation = new UpdateOperation<>(table);
        deleteOperation = new DeleteOperation<>(table);
        elapsedTime.stop();
    }


    @Override
    public void close() throws Exception
    {
        // use these to get SQL string for use in JdbcBenchMarkThread's 
        //new ClassLogger().info(insertOperation.getPreparedSql());
        //new ClassLogger().info(selectOperation.getPreparedSql());
        //new ClassLogger().info(updateOperation.getPreparedSql());

        elapsedTime.start();
        selectOperation.close();
        insertOperation.close();
        updateOperation.close();
        deleteOperation.close();
        database.close();
        elapsedTime.stop();
        
        super.close();
    }


    @Override
    protected void beginOperation() throws Exception
    {
        elapsedTime.start();
        database.getTransaction().begin();
    }


    @Override
    protected void endOperation() throws Exception
    {
        database.getTransaction().commit();
        elapsedTime.stop();
    }


    protected void select(int quantity) throws Exception
    {
        beginOperation();
        selectLikeBenchmarkName(selectOperation, quantity);
        endOperation();
    }
    
    
    protected void insert(int quantity) throws Exception
    {
        List<Benchmark> benchmarks = newBenchmarks(quantity);
        beginOperation();
        insertOperation.insertAll(benchmarks);
        endOperation();
    }
    
    
    protected void update(int quantity) throws Exception
    {
        List<Integer> randomIds = getRandomIds(quantity);
        beginOperation();
        List<Benchmark> benchmarks = selectBenchmarks(randomIds);
        setUpdateMarker(benchmarks);
        updateOperation.updateAll(benchmarks);
        endOperation();
    }

    
    protected void delete(int quantity) throws Exception
    {
        List<Integer> randomIds = removeRandomIds(quantity);
        beginOperation();
        List<Benchmark> benchmarks = selectBenchmarks(randomIds);
        deleteOperation.deleteAll(benchmarks);
        endOperation();
    }
}
