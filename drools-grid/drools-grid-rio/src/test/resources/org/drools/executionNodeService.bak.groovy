import org.rioproject.config.Constants
import java.util.logging.Level

import org.rioproject.resources.servicecore.Service

deployment(name:'executionNodeService',  debug: 'true') {

    logging {
        logger 'org.rioproject.resolver', Level.FINE
    }
    /* Configuration for the discovery group that the service should join.
     * This first checks if the org.rioproject.groups property is set, if not
     * the user name is used */
    groups System.getProperty(Constants.GROUPS_PROPERTY_NAME,
                              System.getProperty('user.name'))

    /* Declares the artifacts required for deployment. Note the 'dl'
     * classifier used for the 'download' jar */
    artifact id:'service', 'org.drools:drools-grid-rio:5.1.0.SNAPSHOT'
    artifact id:'service-dl', 'org.drools:drools-grid-rio:dl:5.1.0.SNAPSHOT'

    /*
     * Declare the service to be deployed. The number of instances deployed
     * defaults to 1. If you require > 1 instances change as needed
     */
    service(name: 'ExecutionNodeService') {
        interfaces {
            classes 'org.drools.grid.ExecutionNodeService'
            artifact ref:'service-dl'
        }
        implementation(class:'org.drools.grid.distributed.impl.ExecutionNodeServiceImpl') {
            artifact ref:'service'
        }
        sla(id:'load', low:10, high: 30) {
            rule resource: 'ScalingRuleHandler', max:5
        }
        sla(id:'ksessionCounter', low:0, high: 2) {
            rule resource: 'ScalingRuleHandler', max:5
        }
        maintain 1
    }
    service(name: 'DirectoryNodeService') {
        interfaces {
            classes 'org.drools.grid.DirectoryNodeService'
            artifact ref:'service-dl'
        }
        implementation(class:'org.drools.grid.distributed.impl.DirectoryNodeServiceImpl') {
            artifact ref:'service'
        }
         
        association (name:'ExecutionNodeService', type:'uses', property:'nodeServices')
         
        maintain 1
    }

    service(name: 'Gnostic') {
        interfaces {
            classes 'org.rioproject.gnostic.Gnostic'
            artifact ref: 'service-dl'
        }
        implementation(class: 'org.rioproject.gnostic.GnosticImpl') {
            artifact ref: 'service'
        }

        parameters {
            parameter name: "create-core-associations", value: "yes"
        }

        associations {
            ['ExecutionNodeService'].each {
                association name: "$it",
                            type: 'uses', property: 'service',
                            serviceType: Service.name
            }
        }
        maintain 1
    }

}