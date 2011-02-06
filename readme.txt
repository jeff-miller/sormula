sormula

See SimpleExample directory for stand-alone example


To view all ant tasks:
ant -p


To run an example:
ant SelectExample1
ant SelectExample2
ant InsertExample1
etc.


To run tests:
1. Edit build.properties
   a) Set groups property to indicate tests to run (default is all)
   b) Set class property to indicate a specific class to test (default is all)
   c) Set db.dir property to indicate database to use (default is hsqldb)
2. ant tests

