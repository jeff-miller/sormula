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


/**
 * Converts Java class or member name that is in mixed case to a SQL table or column name that
 * is case insensitive. For example:
 * <ul>
 * <li>ProductOrder to product_order</li> 
 * <li>orderStatus to order_status</li>
 * <li>WidgetInvoiceHistory to widget_invoice_history</li>
 * </ul>
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class StandardNameTranslator implements NameTranslator
{
    boolean upperCase;
    String wordDelimiter;
    
    
    /**
     * Constructs for defaults of uppercase output is false and word delimiter of "_".
     */
    public StandardNameTranslator()
    {
        upperCase = false;
        wordDelimiter = "_";
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String translate(String javaName, Class rowClass)
    {
        StringBuilder result = new StringBuilder(javaName.length() + 20);
        
        for (int i = 0; i < javaName.length(); ++i)
        {
            char c = javaName.charAt(i);
            
            if (Character.isUpperCase(c) && i > 0)
            {
                // word break occurs before uppercase but not first letter
                result.append(wordDelimiter);
            }
            
            if (isUpperCase())
            {
                // result is all upper
                result.append(Character.toUpperCase(c));
            }
            else
            {
                // result is all lower
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    

    /**
     * Reports case of {@link #translate(String, Class)}.
     * 
     * @return true if sql result is all uppercase; default is false
     */
    public boolean isUpperCase()
    {
        return upperCase;
    }
    
    
    /**
     * Sets desired case of {@link #translate(String, Class)}.
     * 
     * @param upperCase true for sql names in uppercase; false for all lowercase sql names
     */
    public void setUpperCase(boolean upperCase)
    {
        this.upperCase = upperCase;
    }
    
    
    /**
     * Gets the delimiter to use between words in sql names.
     * 
     * @return string to insert between each word; default is "_"
     */
    public String getWordDelimiter()
    {
        return wordDelimiter;
    }
    
    
    /**
     * Sets delimiter between words in sql names.
     * 
     * @param wordDelimiter String to appear between words
     */
    public void setWordDelimiter(String wordDelimiter)
    {
        this.wordDelimiter = wordDelimiter;
    }
}
