package org.jbpm.springboot;

import java.io.File;
import java.io.FilenameFilter;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.appformer.maven.integration.MavenRepository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;

import static org.kie.scanner.KieMavenRepository.getMavenRepository;

public abstract class AbstractTest {

	protected static final String ARTIFACT_ID = "test-module";
	protected static final String GROUP_ID = "org.jbpm.test";
	protected static final String VERSION = "1.0.0";
	protected static PoolingDataSource pds;

	@BeforeClass
	public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("src/test/resources/kjar/jbpm-module.jar");
        File pom = new File("src/test/resources/kjar/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);
		
		System.setProperty("java.naming.factory.initial",
				"bitronix.tm.jndi.BitronixInitialContextFactory");
		pds = setupPoolingDataSource();
	}

	@Before
	public void setup() {
		cleanupSingletonSessionId();

	}

	@AfterClass
	public static void generalCleanup() {
		System.clearProperty("java.naming.factory.initial");
		if (pds != null) {
			pds.close();
		}
	}

	protected static PoolingDataSource setupPoolingDataSource() {
		PoolingDataSource pds = new PoolingDataSource();
		pds.setUniqueName("jdbc/jbpm");
		pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		pds.setMaxPoolSize(50);
		pds.setAllowLocalTransactions(true);
		pds.getDriverProperties().put("user", "sa");
		pds.getDriverProperties().put("password", "");
		pds.getDriverProperties().put("url", "jdbc:h2:mem:jbpm-db;MVCC=true");
		pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
		pds.init();
		return pds;
	}

	protected static void cleanupSingletonSessionId() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		if (tempDir.exists()) {

			String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {

					return name.endsWith("-jbpmSessionId.ser");
				}
			});
			for (String file : jbpmSerFiles) {

				new File(tempDir, file).delete();
			}
		}
	}
}
