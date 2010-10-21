package org.drools.grid.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class HazelCastMap<K, V> implements DistributedMap<K, V> {
    private Map<K, V> map;
    
    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey( key );
    }

    public boolean containsValue(Object value) {
        return map.containsValue( value );
    }
    public boolean equals(Object o) {
        return map.equals( o );
    }
    
    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }


    public int size() {
        return map.size();
    }


    public HazelCastMap(Map<K, V>  map) {
        this.map = map;
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    public V get(Object key) {
        return this.map.get( key );
    }

    public Set<K> keySet() {
        return this.map.keySet();
    }

    public V put(K key,
                 V value) {
        return this.map.put(  key, value );
    }

    public void putAll(Map< ? extends K, ? extends V> m) {
        this.map.putAll( m );
    }

    public V remove(Object key) {
        return this.map.remove(  key );
    }

    public Collection<V> values() {
        return this.map.values();
    }
}
