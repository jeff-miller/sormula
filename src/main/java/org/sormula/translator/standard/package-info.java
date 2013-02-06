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


/**
 * Classes that convert values from standard Java data types to a prepared statement and 
 * to convert from result set to standard Java data types.
 * 
 * @see org.sormula.annotation.ImplicitType
 * @see org.sormula.Database#putTypeTranslator(Class, org.sormula.translator.TypeTranslator)
 * @see org.sormula.Table#putTypeTranslator(Class, org.sormula.translator.TypeTranslator)
 */
package org.sormula.translator.standard;