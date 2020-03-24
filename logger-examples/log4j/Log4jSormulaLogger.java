/* sormula - Simple String relational mapping
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sormula.log.SormulaLogger;


/**
 * A {@link SormulaLogger} that logs using log4j2 api. 
 * 
 * @author Jeff Miller
 * @since 4.4
 */
public class Log4jSormulaLogger implements SormulaLogger
{
    Logger logger;
    
    
    public Log4jSormulaLogger(String name)
    {
        this.logger = LogManager.getLogger(name);
    }


    @Override
    public void info(String message)
    {
        logger.info(message);
    }

    
    @Override
    public void debug(String message)
    {
        logger.debug(message);
    }

    
    @Override
    public void error(String message)
    {
        logger.error(message);
    }

    
    @Override
    public void error(String message, Throwable throwable)
    {
        logger.error(message, throwable);
    }

    
    @Override
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }
}
