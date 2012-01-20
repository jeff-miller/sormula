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
package org.sormula.translator;

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderByAnnotationReader;
import org.sormula.annotation.OrderByField;


/**
 * Supplies order by phrase.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 */
public class OrderByTranslator<R> extends ColumnsTranslator<R>
{
    List<String> orderQualifierList;
    String orderByName;
    
    
    /**
     * Not used. Looks for order condition only in row class and not any other class. 
     * 
     * @param rowTranslator row translator from which to get column information
     * @param orderByName name supplied in {@link OrderBy}
     * @throws TranslatorException if error
     */
    @Deprecated
    public OrderByTranslator(RowTranslator<R> rowTranslator, String orderByName) throws TranslatorException
    {
        super(rowTranslator.getRowClass());
        this.orderByName = orderByName;
        
        OrderBy orderByAnnotation = new OrderByAnnotationReader(
                rowTranslator.getRowClass()).getAnnotation(orderByName);        

        if (orderByAnnotation != null)
        {
            init(rowTranslator, orderByAnnotation);
        }
        else
        {
            throw new TranslatorException("no OrderBy named " + orderByName + " for " + 
                    rowTranslator.getRowClass().getCanonicalName());
        }
    }
    
    
    /**
     * Constructs for an order by annotation. 
     * 
     * @param rowTranslator row translator from which to get column information
     * @param orderByAnnotation annotation with order information
     * @throws TranslatorException if error
     */
    public OrderByTranslator(RowTranslator<R> rowTranslator, OrderBy orderByAnnotation) throws TranslatorException
    {
        super(rowTranslator.getRowClass());
        this.orderByName = orderByAnnotation.name();
        init(rowTranslator, orderByAnnotation);
    }
    
    
    void init(RowTranslator<R> rowTranslator, OrderBy orderByAnnotation) throws TranslatorException
    {
        // add translators for each column in order by condition
        String [] ascending  = orderByAnnotation.ascending();
        String [] descending = orderByAnnotation.descending();
        
        if (ascending.length > 0)
        {
            // all columns ascending  
            initSimpleOrderByColumns(rowTranslator, ascending, "");
        }
        else if (descending.length > 0)
        {
            // all columns descending  
            initSimpleOrderByColumns(rowTranslator, descending, "DESC");
        }
        else
        {
            // mix of ascending and descending
            OrderByField[] orderByColumns = orderByAnnotation.orderByFields();
            initColumnTranslatorList(orderByColumns.length);
            
            for (OrderByField o: orderByColumns)
            {
                String qualifier;
                if (o.descending()) qualifier = "DESC";
                else qualifier = "ASC";

                ColumnTranslator<R> columnTranslator = rowTranslator.getColumnTranslator(o.name());
                
                if (columnTranslator != null)
                {
                    addColumnTranslator(columnTranslator, qualifier);
                }
                else
                {
                    throw new NoColumnTranslatorException(rowTranslator.getRowClass(), o.name(), "order by named, " + orderByName);
                }
            }
        }
    }
    
    
    protected void initSimpleOrderByColumns(RowTranslator<R> rowTranslator, String[] fieldNames, String qualifier) throws NoColumnTranslatorException
    {
        initColumnTranslatorList(fieldNames.length);
        
        for (String fn: fieldNames)
        {
            ColumnTranslator<R> columnTranslator = rowTranslator.getColumnTranslator(fn);
            
            if (columnTranslator != null)
            {
                addColumnTranslator(columnTranslator, qualifier);
            }
            else
            {
                throw new NoColumnTranslatorException(rowTranslator.getRowClass(), fn, "order by named, " + orderByName);
            }
        }
    }


    /**
     * Adds column translator with empty string qualifier.
     * 
     * @param c translator to add
     */
    @Override
    public void addColumnTranslator(ColumnTranslator<R> c)
    {
        addColumnTranslator(c, "");
    }
    
    
    /**
     * Adds translator with a specific qualifier.
     * 
     * @param c translator to add
     * @param qualifier string to append after the order by column (example "ASC" or "DESC")
     */
    public void addColumnTranslator(ColumnTranslator<R> c, String qualifier)
    {
        super.addColumnTranslator(c);
        orderQualifierList.add(qualifier);
    }
    

    @Override
    protected void initColumnTranslatorList(int columns)
    {
        super.initColumnTranslatorList(columns);
        orderQualifierList = new ArrayList<String>(columns);
    }


    /**
     * Creates order by phrase.
     * 
     * @return "ORDER BY c1, c2, c3..." 
     */
    public String createSql()
    {
        return "ORDER BY " + createColumnOrderByPhrase();
    }


    /**
     * Creates column phrase with qualifiers. Typically used in order by clause.
     * 
     * @return "c1 q1, c2 q2, c3 q3..."
     */
    public String createColumnOrderByPhrase()
    {
        List<ColumnTranslator<R>> columnTranslatorList = getColumnTranslatorList();
        StringBuilder phrase = new StringBuilder(columnTranslatorList.size() * 20);
        int i = 0;
        
        for (ColumnTranslator<R> c: columnTranslatorList)
        {
            phrase.append(c.getColumnName());
            
            String q = orderQualifierList.get(i++);
            if (q.length() > 0)
            {
                phrase.append(" ");
                phrase.append(q);
            }
            
            phrase.append(", ");
        }

        if (columnTranslatorList.size() > 0)
        {
            // remove last delimiter
            phrase.setLength(phrase.length() - 2);
        }
        
        return phrase.toString();
    }
}
