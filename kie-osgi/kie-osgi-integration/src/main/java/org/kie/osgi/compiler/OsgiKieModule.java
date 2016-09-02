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

package org.kie.osgi.compiler;

import org.drools.compiler.kie.builder.impl.AbstractKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import static org.drools.core.util.IoUtils.readBytesFromInputStream;

public class OsgiKieModule extends AbstractKieModule {

    private static final String WEB_INF_CLASSES_PATH = "/WEB-INF/classes";

    private final Bundle bundle;

    private Collection<String> fileNames;

    private final long creationTimestamp = System.currentTimeMillis();

    private OsgiKieModule(ReleaseId releaseId, KieModuleModel kModuleModel, Bundle bundle) {
        super(releaseId, kModuleModel);
        this.bundle = bundle;
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAvailable(String pResourceName) {
        return fileNames.contains(pResourceName);
    }

    @Override
    public byte[] getBytes(String pResourceName) {
        // Add a leading '/' char as it has been removed from
        // file Resource name
        pResourceName = "/" + pResourceName;
        URL url = bundle.getResource(pResourceName);
        // the following is a hack for specific use case - resources are under WEB-INF/classes when the deployed bundle is a WAR
        if (url == null) {
            url = bundle.getResource(WEB_INF_CLASSES_PATH + pResourceName);
        }
        return url == null ? null : readUrlAsBytes(url);
    }

    @Override
    public Collection<String> getFileNames() {
        if (fileNames != null) {
            return fileNames;
        }
        fileNames = new ArrayList<String>();
        Enumeration<URL> e = bundle.findEntries("", "*", true);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            String path = url.getPath();
            if (path.endsWith("/")) {
                continue;
            }
            // the following is a hack for specific use case - WEB-INF/classes prefix is present when the deployed bundle is a WAR
            // remove the prefix if present
            if (path.startsWith(WEB_INF_CLASSES_PATH)) {
                path = path.substring(WEB_INF_CLASSES_PATH.length());
            }
            fileNames.add(path.substring(1));

        }
        return fileNames;
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public String toString() {
        return "OsgiKieModule[releaseId=" + this.getReleaseId() +
                ", bundle-id=" + bundle.getBundleId() +
                ", bundle-location=" + bundle.getLocation() +
                "]";
    }

    public static OsgiKieModule create(URL url) {
        KieModuleModel kieProject = KieModuleModelImpl.fromXML(url);
        Bundle bundle = getBundle(url.toString());
        if (bundle != null) {
            String pomProperties = getPomProperties(bundle);
            ReleaseId releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
            return create(releaseId, kieProject, bundle);
        } else {
            throw new RuntimeException("Bundle does not exist or no retrieved for this URL :  " + url);
        }
    }

    public static OsgiKieModule create(URL url, ReleaseId releaseId, KieModuleModel kieProject) {
        Bundle bundle = getBundle(url.toString());
        if (bundle != null) {
            return create(releaseId, kieProject, bundle);
        } else {
            throw new RuntimeException("Bundle does not exist or no retrieved for this URL :  " + url);
        }
    }

    public static OsgiKieModule create(ReleaseId releaseId, KieModuleModel kieProject, Bundle bundle) {
        return new OsgiKieModule(releaseId, kieProject, bundle);
    }

    /**
     * Parses OSGi bundle ID from the provided URL. The URL may not be coming from OSGi,
     * in which case "null" is returned
     *
     * @param url URL
     * @return parsed bundle ID, or null if the url is not OSGi bundle URL
     */
    public static String parseBundleId(String url) {
        if (isOsgiBundleUrl(url)) {
            int slashesIdx = url.indexOf("://");
            return url.substring(slashesIdx + "://".length(), url.indexOf('.'));
        } else {
            return null;
        }
    }

    /**
     * Determines if the provided string is OSGi bundle URL or not.
     *
     * @param str string to check
     * @return true if the string is OSGi bundle URL, otherwise false
     */
    public static boolean isOsgiBundleUrl(String str) {
        if (str == null) {
            throw new NullPointerException("Specified string can not be null!");
        }
        return str.startsWith("bundle") && str.contains("://");
    }

    private static String getPomProperties(Bundle bundle) {
        Enumeration<URL> e = bundle.findEntries("META-INF/maven", "pom.properties", true);
        if (e == null || !e.hasMoreElements()) {
            throw new RuntimeException("Cannot find pom.properties file in bundle " + bundle);
        }
        return readUrlAsString(e.nextElement());
    }

    private static Bundle getBundle(String url) {
        String id = parseBundleId(url);
        if (id == null) {
            return null;
        }
        long bundleId = Long.parseLong(id);
        return FrameworkUtil.getBundle(OsgiKieModule.class).getBundleContext().getBundle(bundleId);
    }

    private static String readUrlAsString(URL url) {
        return new String(readUrlAsBytes(url));
    }

    private static byte[] readUrlAsBytes(URL url) {
        InputStream is = null;
        try {
            is = url.openStream();
            return readBytesFromInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
