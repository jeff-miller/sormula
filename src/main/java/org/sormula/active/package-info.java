/**
 * An implementation of the <a href="http://www.google.com/search?q=active+record+pattern&oq=active+record">
 * active record pattern</a> built on top of sormula. Classes in this package may be used as an independent
 * alternative to other classes in sormula.
 * <p>
 * {@link org.sormula.active.ActiveRecord} objects know about their {@link org.sormula.active.ActiveDatabase} 
 * so they may be inserted, updated, deleted by simply calling the inherited methods 
 * {@link org.sormula.active.ActiveRecord#save()}, {@link org.sormula.active.ActiveRecord#insert()}, 
 * {@link org.sormula.active.ActiveRecord#update()}, and {@link org.sormula.active.ActiveRecord#delete()}. An 
 * active record may also be processed by analogous methods in {@link org.sormula.active.ActiveTable}, 
 * {@link org.sormula.active.ActiveTable#save(ActiveRecord)},
 * {@link org.sormula.active.ActiveTable#insert(ActiveRecord)}, 
 * {@link org.sormula.active.ActiveTable#update(ActiveRecord)},
 * {@link org.sormula.active.ActiveTable#delete(ActiveRecord)}. 
 * <p>
 * {@link org.sormula.active.ActiveTable} can find active records and operate upon a {@link java.util.Collection} 
 * of active records.
 * <p>
 * For example:
 * <blockquote><pre>
   // get part by primary key
   Inventory inventory = Inventory.table.select(partNumber);
         
   // update
   inventory.setQuantity(inventory.getQuantity() - 42);
   inventory.update();
   </pre></blockquote>
 * 
 * For all active record and active table methods that modify the database, if no
 * {@link org.sormula.active.ActiveTransaction} is used and the connection obtained from 
 * {@link javax.sql.DataSource} has autocommit turned off, then an {@link org.sormula.active.ActiveTransaction}
 * will be created for the duration of each active record method. Therefore you can use the active
 * record methods with or without an {@link org.sormula.active.ActiveTransaction}. However it is 
 * probably better to group related active record methods into a single {@link org.sormula.active.ActiveTransaction}.
 * <p>
 * For example:
 * <blockquote><pre>
    ActiveTransaction transaction = new ActiveTransaction(new ActiveDatabase(dataSource));
    
    try
    {
        transaction.begin();

        // get part by primary key
        Inventory inventory = Inventory.table.select(partNumber);
             
        // update
        inventory.setQuantity(inventory.getQuantity() - 42);
        inventory.update();
        
        transaction.commit();
    }
    catch (ActiveException e)
    {
        transaction.rollback();
    }
 * </pre></blockquote>
 */
package org.sormula.active;
