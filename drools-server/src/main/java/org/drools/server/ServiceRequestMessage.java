package org.drools.server;


public class ServiceRequestMessage {
	public QueryType[] queries;
	public NamedFact[] globals;
	public NamedFact[] inOutFacts;
	public AnonFact[] inFacts;
}
