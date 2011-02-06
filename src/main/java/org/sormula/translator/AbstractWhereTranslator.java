/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
package org.sormula.translator;

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.WhereField;


/**
 * Base class for translators that provide parameters for where clause.
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class 
 */
public abstract class AbstractWhereTranslator<R> extends ColumnsTranslator<R>
{
    RowTranslator<R> rowTranslator;
    List<String> operatorList;
    List<String> booleanOperatorList;
    
    
    /**
     * Constructs for row translator.
     * 
     * @param rowTranslator row translator associated with where condition
     * @throws TranslatorException if error
     */
    public AbstractWhereTranslator(RowTranslator<R> rowTranslator) throws TranslatorException
    {
        super(rowTranslator.getRowClass());
        this.rowTranslator = rowTranslator;
    }
    
    
    
    @Override
    protected void initColumnTranslatorList(int columns)
    {
        super.initColumnTranslatorList(columns);
        operatorList = new ArrayList<String>(columns);
        booleanOperatorList = new ArrayList<String>(columns);
    }


    /**
     * @return complete WHERE phrase
     */
    public String createSql()
    {
        return "WHERE " + createColumnParameterPhrase();
    }


    /**
     * @return row translator associated with where condition
     */
    public RowTranslator<R> getRowTranslator()
    {
        return rowTranslator;
    }
    
    
    /**
     * Adds translator with equal as operator and "AND" as boolean operator.
     * 
     * @param c column translator to add
     */
    @Override
    public void addColumnTranslator(ColumnTranslator<R> c)
    {
        addColumnTranslator(c, "=", "AND");
    }
    
    
    /**
     * Adds translator with a specific sql comparison operator and boolean operator.
     * 
     * @param c column translator to add
     * @param operator sql comparison operator to use in where condition (examples: ">", "=<", "<>", etc.)
     * @param booleanOperator logical operator to precede this column (examples: "AND", "OR", "AND NOT", etc.)
     */
    public void addColumnTranslator(ColumnTranslator<R> c, String operator, String booleanOperator)
    {
        super.addColumnTranslator(c);
        operatorList.add(operator);
        booleanOperatorList.add(booleanOperator);
    }


    /**
     * Creates column phrase with parameters and comparison operators. 
     * 
     * @return "c1 op1 ? bo2 c2 operator2 ? bo3 c3 operator3 ?..." where
     * cn is column name, opn is {@link WhereField#comparisonOperator()}, and bon is 
     * {@linkplain WhereField#booleanOperator()}
     */
    @Override
    public String createColumnParameterPhrase()
    {
        StringBuilder phrase = new StringBuilder(columnTranslatorList.size() * 20);
        int i = 0;
        
        for (ColumnTranslator<R> c: columnTranslatorList)
        {
            if (i > 0)
            {
                // for 2nd column and beyond, combine with previous column using boolean operator
                phrase.append(" "); // space around operators
                phrase.append(booleanOperatorList.get(i));
                phrase.append(" "); // space around operators
            }
            
            phrase.append(c.getColumnName());
            phrase.append(" "); // space around operators
            phrase.append(operatorList.get(i++));
            phrase.append(" "); // space around operators
            phrase.append("?");
        }

        return phrase.toString();
    }
}
