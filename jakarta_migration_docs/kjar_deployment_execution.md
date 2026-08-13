KJAR DEPLOYMENT - EXECUTION
===========================

DEPLOYMENT
----------

curl command:
POST http://localhost:8080/kie-server/services/rest/server/containers/{containerId}


{
    "container-id" : "{containerId}",
    "release-id" : {
        "group-id" : "org.kie",
        "artifact-id" : "pmmljar",
        "version" : "2.0"
    }
}

1. Create Container request goes to [KieServerImpl.createContainer(String, KieContainerResource)](../kie-server-parent/kie-server-services/kie-server-services-common/src/main/java/org/kie/server/services/impl/KieServerImpl.java?plain=1#L304)
2. this invokes instantiation of `KieModuleMetaDataImpl` through `org.kie.scanner.KieModuleMetaDataImpl.init(ArtifactResolver)`
   1. add current jar - i.e. all classes of the kjar in the `KieModuleMetaDataImpl.classes` `Map`
   2. scans all the `compile` dependencies of the kjar (`org.appformer.maven.integration.ArtifactResolver.getAllDependencies(DependencyFilter)`) and add their classes in the `KieModuleMetaDataImpl.classes` property (`Map`)
3. add all the entries of the `KieModuleMetaDataImpl.classes` in the `KieModuleMetaDataImpl.packages` property (`Set`)
4. [DroolsKieServerExtension.createContainer(String, KieContainerInstance, Map<String, Object>)](../kie-server-parent/kie-server-services/kie-server-services-drools/src/main/java/org/kie/server/services/drools/DroolsKieServerExtension.java?plain=1#L91) is invoked
   1. retrieve the `KieModuleMetaDataImpl` created above 
   2. get all packages from it 
   3. scans all the classes from all the packages and add them to method property `extraClasses`
   4. add the above `extraClasses` to given KieContainerInstance.extraClasses


EXECUTION
---------

1. [CommandResource.manageContainer(HttpHeaders headers, String, String)](../kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-drools/src/main/java/org/kie/server/remote/rest/drools/CommandResource.java?plain=1#L82) is invoked 
2. [KieContainerInstanceImpl.getMarshaller(MarshallingFormat)](../kie-server-parent/kie-server-services/kie-server-services-common/src/main/java/org/kie/server/services/impl/KieContainerInstanceImpl.java?plain=1#L180) is invoked 
3. MarshallerFactory.getMarshaller( getExtraClasses(), format, this.kieContainer.getClassLoader() ); 
   1. here `getExtraClasses()` retrieve all the classes stored as extraClasses at previous point 2
4. BaseMarshallerBuilder build(Set<Class<?>> classes, MarshallingFormat format, ClassLoader classLoader) is invoked
   1. here `classes contains all the extraClasses` retrieved above 
5. in case of JSON, a new JSONMarshaller is instantiated 
6. JSONMarshaller.configureMarshaller(Set<Class<?>> classes, final ClassLoader classLoader)
   1. here `classes contains all the extraClasses` retrieved above 
   2. all the above `extraClasses` become the initial Set for the `JSONMarshaller.classSet`
   3. few hardcoded classes are also add 
   4. at the end, JSONMarshaller.classesSet contains 1. the `extraClasses` discovered during kjar deployment and the few hardcoded ones 
7. JSONMarshaller.unmarshall(String serializedInput, Class<T> type) // serializedInput = payload, type = org.drools.core.command.runtime.BatchExecutionCommandImpl 
   1. since classesSet does not contain the BatchExecutionCommandImpl, the type used to unmarshall is actually BatchExecutionCommandImpl 
   2. then, the payload is correctly unmarshalled (then, execution is delegated to engine implementation)



Rest-enabled commands
---------------------

See org.drools.core.command.runtime.BatchExecutionCommandImpl

| Command-id                          | Class |
|-------------------------------------|---|
| abort-work-item                     | AbortWorkItemCommand |
| apply-pmml-model-command            | ApplyPmmlModelCommand |
| clear-activation-group              | ClearActivationGroupCommand |
| clear-agenda                        | ClearAgendaCommand |
| clear-agenda-group                  | ClearAgendaGroupCommand |
| clear-ruleflow-group                | ClearRuleFlowGroupCommand |
| complete-work-item                  | CompleteWorkItemCommand |
| dispose                             | DisposeCommand |
| fire-all-rules                      | FireAllRulesCommand |
| fire-until-halt                     | FireUntilHaltCommand |
| get-fact-handles                    | GetFactHandlesCommand |
| get-global                          | GetGlobalCommand |
| get-object                          | GetObjectCommand |
| get-objects                         | GetObjectsCommand |
| insert                              | InsertObjectCommand |
| insert-elements                     | InsertElementsCommand |
| modify                              | ModifyCommand |
| query                               | QueryCommand |
| retract                             | DeleteCommand |
| rule-name-ends-with-agenda-filter   | RuleNameEndsWithAgendaFilter |
| rule-name-equals-agenda-filter      | RuleNameEqualsAgendaFilter |
| rule-name-matches-agenda-filter     | RuleNameMatchesAgendaFilter |
| rule-name-starts-with-agenda-filter | RuleNameStartsWithAgendaFilter |
| set-focus                           | AgendaGroupSetFocusCommand |
| set-global                          | SetGlobalCommand |
| signal-event                        | SignalEventCommand |
| start-process                       | StartProcessCommand |
| update                              | UpdateCommand |


