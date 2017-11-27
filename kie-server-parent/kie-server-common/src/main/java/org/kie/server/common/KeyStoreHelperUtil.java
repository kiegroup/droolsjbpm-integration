package org.kie.server.common;

import org.drools.core.util.KeyStoreHelper;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreHelperUtil {
    // the private key identifier for REST controller
    private static final String PROP_PWD_SERVER_ALIAS = "kie.keystore.key.server.alias";
    // the private key identifier for REST controller
    private static final String PROP_PWD_SERVER_PWD = "kie.keystore.key.server.pwd";

    // the private key identifier for controller
     private static final String PROP_PWD_CTRL_ALIAS = "kie.keystore.key.ctrl.alias";
    // the private key identifier for controller
    private static final String PROP_PWD_CTRL_PWD = "kie.keystore.key.ctrl.pwd";

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreHelperUtil.class);

    public static String loadServerPassword() {
        String passwordKey;
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper();

        try {
            String pwdKeyAlias = System.getProperty(PROP_PWD_SERVER_ALIAS, "");
            char[] pwdKeyPassword = System.getProperty(PROP_PWD_SERVER_PWD, "").toCharArray();

            passwordKey = keyStoreHelper.getPasswordKey(pwdKeyAlias, pwdKeyPassword);
        } catch (RuntimeException re) {
            logger.warn("Unable to load key store. Using password from configuration");
            passwordKey = System.getProperty(KieServerConstants.CFG_KIE_PASSWORD, "kieserver1!");
        }

        return passwordKey;
    }

    public static String loadControllerPassword(KieServerConfig config) {
        String passwordKey;
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper();

        try {
            String pwdKeyAlias = System.getProperty(PROP_PWD_CTRL_ALIAS, "");
            char[] pwdKeyPassword = System.getProperty(PROP_PWD_CTRL_PWD, "").toCharArray();

            passwordKey = keyStoreHelper.getPasswordKey(pwdKeyAlias, pwdKeyPassword);
        } catch (RuntimeException re) {
            logger.warn("Unable to load key store. Using password from configuration");
            passwordKey = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
        }

        return passwordKey;
    }
}
