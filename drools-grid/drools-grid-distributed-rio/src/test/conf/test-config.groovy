/*
 * Configuration file for integration test cases
 */
ITGridExecutionTest {
    groups = "ITGridExecutionTest"
    numCybernodes = 1
    numMonitors = 1
    //numLookups = 1
    opstring = 'src/test/resources/org/drools/executionNodeServiceTest.groovy'
    autoDeploy = true
    //harvest = true
}

