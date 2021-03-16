Kie-maven-plugin integration tests.
===================================

kie-maven-plugin integrations tests are tests that:
1) builds a kjar out of some resources, using the kie-maven-plugin
2) uses the generated kjar

They are written following the maven standard layout and they features the maven-invoker-plugin.
For each of those test, there is a specific directory under _src/it_.
Poms and sources are filtered to have the current drools version during execution.
Surefire (unit) tests are excluded, while integration (falsafe) ones are executed. To allow that, test classes must be named with trailing "IT" (e.g. _BuildPMMLTrustyTestIT_).
If java tests classes needs to be filtered, they have to be put under _src/test/java-filtered_ directory (from here, they will be filtered and pasted under _scr/test/java_ one).
Test compilation and reports may be found under _target/it/{module_name}_ directory.
_target/it/{module_name}/build.log_ will contain overall build output, while additionally test reports may be found under
_target/it/{module_name}/target/failsafe-reports_ and _target/it/{module_name}/target/surefire_


Ignored tests
=============

Tests using the _SerializeMojo_ have been removed because such mojo was used for Android, whose support has been dropped down.

KieContainer
============

Due to the order of execution of different steps, KieContainer for a given kjar can not be instantiated from local repository.
So, a workaround has been devised to create a KieContainer out of the kjar locally packaged (inside _target_ folder).

Here's an example on how to achieve that (_DeclaredTypesTestIT_ is the class this snippet has been taken from, _DeclaredTypeKBase_ is the kiebase name defined in the companion kmodule.xml):
 
        
        final URL targetLocation = DeclaredTypesTestIT.class.getProtectionDomain().getCodeSource().getLocation();
        final File basedir = new File(targetLocation.getFile().replace("/test-classes/", ""));
        final File kjarFile = new File(basedir, GAV_ARTIFACT_ID + "-" + GAV_VERSION + ".jar");
        Assertions.assertThat(kjarFile).exists();
        Set<URL> urls = new HashSet<>();
        urls.add(kjarFile.toURI().toURL());
        URLClassLoader projectClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), getClass().getClassLoader());

        final KieServices kieServices = KieServices.get();
        final KieContainer kieContainer =  kieServices.getKieClasspathContainer(projectClassLoader);

        KieSession kSession = null;
        try {
            final KieBase kieBase = kieContainer.getKieBase("DeclaredTypeKBase");
            kSession = kieBase.newKieSession();
        ....
