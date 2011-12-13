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
    
    // parallel arrays for the following, TODO use class?
    List<String> booleanOperatorList;
    List<String> comparisonOperatorList;
    List<String> operandList;

    @Deprecated
    boolean inOperator; 
    boolean inOperatorCollection;
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
        booleanOperatorList = new ArrayList<String>(columns);
        comparisonOperatorList = new ArrayList<String>(columns);
        operandList = new ArrayList<String>(columns);
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
        addColumnTranslator(c, "AND", "=", "?");
    }
    
    
    /**
     * Adds translator with a specific sql comparison operator and boolean operator. This
     * method is kept for backward compatibility to versions prior to 1.4. Note that parameter 
     * order is different from {@link #addColumnTranslator(ColumnTranslator, String, String, String)}.
     * 
     * @param c column translator to add
     * @param comparisonOperator sql comparison operator to use in where condition (examples: ">", "=<", "<>", etc.)
     * @param booleanOperator logical operator to precede this column (examples: "AND", "OR", "AND NOT", etc.)
     */
    @Deprecated
    public void addColumnTranslator(ColumnTranslator<R> c, String comparisonOperator, String booleanOperator)
    {
        // note parameter order is different
        addColumnTranslator(c, booleanOperator, comparisonOperator, "");
    }
    
    
    /**
     * Adds translator with a specific sql comparison operator and boolean operator. Note that
     * parameter order has changed so that booleanOperator, operator, operand appear in same order
     * as in SQL. 
     * 
     * @param c column translator to add
     * @param booleanOperator logical operator to precede this column (examples: "AND", "OR", "AND NOT", etc.)
     * @param comparisonOperator sql comparison operator to use in where condition (examples: ">", "=<", "<>", etc.)
     * @param operand operand to follow operator; typically "?" indicates operand is SQL parameter
     * @since 1.4
     */
    public void addColumnTranslator(ColumnTranslator<R> c, String booleanOperator, String comparisonOperator, String operand)
    {
        super.addColumnTranslator(c);
        booleanOperatorList.add(booleanOperator);
        comparisonOperatorList.add(comparisonOperator);
        operandList.add(operand);
        
        // remember if IN operator was used (keep for backward compatibility)
        if (isInOperator(comparisonOperator)) inOperator = true;
        
        if (isInOperatorCollection(comparisonOperator, operand)) inOperatorCollection = true;
    }


    /**
     * Use {@link #isInOperatorCollection()} instead.
     * 
     * @return true if one or more column uses the "IN" operator
     */
    @Deprecated
    public boolean isInOperator()
    {
        return inOperator;
    }
    
    
    @Deprecated
    protected boolean isInOperator(String operator)
    {
        return operator.equalsIgnoreCase("IN") || operator.equalsIgnoreCase("NOT IN");
    }
    
    
    /**
     * Reports if any column uses IN operator where "?" parameters are dynamically generated
     * based upon the size of the collection parameter.
     * 
     * @return true if at least one column uses "IN" or "NOT IN" operator and operand is 
     * the default "?"
     * @since 1.4 
     */
    public boolean isInOperatorCollection()
    {
        return inOperatorCollection;
    }
    
    
    protected boolean isInOperatorCollection(String operator, String operand)
    {
        // return true if IN operator is used and operand should be built from collection parameter
        return (operator.equalsIgnoreCase("IN") || operator.equalsIgnoreCase("NOT IN")) && operand.equals("?");
    }


    /**
     * Creates column phrase with parameter placeholders and comparison operators like:<br> 
     * "c1 cop1 a1 bo2 c2 cop2 a2 bo3 c3 cop3 a3..." where cN is column name,
     * copN is {@link WhereField#comparisonOperator()}, aN is operand (typically "?"), and 
     * boN is {@linkplain WhereField#booleanOperator()}
     * 
     * @return "c1 cop1 a1 bo2 c2 cop2 a2 bo3 c3 cop3 a3..."
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
            String operator = comparisonOperatorList.get(i);
            phrase.append(operator);
            phrase.append(" "); // space around operators
            
            // add operand based upon type of operator
            String operand = operandList.get(i);
            
            if (inOperatorCollection && isInOperatorCollection(operator, operand))
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
                        for (int p = 0; p < inParameterCount; ++p) phrase.append("?, ");
                        
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
                // not IN operator
                phrase.append(operand);
            }
            
            // next column
            ++i;
        }

        return phrase.toString();
    }
}
