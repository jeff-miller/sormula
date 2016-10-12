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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class HierarchyInsert extends ExampleBase
{
    Table<HierarchyNode> table;
    Random random;
    
    
    public static void main(String[] args) throws Exception
    {
        new HierarchyInsert();
    }
    
    
    public HierarchyInsert() throws Exception
    {
    	random = new Random(System.currentTimeMillis());
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "hierarchynode";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(nodeid INTEGER NOT NULL PRIMARY KEY," +
                " parentnodeid INTEGER," +
                " description VARCHAR(30))" 
        );
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(HierarchyNode.class);
            insertRows();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRows() throws SormulaException
    {
        HierarchyNode root = new HierarchyNode();
        root.setNodeId(1);
        root.setDescription("root node");
        createHierachy(root, 1);
        
        System.out.println("\nInserting hierarchy:");
        root.print();
        
        table.insert(root);
    }
    
    
    void createHierachy(HierarchyNode parent, int level)
    {
    	// random depth of at least 3 but no deeper than 6 levels
    	int maxDepth = Math.max(3, random.nextInt(7));
    	
    	if (level < maxDepth) 
    	{
	    	// create random number of children
    		// ensure that upper levels create minimum number of 
	    	// children so that hierarchy is not trivial 
    		int quantityChildren = Math.max(4 - level, random.nextInt(6));

	    	if (quantityChildren > 0)
	    	{
	    		// at least one child
	    		List<HierarchyNode> children = new ArrayList<>();
	    		parent.setChildren(children);
	    		int childLevel = level + 1;
	    		int childBaseNodeId = parent.getNodeId() * 10;
	    		
	    		for (int i = 1; i <= quantityChildren; ++i)
	    		{
	    			// create child
	    			HierarchyNode child = new HierarchyNode();
	    			child.setNodeId(childBaseNodeId + i);
	    			child.setParentNodeId(parent.getNodeId());
	    			child.setDescription("node " + child.getNodeId());
	    			children.add(child);
	    			
	    			// create next level children
	    			createHierachy(child, childLevel);
	    		}
	    	}
    	}
    }
}
