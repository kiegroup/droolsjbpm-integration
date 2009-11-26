package org.drools.pipeline.camel;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.io.Resource;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ToXmlNodeTransformer extends BaseEmitter
    implements
    Transformer, Processor {

    public void receive(Object object,
                        PipelineContext context) {
        Document d = processTranslate(object);

        emit( d,
              context );

    }

    public void process(Exchange exchange) throws Exception {
        Document d = processTranslate(exchange.getIn().getBody());
        exchange.getIn().setBody(d);
    }

    private Document processTranslate(Object object) {
        // Create a DOM builder and parse the fragment
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document d = null;
        try {
            d = factory.newDocumentBuilder().parse((InputSource) null);
        } catch (Exception e) {
        }
        try {
            if (object instanceof String) {
                d = factory.newDocumentBuilder().parse(new InputSource(new StringReader((String) object)));
            } else if (object instanceof Reader) {
                d = factory.newDocumentBuilder().parse(new InputSource((Reader) object));
            } else if (object instanceof InputStream) {
                d = factory.newDocumentBuilder().parse((InputStream) object);
            } else if (object instanceof Resource) {
                d = factory.newDocumentBuilder().parse(new InputSource(((Resource) object).getReader()));
            } else {
                throw new IllegalArgumentException("signal object must be instance of InputStream or Resource");
            }
        } catch (Exception e) {
            handleException(this, object, e);
        }
        return d;
    }

}
