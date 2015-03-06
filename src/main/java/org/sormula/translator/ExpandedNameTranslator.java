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


/**
 * Converts name from CamelCase (mixed case) to a SQL name that is case insensitive with
 * a delimiter between each word. For example:
 * <ul>
 * <li>ProductOrder to product_order</li> 
 * <li>orderStatus to order_status</li>
 * <li>WidgetInvoiceHistory to widget_invoice_history</li>
 * </ul>
 * 
 * @since 1.8 and 2.2
 * @author Jeff Miller
 */
public class ExpandedNameTranslator implements NameTranslator
{
    String wordDelimiter;
    
    
    /**
     * Constructs for a default word delimiter of "_".
     */
    public ExpandedNameTranslator()
    {
        wordDelimiter = "_";
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String translate(String camelCaseName, Class rowClass)
    {
        StringBuilder result = new StringBuilder(camelCaseName.length() + 20);
        
        for (int i = 0; i < camelCaseName.length(); ++i)
        {
            char c = camelCaseName.charAt(i);
            
            if (Character.isUpperCase(c) && i > 0)
            {
                // word break occurs before upper case but not first letter
                result.append(wordDelimiter);
            }
            
            result.append(c);
        }
        
        return result.toString();
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
