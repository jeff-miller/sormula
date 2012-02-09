sormula for JDK 7

- This version is built from Sormula 1.6 to use new featues of JDK 7 
- SqlOperation (and subclasses) implement AutoCloseable
- Deprecated classes and methods from Sormula 1.6 have been removed
- See SimpleExample directory for stand-alone example
- See ZeroConfigExample directory for a stand-alone example using sormula with 
  nothing but sormula.jar in path and no configuration
- See src/examples directory for more complex examples


To view all ant tasks:
ant -p


To run an example:
ant BasicSelect
ant CascadeSelect
ant BasicInsert
etc.


To run tests:

1. Edit build.properties
   a) Set groups property to indicate tests to run (default is all)
   b) Set class property to indicate a specific class to test (default is all)
   c) Set db.dir property to indicate database to use (default is hsqldb)

2. ant tests



To add a database for testing, see add-database.txt

