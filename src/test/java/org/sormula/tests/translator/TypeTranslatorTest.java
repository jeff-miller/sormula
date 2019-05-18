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
package org.sormula.tests.translator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.TypeTranslator;
import org.testng.annotations.Test;


/**
 * Tests all of the ways to use a {@link TypeTranslator} 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class TypeTranslatorTest extends DatabaseTest<SormulaTestTT>
{
    SormulaTestTT inserted;
    
    
    @Override
    protected void open() throws Exception
    {
        super.open();
        
        // createTable will use this table with custom type translator
        getDatabase().addTable(new Table<SormulaTestTT>(getDatabase(), SormulaTestTT.class)
        {
            @Override
            protected void initTypeTranslatorMap() throws SormulaException
            {
                super.initTypeTranslatorMap();
                putTypeTranslator("int", new NegativeIntTranslator()); // primitive type
                putTypeTranslator(String.class, new UpperTranslator()); // non primitive type
            }
        });
        
        createTable(SormulaTestTT.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestTT.class.getSimpleName() + " (" +
            " testbooleanyesno1 CHAR(1)," +
            " testbooleanyesno2 CHAR(1)," +
            " testbooleanyesno3 CHAR(1)," +
            " testinteger INTEGER," +
            " testdate TIMESTAMP" + getSecondsPrecisionDDL() + "," +
            " teststring VARCHAR(10), " +
            " testEnum SMALLINT " + // convert Enum to/from int's
            ")"
        );
    }
    
    
    @Test
    public void insertTestTT() throws SormulaException
    {
        inserted = new SormulaTestTT();
        
        inserted.setTestBooleanYesNo1(false);
        inserted.setTestBooleanYesNo2(true);
        inserted.setTestBooleanYesNo3(null);
        inserted.setTestInteger(1234567890);
        inserted.setTestString("abcdefghij");
        inserted.setTestDate(new java.util.Date(System.currentTimeMillis()));
        inserted.setTestEnum(EnumField.Ugly);
        
        begin();
        assert getTable().insert(inserted) == 1 : "1 row not inserted";
        commit();
    }
    
    
    @Test(dependsOnMethods="insertTestTT")
    public void selectTestTT() throws SormulaException
    {
        begin();
        List<SormulaTestTT> list = getTable().selectAll();
        assert list.size() == 1 : "unexpected row count";
        SormulaTestTT selected = list.get(0);
        String message = " column inserted != selected";
         
        assert inserted.isTestBooleanYesNo1() == selected.isTestBooleanYesNo1() : "testBooleanYN1" + message;
        assert inserted.getTestBooleanYesNo2() == selected.getTestBooleanYesNo2() : "testBooleanYN2" + message;
        assert selected.getTestBooleanYesNo3() == null : "testBooleanYN3 should be null";
        assert inserted.getTestInteger() == selected.getTestInteger() : "testInteger" + message;
        assert inserted.getTestString().equals(selected.getTestString()) : "testString" + message;
        assert inserted.getTestDate().equals(selected.getTestDate()) : "testDate" + message;
        assert inserted.getTestEnum().equals(selected.getTestEnum()) : "testEnum" + message;
        
        commit();
    }
}


// simplistic translator that stores negative value
class NegativeIntTranslator implements TypeTranslator<Integer>
{
    public void write(PreparedStatement preparedStatement, int parameterIndex, Integer parameter) throws Exception
    {
        preparedStatement.setInt(parameterIndex, -parameter.intValue());
    }
    
    
    public Integer read(ResultSet resultSet, int columnIndex) throws Exception
    {
        return -resultSet.getInt(columnIndex);
    }
}


// simplistic translator that stores upper case
class UpperTranslator implements TypeTranslator<String>
{
     public void write(PreparedStatement preparedStatement, int parameterIndex, String parameter) throws Exception
     {
         preparedStatement.setString(parameterIndex, parameter.toUpperCase());
     }
     
     
     public String read(ResultSet resultSet, int columnIndex) throws Exception
     {
         return resultSet.getString(columnIndex).toLowerCase();
     }
}
