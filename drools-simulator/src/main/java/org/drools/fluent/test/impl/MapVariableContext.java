package org.drools.fluent.test.impl;

import java.util.HashMap;
import java.util.Map;

import org.drools.fluent.VariableContext;

public class MapVariableContext<P> implements VariableContext<P>{
	
	private Map<String, P> vars;
	
	public MapVariableContext() {
		this.vars = new HashMap<String, P>();
	}

	public P get(String name) {
		return vars.get( name );
	}

	public <T> T get(String name, Class<T> type) {
		return (T) vars.get( name );
	}

}
