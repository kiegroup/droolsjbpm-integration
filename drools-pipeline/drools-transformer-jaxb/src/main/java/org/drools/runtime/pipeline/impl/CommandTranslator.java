package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.drools.command.Setter;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.process.SignalEventCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertElementsCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.ModifyCommand;
import org.drools.command.runtime.rule.QueryCommand;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author Wolfgang Laun
 */
public class CommandTranslator {
	private Map<Class<?>, CommandTransformer> class2trans;

	public CommandTranslator(){
		class2trans = new HashMap<Class<?>, CommandTransformer>();
		class2trans.put(BatchExecutionCommand.class,   new BatchExecutionTransformer());
		class2trans.put(InsertElementsCommand.class,   new InsertElementsTransformer());
		class2trans.put(InsertObjectCommand.class,     new InsertObjectTransformer());
		class2trans.put(ModifyCommand.class,     	   new ModifyObjectTransformer());
		class2trans.put(QueryCommand.class,            new QueryTransformer());
		class2trans.put(SetGlobalCommand.class,        new SetGlobalTransformer());
		class2trans.put(SignalEventCommand.class,      new SignalEventTransformer());
		class2trans.put(StartProcessCommand.class,     new StartProcessTransformer());
		class2trans.put(FireAllRulesCommand.class,     new FireAllRulesTransformer());
	}

	/**
	 * Transforms a list of XML elements representing Drools command objects
	 * to a list of <tt>Command&lt;?&gt;</tt> objects. Application objects
	 * are unmarshalled, using an <tt>Unmarshaller</tt> object derived from
	 * the object's class.
	 * 
	 * @param xmlCmds the list of XML elements representing Drools command objects
	 * @return 
	 * @return a list of <tt>Command&lt;?&gt;</tt> objects
	 */
	public GenericCommand<?> transform(BatchExecutionCommand batchExecution, Unmarshaller unmarshaller) {
		CommandTransformer commandTransformer = class2trans.get(batchExecution.getClass());
		return commandTransformer.transform(this, batchExecution, unmarshaller);
	}

	protected CommandTransformer getCommandTransformer(Class<?> clazz) {
		return class2trans.get(clazz);
	}

	protected Object makeObject(Node node, Unmarshaller unmarshaller) {        
		Object obj = null;
		try {
			obj = unmarshaller.unmarshal(node);
		} catch (JAXBException e) {
			// TODO: remove this
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return obj;
	}
}


/**
 * Abstract base class for all command transformers.
 */
abstract class CommandTransformer {
	abstract GenericCommand<?> transform(CommandTranslator ct, Object o , Unmarshaller unmarshaller);
}

/**
 * Class for transforming a BatchExecution command.
 */
class BatchExecutionTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator cmdTrans, Object o , Unmarshaller unmarshaller) {
		BatchExecutionCommand be = (BatchExecutionCommand)o;
		List<GenericCommand<?>> xmlCmds = be.getCommands();
		for (ListIterator<GenericCommand<?>> i = xmlCmds.listIterator(); i.hasNext();) {
			GenericCommand<?> cmd = i.next();
			CommandTransformer ct = cmdTrans.getCommandTransformer(cmd.getClass());
			if (ct != null) {
				i.set(ct.transform(cmdTrans, cmd, unmarshaller));
			}
		}
		return be;
	}
}

/**
 * Class for transforming an InsertElements command.
 */
class InsertElementsTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		InsertElementsCommand insertElementsCmd = (InsertElementsCommand)o;
		List<Object> objectsList = new ArrayList<Object>(insertElementsCmd.getObjects().size());
		
		for (Object obj : insertElementsCmd.getObjects()) {
			if (obj instanceof Node) {
				objectsList.add(ct.makeObject((Node) obj, unmarshaller));
			} else {
				objectsList.add(obj);
			}
		}

		insertElementsCmd.setObjects(objectsList);
		return insertElementsCmd;
	}
}

/**
 * Class for transforming a InsertObject command.
 */
class InsertObjectTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		InsertObjectCommand io = (InsertObjectCommand)o;
		Object object = io.getObject();
		if (object instanceof Element) {
			Element el = (Element)object;
			Object obj = ct.makeObject(el , unmarshaller);
			io.setObject(obj);
		}
		return io;
	}
}

/**
 * Class for transforming a ModifyObject command.
 */
class ModifyObjectTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		ModifyCommand mo = (ModifyCommand)o;
		List<?> setters = mo.getSetters();
		List<Setter> convertedSetters = new ArrayList<Setter>();
		for (Object node : setters) {
			Setter setter;
			if (node instanceof Element) {
				setter = (Setter) ct.makeObject((Element)node, unmarshaller);
			}
			else {
				setter = (Setter) node;
			}
			convertedSetters.add(setter);
		}
		mo.setSetters(convertedSetters);
		return mo;
	}
}

/**
 * Class for transforming a FireAllRules command.
 */
class FireAllRulesTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		return (FireAllRulesCommand)o;
	}
}

/**
 * Class for transforming a Query command.
 */
class QueryTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		QueryCommand q = (QueryCommand)o;
		for (ListIterator<Object> i = q.getArguments().listIterator(); i.hasNext();) {
			Object object = i.next();
			if (object instanceof Element) {
				i.set(ct.makeObject((Element) object, unmarshaller));
			}
		}
		return q;
	}
}

/**
 * Class for transforming a SetGlobal command.
 */
class SetGlobalTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		SetGlobalCommand sg = (SetGlobalCommand)o;
		Object object = sg.getObject();
		if (object instanceof Element) {
			Element el = (Element)object;
			Object obj = ct.makeObject(el , unmarshaller);
			sg.setObject(obj);
		}
		return sg;
	}
}

/**
 * Class for transforming a SignalEvent command.
 */
class SignalEventTransformer extends CommandTransformer {
	public GenericCommand<?> transform(CommandTranslator ct, Object o, Unmarshaller unmarshaller) {
		SignalEventCommand se = (SignalEventCommand)o;
		Object ev = se.getEvent();
		if (ev != null) {
			Object obj = ct.makeObject((Element)ev , unmarshaller);
			se.setEvent(obj);
		}
		return se;
	}
}

/**
 * Class for transforming a StartProcess command.
 */
class StartProcessTransformer extends CommandTransformer {
	public GenericCommand<?> transform( CommandTranslator ct, Object o, Unmarshaller unmarshaller ) {
		StartProcessCommand sp = (StartProcessCommand)o;
		for (Map.Entry<String, Object> entry: sp.getParameters().entrySet()) {
			Object obj = entry.getValue();
			if (obj instanceof Element) {
				entry.setValue(ct.makeObject((Element) obj, unmarshaller));
			}
		}
		return sp;
	}
}
