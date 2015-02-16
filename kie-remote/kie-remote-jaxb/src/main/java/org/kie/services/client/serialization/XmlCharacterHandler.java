package org.kie.services.client.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public class XmlCharacterHandler implements CharacterEscapeHandler {

    private static final XmlCharacterHandler _instance = new XmlCharacterHandler();
    
    private XmlCharacterHandler() {
        // Single instance class, since the one methd it implements does not modify any state
    }
    
    public static XmlCharacterHandler getInstance() { 
        return _instance;
    }
    
    @Override
    public void escape( char[] ch, int start, int length, boolean isAttVal, Writer out ) throws IOException {
        int limit = start + length;
        for( int i = start; i < limit; i++ ) {
            char c = ch[i];
            if( c == '&' || c == '<' || c == '>' || (c == '\"' && isAttVal) ) {
                if( i != start ) {
                    out.write(ch, start, i - start);
                }
                start = i + 1;
                switch ( ch[i] ) {
                case '&':
                    out.write("&amp;");
                    break;
                case '<':
                    out.write("&lt;");
                    break;
                case '>':
                    out.write("&gt;");
                    break;
                case '\"':
                    out.write("&quot;");
                    break;
                case '\'':
                    out.write("&apos;");
                    break;
                }
            }
        }

        if( start != limit ) {
            out.write(ch, start, limit - start);
        }
    }
}
