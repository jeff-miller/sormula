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
import java.util.Collection;
import java.util.List;

import org.sormula.annotation.WhereField;
import org.sormula.operation.SqlOperation;


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
    boolean inOperator; 
    Object[] parameters;
    
    
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
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initColumnTranslatorList(int columns)
    {
        super.initColumnTranslatorList(columns);
        operatorList = new ArrayList<String>(columns);
        booleanOperatorList = new ArrayList<String>(columns);
    }


    /**
     * @return parameters set by {@link #setParameters(Object[])}
     */
    public Object[] getParameters()
    {
        return parameters;
    }


    /**
     * Sets parameters that are used in where phrase. Some where phrases need 
     * to know the number and type of the parameters. Parameters typically
     * are obtained from {@link SqlOperation#getParameters()}.
     * 
     * @param parameters parameters for where phrase; null if none
     */
    public void setParameters(Object[] parameters)
    {
        this.parameters = parameters;
    }


    /**
     * Creates SQL for where phrase. parameters are supplied because
     * some where phrases need to know the number and type of the parameters. 
     * 
     * @return complete WHERE phrase
     */
    public String createSql()
    {
        return "WHERE " + createColumnParameterPhrase();
    }


    /**
     * Gets row translator that was supplied in constructor.
     * 
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
        
        // remember if IN operator was used
        if (operator.equalsIgnoreCase("in")) inOperator = true;
    }


    /**
     * @return true if one or more column uses the "IN" operator
     */
    public boolean isInOperator()
    {
        return inOperator;
    }


    /**
     * Creates column phrase with parameter placeholders and comparison operators. 
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
            String operator = operatorList.get(i);
            phrase.append(operator);
            phrase.append(" "); // space around operators
            
            if (inOperator && operator.equalsIgnoreCase("in"))
            {
                // add parameter placeholders for IN phrase
                phrase.append("(");
                Object parameter = parameters[i];
                
                if (parameter instanceof Collection<?>)
                {
                    // one parameter placeholder for each item
                    int inParameterCount = ((Collection<?>)parameter).size();
                    
                    if (inParameterCount > 0)
                    {
                        for (int p = 0; p < inParameterCount; ++p)
                        {
                            phrase.append("?, ");
                        }
                        
                        // remove last comma
                        phrase.setLength(phrase.length() - 2);
                    }
                    else
                    {
                        // empty collection, use "in (null)" to avoid sql error
                        phrase.append("null");
                    }
                }
                else
                {
                    // one parameter within IN phrase
                    phrase.append("?");
                }
                
                phrase.append(")");
            }
            else
            {
                // standard operator followed by one parameter placeholder
                phrase.append("?");
            }
            
            // next
            ++i;
        }

        return phrase.toString();
    }
}
