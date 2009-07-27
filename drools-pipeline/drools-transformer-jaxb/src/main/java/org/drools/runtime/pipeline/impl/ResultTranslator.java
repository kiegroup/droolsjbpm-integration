package org.drools.runtime.pipeline.impl;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.drools.command.Command;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.rule.InsertElementsCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.QueryCommand;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.result.ExecutionResults;
import org.drools.result.ExecutionResultsImpl;
import org.drools.result.GenericResult;
import org.drools.result.GetGlobalResult;
import org.drools.result.GetObjectsResult;
import org.drools.result.InsertElementsResult;
import org.drools.result.InsertObjectResult;
import org.drools.result.SetGlobalResult;
import org.drools.result.QueryResult;

import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.drools.process.result.ExecutionResultsType;
import org.drools.process.result.FactHandleListType;
import org.drools.process.result.FactObjectListType;
import org.drools.process.result.GlobalType;
import org.drools.process.result.InsertResultsType;
import org.drools.process.result.ObjectFactory;
import org.drools.process.result.QueryFieldType;
import org.drools.process.result.QueryResultsType;
import org.drools.process.result.QueryRowType;

public class ResultTranslator {
    private JaxbTransformer jaxbTransformer;
    private Map<Class<?>,ResultTransformer> class2trans;
    private ObjectFactory objFact;
    private ExecutionResultsType execRes;

    /**
     * Constructor.
     *  
     * @param jaxbTransformer the <tt>JaxbTransformer</tt>
     */
    public ResultTranslator( JaxbTransformer jaxbTransformer ){
        this.jaxbTransformer = jaxbTransformer;
        class2trans = new HashMap<Class<?>,ResultTransformer>();
        class2trans.put( InsertElementsResult.class, new InsertElementsResultTransformer() );
        class2trans.put( InsertObjectResult.class,   new InsertObjectResultTransformer() );
        class2trans.put( GetGlobalResult.class,      new GetGlobalResultTransformer() );
	/** @TODO: LATER
        class2trans.put( GetObjectsResult.class, new GetObjectsResultTransformer() );
        class2trans.put( SetGlobalResult.class,  new SetGlobalResultTransformer() );
        */
        class2trans.put( QueryResult.class,      new QueryResultTransformer() );

        objFact = new ObjectFactory();
    }

    ObjectFactory getObjFact(){
    	return objFact;
    }

    void addResult( Object result ){
    	execRes.getQueryResultsOrInsertResultsOrGlobalValue().add( result );
    }

    ResultTransformer getResultTransformer( Class<?> clazz ){
    	return class2trans.get( clazz );
    }


    Element makeElement( Object obj ) throws JAXBException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        JAXBContext ctxt = jaxbTransformer.getContext();
        Marshaller m = ctxt.createMarshaller();
        m.marshal( obj, doc );
        Element el = doc.getDocumentElement();
        
        Node xsi = doc.createAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns:xsi" );
        xsi.setTextContent( "http://www.w3.org/2001/XMLSchema-instance" );
        el.getAttributes().setNamedItem( xsi );
        
        Node type = doc.createAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "xsi:type" );
        type.setTextContent( obj.getClass().getName() );
        el.getAttributes().setNamedItem( type );
        
        return el;
    }
    
    public ExecutionResultsType transform( ExecutionResults results ){
        execRes = new ExecutionResultsType();
        for( GenericResult aResult: results.getResults() ){
            ResultTransformer resultTransformer = this.getResultTransformer( aResult.getClass() );
            if( resultTransformer != null ){
                resultTransformer.transform( this, aResult );
	    }
	}
        return execRes;
    }

}

/**
 * Abstract base class for all result transformers.
 */
abstract class ResultTransformer {
    abstract void transform( ResultTranslator rt,
                             GenericResult result );
}


class InsertElementsResultTransformer extends ResultTransformer {
    void transform( ResultTranslator rt, GenericResult result ){
        InsertElementsResult insertResult = (InsertElementsResult)result;
        String ident = insertResult.getIdentifier();

        ObjectFactory objFact = rt.getObjFact();
        InsertResultsType insResultXml = objFact.createInsertResultsType();
        FactHandleListType fhList = objFact.createFactHandleListType();
        insResultXml.setFactHandles( fhList );
        FactObjectListType foList = null;
        insResultXml.setIdentifier( ident );

        List<FactHandle> fhResList = insertResult.getHandles();
        List<Object>     obResList = insertResult.getObjects();

        if( obResList != null ){
            foList = objFact.createFactObjectListType();
            insResultXml.setFactObjects( foList );
        }
            
        try {
            for( int i = 0; i < fhResList.size(); i++ ){
                fhList.getFactHandle().add( fhResList.get( i ).toExternalForm() );
                if( obResList != null ){
                    Object factObj = obResList.get( i );
                    foList.getAny().add( rt.makeElement( factObj ) );
                }
            }
            rt.addResult( insResultXml );
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class InsertObjectResultTransformer extends ResultTransformer {
    void transform( ResultTranslator rt, GenericResult result ){
        InsertObjectResult insertResult = (InsertObjectResult)result;
        String ident = insertResult.getIdentifier();

        ObjectFactory objFact = rt.getObjFact();
        InsertResultsType insResultXml = objFact.createInsertResultsType();
        FactHandleListType fhList = objFact.createFactHandleListType();
        insResultXml.setFactHandles( fhList );
        FactObjectListType foList = null;
        insResultXml.setIdentifier( ident );

        FactHandle handle = (FactHandle)insertResult.getFactHandle();
        Object object = insertResult.getValue();

        if( object != null ){
            foList = objFact.createFactObjectListType();
            insResultXml.setFactObjects( foList );
        }
            
        try {
            fhList.getFactHandle().add( handle.toExternalForm() );

            if( object != null ){
                foList.getAny().add( rt.makeElement( object ) );
            }

            rt.addResult( insResultXml );
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}


class GetGlobalResultTransformer extends ResultTransformer {
    void transform( ResultTranslator rt, GenericResult result ){

        GetGlobalResult getGlobalResult = (GetGlobalResult)result;
        String identifier = getGlobalResult.getIdentifier();
        try {
            Object globalObject = getGlobalResult.getValue();
            Element el = rt.makeElement( globalObject );
            ObjectFactory objFact = rt.getObjFact();
            GlobalType getGlobalXml = objFact.createGlobalType();
            getGlobalXml.setIdentifier( identifier );
            getGlobalXml.setAny( el );
            rt.addResult( getGlobalXml );
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class QueryResultTransformer extends ResultTransformer {
    void transform( ResultTranslator rt, GenericResult result ){
        QueryResult queryResult = (QueryResult)result;

        String identifier = queryResult.getIdentifier();
       	QueryResults qr = queryResult.getResults();
        String[] idents = qr.getIdentifiers();
        int size = qr.size();

        ObjectFactory objFact = rt.getObjFact();
        QueryResultsType queryResXml = objFact.createQueryResultsType();
        queryResXml.setIdentifier( identifier );
        queryResXml.setSize( size );

        String[] quids = qr.getIdentifiers();
        for( QueryResultsRow row: qr ){
            QueryRowType queryRowXml = objFact.createQueryRowType();
            queryResXml.getRow().add( queryRowXml );
            for( String quid: quids  ){
                QueryFieldType queryFieldXml = objFact.createQueryFieldType();
                queryRowXml.getField().add( queryFieldXml );
                queryFieldXml.setName( quid );
                
                try {
                    Element el = rt.makeElement( row.get( quid ) );
                    queryFieldXml.setAny( el );
                } catch( Throwable t ){
                    queryFieldXml.setValue( row.get( quid ).toString() );
                }
            }
        }
        rt.addResult( queryResXml );
    }
}


