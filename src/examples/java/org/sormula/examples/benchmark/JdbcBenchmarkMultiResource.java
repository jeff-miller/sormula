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

import java.sql.PreparedStatement;
import java.util.List;


/** 
 * {@link BenchmarkThread} that uses JDBC. New resources are
 * created each time an operation is performed.
 */
public class JdbcBenchmarkMultiResource extends JdbcBenchmarkThread
{
    public JdbcBenchmarkMultiResource(BenchmarkSuite benchmarkSuite)
    {
        super(benchmarkSuite, "JDBC multi resource");
    }
    

    protected void select(int quantity) throws Exception
    {
        beginOperation();
        try (PreparedStatement selectStatement = prepareSelectForDescription())
        {
            selectForBenchmarkName(selectStatement, quantity);
        }
        endOperation();
    }
    
    
    protected void insert(int quantity) throws Exception
    {
        List<Benchmark> benchmarks = newBenchmarks(quantity);
        beginOperation();
        try (PreparedStatement insertStatement = prepareInsert())
        {
            for (Benchmark b : benchmarks) insert(insertStatement, b);
        }
        endOperation();
    }
    
    
    protected void update(int quantity) throws Exception
    {
        List<Integer> randomIds = getRandomIds(quantity);
        beginOperation();
        List<Benchmark> benchmarks = selectBenchmarks(randomIds);
        setUpdateMarker(benchmarks);
        try (PreparedStatement updateStatement = prepareUpdate())
        {
            for (Benchmark b : benchmarks) update(updateStatement, b);
        }
        endOperation();
    }

    
    protected void delete(int quantity) throws Exception
    {
        List<Integer> randomIds = removeRandomIds(quantity);
        beginOperation();
        List<Benchmark> benchmarks = selectBenchmarks(randomIds);
        try (PreparedStatement deleteStatement = prepareDelete())
        {
            for (Benchmark b : benchmarks) delete(deleteStatement, b);
        }
        endOperation();
    }
}
