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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/** 
 * Common base for all JDBC benchmark threads.
 */
public abstract class JdbcBenchmarkThread extends BenchmarkThread
{
    static final String SELECT_COLUMNS = "SELECT id, description, integer2, boolean1, boolean2, float1, float2, double1, double2, short1, short2, javaDate, sqlDate, sqlTimestamp FROM Benchmark";
    
    public JdbcBenchmarkThread(BenchmarkSuite benchmarkSuite, String benchmarkName)
    {
        super(benchmarkSuite, benchmarkName);
    }


    @Override
    protected void beginOperation() throws Exception
    {
        elapsedTime.start();
        getConnection().setAutoCommit(false);
    }


    @Override
    protected void endOperation() throws Exception
    {
        connection.commit();
        elapsedTime.stop();
    }
    
    
    protected PreparedStatement prepareSelectForDescription() throws SQLException
    {
        return getConnection().prepareStatement(SELECT_COLUMNS + " WHERE description = ?");
    }
    
    
    protected List<Benchmark> selectForBenchmarkName(PreparedStatement selectStatement, int quantity) throws SQLException
    {
        List<Benchmark> benchmarks = new ArrayList<Benchmark>(quantity);
        selectStatement.setString(1, getBenchmarkName());
        ResultSet rs = selectStatement.executeQuery();
        
        // read quantity of rows
        for (int i = 0; i < quantity; ++i)
        {
            Benchmark b = readBenchmark(rs); 
            if (b != null)
            {
                benchmarks.add(b);
            }
            else
            {
                break;
            }
        }
        
        rs.close();
        return benchmarks;
    }

    
    protected List<Benchmark> selectBenchmarks(List<Integer> ids) throws SQLException
    {
        List<Benchmark> benchmarks = new ArrayList<Benchmark>(ids.size());
        
        // select one at-a-time since size may be too big for IN operator 
        PreparedStatement ps = getConnection().prepareStatement(SELECT_COLUMNS + " WHERE id=?");
        for (Integer id : ids)
        {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            benchmarks.add(readBenchmark(rs));
            rs.close();
        }
        ps.close();
        
        return benchmarks;
    }
    
    
    protected Benchmark readBenchmark(ResultSet rs) throws SQLException
    {
        Benchmark benchmark;
        
        if (rs.next())
        {
            benchmark = new Benchmark();
            benchmark.setId(rs.getInt(1));
            benchmark.setDescription(rs.getString(2));
            benchmark.setInteger2(rs.getInt(3));
            benchmark.setBoolean1(rs.getBoolean(4));
            benchmark.setBoolean2(rs.getBoolean(5));
            benchmark.setFloat1(rs.getFloat(6));
            benchmark.setFloat2(rs.getFloat(7));
            benchmark.setDouble1(rs.getDouble(8));
            benchmark.setDouble2(rs.getDouble(9));
            benchmark.setShort1(rs.getShort(10));
            benchmark.setShort2(rs.getShort(11));
            benchmark.setJavaDate(new java.util.Date(rs.getTimestamp(12).getTime()));
            benchmark.setSqlDate(rs.getDate(13));
            benchmark.setSqlTimestamp(rs.getTimestamp(14));
        }
        else
        {
            benchmark = null;
        }
        
        return benchmark;
    }
    
    
    protected PreparedStatement prepareInsert() throws SQLException
    {
        return getConnection().prepareStatement(
                "INSERT INTO Benchmark(id, description, integer2, boolean1, boolean2, float1, float2, double1, double2, short1, short2, javaDate, sqlDate, sqlTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }
    
    
    protected void insert(PreparedStatement insertStatement, Benchmark benchmark) throws SQLException
    {
        setParameters(insertStatement, benchmark);
        insertStatement.executeUpdate();
    }
    
    
    protected PreparedStatement prepareUpdate() throws SQLException
    {
        return getConnection().prepareStatement(
                "UPDATE Benchmark SET id=?, description=?, integer2=?, boolean1=?, boolean2=?, float1=?, float2=?, double1=?, double2=?, short1=?, short2=?, javaDate=?, sqlDate=?, sqlTimestamp=? WHERE id = ?");
    }
    
    
    protected void update(PreparedStatement updateStatement, Benchmark benchmark) throws SQLException
    {
        updateStatement.setInt(setParameters(updateStatement, benchmark), benchmark.getId());
        updateStatement.executeUpdate();
    }
    
    
    protected int setParameters(PreparedStatement statement, Benchmark benchmark) throws SQLException
    {
        int p = 1;
        
        statement.setInt(       p++, benchmark.getId());
        statement.setString(    p++, benchmark.getDescription());
        statement.setInt(       p++, benchmark.getInteger2());
        statement.setBoolean(   p++, benchmark.isBoolean1());
        statement.setBoolean(   p++, benchmark.getBoolean2());
        statement.setFloat(     p++, benchmark.getFloat1());
        statement.setFloat(     p++, benchmark.getFloat2());
        statement.setDouble(    p++, benchmark.getDouble1());
        statement.setDouble(    p++, benchmark.getDouble2());
        statement.setShort(     p++, benchmark.getShort1());
        statement.setShort(     p++, benchmark.getShort2());
        statement.setTimestamp( p++, new Timestamp(benchmark.getJavaDate().getTime()));
        statement.setDate(      p++, benchmark.getSqlDate());
        statement.setTimestamp( p++, benchmark.getSqlTimestamp());
        
        return p;
    }
    
    
    protected PreparedStatement prepareDelete() throws SQLException
    {
        return getConnection().prepareStatement("DELETE FROM Benchmark WHERE id = ?");
    }
    
    
    protected void delete(PreparedStatement deleteStatement, Benchmark benchmark) throws SQLException
    {
        deleteStatement.setInt(1, benchmark.getId());
        deleteStatement.executeUpdate();
    }
}
