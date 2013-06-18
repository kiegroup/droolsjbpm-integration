package org.drools.osgi.compiler;

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

    private final Bundle bundle;
    private final String bundleUrlPrefix;

    private Collection<String> fileNames;

    public OsgiKieModule(ReleaseId releaseId, KieModuleModel kModuleModel, Bundle bundle, String bundleUrlPrefix) {
        super(releaseId, kModuleModel);
        this.bundle = bundle;
        this.bundleUrlPrefix = bundleUrlPrefix;
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
        URL url = bundle.getEntry(pResourceName);
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
            String urlString = url.toString();
            if (urlString.endsWith("/")) {
                continue;
            }
            fileNames.add(urlString.substring(bundleUrlPrefix.length()));
        }
        return fileNames;
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    public static OsgiKieModule create(URL url) {
        KieModuleModel kieProject = KieModuleModelImpl.fromXML(url);
        String urlString = url.toString();
        String id = urlString.substring("bundle://".length(), urlString.indexOf('.'));
        long bundleId = Long.parseLong(id);

        Bundle bundle = FrameworkUtil.getBundle(OsgiKieModule.class).getBundleContext().getBundle(bundleId);
        String pomProperties = getPomProperties( bundle );
        ReleaseId releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
        return new OsgiKieModule(releaseId, kieProject, bundle, urlString.substring(0, urlString.indexOf("META-INF")));
    }

    private static String getPomProperties(Bundle bundle) {
        Enumeration<URL> e = bundle.findEntries("META-INF/maven", "pom.properties", true);
        if (!e.hasMoreElements()) {
            throw new RuntimeException("Cannot find pom.properties file in bundle " + bundle);
        }
        return readUrlAsString(e.nextElement());
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
                } catch (IOException e) { }
            }
        }
    }
}
