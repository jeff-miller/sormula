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
package org.sormula;

import java.sql.Connection;


/**
 * Listener interface for {@link Transaction}. Used by {@link Table} to notify cache of
 * transaction boundaries.
 * 
 * @author Jeff Miller
 * @since 3.0
 */
public interface TransactionListener 
{
    /**
     * Indicates start of transaction. Invoked after connection is ready and 
     * {@link Transaction#isActive()}.
     * 
     * @param transaction database transaction that is source of event
     */
    public void begin(Transaction transaction);
    
    
    /**
     * Indicates that transaction is completing normally. Invoked prior to {@link Connection#commit()} and 
     * while {@link Transaction#isActive()} so that connection may be used for additional related
     * database activity if needed.
     * 
     * @param transaction database transaction that is source of event
     */
    public void commit(Transaction transaction);

    
    /**
     * Indicates that transaction is completing normally. Invoked prior to {@link Connection#rollback()} and 
     * while {@link Transaction#isActive()} so that connection may be used for additional related
     * database activity if needed.
     * 
     * @param transaction database transaction that is source of event
     */
    public void rollback(Transaction transaction);
}
