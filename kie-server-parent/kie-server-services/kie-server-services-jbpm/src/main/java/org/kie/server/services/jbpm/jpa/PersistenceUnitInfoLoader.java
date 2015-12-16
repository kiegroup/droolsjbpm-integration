/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.jpa;

import java.io.InputStream;
import javax.naming.InitialContext;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PersistenceUnitInfoLoader {

    enum PersistenceXml {
        TAG_PERSISTENCE("persistence"),
        TAG_PERSISTENCE_UNIT("persistence-unit"),
        TAG_PROPERTIES("properties"),
        TAG_PROPERTY("property"),
        TAG_NON_JTA_DATA_SOURCE("non-jta-data-source"),
        TAG_JTA_DATA_SOURCE("jta-data-source"),
        TAG_CLASS("class"),
        TAG_MAPPING_FILE("mapping-file"),
        TAG_JAR_FILE("jar-file"),
        TAG_EXCLUDE_UNLISTED_CLASSES("exclude-unlisted-classes"),
        TAG_VALIDATION_MODE("validation-mode"),
        TAG_SHARED_CACHE_MODE("shared-cache-mode"),
        TAG_PROVIDER("provider"),
        TAG_UNKNOWN("unknown"),
        ATTR_UNIT_NAME("name"),
        ATTR_TRANSACTION_TYPE("transaction-type"),
        ATTR_SCHEMA_VERSION("version");

        private final String	name;

        PersistenceXml(String name) {
            this.name = name;
        }

        public static PersistenceXml parse(String aName) {
            try {
                return valueOf("TAG_" + aName.replace('-', '_').toUpperCase());
            } catch (IllegalArgumentException e) {
                return TAG_UNKNOWN;
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static PersistenceUnitInfo load(InputStream inputStream, InitialContext initialContext, ClassLoader classLoader) throws XMLStreamException {
        PersistenceUnitInfoImpl persistenceUnitInfo = new PersistenceUnitInfoImpl(initialContext, classLoader);

        StringBuffer tagContent = new StringBuffer();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
        PersistenceXml tag = PersistenceXml.TAG_UNKNOWN;
        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    tag = PersistenceXml.parse(reader.getLocalName());
                    switch (tag) {
                        case TAG_PERSISTENCE:
                            String schemaVersion = reader.getAttributeValue("", PersistenceXml.ATTR_SCHEMA_VERSION.toString());
                            persistenceUnitInfo.setPersistenceXMLSchemaVersion(schemaVersion);
                            break;
                        case TAG_PERSISTENCE_UNIT:
                            String unitName = reader.getAttributeValue("", PersistenceXml.ATTR_UNIT_NAME.toString());
                            String transactionType = reader.getAttributeValue("", PersistenceXml.ATTR_TRANSACTION_TYPE.toString());

                            persistenceUnitInfo.setPersistenceUnitName(unitName);
                            persistenceUnitInfo.setTransactionType(transactionType);
                            break;
                        case TAG_EXCLUDE_UNLISTED_CLASSES:
                            persistenceUnitInfo.setExcludeUnlistedClasses(true);
                            break;
                        case TAG_PROPERTY:
                            persistenceUnitInfo.addProperty(reader.getAttributeValue("", "name"), reader.getAttributeValue("", "value"));
                            break;
                        default:
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    if (!tag.equals(PersistenceXml.TAG_UNKNOWN)) {
                        tagContent.append(reader.getText());
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    String s = tagContent.toString().trim();
                    tagContent = new StringBuffer();

                    if (s.isEmpty()) {
                        break;
                    }
                    switch (tag) {
                        case TAG_PROVIDER:
                            persistenceUnitInfo.setPersistenceProviderClassName(s);
                            break;
                        case TAG_JTA_DATA_SOURCE:
                            persistenceUnitInfo.setJtaDataSource(s);
                            break;
                        case TAG_NON_JTA_DATA_SOURCE:
                            persistenceUnitInfo.setNonJtaDataSource(s);
                            break;
                        case TAG_MAPPING_FILE:
                            persistenceUnitInfo.addMappingFile(s);
                            break;
                        case TAG_JAR_FILE:
                            persistenceUnitInfo.addJarFileUrl(s);
                            break;
                        case TAG_CLASS:
                            persistenceUnitInfo.addManagedClassName(s);
                            break;
                        case TAG_EXCLUDE_UNLISTED_CLASSES:
                            persistenceUnitInfo.setExcludeUnlistedClasses(Boolean.parseBoolean(s));
                            break;
                        case TAG_SHARED_CACHE_MODE:
                            persistenceUnitInfo.setSharedCacheMode(s);
                            break;
                        case TAG_VALIDATION_MODE:
                            persistenceUnitInfo.setValidationMode(s);
                            break;
                        default:
                    }
                    break;
            }
        }

        return persistenceUnitInfo;
    }

}
