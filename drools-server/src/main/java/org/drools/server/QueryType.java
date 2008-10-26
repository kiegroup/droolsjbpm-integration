package org.drools.server;

public class QueryType {
	
	public String queryName;
	public String[] factNames;
	public Object[] args;

	public QueryType(String queryName, String[] factNames, Object[] args) {
		super();
		this.queryName = queryName;
		this.factNames = factNames;
		this.args = args;
	}

}
