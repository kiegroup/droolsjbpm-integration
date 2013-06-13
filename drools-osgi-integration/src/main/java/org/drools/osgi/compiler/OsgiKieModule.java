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
import java.util.Collection;
import java.util.Enumeration;

import static org.drools.core.util.IoUtils.readBytesFromInputStream;

public class OsgiKieModule extends AbstractKieModule {

    private final Bundle bundle;

    public OsgiKieModule(ReleaseId releaseId, KieModuleModel kModuleModel, Bundle bundle) {
        super(releaseId, kModuleModel);
        this.bundle = bundle;
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("org.drools.osgi.compiler.OsgiKieModule.getBytes -> TODO");

    }

    @Override
    public boolean isAvailable(String pResourceName) {
        throw new UnsupportedOperationException("org.drools.osgi.compiler.OsgiKieModule.isAvailable -> TODO");

    }

    @Override
    public byte[] getBytes(String pResourceName) {
        throw new UnsupportedOperationException("org.drools.osgi.compiler.OsgiKieModule.getBytes -> TODO");

    }

    @Override
    public Collection<String> getFileNames() {
        throw new UnsupportedOperationException("org.drools.osgi.compiler.OsgiKieModule.getFileNames -> TODO");

    }

    @Override
    public File getFile() {
        return null;
    }

    public static OsgiKieModule create(URL url) {
        KieModuleModel kieProject = KieModuleModelImpl.fromXML(url);
        long bundleId = getBundleIdFromUrl(url);
        Bundle bundle = FrameworkUtil.getBundle(OsgiKieModule.class).getBundleContext().getBundle(bundleId);
        String pomProperties = getPomProperties( bundle );
        ReleaseId releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
        return new OsgiKieModule(releaseId, kieProject, bundle);
    }

    private static long getBundleIdFromUrl(URL url) {
        String urlString = url.toString();
        String id = urlString.substring("bundle://".length(), urlString.indexOf('.'));
        return Long.parseLong(id);
    }

    private static String getPomProperties(Bundle bundle) {
        Enumeration<URL> e = bundle.findEntries("META-INF/maven", "pom.properties", true);
        if (!e.hasMoreElements()) {
            throw new RuntimeException("Cannot find pom.properties file in bundle " + bundle);
        }
        return readUrlAsString(e.nextElement());
    }

    private static String readUrlAsString(URL url) {
        InputStream is = null;
        try {
            is = url.openStream();
            return new String(readBytesFromInputStream(is));
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
