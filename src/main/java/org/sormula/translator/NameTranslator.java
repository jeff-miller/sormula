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
 * Converts Java class or member name to the corresponding SQL table or column name.
 * For example, class name of "UserProfile" may correspond to table name of "user_profile".
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public interface NameTranslator
{
    /**
     * Provides SQL name for corresponding Java class or member name
     * @param javaName Java class or member name
     * @param rowClass class for row objects (not parameterized so that NameTranslator's may be used in annotations)
     * @return SQL name that corresponds to Java name
     */
    public String translate(String javaName, Class rowClass);
}
