package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.drools.command.impl.GenericCommand;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.rule.InsertElementsCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.QueryCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.process.SignalEventCommand;
import org.drools.command.runtime.process.StartProcessCommand;

/**
 * 
 * @author Wolfgang Laun
 */
public class CommandTranslator {
    private JaxbTransformer jaxbTransformer;
    private Map<Class<?>,CommandTransformer> class2trans;
    
    /**
     * Constructor
     *  
     */
    public CommandTranslator( JaxbTransformer jaxbTransformer ){
        class2trans = new HashMap<Class<?>,CommandTransformer>();
        class2trans.put( BatchExecutionCommand.class,   new BatchExecutionTransformer() );
        class2trans.put( InsertElementsCommand.class,   new InsertElementsTransformer() );
        class2trans.put( InsertObjectCommand.class,     new InsertObjectTransformer() );
        class2trans.put( QueryCommand.class,            new QueryTransformer() );
        class2trans.put( SetGlobalCommand.class,        new SetGlobalTransformer() );
        class2trans.put( SignalEventCommand.class,      new SignalEventTransformer() );
        class2trans.put( StartProcessCommand.class,     new StartProcessTransformer() );
        this.jaxbTransformer = jaxbTransformer;
    }
    
    /**
     * Transforms a list of XML elements representing Drools command objects
     * to a list of <tt>Command&lt;?&gt;</tt> objects. Application objects
     * are unmarshalled, using an <tt>Unmarshaller</tt> object derived from
     * the object's class.
     * 
     * @param xmlCmds the list of XML elements representing Drools command objects
     * @return a list of <tt>Command&lt;?&gt;</tt> objects
     */
    public void transform( BatchExecutionCommand batchExecution ){
        CommandTransformer ct = class2trans.get( batchExecution.getClass() );
        ct.transform( this, batchExecution );
    }

    CommandTransformer getCommandTransformer( Class clazz ){
        return class2trans.get( clazz );
    }
        
    protected Object makeObject( Element element ){        
        NamedNodeMap nnmap = element.getAttributes();
        Node typeNode = nnmap.getNamedItem( "xsi:type" );
        String className = typeNode.getNodeValue();
        Object obj = null;
        try {
            Class<?> clazz = Class.forName( className );
            JAXBContext ctxt = jaxbTransformer.getContext();
            Unmarshaller um = ctxt.createUnmarshaller();
            obj = um.unmarshal( element );
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return obj;
    }
}


/**
 * Abstract base class for all command transformers.
 */
abstract class CommandTransformer {
    abstract GenericCommand<?> transform( CommandTranslator ct, Object o );
}

/**
 * Class for transforming a BatchExecution command.
 */
class BatchExecutionTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator cmdTrans, Object o ){
        BatchExecutionCommand be = (BatchExecutionCommand)o;
        List<GenericCommand<?>> xmlCmds = be.getCommands();
        for( int i = 0; i < xmlCmds.size(); i++ ){
            GenericCommand<?> cmd = xmlCmds.get( i );
            CommandTransformer ct = cmdTrans.getCommandTransformer( cmd.getClass() );
            if( ct != null ){
                xmlCmds.set( i, ct.transform( cmdTrans, cmd ) );
            }
        }
        return be;
    }
}




/**
 * Class for transforming an InsertElements command.
 */
class InsertElementsTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator ct, Object o ){
        InsertElementsCommand ie = (InsertElementsCommand)o;
        Iterable<?> ioList = ie.getObjects();        
        List<Object> coList = new ArrayList<Object>();
        for( Object io: ioList ){
            System.out.println( io.getClass() );
            Element el = (Element)io;
            Object co = ct.makeObject( el );
            coList.add( co );
        }
        System.out.println( "insert " + coList.size() + " elements" );
        ie.setObjects( coList );
        return ie;
    }
}

/**
 * Class for transforming a InsertObject command.
 */
class InsertObjectTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator ct, Object o ){
        InsertObjectCommand io = (InsertObjectCommand)o;
        Element el = (Element)io.getObject();
        Object obj = ct.makeObject( el );
        io.setObject( obj );
        return io;
    }
}

/**
 * Class for transforming a Query command.
 */
class QueryTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator ct, Object o ){
        QueryCommand q = (QueryCommand)o;
        List<Object> argList = q.getArguments();
        for( int i = 0; i < argList.size(); i++ ){
            Element el = (Element)argList.get( i );
            Object ao = ct.makeObject( el );
            argList.add( i, ao );
        }
        return q;
    }
}

/**
 * Class for transforming a SetGlobal command.
 */
class SetGlobalTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator ct, Object o ){
        SetGlobalCommand sg = (SetGlobalCommand)o;
        Element el = (Element)sg.getObject();
        Object obj = ct.makeObject( el );
        sg.setObject( obj );
        return sg;
    }
}

/**
 * Class for transforming a SignalEvent command.
 */
class SignalEventTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator ct, Object o ){
        SignalEventCommand se = (SignalEventCommand)o;
        Object ev = se.getEvent();
        if( ev != null ){
            Object obj = ct.makeObject( (Element)ev );
            se.setEvent( obj );
        }
        return se;
    }
}

/**
 * Class for transforming a StartProcess command.
 */
class StartProcessTransformer extends CommandTransformer {
    public GenericCommand<?> transform( CommandTranslator ct, Object o ){
        StartProcessCommand sp = (StartProcessCommand)o;
        // data items
        List<Object> diList = sp.getData();
        for( int i = 0; i < diList.size(); i++ ){
            Element el = (Element)diList.get( i );
            Object obj = ct.makeObject( el );
            diList.add( i, obj );
        }
        // parameters
        Map<String,Object> parMap = sp.getParameters();;
        for( Map.Entry<String, Object> entry: parMap.entrySet() ){
            Element el = (Element)entry.getValue();
            Object obj = ct.makeObject( el );
            entry.setValue( obj );
        }
        return sp;
    }
}
