README
======


Test
----
**src/it** folder contains example projects used for **integration tests**. For such tests, the *maven-invoker-plugin* is used; invoke it with:

    mvn clean install verify -Dkie-pmml-implementation=new
    
At the end of execution, built artifacts are put inside **target/it** folder, while reports are written inside **target/invoker-reports**.


**src/test** folder contains **unit tests**. For such tests, the *maven-surefire-plugin* is used; invoke it with:

    mvn clean test
    
Debug
-----
To debug execution on test project, uncomment the *invoker.mavenOpts* line in the **invoker.properties** file. Launch the remote debugger (on port 8000) after
*maven-invoker-plugin:3.2.0:run* has been print on console 
    




