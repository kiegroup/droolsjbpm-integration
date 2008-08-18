package org.drools.server;


public class NamedFact {



	public NamedFact(String id, Object fact) {
		this.id = id;
		this.fact = fact;
	}

	public NamedFact() {}
	public String id;
	public Object fact;

}
