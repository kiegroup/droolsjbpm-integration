package org.kie.integration.eap.maven.template.assembly;

/**
 * Represents an assembly file tag.
 */
public interface EAPAssemblyTemplateFile {
    String getSource();
    String getOutputDirectory();
    String getFinalName();
    boolean isFiltered();
}