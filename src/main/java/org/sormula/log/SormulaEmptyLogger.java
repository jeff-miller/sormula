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
package org.sormula.log; // TODO separate package?

/**
 * TODO
 * @author Jeff Miller
 * @since 4.3
 */
public class SormulaEmptyLogger implements SormulaLogger
{
    public SormulaEmptyLogger(String className)
    {
    }


    @Override
    public void info(Object message)
    {
    }

    
    @Override
    public void debug(Object message)
    {
    }

    
    @Override
    public void error(Object message)
    {
    }

    
    @Override
    public void error(Object message, Throwable throwable)
    {
    }

    
    @Override
    public boolean isDebugEnabled()
    {
        return false;
    }
}
