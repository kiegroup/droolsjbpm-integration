/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.maven.plugin;

import org.kie.memorycompiler.resources.KiePath;
import org.kie.memorycompiler.resources.ResourceStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.drools.core.util.IoUtils.readBytesFromInputStream;

public class DiskResourceStore implements ResourceStore {
    private final KiePath rootPath;

    public DiskResourceStore(File root) {
        this.rootPath = KiePath.of(root.getAbsolutePath());
    }

    @Override
    public void write(KiePath resourcePath, byte[] pResourceData) {
        write(resourcePath, pResourceData, false);
    }

    @Override
    public void write(KiePath resourcePath, byte[] pResourceData, boolean createFolder) {
        File file = new File(getFilePath(resourcePath));
        if (createFolder) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(pResourceData);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) { }
            }
        }
    }

    @Override
    public byte[] read(KiePath resourcePath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getFilePath(resourcePath));
            return readBytesFromInputStream(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) { }
            }
        }
    }

    @Override
    public void remove(KiePath resourcePath) {
        File file = new File(getFilePath(resourcePath));
        if (file.exists()) {
            file.delete();
        }
    }

    private String getFilePath(KiePath resourcePath) {
        return rootPath.resolve(resourcePath).asString();
    }
}
