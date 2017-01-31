/*
 * Copyright 2016 JBoss by Red Hat.
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
package org.kie.server.integrationtests.shared;

import java.io.File;

import org.kie.server.api.KieServerConstants;

public class KieServerUtil {

    public static void deleteDocumentStorageFolder() {
        File storagePath = new File(System.getProperty(KieServerConstants.CFG_DOCUMENT_STORAGE_PATH, "target/docs"));
        deleteFolder(storagePath);
    }

    private static void deleteFolder(File path) {
        File[] directories = path.listFiles();
        if (directories != null) {
            for (File file : directories) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                }
                file.delete();
            }
        }
    }
}
