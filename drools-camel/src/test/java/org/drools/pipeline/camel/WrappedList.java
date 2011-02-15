/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package org.drools.pipeline.camel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
public class WrappedList {
    //		@XmlElementWrapper(name="list")
    @XmlElements({@XmlElement(name = "org.drools.pipeline.camel.Person", type = Person.class)})
    private List<Person> people = new ArrayList<Person>();

    public void add(int index,
                    Person element) {
        people.add( index,
                    element );
    }

    public boolean add(Person e) {
        return people.add( e );
    }

    public boolean addAll(Collection< ? extends Person> c) {
        return people.addAll( c );
    }

    public boolean addAll(int index,
                          Collection< ? extends Person> c) {
        return people.addAll( index,
                              c );
    }

    public void clear() {
        people.clear();
    }

    public boolean contains(Object o) {
        return people.contains( o );
    }

    public boolean containsAll(Collection< ? > c) {
        return people.containsAll( c );
    }

    public boolean equals(Object o) {
        return people.equals( o );
    }

    public Person get(int index) {
        return people.get( index );
    }

    public int hashCode() {
        return people.hashCode();
    }

    public int indexOf(Object o) {
        return people.indexOf( o );
    }

    public boolean isEmpty() {
        return people.isEmpty();
    }

    public Iterator<Person> iterator() {
        return people.iterator();
    }

    public int lastIndexOf(Object o) {
        return people.lastIndexOf( o );
    }

    public ListIterator<Person> listIterator() {
        return people.listIterator();
    }

    public ListIterator<Person> listIterator(int index) {
        return people.listIterator( index );
    }

    public Person remove(int index) {
        return people.remove( index );
    }

    public boolean remove(Object o) {
        return people.remove( o );
    }

    public boolean removeAll(Collection< ? > c) {
        return people.removeAll( c );
    }

    public boolean retainAll(Collection< ? > c) {
        return people.retainAll( c );
    }

    public Person set(int index,
                      Person element) {
        return people.set( index,
                           element );
    }

    public int size() {
        return people.size();
    }

    public List<Person> subList(int fromIndex,
                                int toIndex) {
        return people.subList( fromIndex,
                               toIndex );
    }

    public Object[] toArray() {
        return people.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return people.toArray( a );
    }

}