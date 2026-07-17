/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.jbpm.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;
import org.kie.server.api.marshalling.xstream.XStreamMarshallerExtension;
import org.kie.server.services.jbpm.xstream.kieadapters.KieHibernateMapper;
import org.kie.server.services.jbpm.xstream.kieadapters.KieHibernatePersistentCollectionConverter;
import org.kie.server.services.jbpm.xstream.kieadapters.KieHibernatePersistentMapConverter;
import org.kie.server.services.jbpm.xstream.kieadapters.KieHibernatePersistentSortedMapConverter;
import org.kie.server.services.jbpm.xstream.kieadapters.KieHibernatePersistentSortedSetConverter;

public class HibernateXStreamMarshallerExtension implements XStreamMarshallerExtension {

    @Override
    public void extend(XStreamMarshaller marshaller) {
        XStream xstream = marshaller.getXstream();
        xstream.registerConverter(new HibernateProxyConverter());
        xstream.registerConverter(new KieHibernatePersistentCollectionConverter(xstream.getMapper()));
        xstream.registerConverter(new KieHibernatePersistentMapConverter(xstream.getMapper()));
        xstream.registerConverter(new KieHibernatePersistentSortedMapConverter(xstream.getMapper()));
        xstream.registerConverter(new KieHibernatePersistentSortedSetConverter(xstream.getMapper()));
    }

    @Override
    public MapperWrapper chainMapperWrapper(MapperWrapper next) {
        return new KieHibernateMapper(next);
    }

}
