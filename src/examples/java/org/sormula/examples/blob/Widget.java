package org.sormula.examples.blob;

import java.io.Serializable;


/**
 * Stored as a SQL BLOB for examples {@link BlobExample}.
 */
@SuppressWarnings("serial")
public class Widget implements Serializable
{
	int test;
	String something;
	
	
    public Widget(int test, String something)
    {
        this.test = test;
        this.something = something;
    }
    
    
    public int getTest()
    {
        return test;
    }
    
    
    public String getSomething()
    {
        return something;
    }
}
