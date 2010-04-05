/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.rioproject.config.Constants

/*
* Configuration properties for the Gnostic
*/
manager {
    execClassPath =
        '${RIO_HOME}${/}lib${/}boot.jar${:}${RIO_HOME}${/}lib/${/}start.jar${:}${JAVA_HOME}${/}lib${/}tools.jar${:}${RIO_HOME}${/}lib${/}groovy-all.jar'

    inheritOptions = true

    jvmOptions = '''
        -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts
        -Djava.security.policy=${RIO_HOME}${/}policy${/}policy.all
        -DRIO_HOME=${RIO_HOME} -Dorg.rioproject.groups=${org.rioproject.groups}'''

    mainClass = 'com.sun.jini.start.ServiceStarter'

    /* The ${service} token will be replaced by the name of the starter file.
     * For start-reggie the service name will be reggie, for start-monitor the
     * service name will be monitor, etc ... */
    String logExt = System.getProperty(Constants.GROUPS_PROPERTY_NAME,
                                       System.getProperty('user.name'))
    String logDir = '/tmp/logs/rio/'
    String opSys = System.getProperty('os.name')
    if(opSys.startsWith("Windows"))
        logDir = '${java.io.tmpdir}/logs/rio/'
    log = logDir + logExt + '/${service}.log'

    /*
    * Remove any previously created service log files
    */
    cleanLogs = true

    monitorStarter = '${user.dir}${/}src${/}test${/}conf${/}start-monitor.groovy'
    cybernodeStarter = '${RIO_HOME}/config/start-cybernode.groovy'
}