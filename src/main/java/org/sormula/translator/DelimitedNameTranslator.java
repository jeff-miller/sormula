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
 * Converts name to name surrounded with delimiters. For example:
 * <ul>
 * <li>SomeName to "SomeName"</li>
 * <li>SomeName to [SomeName]</li>
 * </ul>
 * Delimiters can be set with {@link #setOpeningDelimiter(String)} and {@link #setClosingDelimiter(String)}.
 * They are double quotes by default.
 * <p>
 * Case is preserved. Use {@link UpperCaseNameTranslator} or {@link LowerCaseNameTranslator}
 * in addition to this class to force a specific case. With most datatabases a delimited identifier
 * is case-sensitive.
 * 
 * @since 1.8
 * @author Jeff Miller
 */
public class DelimitedNameTranslator implements NameTranslator
{
    String openingDelimiter;
    String closingDelimiter;
    
    
    /**
     * Constructs for default opening and closing delimiters of double quotes.
     */
    public DelimitedNameTranslator()
    {
        this("\"", "\"");
    }
    
    
    public DelimitedNameTranslator(String openingDelimiter, String closingDelimiter)
    {
        this.openingDelimiter = openingDelimiter;
        this.closingDelimiter = closingDelimiter;
    }


    /**
     * {@inheritDoc}
     */
    public String translate(String name, Class rowClass)
    {
        return openingDelimiter + name + closingDelimiter;
    }


    /**
     * Gets the opening delimiter.
     * 
     * @return string to preceed name
     */
    public String getOpeningDelimiter()
    {
        return openingDelimiter;
    }


    /**
     * Sets the opening delimiter.
     * 
     * @param openingDelimiter string to preceed name
     */
    public void setOpeningDelimiter(String openingDelimiter)
    {
        this.openingDelimiter = openingDelimiter;
    }


    /**
     * Gets the closing delimiter.
     * 
     * @return string to follow name
     */
    public String getClosingDelimiter()
    {
        return closingDelimiter;
    }


    /**
     * Sets the closing delimiter.
     * 
     * @param closingDelimiter string to follow name
     */
    public void setClosingDelimiter(String closingDelimiter)
    {
        this.closingDelimiter = closingDelimiter;
    }
}
