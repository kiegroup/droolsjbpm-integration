package org.drools.android.roboguice;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.drools.android.DroolsAndroidContext;
import org.drools.android.roboguice.KnowledgeBaseListener;
import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.cdi.KBase;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roboguice.inject.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Roboguice module for preserialized kiebases
 * @author kedzie
 */
public class DroolsModule extends AbstractModule {
   private static final Logger logger = LoggerFactory.getLogger(DroolsModule.class);

   private Application application;

   public DroolsModule(Application ctx) {
      this.application = ctx;
   }

   @Override
   protected void configure() {
      DroolsAndroidContext.setContext(application);
      final KnowledgeBaseListener kbListener = new KnowledgeBaseListener(application);
      bind(KnowledgeBaseListener.class).toInstance(kbListener);
      bindListener(Matchers.any(), kbListener);
   }
}

class KnowledgeBaseListener implements TypeListener {
   private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseListener.class);

   protected Application application;
   protected Resources resources;

   public KnowledgeBaseListener(Application application) {
      this.application = application;
      this.resources = application.getResources();
   }

   public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
      for( Class<?> c = typeLiteral.getRawType(); c!=Object.class; c = c.getSuperclass() )
         for (Field field : c.getDeclaredFields())
            if ( field.isAnnotationPresent(KBase.class) && !Modifier.isStatic(field.getModifiers()) )
               typeEncounter.register(new KnowledgeBaseMembersInjector<I>(field, application, field.getAnnotation(KBase.class)));
   }

   private KieServices ks;
   private KieContainer classpathContainer;
   private Map<String, KieBase> kbases = new HashMap<String, KieBase>();

   protected class KnowledgeBaseMembersInjector<T> implements MembersInjector<T> {

      protected Field field;
      protected Application application;
      protected KBase annotation;

      public KnowledgeBaseMembersInjector(Field field, Application application, KBase annotation) {
         this.field = field;
         this.application = application;
         this.annotation = annotation;
      }

      public void injectMembers(T instance) {
         KieBase knowledgeBase = null;
         try {
            final int id = resources.getIdentifier(annotation.value().toLowerCase(),
                    "raw", application.getPackageName());

            if(id!=0) { //Inject serialized knowledgebase
               if(!kbases.containsKey(annotation.value())) {
                  logger.trace("Deserializing knowledge base {}", annotation.value());
                  knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
                  ((KnowledgeBase)knowledgeBase).addKnowledgePackages(
                          (List<KnowledgePackage>) DroolsStreamUtils.streamIn(resources.openRawResource(id)));
               }
               knowledgeBase = kbases.get(annotation.value());
            } else { //inject knowledge base from classpath container
               if(classpathContainer==null) {
                  ks = KieServices.Factory.get();
                  classpathContainer = ks.getKieClasspathContainer();
               }
               knowledgeBase = classpathContainer.getKieBase(annotation.value());
            }

            if (knowledgeBase == null && Nullable.notNullable(field) ) {
               throw new NullPointerException(String.format("Can't inject null value into %s.%s when field is not @Nullable", field.getDeclaringClass(), field
                       .getName()));
            }

            field.setAccessible(true);
            field.set(instance, knowledgeBase);
            logger.debug("Injected Knowledge Base: " + knowledgeBase);
         } catch (IllegalArgumentException f) {
            throw new IllegalArgumentException(String.format("Can't assign %s value %s to %s field %s", knowledgeBase != null ? knowledgeBase.getClass() : "(null)", knowledgeBase,
                    field.getType(), field.getName()));
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }

   }
}
