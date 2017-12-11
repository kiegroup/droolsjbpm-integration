package org.jbpm.springboot;

import java.io.File;

import javax.naming.Context;

import org.appformer.maven.integration.MavenRepository;
import org.jbpm.test.util.PoolingDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;

import static org.kie.scanner.KieMavenRepository.getMavenRepository;

public abstract class AbstractTest {

	static final String ARTIFACT_ID = "test-module";
	static final String GROUP_ID = "org.jbpm.test";
	static final String VERSION = "1.0.0";

	private static PoolingDataSource poolingDataSource;

	@BeforeClass
	public static void generalSetup() {
		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
		File kjar = new File("src/test/resources/kjar/jbpm-module.jar");
		File pom = new File("src/test/resources/kjar/pom.xml");
		MavenRepository repository = getMavenRepository();
		repository.installArtifact(releaseId, kjar, pom);

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jbpm.test.util.CloseSafeMemoryContextFactory");
		poolingDataSource = setupDataSource();
	}

	@Before
	public void setup() {
		cleanupSingletonSessionId();
	}

	@AfterClass
	public static void generalCleanup() {
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
		if (poolingDataSource != null) {
			poolingDataSource.close();
		}
	}

	private static PoolingDataSource setupDataSource() {
		PoolingDataSource pds = new PoolingDataSource();
		pds.setUniqueName("jdbc/jbpm");
		pds.setClassName("org.h2.jdbcx.JdbcDataSource");
		pds.getDriverProperties().put("user", "sa");
		pds.getDriverProperties().put("password", "");
		pds.getDriverProperties().put("url", "jdbc:h2:mem:jbpm-db;MVCC=true");
		pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
		pds.init();
		return pds;
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	private static void cleanupSingletonSessionId() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		if (tempDir.exists()) {

			String[] jbpmSerFiles = tempDir.list((dir, name) -> name.endsWith("-jbpmSessionId.ser"));
			assert jbpmSerFiles != null;
			for (String file : jbpmSerFiles) {

				new File(tempDir, file).delete();
			}
		}
	}
}
