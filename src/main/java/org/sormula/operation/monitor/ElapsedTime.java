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


/**
 * Records elapsed time in nano seconds. A count, {@link #getCount()}, is incremented
 * for each start/stop so that an average elapsed time can be calculated, {@link #getAverageTime()}.
 * 
 * @author Jeff Miller
 * @since 1.5
 */
public class ElapsedTime
{
    private static long nsPerSecond = 1000000000L;
    private static long nsPerMintue = 60 * nsPerSecond;
    private static long nsPerHour   = 60 * nsPerMintue;
    
    String name;
    ElapsedTime total;
    ElapsedTime parent;
    long time;
    long startTime;
    int count;
    String timeFormat;
    
    
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
        startTime = System.nanoTime();
    }
    
    
    /**
     * Stops recording elapsed time.
     */
    public void stop()
    {
        add(System.nanoTime() - startTime);
    }
    
    
    /**
     * Adds nanoseconds to this time and adds to total and parent if they are not
     * null. Increments the count by 1.
     * 
     * @param elpasedNanos nanoseconds to add; positive to add or negative to subtract
     */
    public void add(long elpasedNanos)
    {
        time += elpasedNanos;
        ++count;
        if (total != null)  total.add(elpasedNanos);
        if (parent != null) parent.add(elpasedNanos);
    }
    
    
    /**
     * Adds nanoseconds to this time and adds to total and parent if they are not
     * null. Count is not incremented.
     * 
     * @param elpasedNanos nanoseconds to add; positive to add or negative to subtract
     */
    public void adjust(long elpasedNanos)
    {
        time += elpasedNanos;
        if (total != null)  total.adjust(elpasedNanos);
        if (parent != null) parent.adjust(elpasedNanos);
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
