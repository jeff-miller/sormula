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
import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.Cache;
import org.sormula.cache.readonly.ReadOnlyCache;
import org.sormula.cache.readwrite.ReadWriteCache;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.ScalarSelectOperation;


/** 
 * Common base for all Sormula benchmark threads.
 */
public abstract class SormulaBenchmarkThread extends BenchmarkThread
{
    public enum CacheType 
    {
        NONE("no cache"), READ_ONLY("readonly cache"), READ_WRITE("read/write cache");
        
        private String description;
        
        CacheType(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }
    
    Database database;
    CacheType cacheType;
    boolean logCacheStatistics;
    
    
    public SormulaBenchmarkThread(BenchmarkSuite benchmarkSuite, String benchmarkName, CacheType cacheType, boolean logCacheStatistics)
    {
        super(benchmarkSuite, benchmarkName + " - " + cacheType.getDescription());
        this.cacheType = cacheType;
        this.logCacheStatistics = logCacheStatistics;
    }
    
    
    protected void createDatabase() throws SormulaException
    {
        switch (cacheType)
        {
            case NONE:
                database = new Database(getConnection(), benchmarkSuite.getSchema());
                break;
                
            case READ_ONLY:
                database = new ROCacheDatabase(getConnection(), benchmarkSuite.getSchema(), logCacheStatistics);
                break;
                
            case READ_WRITE:
                database = new RWCacheDatabase(getConnection(), benchmarkSuite.getSchema(), logCacheStatistics);
                break;
        }
    }
    
    
    protected ListSelectOperation<Benchmark> createSelectForDescription() throws SormulaException
    {
        // select only from records inserted by this benchmark thread
        return new ArrayListSelectOperation<>(
                database.getTable(Benchmark.class), "forDescription");
    }
    
    
    protected List<Benchmark> selectLikeBenchmarkName(ListSelectOperation<Benchmark> selectOperation, int quantity) throws SormulaException
    {
        List<Benchmark> benchmarks = new ArrayList<>(quantity);
        selectOperation.setParameters(getBenchmarkName());
        selectOperation.execute();
        
        // read quantity of rows
        for (int i = 0; i < quantity; ++i)
        {
            Benchmark b = selectOperation.readNext();
            if (b != null) 
            {
                benchmarks.add(b);
            }
            else
            {
                break;
            }
        }
        
        return benchmarks;
    }
    
    
    protected List<Benchmark> selectBenchmarks(List<Integer> ids) throws SormulaException
    {
        List<Benchmark> benchmarks = new ArrayList<>(ids.size());
        
        // select one at-a-time since size may be too big for IN operator
        try (ScalarSelectOperation<Benchmark> selectById = new ScalarSelectOperation<>(
                database.getTable(Benchmark.class)))
        {
            for (Integer id : ids)
            {
                selectById.setParameters(id);
                selectById.execute();
                benchmarks.add(selectById.readNext());
            }
        }
        
        return benchmarks;
    }
}


@Cached(type=ReadOnlyCache.class, size=100)
class ROCacheDatabase extends CachedDatabase
{
    public ROCacheDatabase(Connection connection, String schema, boolean logCacheStatistics)
    {
        super(connection, schema, logCacheStatistics);
    }
}


@Cached(type=ReadWriteCache.class, size=100)
class RWCacheDatabase extends CachedDatabase
{
    public RWCacheDatabase(Connection connection, String schema, boolean logCacheStatistics)
    {
        super(connection, schema, logCacheStatistics);
    }
}


class CachedDatabase extends Database
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    boolean logCacheStatistics;
    
    public CachedDatabase(Connection connection, String schema, boolean logCacheStatistics)
    {
        super(connection, schema);
        this.logCacheStatistics = logCacheStatistics;
    }

    @Override
    public void close() 
    {
        if (logCacheStatistics)
        {
            try
            {
                Cache<Benchmark> cache = getTable(Benchmark.class).getCache();
                if (cache != null) cache.log(); 
            }
            catch (SormulaException e)
            {
                log.error("error logging cache statistics", e);
            }
        }
        
        super.close();
    }
}
