package org.sormula.operation.monitor;



// TODO name?
public class ElapsedTime
{
    private static int msPerSecond = 1000;
    private static int msPerMintue = 60 * msPerSecond;
    private static int msPerHour   = 60 * msPerMintue;
    
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
        startTime = System.currentTimeMillis();
    }
    
    
    public void stop()
    {
        add(System.currentTimeMillis() - startTime);
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
        
        int hours   = (int)(remaining / msPerHour);
        remaining = remaining % msPerHour;
        
        int minutes = (int)(remaining / msPerMintue);
        remaining = remaining % msPerMintue;
        
        int seconds = (int)(remaining / msPerSecond);
        remaining = remaining % msPerSecond;
        
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, remaining);
    }
}
