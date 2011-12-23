package org.sormula.operation.monitor;

import org.sormula.log.ClassLogger;
import org.sormula.operation.SqlOperation;


/**
 * Records execution times for subclasses of {@link SqlOperation}. An optional
 * parentOperationTime can be set with TODO to hold subtotals of more than
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
    
    String id;
    String description;
    ElapsedTime prepareTime;
    ElapsedTime writeTime;
    ElapsedTime executeTime;
    ElapsedTime readTime;
    ElapsedTime totalTime;
    ElapsedTime active;
    long pauseStartTime;
    long pauseDuration;
    
    
    public OperationTime(String id)
    {
        this(id, null);
    }
    
    
    public OperationTime(String id, OperationTime parentOperationTime)
    {
        this.id = id;
        totalTime   = new ElapsedTime("totalTime", null, null);

        ElapsedTime parentPrepareTime;
        ElapsedTime parentWriteTime;
        ElapsedTime parentExecuteTime;
        ElapsedTime parentReadTime;

        if (parentOperationTime != null)
        {
            parentPrepareTime = parentOperationTime.getPrepareTime();
            parentWriteTime = parentOperationTime.getWriteTime();
            parentExecuteTime = parentOperationTime.getExecuteTime();
            parentReadTime = parentOperationTime.getReadTime();
        }
        else
        {
            parentPrepareTime = null;
            parentWriteTime = null;
            parentExecuteTime = null;
            parentReadTime = null;
        }
        
        prepareTime = new ElapsedTime("prepareTime", totalTime, parentPrepareTime);
        writeTime   = new ElapsedTime("writeTime",   totalTime, parentWriteTime);
        executeTime = new ElapsedTime("executeTime", totalTime, parentExecuteTime);
        readTime    = new ElapsedTime("readTime",    totalTime, parentReadTime);
    }

    
    // TODO javadoc's
    public String getId()
    {
        return id;
    }
    
    
    public String getDescription()
    {
        return description;
    }


    /**
     * Sets description to appear in logs. For operations that use
     * the same sql, {@link SqlOperation#getSql()} is a good 
     * description.
     * 
     * @param description TODO
     */
    public void setDescription(String description)
    {
        this.description = description;
    }


    public void startPrepareTime()
    {
        start(prepareTime);
    }
    
    
    public void startWriteTime()
    {
        start(writeTime);
    }
    
    
    public void startExecuteTime()
    {
        start(executeTime);
    }
    
    
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


    public ElapsedTime getPrepareTime()
    {
        return prepareTime;
    }


    public ElapsedTime getWriteTime()
    {
        return writeTime;
    }


    public ElapsedTime getExecuteTime()
    {
        return executeTime;
    }


    public ElapsedTime getReadTime()
    {
        return readTime;
    }


    public ElapsedTime getTotalTime()
    {
        return totalTime;
    }
    
    
    public void logTimings()
    {
        log.info(description);
        log.info("id=" + id);
        log.info(format("prepare", getPrepareTime()));
        log.info(format("write  ", getWriteTime()));
        log.info(format("execute", getExecuteTime()));
        log.info(format("read   ", getReadTime()));
        
        // don't show count, avg, or percent for total since counts may vary for prepare, write, execute, read
        log.info("total   100% " + getTotalTime().getFormattedTime()); 
    }
    
    
    protected String format(String description, ElapsedTime et)
    {
        int percent = (int)Math.round(100d * et.getTime() / totalTime.getTime());
        
        return description + String.format(" %3d%% %s n=%3d avg=%s",
            percent, et.getFormattedTime(), et.getCount(), et.getFormattedAverageTime());
    }
}
