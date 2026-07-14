package org.kie.server.services.jbpm.xstream.kieadapters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.TreeMapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class KieHibernatePersistentSortedMapConverter extends TreeMapConverter {

    /**
     * Construct a HibernatePersistentSortedMapConverter.
     * 
     * @param mapper
     * @since 1.4
     */
    public KieHibernatePersistentSortedMapConverter(final Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(final Class type) {
        return type != null && (type == KieHibernate.PersistentSortedMap /*|| type == KieHibernate.EnversSortedMap*/);
    }

    public Object unmarshal(final HierarchicalStreamReader reader,
        final UnmarshallingContext context) {
        throw new ConversionException("Cannot deserialize Hibernate collection");
    }
}