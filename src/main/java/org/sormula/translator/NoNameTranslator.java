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

import org.sormula.Database;
import org.sormula.annotation.Row;


/**
 * Default name translator that simply returns java name for all name translations. Used
 * as default for {@link Row#nameTranslator()} annotation. 
 * <p>
 * Use {@link Row#nameTranslators()} with an empty array of translators for no name translation.
 * Use this class to force no name translator because an empty array of translators 
 * specified with {@link Row#nameTranslators()} will cause sormula to use
 * {@link Database#getNameTranslatorClasses()}.
 *
 * @since 1.0
 * @author Jeff Miller
 */
public class NoNameTranslator implements NameTranslator
{
    /**
     * @return javaName
     */
    public String translate(String javaName, Class rowClass)
    {
        return javaName;
    }
}
