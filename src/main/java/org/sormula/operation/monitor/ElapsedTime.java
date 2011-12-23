package org.sormula.operation.monitor;



// TODO name?
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
    
    
    public ElapsedTime(String name, ElapsedTime total, ElapsedTime parent)
    {
        this.name = name;
        this.total = total;
        this.parent = parent;
    }
    
    
    public String getName()
    {
        return name;
    }


    public ElapsedTime getTotal()
    {
        return total;
    }


    public ElapsedTime getParent()
    {
        return parent;
    }


    public void start()
    {
        startTime = System.nanoTime();
    }
    
    
    public void stop()
    {
        add(System.nanoTime() - startTime);
    }
    
    
    public void add(long elpased)
    {
        time += elpased;
        ++count;
        if (total != null)  total.add(elpased);
        if (parent != null) parent.add(elpased);
    }
    
    
    // TODO adds (negative subtracts) time without updating count
    public void adjust(long elpased)
    {
        time += elpased;
        if (total != null)  total.adjust(elpased);
        if (parent != null) parent.adjust(elpased);
    }
    
    
    public long getTime()
    {
        return time;
    }

    
    public long getAverageTime()
    {
        if (count > 0) return Math.round((double)time / count);
        else return 0;
    }
    

    public int getCount()
    {
        return count;
    }


    public String getFormattedTime()
    {
        return format(time);
    }


    public String getFormattedAverageTime()
    {
        return format(getAverageTime());
    }
    
    
    protected String format(long t)
    {
        long remaining = t;
        
        long hours   = remaining / nsPerHour;
        remaining = remaining % nsPerHour;
        
        long minutes = remaining / nsPerMintue;
        remaining = remaining % nsPerMintue;
        
        long seconds = remaining / nsPerSecond;
        remaining = remaining % nsPerSecond;
        
        return String.format("%02d:%02d:%02d.%09d", hours, minutes, seconds, remaining);
    }
}
