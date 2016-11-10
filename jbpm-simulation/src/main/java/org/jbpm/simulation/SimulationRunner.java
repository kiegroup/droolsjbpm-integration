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

package org.jbpm.simulation;

import java.util.List;

import org.drools.core.command.SetVariableCommandFromLastReturn;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.fluent.impl.BaseBatchFluent;
import org.drools.core.fluent.impl.FluentBuilderImpl;
import org.drools.core.fluent.impl.PseudoClockRunner;
import org.jbpm.process.core.validation.ProcessValidatorRegistry;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.impl.SimulationProcessValidator;
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
import org.kie.internal.fluent.runtime.FluentBuilder;
import org.kie.internal.fluent.runtime.KieSessionFluent;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class SimulationRunner {
    
    static {
        ProcessValidatorRegistry.getInstance().registerAdditonalValidator(new SimulationProcessValidator());
    }

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
        
        final ReleaseId releaseId = createKJarWithMultipleResources(processId,
                new String[]{bpmn2Container}, new ResourceType[]{ResourceType.BPMN2});

        PseudoClockRunner runner = new PseudoClockRunner();
        FluentBuilder f = new FluentBuilderImpl();

        // @formatter:off        
        int counter = 0;
        int remainingInstances = numberOfAllInstances;
        for (SimulationPath path : paths) {
            // only paths that can be started are considered
            if (!path.isStartable()) {
                continue;
            }
            double probability = path.getProbability();
            f.newApplicationContext("path" + counter);

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
                    KieSessionFluent sessionFluent = f.after(interval * i)
                        .getKieContainer(releaseId)
                        .newSession();

                        ((BaseBatchFluent) sessionFluent).addCommand(new SimulateProcessPathCommand(processId, context, path));
//                        ((BaseBatchFluent) sessionFluent).addCommand(new SetVariableCommandFromLastReturn(StatefulKnowledgeSession.class.getName()));
                        ((BaseBatchFluent) sessionFluent).addCommand(new DisposeCommand());
                }
            } else {
                KieSessionFluent sessionFluent = f.after(interval)
                .getKieContainer(releaseId)
                .newSession();
                ((BaseBatchFluent) sessionFluent).addCommand(new SimulateProcessPathCommand(processId, context, path));
//                ((BaseBatchFluent) sessionFluent).addCommand(new SetVariableCommandFromLastReturn(StatefulKnowledgeSession.class.getName()));
                ((BaseBatchFluent) sessionFluent).addCommand(new DisposeCommand());
                break;
            }
            
            counter++;
// currently standalone paths within single definition are not supported
//            if (probability == 1) {
//                // in case given path has probability of 100% there is a need to reset the remaining instances
//                // as this is standalone process path
//                remainingInstances = numberOfAllInstances;
//            }
        }
        runner.execute(f.getExecutable());
        // @formatter:on
        
        context.getRepository().getSimulationInfo().setEndTime(context.getMaxEndTime());

        return context.getRepository();
    }
    
    protected static ReleaseId createKJarWithMultipleResources(String id, String[] resources, ResourceType[] types) {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writePomXML(getPom("org.jbpm.sim", id, "1.0"));

        for (int i = 0; i < resources.length; i++) {
            String res = resources[i];
            String type = types[i].getDefaultExtension();

            kfs.write("src/main/resources/" + id.replaceAll("\\.", "/")
                    + "/org/test/res" + i + ".bpsim." + type, res);
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel(id)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM)
                .setDefault(true);

        KieSessionModel ksession1 = kBase1
                .newKieSessionModel(id + ".KSession1")
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"))
                .setDefault(true);

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

    protected static String getPom(String groupId, String artifactId, String version) {
        String pom =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <groupId>" + groupId + "</groupId>\n" +
                        "  <artifactId>" + artifactId + "</artifactId>\n" +
                        "  <version>" + version + "</version>\n" +
                        "\n";
        pom += "</project>";
        return pom;
    }
}
