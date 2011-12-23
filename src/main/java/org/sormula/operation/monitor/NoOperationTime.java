package org.sormula.operation.monitor;



/**
 * Do nothing implementation when no timings are desired.
 * 
 * @author Jeff Miller
 * @since 1.5
 */
// TODO rename?
public class NoOperationTime extends OperationTime
{
    public NoOperationTime()
    {
        super("no-op");
    }
    
    
    @Override
    public void startPrepareTime()
    {
    }
    
    
    @Override
    public void startWriteTime()
    {
    }
    
    
    @Override
    public void startExecuteTime()
    {
    }
    
    
    @Override
    public void startReadTime()
    {
    }
    
    
    @Override
    public void stop() 
    {
    }


    @Override
    public void pause()
    {
    }


    @Override
    public void resume()
    {
    }
    

    @Override
    public void logTimings()
    {
    }
}
