/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import org.drools.runtime.pipeline.SmooksTransformerProvider;
import org.drools.runtime.pipeline.Transformer;
import org.milyn.Smooks;

public class SmooksTransformerProviderImpl implements SmooksTransformerProvider {
    public Transformer newSmooksFromSourceTransformer(Smooks smooks,
                                                      String rootId) {
        DroolsSmooksConfiguration conf = new DroolsSmooksConfiguration( rootId );
        return new SmooksFromSourceTransformer( smooks,
                                      conf );
    }
    
    public Transformer newSmooksToSourceTransformer(Smooks smooks) {
        return new SmooksToSourceTransformer( smooks );
    }    
}