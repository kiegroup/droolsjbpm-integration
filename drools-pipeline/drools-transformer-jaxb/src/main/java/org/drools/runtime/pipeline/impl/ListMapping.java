package org.drools.runtime.pipeline.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class ListMapping {

	public static void main(String[] args) throws Exception {
		Object asd = new Person("lucaz");
		Field fieldName = asd.getClass().getDeclaredField("name");
		fieldName.setAccessible(true);
		String fieldValue = (String) fieldName.get(asd);
		System.out.println("fieldValue = " + fieldValue);

//		Object object = fieldName.get(asd);
//		System.out.println(object);

//		Course course = new Course();
//		
//		course.addPerson(new Person("lucas"));
//		course.addPerson(new Person("jose"));
//		
//		Class<?>[] classes = {Person.class, Course.class};
//		
//		JAXBContext jaxb = JAXBContext.newInstance(classes);
//
//		Marshaller xmlConverter = jaxb.createMarshaller();
//		xmlConverter.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//		xmlConverter.marshal(course, System.out);
		
	}

}

@XmlRootElement

class Course {
	
//	@XmlElementWrapper(name="persons")
	@XmlElement(name="person")
	private List<Person> persons = new ArrayList<Person>();
	
	public Course() {
	}
	
	public void addPerson(Person person) {
		persons.add(person);
	}
	
	public List<Person> getPersons() {
		return this.persons;
	}
	
}

@XmlRootElement(name="person")
class Person {

	@XmlAttribute
	private String name;
	
	public Person() {
	}
	
	public Person(String name) {
		this.setName(name);
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
