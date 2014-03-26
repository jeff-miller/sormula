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
import org.sormula.operation.ListSelectOperation;


/** 
 * {@link BenchmarkThread} that uses Sormula operations. New resources are
 * created each time an operation is performed.
 */
public class SormulaBenchmarkMultiResource extends SormulaBenchmarkThread
{
    public SormulaBenchmarkMultiResource(BenchmarkSuite benchmarkSuite, CacheType cacheType)
    {
        super(benchmarkSuite, "Sormula multi resource", cacheType);
    }


    @Override
    protected void beginOperation() throws Exception
    {
        elapsedTime.start();
        createDatabase();
        database.getTransaction().begin();
    }


    @Override
    protected void endOperation() throws Exception
    {
        database.getTransaction().commit();
        database.close();
        elapsedTime.stop();
    }
    

    protected void select(int quantity) throws Exception
    {
        beginOperation();
        try (ListSelectOperation<Benchmark> selectOperation = createSelectForDescription())
        {
            selectLikeBenchmarkName(selectOperation, quantity);
        }
        endOperation();
    }
    
    
    protected void insert(int quantity) throws Exception
    {
        List<Benchmark> benchmarks = newBenchmarks(quantity);
        beginOperation();
        Table<Benchmark> table = database.getTable(Benchmark.class);
        table.insertAll(benchmarks);
        endOperation();
    }
    
    
    protected void update(int quantity) throws Exception
    {
        List<Integer> randomIds = getRandomIds(quantity);
        beginOperation();
        List<Benchmark> benchmarks = selectBenchmarks(randomIds);
        setUpdateMarker(benchmarks);
        database.getTable(Benchmark.class).updateAll(benchmarks);
        endOperation();
    }

    
    protected void delete(int quantity) throws Exception
    {
        List<Integer> randomIds = removeRandomIds(quantity);
        beginOperation();
        List<Benchmark> benchmarks = selectBenchmarks(randomIds);
        database.getTable(Benchmark.class).deleteAll(benchmarks);
        endOperation();
    }
}
