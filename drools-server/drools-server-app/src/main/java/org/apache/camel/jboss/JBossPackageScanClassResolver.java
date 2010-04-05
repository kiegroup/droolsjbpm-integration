package org.apache.camel.jboss;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import org.apache.camel.impl.DefaultPackageScanClassResolver;
import org.apache.camel.spi.PackageScanFilter;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.vfs.helpers.AbstractVirtualFileVisitor;

/**
 * JBoss specific package scan classloader to be used when Camel is running
 * inside JBoss Application Server.
 */
public class JBossPackageScanClassResolver extends DefaultPackageScanClassResolver {

    @Override
    protected void find(PackageScanFilter test, String packageName, ClassLoader loader, Set<Class<?>> classes) {
        if (log.isTraceEnabled()) {
            log.trace("Searching for: " + test + " in package: " + packageName
                    + " using classloader: " + loader.getClass().getName());
        }

        Enumeration<URL> urls;
        try {
            urls = getResources(loader, packageName);
            if (!urls.hasMoreElements()) {
                log.trace("No URLs returned by classloader");
            }
        }
        catch (IOException ioe) {
            log.warn("Could not read package: " + packageName, ioe);
            return;
        }

        while (urls.hasMoreElements()) {
            URL url = null;
            try {
                url = urls.nextElement();
                if (log.isTraceEnabled()) {
                    log.trace("URL from classloader: " + url);
                }
                VirtualFile root = VFS.getRoot(url);
                root.visit(new MatchingClassVisitor(test, classes));
            }
            catch (IOException ioe) {
                log.warn("Could not read entries in url: " + url, ioe);
            }
        }
    }

    private class MatchingClassVisitor extends AbstractVirtualFileVisitor {
        private PackageScanFilter filter;
        private Set<Class<?>> classes;

        private MatchingClassVisitor(PackageScanFilter filter, Set<Class<?>> classes) {
            super(VisitorAttributes.RECURSE_LEAVES_ONLY);
            this.filter = filter;
            this.classes = classes;
        }

        public void visit(VirtualFile file) {
            if (file.getName().endsWith(".class")) {
                String fqn = file.getPathName();
                String qn;
                if (fqn.indexOf("jar/") != -1) {
                    qn = fqn.substring(fqn.indexOf("jar/") + 4);
                } else {
                    qn = fqn.substring(fqn.indexOf("/") + 1);
                }

                addIfMatching(filter, qn, classes);
            }
        }
    }

}
