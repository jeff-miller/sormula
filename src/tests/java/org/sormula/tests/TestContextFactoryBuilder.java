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
package org.sormula.tests;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.sormula.log.ClassLogger;


/**
 * Simulates context for testing JNDI lookups.
 *  
 * @author Jeff Miller
 */
public class TestContextFactoryBuilder implements InitialContextFactoryBuilder
{
    private static final ClassLogger log = new ClassLogger();
    private static final TestContextFactory testContextFactory = new TestContextFactory();
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException
    {
        log.debug("createInitialContextFactory"); 
        return testContextFactory;
    }
}


class TestContextFactory implements InitialContextFactory
{
    private static final ClassLogger log = new ClassLogger();
    private static TestInitialContext testInitialContext;
    static 
    {
        try
        {
            testInitialContext = new TestInitialContext();
        }
        catch (NamingException e)
        {
            log.error("error creating factory", e);
        }
    }
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
    {
        log.debug("getInitialContext");
        return testInitialContext;
    }
}


class TestInitialContext extends InitialContext
{
    private static final ClassLogger log = new ClassLogger();
    Map<String, Object> bindings = new HashMap<String, Object>();

    public TestInitialContext() throws NamingException
    {
        super();
    }

    @Override
    public void bind(String name, Object obj) throws NamingException
    {
        if (log.isDebugEnabled()) log.debug("bind " + name + " to " + obj);
        bindings.put(name, obj);
    }
    
    @Override
    public Object lookup(String name) throws NamingException
    {
        Object object = bindings.get(name);
        if (log.isDebugEnabled()) log.debug("lookup " + name + " returns " + object);
        return object; 
    }
}
