/**
 * An implementation of the <a href="http://www.google.com/search?q=active+record+pattern&oq=active+record">
 * active record pattern</a> built on top of sormula. Classes in this package may be used as an independent
 * alternative to other class in sormula.
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
 * 
 * TODO example? get from example package
 */
package org.sormula.active;