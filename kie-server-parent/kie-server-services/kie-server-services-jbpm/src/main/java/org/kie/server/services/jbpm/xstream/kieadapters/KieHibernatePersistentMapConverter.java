package org.kie.server.services.jbpm.xstream.kieadapters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class KieHibernatePersistentMapConverter extends MapConverter {

    /**
     * Construct a HibernatePersistentMapConverter.
     * 
     * @param mapper
     * @since 1.4
     */
    public KieHibernatePersistentMapConverter(final Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(final Class type) {
        return type != null && (type == KieHibernate.PersistentMap || type == KieHibernate.EnversMap);
    }

    public Object unmarshal(final HierarchicalStreamReader reader,
        final UnmarshallingContext context) {
        throw new ConversionException("Cannot deserialize Hibernate collection");
    }
}