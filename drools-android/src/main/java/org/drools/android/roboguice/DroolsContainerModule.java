package org.drools.android.roboguice;

import android.app.Application;
import android.content.Context;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.drools.android.DroolsAndroidContext;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.cdi.KBase;
import org.kie.api.cdi.KContainer;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.roboguice.shaded.goole.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Roboguice module for KieContainer, KieBase, and KieSession injections.
 * @author kedzie
 */
public class DroolsContainerModule  extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(DroolsContainerModule.class);

    private Application application;
    private KieServices ks;
    private KieContainerImpl classpathContainer;

    public DroolsContainerModule(Application ctx) {
        this.application = ctx;
    }

    @Override
    protected void configure() {
        DroolsAndroidContext.setContext(application);
        ks = KieServices.Factory.get();
        classpathContainer = (KieContainerImpl) ks.getKieClasspathContainer();
        KieProject kieProject = classpathContainer.getKieProject();

        logger.debug("Binding @KContainer");
        bind(KieContainer.class).toInstance(classpathContainer);
        if(kieProject.getDefaultKieBaseModel()!=null) {
            bind(KieBase.class).toProvider(
                    new KBaseProvider(kieProject.getDefaultKieBaseModel(), classpathContainer));
        }
        if(kieProject.getDefaultKieSession()!=null) {
            bind(KieSession.class).toProvider(
                    new KSessionProvider(kieProject.getDefaultKieSession(), classpathContainer));
        }
        if(kieProject.getDefaultStatelessKieSession()!=null) {
            bind(StatelessKieSession.class).toProvider(
                    new KStatelessSessionProvider(classpathContainer.getKieProject().getDefaultStatelessKieSession(), classpathContainer));
        }
        bind(KieContainer.class).annotatedWith(KContainer.class).toInstance(classpathContainer);
        for(final String kbaseName : classpathContainer.getKieBaseNames()) {
            logger.debug("Binding @KBase({})", kbaseName);
            bind(KieBase.class).annotatedWith(new KBaseImpl(kbaseName)).toProvider(
                    new KBaseProvider(classpathContainer.getKieBaseModel(kbaseName), classpathContainer));

            for(final String ksessionName : classpathContainer.getKieSessionNamesInKieBase(kbaseName)) {
                logger.debug("Binding @KSession({})", ksessionName);
                KieSessionModel model = classpathContainer.getKieSessionModel(ksessionName);
                if(model.getType().equals(KieSessionModel.KieSessionType.STATEFUL)) {
                    bind(KieSession.class).annotatedWith(new KSessionImpl(ksessionName))
                            .toProvider(new KSessionProvider(model, classpathContainer));
                } else {
                    bind(StatelessKieSession.class).annotatedWith(new KSessionImpl(ksessionName))
                            .toProvider(new KStatelessSessionProvider(model, classpathContainer));
                }
            }
        }
    }

    private static class KBaseProvider implements Provider<KieBase> {

        private KieContainer kContainer;

        private final KieBaseModel kBaseModel;

        public KBaseProvider(final KieBaseModel kBaseModel,
                         KieContainer kContainer) {
            this.kBaseModel = kBaseModel;
            this.kContainer = kContainer;
        }
        @Override
        public KieBase get() {
            return kContainer.getKieBase( kBaseModel.getName() );
        }
    }

    private static class KSessionProvider implements Provider<KieSession> {

        private KieContainer kContainer;

        private final KieSessionModel kSessionModel;

        public KSessionProvider(final KieSessionModel kSessionModel,
                             KieContainer kContainer) {
            this.kSessionModel = kSessionModel;
            this.kContainer = kContainer;
        }
        @Override
        public KieSession get() {
            return kContainer.newKieSession(kSessionModel.getName());
        }
    }

    private static class KStatelessSessionProvider implements Provider<StatelessKieSession> {

        private KieContainer kContainer;

        private final KieSessionModel kSessionModel;

        public KStatelessSessionProvider(final KieSessionModel kSessionModel,
                                KieContainer kContainer) {
            this.kSessionModel = kSessionModel;
            this.kContainer = kContainer;
        }
        @Override
        public StatelessKieSession get() {
            return kContainer.newStatelessKieSession(kSessionModel.getName());
        }
    }
}

class KBaseImpl implements KBase, Serializable {
    private final String value;
    private final String name;

    private static final long serialVersionUID = 0L;

    public KBaseImpl(String v) {
        this(v, "");
    }

    public KBaseImpl(String value, String name) {
        this.value = (String) Preconditions.checkNotNull(value, "value");
        this.name = (String) Preconditions.checkNotNull(name, "name");
    }

    public String value() {
        return this.value;
    }

    public String name() {
        return name;
    }

    public int hashCode() {
        return (127 * "value".hashCode() ^ this.value.hashCode()) +
                (127 * "name".hashCode() ^ this.name.hashCode());
    }

    public boolean equals(Object o) {
        if(!(o instanceof KBase)) {
            return false;
        } else {
            KBase other = (KBase)o;
            return this.value.equals(other.value())
                    && this.name.equals(other.name());
        }
    }

    public String toString() {
        return String.format("@%s(value=%s, name=%s)", KBase.class.getName(), this.value, this.name);
    }

    public Class<? extends Annotation> annotationType() {
        return KBase.class;
    }
}

class KSessionImpl implements KSession, Serializable  {
    private final String value;
    private final String name;

    private static final long serialVersionUID = 0L;

    public KSessionImpl(String v) {
        this(v, "");
    }

    public KSessionImpl(String v, String n) {
        this.value = (String) Preconditions.checkNotNull(v, "value");
        this.name = (String) Preconditions.checkNotNull(n, "name");
    }

    public Class< ? extends Annotation> annotationType() {
        return KSession.class;
    }

    public String value() {
        return value;
    }

    public String name() {
        return name;
    }

    public int hashCode() {
        return (127 * "value".hashCode() ^ this.value.hashCode()) +
                (127 * "name".hashCode() ^ this.name.hashCode());
    }

    public boolean equals(Object o) {
        if(!(o instanceof KSession)) {
            return false;
        } else {
            KSession other = (KSession)o;
            return this.value.equals(other.value())
                    && this.name.equals(other.name());
        }
    }

    public String toString() {
        return String.format("@%s(value=%s, name=%s)", KSession.class.getName(), this.value, this.name);
    }
}