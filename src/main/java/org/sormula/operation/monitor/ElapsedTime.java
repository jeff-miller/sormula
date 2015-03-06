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

import org.sormula.log.ClassLogger;


/**
 * Records elapsed time in nanoseconds. A count, {@link #getCount()}, is incremented
 * for each start/stop so that an average elapsed time can be calculated, {@link #getAverageTime()}.
 * 
 * @author Jeff Miller
 * @since 1.5
 */
public class ElapsedTime
{
    private static final ClassLogger log = new ClassLogger();
    private static long nsPerSecond = 1000000000L;
    private static long nsPerMintue = 60 * nsPerSecond;
    private static long nsPerHour   = 60 * nsPerMintue;
    
    String name;
    ElapsedTime total;
    ElapsedTime parent;
    long time;
    long startTime;
    int count;
    long pauseStartTime;
    long pauseDuration;
    String timeFormat;
    boolean ignoreFirst;
    boolean firstIgnored;
    
    
    /**
     * Constructs for name and no other totals.
     * 
     * @param name name to display in log file
     */
    public ElapsedTime(String name)
    {
        this(name, null, null);
    }
    
    
    /**
     * Constructs for name, total, and parent.
     * 
     * @param name name to display in log file
     * @param total elapsed times are summed into this total; null for none
     * @param parent elapsed times are summed into this parent total; null for none
     */
    public ElapsedTime(String name, ElapsedTime total, ElapsedTime parent)
    {
        this.name = name;
        this.total = total;
        this.parent = parent;
        timeFormat = "%02d:%02d:%02d.%09d";
    }
    
    
    /**
     * @return name supplied in constructor
     */
    public String getName()
    {
        return name;
    }


    /**
     * @return total time supplied in constructor
     */
    public ElapsedTime getTotal()
    {
        return total;
    }


    /**
     * @return parent time supplied in constructor
     */
    public ElapsedTime getParent()
    {
        return parent;
    }


    /**
     * Starts recording eplased time.
     */
    public void start()
    {
        pauseDuration = 0;
        pauseStartTime = 0;
        startTime = System.nanoTime();
    }
    
    
    /**
     * Stops recording elapsed time.
     */
    public void stop()
    {
        if (ignoreFirst && !firstIgnored)
        {
            // don't record first sample, set indicator start recording 
            firstIgnored = true;
        }
        else
        {
            add(System.nanoTime() - startTime - pauseDuration);
        }
    }
    
    
    /**
     * Cancels any time recorded since {@link #start()} and time paused with {@link #pause()}.
     */
    public void cancel()
    {
        startTime = 0;
        pauseStartTime = 0;
        pauseDuration = 0;
    }
    
    
    /**
     * Stops recording time until {@link #resume()} is invoked. Zero
     * or more pause/resume pairs may be invoked.
     */
    public void pause()
    {
        if (startTime != 0)
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
     * Starts recording the after pause.
     */
    public void resume()
    {
        if (pauseStartTime != 0)
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
     * Adds nanoseconds to this time and adds to total and parent if they are not
     * null. Increments the count by 1.
     * 
     * @param elpasedNanos nanoseconds to add; positive to add or negative to subtract
     */
    protected void add(long elpasedNanos)
    {
        time += elpasedNanos;
        ++count;
        if (total != null)  total.add(elpasedNanos);
        if (parent != null) parent.add(elpasedNanos);
    }
    
    
    /**
     * @return elapsed nanoseconds
     */
    public long getTime()
    {
        return time;
    }

    
    /**
     * @return average elapsed nanoseconds if count > 0; otherwise 0
     */
    public long getAverageTime()
    {
        if (count > 0) return Math.round((double)time / count);
        else return 0;
    }
    

    /**
     * @return number of times that {@link #stop()} or {@link #add(long)} have been invoked
     */
    public int getCount()
    {
        return count;
    }


    /**
     * @return true if first time is ignored; false if all times are recorded
     */
    public boolean isIgnoreFirst()
    {
        return ignoreFirst;
    }


    /**
     * Sets what to do with the first time recorded. Ignoring first time value (the
     * first time that {@link #stop()} is used) is a rough way to exclude class loading
     * times which may skew the average time for all occurrences.
     * 
     * @param ignoreFirst true to ignore the first time; false to record all times
     */
    public void setIgnoreFirst(boolean ignoreFirst)
    {
        this.ignoreFirst = ignoreFirst;
    }


    /**
     * @return if the first time has occurred and was ignored
     */
    public boolean isFirstIgnored()
    {
        return firstIgnored;
    }


    /**
     * Gets the format string to use by {@link #getFormattedTime()} and
     * {@link #getFormattedAverageTime()}.
     * 
     * @return {@link String#format(String, Object...)} format to use
     */
    public String getTimeFormat()
    {
        return timeFormat;
    }

    
    /**
     * Sets the format string to use when formatting time. Default is
     * %02d:%02d:%02d.%09d. Values formatted are hours, minutes, seconds, and
     * nanoseconds in that order. {@link String#format(String, Object...)} is used
     * to format.
     * 
     * @param timeFormat {@link String#format(String, Object...)} format to use
     */
    public void setTimeFormat(String timeFormat)
    {
        this.timeFormat = timeFormat;
    }


    /**
     * @return elapsed time formatted as a String
     */
    public String getFormattedTime()
    {
        return format(time);
    }


    /**
     * @return average elapsed time formatted as a String
     */
    public String getFormattedAverageTime()
    {
        return format(getAverageTime());
    }
    
    
    /**
     * Formats the elapsed time using the current time format.
     * 
     * @param nanoseconds time in nanoseconds 
     * @return nanoseconds formatted into String 
     */
    protected String format(long nanoseconds)
    {
        long remaining = nanoseconds;
        
        long hours   = remaining / nsPerHour;
        remaining = remaining % nsPerHour;
        
        long minutes = remaining / nsPerMintue;
        remaining = remaining % nsPerMintue;
        
        long seconds = remaining / nsPerSecond;
        remaining = remaining % nsPerSecond;
        
        return String.format(timeFormat, hours, minutes, seconds, remaining);
    }
}
