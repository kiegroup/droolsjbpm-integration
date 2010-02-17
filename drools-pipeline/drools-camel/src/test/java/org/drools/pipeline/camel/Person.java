/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.pipeline.camel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author salaboy
 */
@XmlRootElement
public class Person {
	private String name;
	private Integer age;

	public Person() {
	}

	public Person(String name) {
		super();
		this.name = name;
	}

	public Person(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Integer getAge() {
		return age;
	}

	@Override
	public String toString() {
		return "Person [age=" + age + ", name=" + name + "]";
	}
	
}
