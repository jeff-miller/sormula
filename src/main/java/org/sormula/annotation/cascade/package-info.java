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
 * Annotations for defining cascade relationships between two row classes. A cascade relationship
 * allows an operation on a source row to initiate operations on one or more target rows. For example,
 * use a cascade annotation on a parent row so that read/modification on parent row corresponds to
 * read/modify on child rows.
 * <p>
 * If parent table is cached, then cascaded children of parent will be cascaded at the time parent is
 * added to cache to avoid inconsistencies between cache and database. As a result cascades will occur 
 * when cache is modified not when cache writes to database.
 */
package org.sormula.annotation.cascade;