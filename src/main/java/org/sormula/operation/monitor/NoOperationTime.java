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
package org.sormula.operation.monitor;


/**
 * Do nothing implementation when no timings are desired. This class provides
 * a way to omit timing and avoid numerous not null checks in all places where timing
 * is needed.
 * 
 * @author Jeff Miller
 * @since 1.5
 */
public class NoOperationTime extends OperationTime
{
    /**
     * Constructs {@link OperationTime} that does nothing.
     */
    public NoOperationTime()
    {
        super("no-op");
        setDescription("NoOp");
    }

    /**
     * Does nothing.
     */
    @Override
    public void startPrepareTime()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void startWriteTime()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void startExecuteTime()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void startReadTime()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void stop()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void cancel()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void pause()
    {
    }

    /**
     * Does nothing.
     */
    @Override
    public void resume()
    {
    }

    /**
     * Does nothing.
     * @param stackTraceElement not used
     */
    @Override
    public void updateSource(StackTraceElement stackTraceElement)
    {
    }
}
