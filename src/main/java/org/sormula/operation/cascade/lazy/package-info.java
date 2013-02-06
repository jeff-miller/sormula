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
 * Implementation of lazy loading. When {@link org.sormula.annotation.cascade.SelectCascade#lazy()} true,
 * for a field, the field is not selected when the source row is selected. The lazy select field
 * can be selected at a later time by using the method
 * {@link org.sormula.operation.cascade.lazy.AbstractLazySelector#checkLazySelects(String)} in one
 * of the subclasses {@link org.sormula.operation.cascade.lazy.SimpleLazySelector}, 
 * {@link org.sormula.operation.cascade.lazy.DurableLazySelector}, your own subclass, or
 * your own implementation of {@link org.sormula.operation.cascade.lazy.LazySelectable}.
 * <p>
 * See org.sormula.tests.cascade.lazy package in this project for examples.
 */
package org.sormula.operation.cascade.lazy;