/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2015 Jeff Miller
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
package org.sormula.reflect;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;


/**
 * Defines the type of access for a row field. Annotations {@link Column#fieldAccess()} and {@link Row#fieldAccess()}
 * define how a row field is to be accessed either directly or with getter/setter methods.
 * 
 * @author Jeff Miller
 * @since 3.4
 */
public enum FieldAccessType 
{
    /**
     * Field access is not specified. See {@link Column#fieldAccess()} and {@link Row#fieldAccess()}
     * javadoc for rules about how access is determined.
     */
    Default,
    
    
    /**
     * Field is accessed directly without using getter/setter methods.
     */
    Direct,
    
    
    /**
     * Field is accessed with getter/setter methods.
     */
    Method;
}
