/*
 * Copyright 2015 Red Hat
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.drools.karaf.itest;

import org.ops4j.pax.exam.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;

public class CommonKarafOptions {
    private static final transient Logger LOG = LoggerFactory.getLogger(CommonKarafOptions.class);

    public static Option[] get() {
        return new Option[]{
                localMavenRepoOption()
        };
    }

    public static Option localMavenRepoOption() {
        String localRepo = System.getProperty("maven.repo.local", "");
        if (localRepo.length() > 0) {
            LOG.info("Using alternative local Maven repository in {}.", new File(localRepo).getAbsolutePath());
        }
        return when(localRepo.length() > 0).useOptions(
                editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg",
                        "org.ops4j.pax.url.mvn.localRepository",
                        new File(localRepo).getAbsolutePath()));
    }
}
