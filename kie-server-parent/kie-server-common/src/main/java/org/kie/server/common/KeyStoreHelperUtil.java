package org.kie.server.common;

import java.util.HashSet;
import java.util.Set;

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

    private static Set<String> invocationCache = new HashSet<>();

    public static String loadServerPassword() {
        String defaultPassword = System.getProperty(KieServerConstants.CFG_KIE_PASSWORD, "kieserver1!");
        return loadPasswordKey(PROP_PWD_SERVER_ALIAS, PROP_PWD_SERVER_PWD, defaultPassword);
    }

    public static String loadControllerPassword(final KieServerConfig config) {
        return loadControllerPassword(config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!"));
    }

    public static String loadControllerPassword(final String defaultPassword) {
        return loadPasswordKey(PROP_PWD_CTRL_ALIAS, PROP_PWD_CTRL_PWD, defaultPassword);
    }

    private static String loadPasswordKey(String pwdKeyAliasProperty, String pwdKeyPasswordProperty, String defaultPassword) {
        String passwordKey;
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper();
        try {
            String pwdKeyAlias = System.getProperty(pwdKeyAliasProperty, "");
            String pwdKeyPassword = System.getProperty(pwdKeyPasswordProperty, "");
            passwordKey = keyStoreHelper.getPasswordKey(pwdKeyAlias, pwdKeyPassword.toCharArray());
        } catch (RuntimeException re) {
            passwordKey = defaultPassword;
            if (hasNotBeenInvoked(pwdKeyAliasProperty, pwdKeyPasswordProperty, passwordKey)) {
                // only print when it has not been invoked
                logger.warn("Unable to load key store. Using password from configuration");
            }
        }
        return passwordKey;
    }

    private static boolean hasNotBeenInvoked(String pwdKeyAliasProperty, String pwdKeyPasswordProperty, String passwordKey) {
        if (invocationCache.contains(pwdKeyAliasProperty)) {
            return false;
        }
        invocationCache.add(pwdKeyAliasProperty);
        return true;
    }
}
