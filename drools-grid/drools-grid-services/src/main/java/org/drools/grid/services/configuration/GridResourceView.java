package org.drools.grid.services.configuration;

import java.io.Serializable;


public abstract class GridResourceView implements Serializable{

	private String name;
	private GenericProvider provider;
	
	public GridResourceView() {	}
	
	public GridResourceView(String name, GenericProvider provider) {
		this.name = name;
		this.provider = provider;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setProvider(GenericProvider provider) {
		this.provider = provider;
	}

	public GenericProvider getProvider() {
		return provider;
	}
}
