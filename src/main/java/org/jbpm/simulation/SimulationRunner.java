package org.jbpm.simulation;
import java.util.List;

import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.simulation.impl.DefaultSimulationFluent;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.command.World;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class SimulationRunner {

    public static SimulationRepository runSimulation(String processId, String bpmn2Container, int numberOfAllInstances, long interval, String... rules) {
        
        
        return runSimulation(processId, bpmn2Container, numberOfAllInstances, interval, false, rules);
    }
    
    public static SimulationRepository runSimulation(String processId, String bpmn2Container, int numberOfAllInstances, long interval, boolean runRules, String... rules) {
        
        Resource[] resources = new Resource[rules.length];
        for (int i = 0; i < rules.length; i++) {
            resources[i] = ResourceFactory.newClassPathResource(rules[i]);
        }
        
        return runSimulation(processId, bpmn2Container, numberOfAllInstances, interval, runRules, resources);
    }
    
    public static SimulationRepository runSimulation(String processId, String bpmn2Container, int numberOfAllInstances, long interval, boolean runRules, Resource... rules) {
        
        SimulationContext context = SimulationContextFactory.newContext(new BPMN2SimulationDataProvider(bpmn2Container), new WorkingMemorySimulationRepository(runRules, rules));
        SimulationDataProvider provider = context.getDataProvider();
        
        PathFinder finder = PathFinderFactory.getInstance(bpmn2Container);
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter(provider));
        
        // TODO when introduced configurable start time that should be used instead of currentTimeMillis
        context.getRepository().setSimulationInfo(new SimulationInfo(System.currentTimeMillis(), processId, numberOfAllInstances, interval));
        
        ReleaseId releaseId = createKJarWithMultipleResources("TestKbase",
                new String[]{bpmn2Container}, new ResourceType[]{ResourceType.BPMN2});
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off        
        int counter = 0;
        int remainingInstances = numberOfAllInstances;
        for (SimulationPath path : paths) {
            
            double probability = provider.calculatePathProbability(path);
            f.newPath("path" + counter);

            int instancesOfPath = 1;
            // count how many instances/steps should current path have
            if (numberOfAllInstances > 1) {
                instancesOfPath = (int) Math.round((numberOfAllInstances * probability));
                
                // ensure that we won't exceed total number of instance due to rounding
                if (instancesOfPath > remainingInstances) {
                    instancesOfPath = remainingInstances;
                }
                
                remainingInstances -= instancesOfPath;
                        
                for (int i = 0; i < instancesOfPath; i++) {
                    f.newStep( interval * i )
                        .newKieSession( releaseId, "TestKbase.KSession1" )
                            .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                        .addCommand(new SimulateProcessPathCommand(processId, context, path));
                }
            } else {
                f.newStep( interval )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand(processId, context, path));
                break;
            }
            
            counter++;
        }
        f.runSimulation();
        // @formatter:on
        
        context.getRepository().getSimulationInfo().setEndTime(context.getMaxEndTime());
        
        return context.getRepository();
    }
    
    protected static ReleaseId createKJarWithMultipleResources(String id, String[] resources, ResourceType[] types) {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for (int i = 0; i < resources.length; i++) {
            String res = resources[i];
            String type = types[i].getDefaultExtension();

            kfs.write("src/main/resources/" + id.replaceAll("\\.", "/")
                    + "/org/test/res" + i + "." + type, res);
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel(id)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM);

        KieSessionModel ksession1 = kBase1
                .newKieSessionModel(id + ".KSession1")
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kfs.writeKModuleXML(kproj.toXML());

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        if(!kieBuilder.getResults().getMessages().isEmpty()) {
            for (Message msg : kieBuilder.getResults().getMessages()) {
                System.out.println("[ERROR]" + msg.getText());
            }
            throw new RuntimeException("Error building knowledge base, see previous errors");
        }

        KieModule kieModule = kieBuilder.getKieModule();
        return kieModule.getReleaseId();
    }
}
