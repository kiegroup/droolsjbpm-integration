/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.spring.annotations;

import org.drools.core.util.StringUtils;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.cdi.KBase;
import org.kie.api.cdi.KContainer;
import org.kie.api.cdi.KReleaseId;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AnnotationsPostProcessor implements InstantiationAwareBeanPostProcessor,
        MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware, Serializable {

    public static final String KIE_ANNOTATIONS_ARE_NOT_SUPPORTED_ON_STATIC_METHODS = "Kie Annotations are not supported on static methods";
    public static final String INJECTION_OF_KIE_DEPENDENCIES_FAILED = "Injection of kie dependencies failed";
    private transient final Map<Class<?>, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<Class<?>, InjectionMetadata>();
    private int order = Ordered.LOWEST_PRECEDENCE - 4;
    private transient ListableBeanFactory beanFactory;
    private ReleaseId releaseId;
    private Map<ReleaseId, KieContainer> kieContainerMap = new HashMap<ReleaseId, KieContainer>();

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ListableBeanFactory) {
            this.beanFactory = (ListableBeanFactory) beanFactory;
        }
    }

    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findAnnotationMetadata(beanType);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
        return null;
    }

    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

        InjectionMetadata metadata = findAnnotationMetadata(bean.getClass());
        try {
            metadata.inject(bean, beanName, pvs);
        }
        catch (Throwable ex) {
            throw new BeanCreationException(beanName, INJECTION_OF_KIE_DEPENDENCIES_FAILED, ex);
        }

        return pvs;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private InjectionMetadata findAnnotationMetadata(final Class clazz) {
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(clazz);
        if (metadata == null) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(clazz);
                if (metadata == null) {
                    LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
                    Class<?> targetClass = clazz;

                    do {
                        LinkedList<InjectionMetadata.InjectedElement> currElements = new LinkedList<InjectionMetadata.InjectedElement>();
                        checkForFieldInjections(targetClass, currElements);
                        checkForMethodInjections(targetClass, currElements);
                        elements.addAll(0, currElements);
                        targetClass = targetClass.getSuperclass();
                    }
                    while (targetClass != null && targetClass != Object.class);

                    metadata = new InjectionMetadata(clazz, elements);
                    this.injectionMetadataCache.put(clazz, metadata);
                }
            }
        }
        return metadata;
    }

    private void checkForMethodInjections(Class<?> targetClass, LinkedList<InjectionMetadata.InjectedElement> currElements) {
        for (Method method : targetClass.getDeclaredMethods()) {
            KSession kSession = method.getAnnotation(KSession.class);
            KBase kBase = method.getAnnotation(KBase.class);
            KContainer kContainer = method.getAnnotation(KContainer.class);
            if ((kSession != null || kBase != null || kContainer !=null) &&
                    method.equals(ClassUtils.getMostSpecificMethod(method, targetClass))) {
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException(KIE_ANNOTATIONS_ARE_NOT_SUPPORTED_ON_STATIC_METHODS);
                }
                if (method.getParameterTypes().length != 1) {
                    throw new IllegalStateException("Kie Annotation requires a single-arg method: " + method);
                }
                PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
                if ( kSession != null ) {
                    currElements.add(new KSessionInjectedElement(method, pd, releaseId));
                } else if (kBase != null ) {
                    currElements.add(new KBaseInjectedElement(method, pd,releaseId));
                } else if (kContainer != null ) {
                    currElements.add(new KContainerInjectedElement(method, pd, releaseId));
                }
            }
        }
    }

    private void checkForFieldInjections(Class<?> targetClass, LinkedList<InjectionMetadata.InjectedElement> currElements) {
        KieServices ks = KieServices.Factory.get();
        for (Field field : targetClass.getDeclaredFields()) {

            KReleaseId kReleaseId = field.getAnnotation(KReleaseId.class);
            ReleaseId specificReleaseId = null;
            if ( kReleaseId != null ) {
                specificReleaseId = ks.newReleaseId(kReleaseId.groupId(),
                        kReleaseId.artifactId(),
                        kReleaseId.version());
            } else {
                specificReleaseId = this.releaseId;
            }

            KBase kBase = field.getAnnotation(KBase.class);
            if (kBase != null) {
                throwExceptionIfStatic(field);
                currElements.add(new KBaseInjectedElement(field, null, specificReleaseId));
            }
            KSession kSession = field.getAnnotation(KSession.class);
            if (kSession != null) {
                throwExceptionIfStatic(field);
                currElements.add(new KSessionInjectedElement(field, null, specificReleaseId));
            }
            KContainer kContainer = field.getAnnotation(KContainer.class);
            if (kContainer != null) {
                throwExceptionIfStatic(field);
                currElements.add(new KContainerInjectedElement(field, null, specificReleaseId));
            }
        }
    }

    private void throwExceptionIfStatic(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException(KIE_ANNOTATIONS_ARE_NOT_SUPPORTED_ON_STATIC_METHODS);
        }
    }

    private class KieElementInjectedElement extends InjectionMetadata.InjectedElement {
        protected String name;
        protected ReleaseId releaseId;
        public KieElementInjectedElement(Member member, PropertyDescriptor pd, ReleaseId releaseId) {
            super(member, pd);
            setReleaseId(releaseId);
        }

        public KieElementInjectedElement(Member member, PropertyDescriptor pd) {
            this(member, pd, null);
        }

        protected Object getResourceToInject(Object target, String requestingBeanName) {
            return beanFactory.getBean(name);
        }

        public ReleaseId getReleaseId() {
            return releaseId;
        }

        public void setReleaseId(ReleaseId releaseId) {
            this.releaseId = releaseId;
        }
    }

    private class KBaseInjectedElement extends KieElementInjectedElement {

        public KBaseInjectedElement(Member member, PropertyDescriptor pd, ReleaseId releaseId) {
            super(member, pd, releaseId);
            AnnotatedElement ae = (AnnotatedElement) member;
            KBase aeAnnotation = ae.getAnnotation(KBase.class);
            name = aeAnnotation.value();
            checkResourceType(KieBase.class);
        }

        protected Object getResourceToInject(Object target, String requestingBeanName) {
            if (StringUtils.isEmpty(name)) {
                //check for default KieBase in the current KieContainer
                KieContainer kieContainer = kieContainerMap.get(getReleaseId());
                if  ( kieContainer == null){
                    kieContainer = KieServices.Factory.get().newKieContainer(getReleaseId());
                    kieContainerMap.put(releaseId, kieContainer);
                }
                return kieContainer.getKieBase();
            }
            if( getReleaseId().equals(AnnotationsPostProcessor.this.getReleaseId())) {
                return beanFactory.getBean(name);
            } else {
                KieContainer kieContainer = kieContainerMap.get(getReleaseId());
                if  ( kieContainer == null){
                    kieContainer = KieServices.Factory.get().newKieContainer(getReleaseId());
                    kieContainerMap.put(releaseId, kieContainer);
                }
                return kieContainer.getKieBase(name);
            }
        }

    }

    private class KSessionInjectedElement extends KieElementInjectedElement {

        String type;
        public KSessionInjectedElement(Member member, PropertyDescriptor pd, ReleaseId releaseId) {
            super(member, pd, releaseId);
            AnnotatedElement ae = (AnnotatedElement) member;
            KSession kSessionAnnotation = ae.getAnnotation(KSession.class);
            name = kSessionAnnotation.value();

            checkResourceType(CommandExecutor.class);
        }

        protected Object getResourceToInject(Object target, String requestingBeanName) {
            if( getReleaseId().equals(AnnotationsPostProcessor.this.getReleaseId())) {
                return beanFactory.getBean(name);
            } else {
                KieContainer kieContainer = kieContainerMap.get(getReleaseId());
                if  (kieContainer == null){
                    kieContainer = KieServices.Factory.get().newKieContainer(getReleaseId());
                    kieContainerMap.put(releaseId, kieContainer);
                }
                String type = "stateful";
                if ( member instanceof Field){
                    if(((Field)member).getGenericType() instanceof StatelessKieSession){
                        type = "stateless";
                    }
                } else if (member instanceof Method) {
                    if(((Method)member).getParameterTypes()[0].getName().equalsIgnoreCase(StatelessKieSession.class.getName())){
                        type = "stateless";
                    }
                }
                if (type.equalsIgnoreCase("stateful")) {
                    return kieContainer.newKieSession(name);
                } else {
                    return kieContainer.newStatelessKieSession(name);
                }
            }
        }
    }

    private class KContainerInjectedElement extends KieElementInjectedElement {

        public KContainerInjectedElement(Member member, PropertyDescriptor pd, ReleaseId releaseId) {
            super(member, pd, releaseId);
            checkResourceType(KieContainer.class);
        }

        protected Object getResourceToInject(Object target, String requestingBeanName) {
            KieContainer kieContainer = kieContainerMap.get(getReleaseId());
            if  ( kieContainer == null){
                kieContainer = KieServices.Factory.get().newKieContainer(getReleaseId());
                kieContainerMap.put(releaseId, kieContainer);
            }
            return kieContainer;
        }
    }
}
