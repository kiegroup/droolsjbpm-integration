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

package org.drools.grid.generic;

import java.util.Collection;
import java.util.Iterator;

public class CollectionClient<T>
    implements
    Collection {
    private String parentInstanceId;

    public CollectionClient(String parentInstanceId) {
        this.parentInstanceId = parentInstanceId;
    }

    public String getParentInstanceId() {
        return this.parentInstanceId;
    }

    public boolean add(Object e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addAll(Collection c) {
        // TODO Auto-generated method stub
        return false;
    }

    public void clear() {
        // TODO Auto-generated method stub

    }

    public boolean contains(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsAll(Collection c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    public Iterator iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeAll(Collection c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean retainAll(Collection c) {
        // TODO Auto-generated method stub
        return false;
    }

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object[] toArray(Object[] a) {
        // TODO Auto-generated method stub
        return null;
    }

}
