package org.kie.integration.eap.maven.template.assembly;

import java.util.Collection;

public interface EAPAssemblyTemplate {

    /**
     * The assembly id.
     * @return The assembly id.
     */
    String getId();

    /**
     * The assembly formats.
     * @return The assembly formats.
     */
    String[] getFormats();

    /**
     * The dependency set inclusions.
     * @return The dependency set inclusions.
     */
    Collection<String> getInclusions();

    /**
     * The dependency set exclusions.
     * @return The dependency set exclusions.
     */
    Collection<String> getExclusions();

    /**
     * The fileset files.
     * @return The fileset files.
     */
    Collection<EAPAssemblyTemplateFile> getFiles();
}
