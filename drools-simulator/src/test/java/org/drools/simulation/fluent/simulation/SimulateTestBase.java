package org.drools.simulation.fluent.simulation;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.conf.EqualityBehaviorOption;
import org.kie.conf.EventProcessingOption;
import org.kie.io.ResourceType;
import org.kie.runtime.conf.ClockTypeOption;

public class SimulateTestBase {

    protected ReleaseId createKJar(String... pairs) throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for ( int i = 0; i < pairs.length; i += 2 ) {
            String id = pairs[i];
            String rule = pairs[i + 1];

            kfs.write( "src/main/resources/" + id.replaceAll( "\\.", "/" ) + "/org/test/rule" + i + ".drl", rule );

            KieBaseModel kBase1 = kproj.newKieBaseModel( id )
                    .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                    .setEventProcessingMode( EventProcessingOption.STREAM );

            KieSessionModel ksession1 = kBase1.newKieSessionModel(id + ".KSession1")
                    .setType(KieSessionModel.KieSessionType.STATEFUL)
                    .setClockType(ClockTypeOption.get("pseudo"));
        }

        kfs.writeKModuleXML(kproj.toXML());

        // buildAll() automatically adds the module to the kieRepository
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        assertTrue(kieBuilder.getResults().getMessages().isEmpty());
        
        KieModule kieModule = kieBuilder.getKieModule();
        return kieModule.getReleaseId();
    }

    protected ReleaseId createKJarWithMultipleResources(String id,
                                                        String[] resources,
                                                        ResourceType[] types) throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for ( int i = 0; i < resources.length; i++ ) {
            String res = resources[i];
            String type = types[i].getDefaultExtension();

            kfs.write( "src/main/resources/" + id.replaceAll( "\\.", "/" ) + "/org/test/res" + i + "." + type, res );
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel( id )
                .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM );

        KieSessionModel ksession1 = kBase1.newKieSessionModel( id + ".KSession1" )
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType( ClockTypeOption.get( "pseudo" ) );

        kfs.writeKModuleXML(kproj.toXML());

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        assertTrue(kieBuilder.getResults().getMessages().isEmpty());
        
        KieModule kieModule = kieBuilder.getKieModule();
        return kieModule.getReleaseId();
    }
}
