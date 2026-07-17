package org.kie.server.services.jbpm.xstream.kieadapters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class KieHibernatePersistentCollectionConverter extends CollectionConverter {

    /**
     * Construct a HibernatePersistentCollectionConverter.
     * 
     * @param mapper
     *
     */
    public KieHibernatePersistentCollectionConverter(final Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(final Class type) {
        return type != null
            && (
                   type == KieHibernate.PersistentBag
                || type == KieHibernate.PersistentList
                || type == KieHibernate.PersistentSet
            );
    }

    public Object unmarshal(final HierarchicalStreamReader reader,
        final UnmarshallingContext context) {
        throw new ConversionException("Cannot deserialize Hibernate collection");
    }
}