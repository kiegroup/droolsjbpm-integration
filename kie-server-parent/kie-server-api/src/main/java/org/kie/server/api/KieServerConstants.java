package org.kie.server.api;

public class KieServerConstants {

    public static final String LOCATION_HEADER = "Location";

    public static final String CLASS_TYPE_HEADER = "X-KIE-ClassType";
    public static final String KIE_CONTENT_TYPE_HEADER = "X-KIE-ContentType";

    // kie server dedicated parameters
    public static final String CFG_PERSISTANCE_DS = "org.kie.server.persistence.ds";
    public static final String CFG_PERSISTANCE_TM = "org.kie.server.persistence.tm";
    public static final String CFG_PERSISTANCE_DIALECT = "org.kie.server.persistence.dialect";

    public static final String CFG_BYPASS_AUTH_USER = "org.kie.server.bypass.auth.user";

    public static final String CFG_KIE_CONTROLLER_USER = "org.kie.server.user";
    public static final String CFG_KIE_CONTROLLER_PASSWORD = "org.kie.server.pwd";

    // non kie server parameters but used by its extensions etc
    public static final String CFG_HT_CALLBACK = "org.jbpm.ht.callback";
    public static final String CFG_HT_CALLBACK_CLASS = "org.jbpm.ht.custom.callback";

    public static final String CFG_EXECUTOR_INTERVAL = "org.kie.executor.interval";
    public static final String CFG_EXECUTOR_POOL = "org.kie.executor.pool.size";
    public static final String CFG_EXECUTOR_RETRIES = "org.kie.executor.retry.count";
    public static final String CFG_EXECUTOR_TIME_UNIT = "org.kie.executor.timeunit";
    public static final String CFG_EXECUTOR_DISABLED = "org.kie.executor.disabled";

    public static final String CFG_KIE_MVN_SETTINGS = "kie.maven.settings.custom";


}
