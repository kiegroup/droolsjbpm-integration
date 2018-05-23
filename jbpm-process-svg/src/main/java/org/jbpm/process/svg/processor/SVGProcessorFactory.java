package org.jbpm.process.svg.processor;

import java.util.Objects;

import org.w3c.dom.Document;

public class SVGProcessorFactory {

    public SVGProcessor create(Document svg, boolean mapById){

        String attribute = svg.getDocumentElement().getAttributeNS("http://www.w3.org/2000/xmlns/", "oryx");
        if(Objects.equals("http://oryx-editor.org", attribute)){
            return new JBPMDesignerSVGProcessor(svg, mapById);
        }

        return new StunnerSVGProcessor(svg);
    }
}
