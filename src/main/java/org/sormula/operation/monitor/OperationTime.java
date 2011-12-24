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
package org.sormula.operation.monitor;

import org.sormula.log.ClassLogger;
import org.sormula.operation.SqlOperation;


/**
 * Records execution times for subclasses of {@link SqlOperation}. The excecution
 * times that may be recorded are prepare (JDBC prepare), write (setting column and where
 * parameters), execute (JDBC execute), and read (reading result set values into row objects).
 * A total time is the sum of all prepare, write, execute, and read times.
 * <p> 
 * An optional parentOperationTime can be set used to hold subtotals of more than
 * on OperationTime.
 * <p>
 * This class is not thread safe. If more than one thread will be updating times, 
 * then synchronize for proper results.
 * 
 * @author Jeff Miller
 * @since 1.5
 */
public class OperationTime
{
    private static final ClassLogger log = new ClassLogger();
    
    String timingId;
    String description;
    ElapsedTime prepareTime;
    ElapsedTime writeTime;
    ElapsedTime executeTime;
    ElapsedTime readTime;
    ElapsedTime totalTime;
    ElapsedTime active;
    long pauseStartTime;
    long pauseDuration;
    
    
    /**
     * Constructs for a timing id. 
     * 
     * @param timingId a String that uniquely identifies a set of timings
     */
    public OperationTime(String timingId)
    {
        this(timingId, null);
    }
    
    
    /**
     * Constructs for a timing id and a parent summary. 
     * 
     * @param timingId a String that uniquely identifies a set of timings
     * @param parentOperationTime an operation time that will record timings; null for none
     */
    public OperationTime(String timingId, OperationTime parentOperationTime)
    {
        this.timingId = timingId;
        totalTime   = new ElapsedTime("totalTime");

        ElapsedTime parentPrepareTime;
        ElapsedTime parentWriteTime;
        ElapsedTime parentExecuteTime;
        ElapsedTime parentReadTime;

        if (parentOperationTime != null)
        {
            // prepare, write, execute, read times are summed into corresponding parent
            parentPrepareTime = parentOperationTime.getPrepareTime();
            parentWriteTime = parentOperationTime.getWriteTime();
            parentExecuteTime = parentOperationTime.getExecuteTime();
            parentReadTime = parentOperationTime.getReadTime();
        }
        else
        {
            // no parent time to sum into
            parentPrepareTime = null;
            parentWriteTime = null;
            parentExecuteTime = null;
            parentReadTime = null;
        }
        
        prepareTime = new ElapsedTime("prepare", totalTime, parentPrepareTime);
        writeTime   = new ElapsedTime("write",   totalTime, parentWriteTime);
        executeTime = new ElapsedTime("execute", totalTime, parentExecuteTime);
        readTime    = new ElapsedTime("read",    totalTime, parentReadTime);
    }

    
    /**
     * @return timing id that was supplied in constructor
     */
    public String getTimingId()
    {
        return timingId;
    }
    
    
    /**
     * @return description supplied by {@link #setDescription(String)}
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Sets description for timings that are logged with {@link #logTimings()}. A good description 
     * to use would be {@link SqlOperation#getSql()}.
     * 
     * @param description human readable label for timings when they are logged
     */
    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * Starts recording prepare time. Use {@link #stop()} to stop recording.
     */
    public void startPrepareTime()
    {
        start(prepareTime);
    }
    
    
    /**
     * Starts recording write time. Use {@link #stop()} to stop recording.
     */
    public void startWriteTime()
    {
        start(writeTime);
    }
    
    
    /**
     * Starts recording execute time. Use {@link #stop()} to stop recording.
     */
    public void startExecuteTime()
    {
        start(executeTime);
    }
    
    
    /**
     * Starts recording read time. Use {@link #stop()} to stop recording.
     */
    public void startReadTime()
    {
        start(readTime);
    }
    
    
    protected void start(ElapsedTime et) 
    {
        if (active != null)
        {
            // warn but don't throw exception since stop may not have occurred if an exception
            // was thrown while timing was started
            log.warn("start when " + active.getName() + " is already active, ignoring " + active.getName());            
        }

        // start recording 
        pauseDuration = 0;
        pauseStartTime = 0;
        active = et;
        active.start();
    }
    
    
    /**
     * Stops recording the active time that was started with one of the start methods. The
     * total pause time is subtracted from total time between start/stop.
     */
    public void stop()
    {
        if (active != null)
        {
            // currently started
            active.stop();
            active.adjust(pauseDuration);
            pauseDuration = 0;
            pauseStartTime = 0;
            active = null;
        }
    }
    
    
    /**
     * Stops recording the currently active time until {@link #resume()} is invoked. Zero
     * or more pause/resume pairs may be invoked.
     */
    public void pause()
    {
        if (active != null)
        {
            // currently started
            if (pauseStartTime == 0)
            {
                // not paused
                pauseStartTime = System.nanoTime();
            }
        }
        else
        {
            log.warn("attempt to pause without start");
        }
    }
    
    
    /**
     * Starts recording the active time that was paused.
     */
    public void resume()
    {
        if (pauseStartTime > 0)
        {
            // paused (sum to allow multiple pause/resume)
            pauseDuration += System.nanoTime() - pauseStartTime;
            pauseStartTime = 0;
        }
        else
        {
            log.warn("attempt to resume without pause");
        }
    }


    /**
     * @return prepare time
     */
    public ElapsedTime getPrepareTime()
    {
        return prepareTime;
    }


    /**
     * @return write time
     */
    public ElapsedTime getWriteTime()
    {
        return writeTime;
    }


    /**
     * @return execute time
     */
    public ElapsedTime getExecuteTime()
    {
        return executeTime;
    }


    /**
     * @return read time
     */
    public ElapsedTime getReadTime()
    {
        return readTime;
    }


    /**
     * @return sum of prepare, write, execute, and read time
     */
    public ElapsedTime getTotalTime()
    {
        return totalTime;
    }
    
    
    /**
     * Writes prepare, write, execute, and read time to log along with average, total, and percent.
     */
    public void logTimings()
    {
        log.info(description);
        log.info("timingId=" + timingId);
        log.info(format(getPrepareTime()));
        log.info(format(getWriteTime()));
        log.info(format(getExecuteTime()));
        log.info(format(getReadTime()));
        
        // don't show count, avg, or percent for total since counts may vary for prepare, write, execute, read
        log.info("total   100% " + getTotalTime().getFormattedTime()); 
    }
    
    
    protected String format(ElapsedTime et)
    {
        int percent = (int)Math.round(100d * et.getTime() / totalTime.getTime());
        
        return String.format("%-7.7s %3d%% %s n=%3d avg=%s", et.getName(),
            percent, et.getFormattedTime(), et.getCount(), et.getFormattedAverageTime());
    }
}
