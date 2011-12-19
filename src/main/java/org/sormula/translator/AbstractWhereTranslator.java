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
    List<WhereFieldExpression> whereFieldExpressionList;
    @Deprecated boolean inOperator; 
    boolean collectionOperand;
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
        whereFieldExpressionList  = new ArrayList<WhereFieldExpression>(columns);
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
        WhereFieldExpression wfe = new WhereFieldExpression(booleanOperator, comparisonOperator, operand);
        whereFieldExpressionList.add(wfe);

        // remember if collection operand was used (for faster prepares)
        if (wfe.isCollectionOperand()) collectionOperand = true;
        
        // remember if IN operator was used (keep for backward compatibility but is not needed)
        if (isInOperator(comparisonOperator)) inOperator = true;
    }


    /**
     * Kept for backward compatibility but is not needed.
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
     * Tests if where condition contains at least one column with an operand that use
     * a collection as a parameter.
     * 
     * @return true if at least one column uses a collection operand
     */
    public boolean isCollectionOperand()
    {
        return collectionOperand;
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
            WhereFieldExpression wfe = whereFieldExpressionList.get(i);
            
            if (i > 0)
            {
                // for 2nd column and beyond, combine with previous column using boolean operator
                phrase.append(" "); // space around operators
                phrase.append(wfe.getBooleanOperator());
                phrase.append(" "); // space around operators
            }
            
            phrase.append(c.getColumnName());
            phrase.append(" "); // space around operators
            phrase.append(wfe.getComparisonOperator());
            phrase.append(" "); // space around operators
            
            // add operand based upon type of operator
            if (wfe.isCollectionOperand())
            {
                // add parameter placeholders for collection operand for example "IN (?, ?, ...)"
                Object parameter = parameters[i];
                
                if (parameter instanceof Collection<?>)
                {
                    // one parameter placeholder for each item
                    int operandSize = ((Collection<?>)parameter).size();
                    
                    if (operandSize > 0)
                    {
                        phrase.append("(");
                        for (int p = 0; p < operandSize; ++p) phrase.append("?, ");
                        phrase.setLength(phrase.length() - 2); // remove last comma and space
                        phrase.append(")");
                    }
                    else
                    {
                        // empty collection, use "(null)" to avoid sql error
                        phrase.append("(null)");
                    }
                }
                else
                {
                    // non Collection operand
                    phrase.append("(?)");
                }
            }
            else
            {
                // not collection operand, use operand supplied by annotation
                phrase.append(wfe.getOperand());
            }
            
            // next column
            ++i;
        }

        return phrase.toString();
    }
}



class WhereFieldExpression
{
    String booleanOperator;
    String comparisonOperator;
    String operand;
    boolean collectionOperand;
    
    
    public WhereFieldExpression(String booleanOperator, String comparisonOperator, String operand)
    {
        this.booleanOperator = booleanOperator;
        this.comparisonOperator = comparisonOperator;
        this.operand = operand;
        
        // true if operand should be built from collection parameter
        collectionOperand = (comparisonOperator.equalsIgnoreCase("IN") || 
                comparisonOperator.equalsIgnoreCase("NOT IN")) && operand.equals("?");
    }


    public String getBooleanOperator()
    {
        return booleanOperator;
    }


    public String getComparisonOperator()
    {
        return comparisonOperator;
    }


    public String getOperand()
    {
        return operand;
    }


    public boolean isCollectionOperand()
    {
        return collectionOperand;
    }
}