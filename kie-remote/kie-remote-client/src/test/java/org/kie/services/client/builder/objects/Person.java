package org.kie.services.client.builder.objects;

import java.io.Serializable;

public class Person implements Serializable {
	
	/**
	 * Default ID.
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private int age;
	
	public Person() { 
	    // default constructor required
	}
	
	public Person(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}
