package org.kie.server.services.jbpm.xstream.kieadapters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.TreeSetConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class KieHibernatePersistentSortedSetConverter extends TreeSetConverter {

    /**
     * Construct a HibernatePersistentSortedSetConverter.
     *
     * @param mapper
     * @since 1.4
     */
    public KieHibernatePersistentSortedSetConverter(final Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(final Class type) {
        return type != null && (type == KieHibernate.PersistentSortedSet/* || type == KieHibernate.EnversSortedSet*/);
    }

    public Object unmarshal(final HierarchicalStreamReader reader,
                            final UnmarshallingContext context) {
        throw new ConversionException("Cannot deserialize Hibernate collection");
    }
}