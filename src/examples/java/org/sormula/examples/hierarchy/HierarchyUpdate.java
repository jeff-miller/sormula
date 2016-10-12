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
package org.sormula.examples.hierarchy;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class HierarchyUpdate extends ExampleBase
{
    Table<HierarchyNode> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new HierarchyInsert(); // create table and rows
        new HierarchyUpdate();
        new HierarchySelect(1); // display results
    }
    
    
    public HierarchyUpdate() throws Exception
    {
        // init
        openDatabase();
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(HierarchyNode.class);
            update();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void update() throws SormulaException
    {
    	HierarchyNode node12 = table.select(12); // node 12 always exists
    	changeDescription(node12);
    	table.update(node12);
    }
    
    
    void changeDescription(HierarchyNode node) throws SormulaException
    {
    	String type;
    	if (node.getNodeId() % 2 == 0) type = "EVEN";
    	else type = "ODD";
    	node.setDescription(node.getDescription() + " " + type);
    	
    	for (HierarchyNode child : node.getChildren()) changeDescription(child);
    }
}
