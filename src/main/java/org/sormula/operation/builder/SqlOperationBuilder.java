/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2020 Jeff Miller
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
package org.sormula.operation.builder;

import java.util.HashMap;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.SqlOperation;


/**
 * Base class for builders of {@link SqlOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class SqlOperationBuilder<R, B extends SqlOperationBuilder, T extends SqlOperation<R>>
{
    Table<R> table;
    String whereConditionName;
    Object[] parameters;
    Integer queryTimeout;
    Boolean cached;
    Boolean cascade;
    String customSql;
    Boolean includeIdentityColumns;
    Map<String, Object> namedParameterMap;
    Boolean readOnly;
    String[] requiredCascades;
    String timingId;
    Boolean timings;
    
    
    public SqlOperationBuilder(Table<R> table) 
    {
        this.table = table;
    }


    public abstract T build() throws SormulaException;
    
    
    public Table<R> getTable() 
    {
        return table;
    }


    protected void init(T operation) throws SormulaException
    {
        if (whereConditionName != null) operation.setWhere(whereConditionName);
        if (parameters != null) operation.setParameters(parameters);
        if (queryTimeout != null) operation.setQueryTimeout(queryTimeout);
        if (cached != null) operation.setCached(cached);
        if (cascade != null) operation.setCascade(cascade);
        if (customSql != null) operation.setCustomSql(customSql);
        if (includeIdentityColumns != null) operation.setIncludeIdentityColumns(includeIdentityColumns);
        if (namedParameterMap != null) operation.setNamedParameterMap(namedParameterMap);
        if (readOnly != null) operation.setReadOnly(readOnly);
        if (requiredCascades != null) operation.setRequiredCascades(requiredCascades);
        if (timingId != null) operation.setTimingId(timingId);
        if (timings != null) operation.setTimings(timings);
    }
    
    
    @SuppressWarnings("unchecked")
    public B where(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        this.parameters = parameters;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B queryTimeout(int queryTimeout)
    {
        this.queryTimeout = queryTimeout;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B cached(boolean cached)
    {
        this.cached = cached;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B cascade(boolean cascade)
    {
        this.cascade = cascade;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B customSql(String customSql)
    {
        this.customSql = customSql;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B includeIdentityColumns(boolean includeIdentityColumns)
    {
        this.includeIdentityColumns = includeIdentityColumns;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B namedParameterMap(Map<String, Object> namedParameterMap)
    {
        this.namedParameterMap = namedParameterMap;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameter(String name, Object value)
    {
        if (namedParameterMap == null) namedParameterMap = new HashMap<>();
        namedParameterMap.put(name, value);
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B readOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
        return (B)this;
    }
    

    @SuppressWarnings("unchecked")
    public B requiredCascades(String... requiredCascades)
    {
        this.requiredCascades = requiredCascades;
        return (B)this;
    }
    

    @SuppressWarnings("unchecked")
    public B timingId(String timingId)
    {
        this.timingId = timingId;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B timings(boolean timings)
    {
        this.timings = timings;
        return (B)this;
    }
}
