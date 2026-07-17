package org.kie.server.services.jbpm.xstream.kieadapters;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class KieHibernateMapper extends MapperWrapper {

    final private Map collectionMap = new HashMap();

    public KieHibernateMapper(final Mapper mapper) {
        super(mapper);
        collectionMap.put(KieHibernate.PersistentBag, ArrayList.class);
        collectionMap.put(KieHibernate.PersistentList, ArrayList.class);
        collectionMap.put(KieHibernate.PersistentMap, HashMap.class);
        collectionMap.put(KieHibernate.PersistentSet, HashSet.class);
        collectionMap.put(KieHibernate.PersistentSortedMap, TreeMap.class);
        collectionMap.put(KieHibernate.PersistentSortedSet, TreeSet.class);
        collectionMap.put(KieHibernate.EnversMap, TreeSet.class);
        collectionMap.remove(null);
    }

    public Class defaultImplementationOf(final Class clazz) {
        if (collectionMap.containsKey(clazz)) {
            return super.defaultImplementationOf((Class)collectionMap.get(clazz));
        }

        return super.defaultImplementationOf(clazz);
    }

    public String serializedClass(final Class clazz) {
        if (clazz != null) {
            if (collectionMap.containsKey(clazz)) {
                // Pretend this is the underlying collection class and map that instead
                return super.serializedClass((Class)collectionMap.get(clazz));
            }
            // check whether we are Hibernate proxy and substitute real name
            if (HibernateProxy.class.isAssignableFrom(clazz)) {
                return super.serializedClass(clazz.getSuperclass());
            }
        }
        return super.serializedClass(clazz);
    }
}