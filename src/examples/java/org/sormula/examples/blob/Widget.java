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
package org.sormula.examples.blob;

import java.io.Serializable;


/**
 * Stored as a SQL BLOB for examples {@link BlobExample}.
 */
@SuppressWarnings("serial")
public class Widget implements Serializable
{
	int test;
	String something;
	
	
    public Widget(int test, String something)
    {
        this.test = test;
        this.something = something;
    }
    
    
    public int getTest()
    {
        return test;
    }
    
    
    public String getSomething()
    {
        return something;
    }
}
