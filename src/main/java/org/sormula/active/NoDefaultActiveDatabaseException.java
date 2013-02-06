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
package org.sormula.active;


/**
 * Indicates there is no active database defined but default was expected with 
 * {@link ActiveDatabase#getDefault()}. If no default {@link ActiveDatabase} is defined, then
 * one must be explictly set with {@link ActiveRecord#attach(ActiveDatabase)}.
 * 
 * @since 3.0
 * @author Jeff Miller
 */
public class NoDefaultActiveDatabaseException extends ActiveException
{
    private static final long serialVersionUID = 1L;


    /**
     * Default constructor.
     */
    public NoDefaultActiveDatabaseException()
    {
        super("no default active database has been set; use ActiveDatabase#setDefault");
    }
}
