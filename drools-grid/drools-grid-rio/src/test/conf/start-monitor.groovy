/*
 * This configuration is used by the com.sun.jini.start utility to start a
 * ProvisionMonitor, including an embedded Webster
 */

import org.rioproject.config.Component

import org.rioproject.boot.ServiceDescriptorUtil;
import com.sun.jini.start.ServiceDescriptor;

@Component('com.sun.jini.start')
class StartMonitorConfig {

    ServiceDescriptor[] getServiceDescriptors() {
        String m2Home = "${System.getProperty("user.home")}/.m2"
        String rioHome = System.getProperty('RIO_HOME')
        String cwd = System.getProperty('user.dir')
        println "\n********\n$cwd\n********"
        
        def websterRoots = [rioHome+'/lib-dl', ';',
                            rioHome+'/lib',     ';',
                            m2Home+'/repository', ';',
                            cwd+'/target/']

        String policyFile = rioHome+'/policy/policy.all'
        String monitorConfig = rioHome+'/config/monitor.groovy'
        String reggieConfig = rioHome+'/config/reggie.groovy'

        def serviceDescriptors = [
            ServiceDescriptorUtil.getWebster(policyFile, '0', (String[])websterRoots),
            ServiceDescriptorUtil.getLookup(policyFile, reggieConfig),
            ServiceDescriptorUtil.getMonitor(policyFile, monitorConfig)
        ]

        return (ServiceDescriptor[])serviceDescriptors
    }

}
