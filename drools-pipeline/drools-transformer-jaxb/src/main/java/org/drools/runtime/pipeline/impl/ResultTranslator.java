package org.drools.runtime.pipeline.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;

import org.drools.FactHandle;
import org.drools.base.ClassObjectType;
import org.drools.base.DroolsQuery;
import org.drools.common.DisconnectedFactHandle;
import org.drools.rule.Declaration;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.runtime.rule.QueryResultsRow;
import org.drools.runtime.rule.impl.FlatQueryResults;
import org.drools.runtime.rule.impl.NativeQueryResults;
import org.drools.spi.ObjectType;
import org.drools.xml.jaxb.util.JaxbListWrapper;

public class ResultTranslator {

	private Object convert(Object obj) {
		if (obj instanceof FactHandle && !(obj instanceof DisconnectedFactHandle)) {
			return new DisconnectedFactHandle(((FactHandle) obj).toExternalForm());
		}
		
		if (obj instanceof NativeQueryResults) {
			NativeQueryResults nativeQueryResults = (NativeQueryResults) obj;
            List<Declaration> declrs = new ArrayList<Declaration>();
            HashMap<String, Integer> identifiers = new HashMap<String, Integer>(  );
            
            for ( String identifier : nativeQueryResults.getIdentifiers() ) {
            	// we don't want to marshall the query parameters
            	Declaration declr = nativeQueryResults.getDeclarations().get( identifier );
            	ObjectType objectType = declr.getPattern().getObjectType();
            	if ( objectType instanceof ClassObjectType &&
            		((ClassObjectType) objectType).getClassType() == DroolsQuery.class ) {
            			continue;
            	}
            	declrs.add(declr);
            	identifiers.put(identifier, declrs.size() - 1);
            }
            
            ArrayList<ArrayList<Object>> results = new ArrayList<ArrayList<Object>>( nativeQueryResults.size() );
            ArrayList<ArrayList<org.drools.runtime.rule.FactHandle>> factHandles = new ArrayList<ArrayList<org.drools.runtime.rule.FactHandle>> ( nativeQueryResults.size() );
            for (QueryResultsRow row : nativeQueryResults) {
            	ArrayList<Object> objectList = new ArrayList<Object>();
                ArrayList<org.drools.runtime.rule.FactHandle> factHandleList = new ArrayList<org.drools.runtime.rule.FactHandle>();
                for (int i = 0; i < declrs.size(); i++) {
                	objectList.add(convert(row.get(declrs.get(i).getIdentifier())));
                	factHandleList.add(new DisconnectedFactHandle(row.getFactHandle(declrs.get(i).getIdentifier()).toExternalForm()));
                }                
            	factHandles.add(factHandleList);
                results.add(objectList);
            }

            return new FlatQueryResults(identifiers, results, factHandles);
		}
		
		Class<? extends Object> vClass = obj.getClass();
		if (List.class.isAssignableFrom(vClass) && !JaxbListWrapper.class.equals(vClass)) {
			JaxbListWrapper<Object> wrapper = new JaxbListWrapper<Object>(((List<?>) obj).size());
			for (Object item : ((List<?>) obj)) {
				wrapper.add(convert(item));
			}
			return wrapper;
		}
		return obj;
	}
	
	public String transform( ExecutionResultImpl executionResult, Marshaller marshaller ) throws JAXBException, TransformerException{

		//TODO {bauna} remove this try
		try {
			StringWriter writer = new StringWriter();
			for (Map.Entry<String, Object> entry : executionResult.getFactHandles().entrySet()) {
				entry.setValue(convert(entry.getValue()));
			}
			
			for (Map.Entry<String, Object> entry : executionResult.getResults().entrySet()) {
				entry.setValue(convert(entry.getValue()));
			}
			marshaller.marshal(executionResult, writer);
			return writer.toString();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
