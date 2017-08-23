/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.services.impl;

import java.io.PrintStream;

import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs container verification against a given release.
 */
public class KieServerContainerVerifier {

    private static final Logger logger = LoggerFactory.getLogger(KieServerContainerVerifier.class);

    public boolean verify(String releaseId) {
        boolean verified;
        try {
            String[] gav = releaseId.split(":");
            verified = verify(gav[0], gav[1], gav[2]);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public boolean verify(String groupId, String artifactId, String version) {
        boolean verified;
        KieServices services = KieServices.Factory.get();
        try {
            ReleaseId releaseId = services.newReleaseId(groupId, artifactId, version);
            verified = verify(releaseId);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public boolean verify(ReleaseId releaseId) {
        boolean verified;
        KieServices services = KieServices.Factory.get();
        try {
            KieContainer container = services.newKieContainer(releaseId);
            verified = verify(container);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public boolean verify(KieContainer container) {
        boolean verified = true;
        try {
            Results results = container.verify();
            for (Message message : results.getMessages()) {
                Level level = message.getLevel();
                switch (level) {
                    case INFO:
                        logger.info(message.toString());
                        break;
                    case WARNING:
                        logger.warn(message.toString());
                        break;
                    case ERROR:
                        logger.error(message.toString());
                        verified = false;
                        break;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public static void main(String[] args) {
        boolean verified = main(args, System.out, System.err);
        if (!verified) {
            System.exit(1);
        }
    }

    // package-protected for JUnit testing
    static final String USAGE = "Usage: java " + KieServerContainerVerifier.class.getName() + " <gav1> <gav2> ...\n";
    static boolean main(String[] args, PrintStream out, PrintStream err) {
        boolean triggered = false;
        boolean verified = true;
        if (args != null && args.length > 0) {
            KieServerContainerVerifier verifier = new KieServerContainerVerifier();
            for (String arg : args) {
                if (arg != null) {
                    arg = arg.trim();
                    if (!arg.isEmpty()) {
                        triggered = true;
                        if (verifier.verify(arg)) {
                            logger.info(arg + " verified.");
                        } else {
                            logger.error(arg + " not verified.");
                            verified = false;
                        }
                    }
                }
            }
        }
        if (!triggered) {
            err.print(USAGE);
        }
        return verified;
    }

}
