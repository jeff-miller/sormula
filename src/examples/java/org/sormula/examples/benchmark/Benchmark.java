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

import org.sormula.annotation.Column;
import org.sormula.annotation.Where;


/** 
 * Row class for benchmarks.
 */
@Where(name="forDescription", fieldNames="description")
@Where(name="forUpdateMarker", fieldNames={"description", "integer2"})
public class Benchmark
{
    @Column(primaryKey=true)
    int id;
    String description;
    Integer integer2;
    boolean boolean1;
    Boolean boolean2;
    float float1;
    Float float2;
    double double1;
    Double double2;
    short short1;
    Short short2;
    java.util.Date javaDate;
    java.sql.Date sqlDate;
    java.sql.Timestamp sqlTimestamp;
    
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public Integer getInteger2()
    {
        return integer2;
    }
    public void setInteger2(Integer integer2)
    {
        this.integer2 = integer2;
    }
    public boolean isBoolean1()
    {
        return boolean1;
    }
    public void setBoolean1(boolean boolean1)
    {
        this.boolean1 = boolean1;
    }
    public Boolean getBoolean2()
    {
        return boolean2;
    }
    public void setBoolean2(Boolean boolean2)
    {
        this.boolean2 = boolean2;
    }
    public float getFloat1()
    {
        return float1;
    }
    public void setFloat1(float float1)
    {
        this.float1 = float1;
    }
    public Float getFloat2()
    {
        return float2;
    }
    public void setFloat2(Float float2)
    {
        this.float2 = float2;
    }
    public double getDouble1()
    {
        return double1;
    }
    public void setDouble1(double double1)
    {
        this.double1 = double1;
    }
    public Double getDouble2()
    {
        return double2;
    }
    public void setDouble2(Double double2)
    {
        this.double2 = double2;
    }
    public short getShort1()
    {
        return short1;
    }
    public void setShort1(short short1)
    {
        this.short1 = short1;
    }
    public Short getShort2()
    {
        return short2;
    }
    public void setShort2(Short short2)
    {
        this.short2 = short2;
    }
    public java.util.Date getJavaDate()
    {
        return javaDate;
    }
    public void setJavaDate(java.util.Date javaDate)
    {
        this.javaDate = javaDate;
    }
    public java.sql.Date getSqlDate()
    {
        return sqlDate;
    }
    public void setSqlDate(java.sql.Date sqlDate)
    {
        this.sqlDate = sqlDate;
    }
    public java.sql.Timestamp getSqlTimestamp()
    {
        return sqlTimestamp;
    }
    public void setSqlTimestamp(java.sql.Timestamp sqlTimestamp)
    {
        this.sqlTimestamp = sqlTimestamp;
    }
}
