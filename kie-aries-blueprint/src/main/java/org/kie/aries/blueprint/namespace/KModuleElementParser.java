package org.kie.aries.blueprint.namespace;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.reflect.PassThroughMetadataImpl;
import org.drools.core.util.StringUtils;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;

@Deprecated
public class KModuleElementParser extends AbstractElementParser {

    private String mode;
    public static final String ATTRIBUTE_MODE = "mode";

    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);
        mode = element.getAttribute(ATTRIBUTE_MODE);

        if (StringUtils.isEmpty(mode)) {
            throw new ComponentDefinitionException("Mandatory attribute 'mode' missing. Cannot continue.");
        }
        if ("API".equals(mode)) {
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            PassThroughMetadataImpl passThroughMetadata = context.createMetadata(PassThroughMetadataImpl.class);
            passThroughMetadata.setObject(kContainer);
            passThroughMetadata.setId(id);
            return passThroughMetadata;
        }
        return null;
    }
}
