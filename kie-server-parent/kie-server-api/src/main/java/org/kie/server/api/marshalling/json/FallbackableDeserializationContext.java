/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.marshalling.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity;
import com.fasterxml.jackson.databind.util.ClassUtil;

public class FallbackableDeserializationContext extends DefaultDeserializationContext {

    private static final long serialVersionUID = 1L;

    protected transient ClassLoader fallbackClassLoader = null;

    /**
     * Default constructor for a blueprint object, which will use the standard
     * {@link DeserializerCache}, given factory.
     */
    public FallbackableDeserializationContext(DeserializerFactory df) {
        super(df, null);
    }

    protected FallbackableDeserializationContext(FallbackableDeserializationContext src,
                                                 DeserializationConfig config, JsonParser jp, InjectableValues values) {
        super(src, config, jp, values);
    }

    protected FallbackableDeserializationContext(FallbackableDeserializationContext src) {
        super(src);
    }

    protected FallbackableDeserializationContext(FallbackableDeserializationContext src, DeserializerFactory factory) {
        super(src, factory);
    }

    /**
     * Called from JSONMarshaller
     */
    protected FallbackableDeserializationContext(DeserializationContext src, ClassLoader fallbackClassLoader) {
        super((DefaultDeserializationContext)src);
        this.fallbackClassLoader = fallbackClassLoader;
    }

    public ClassLoader getFallbackClassLoader() {
        return fallbackClassLoader;
    }

    public void setFallbackClassLoader(ClassLoader fallbackClassLoader) {
        this.fallbackClassLoader = fallbackClassLoader;
    }

    @Override
    public DefaultDeserializationContext copy() {
        ClassUtil.verifyMustOverride(FallbackableDeserializationContext.class, this, "copy");
        FallbackableDeserializationContext newInstance = new FallbackableDeserializationContext(this);
        newInstance.setFallbackClassLoader(this.fallbackClassLoader);
        return newInstance;
    }

    @Override
    public DefaultDeserializationContext createInstance(DeserializationConfig config,
                                                        JsonParser p,
                                                        InjectableValues values) {
        FallbackableDeserializationContext newInstance = new FallbackableDeserializationContext(this, config, p, values);
        newInstance.setFallbackClassLoader(this.fallbackClassLoader);
        return newInstance;
    }

    @Override
    public DefaultDeserializationContext with(DeserializerFactory factory) {
        FallbackableDeserializationContext newInstance = new FallbackableDeserializationContext(this, factory);
        newInstance.setFallbackClassLoader(this.fallbackClassLoader);
        return newInstance;
    }

    @Override
    public JavaType resolveSubType(JavaType baseType, String subClassName)
        throws JsonMappingException
    {
        // 30-Jan-2010, tatu: Most ids are basic class names; so let's first
        //    check if any generics info is added; and only then ask factory
        //    to do translation when necessary
        if (subClassName.indexOf('<') > 0) {
            // note: may want to try combining with specialization (esp for EnumMap)?
            // 17-Aug-2017, tatu: As per [databind#1735] need to ensure assignment
            //    compatibility -- needed later anyway, and not doing so may open
            //    security issues.
            JavaType t = getTypeFactory().constructFromCanonical(subClassName);
            if (t.isTypeOrSubTypeOf(baseType.getRawClass())) {
                return t;
            }
        } else {
            Class<?> cls;
            try {
                cls =  findClass(subClassName);
            } catch (ClassNotFoundException e) { // let caller handle this problem
                return null;
            } catch (Exception e) {
                throw invalidTypeIdException(baseType, subClassName, String.format(
                        "problem: (%s) %s",
                        e.getClass().getName(),
                        ClassUtil.exceptionMessage(e)));
            }
            if (baseType.isTypeOrSuperTypeOf(cls)) {
                return getTypeFactory().constructSpecializedType(baseType, cls);
            }
        }
        throw invalidTypeIdException(baseType, subClassName, "Not a subtype");
    }

    @Override
    public JavaType resolveAndValidateSubType(JavaType baseType,
                                              String subClass,
                                              PolymorphicTypeValidator ptv) throws JsonMappingException {
        // Off-line the special case of generic (parameterized) type:
        final int ltIndex = subClass.indexOf('<');
        if (ltIndex > 0) {
            return _resolveAndValidateGeneric(baseType, subClass, ptv, ltIndex);
        }
        final MapperConfig<?> config = getConfig();
        PolymorphicTypeValidator.Validity vld = ptv.validateSubClassName(config, baseType, subClass);
        if (vld == Validity.DENIED) {
            return _throwSubtypeNameNotAllowed(baseType, subClass, ptv);
        }
        final Class<?> cls;
        try {
            cls = findClass(subClass);
        } catch (ClassNotFoundException e) { // let caller handle this problem
            return null;
        } catch (Exception e) {
            throw invalidTypeIdException(baseType, subClass, String.format(
                                                                           "problem: (%s) %s",
                                                                           e.getClass().getName(),
                                                                           ClassUtil.exceptionMessage(e)));
        }
        if (!baseType.isTypeOrSuperTypeOf(cls)) {
            return _throwNotASubtype(baseType, subClass);
        }
        final JavaType subType = config.getTypeFactory().constructSpecializedType(baseType, cls);
        // May skip check if type was allowed by subclass name already
        if (vld == Validity.INDETERMINATE) {
            vld = ptv.validateSubType(config, baseType, subType);
            if (vld != Validity.ALLOWED) {
                return _throwSubtypeClassNotAllowed(baseType, subClass, ptv);
            }
        }
        return subType;
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            return getTypeFactory().findClass(className);
        } catch (ClassNotFoundException e) {
            if (fallbackClassLoader != null) {
                Throwable prob = null;
                try {
                    return Class.forName(className, true, fallbackClassLoader);
                } catch (Exception e1) {
                    prob = ClassUtil.getRootCause(e1);
                    throw new ClassNotFoundException(prob.getMessage(), prob);
                }
            }
            throw e;
        }
    }

    private JavaType _resolveAndValidateGeneric(JavaType baseType,
                                                String subClass,
                                                PolymorphicTypeValidator ptv,
                                                int ltIndex) throws JsonMappingException {
        final MapperConfig<?> config = getConfig();
        // 24-Apr-2019, tatu: Not 100% sure if we should pass name with type parameters
        //    or not, but guessing it's more convenient not to have to worry about it so
        //    strip out
        PolymorphicTypeValidator.Validity vld = ptv.validateSubClassName(config, baseType, subClass.substring(0, ltIndex));
        if (vld == Validity.DENIED) {
            return _throwSubtypeNameNotAllowed(baseType, subClass, ptv);
        }
        JavaType subType = getTypeFactory().constructFromCanonical(subClass);
        if (!subType.isTypeOrSubTypeOf(baseType.getRawClass())) {
            return _throwNotASubtype(baseType, subClass);
        }
        // Unless we were approved already by name, check that actual sub-class acceptable:
        if (vld != Validity.ALLOWED) {
            if (ptv.validateSubType(config, baseType, subType) != Validity.ALLOWED) {
                return _throwSubtypeClassNotAllowed(baseType, subClass, ptv);
            }
        }
        return subType;
    }
}
