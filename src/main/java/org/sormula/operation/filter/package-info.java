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
 * Filters that allow filtering algorithms to be written in Java and applied
 * as rows are read from the database. {@link org.sormula.operation.filter.SelectCascadeFilter}
 * filters one row type. {@link org.sormula.operation.filter.AbstractSelectCascadeFilter} is a
 * base class for a filter that fitlers all row types (one method per type). 
 * 
 * @since 3.1
 */
package org.sormula.operation.filter;

