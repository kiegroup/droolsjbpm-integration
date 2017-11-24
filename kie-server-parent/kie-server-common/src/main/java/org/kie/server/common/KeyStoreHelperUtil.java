package org.kie.server.common;

import org.drools.core.util.KeyStoreHelper;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreHelperUtil {

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreHelperUtil.class);

    public static String loadPassword() {
        String passwordKey;
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper();

        try {
            passwordKey = keyStoreHelper.getPasswordKey();
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
            passwordKey = keyStoreHelper.getPasswordKey();
        } catch (RuntimeException re) {
            logger.warn("Unable to load key store. Using password from configuration");
            passwordKey = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
        }

        return passwordKey;
    }
}
