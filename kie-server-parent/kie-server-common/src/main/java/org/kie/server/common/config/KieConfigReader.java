package org.kie.server.common.config;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.common.security.EAPVaultException;
import org.kie.server.common.security.KieVaultReader;

public class KieConfigReader {

    /**
     * Method for loading a password from config or from EAP Vault
     * @param config instance of server config
     * @param hasEAPVault should be true in case EAP Vault is accessible
     * @return loaded password
     * @throws EAPVaultException in case it is marked to use EAP Vault but this is not accessible
     */
    public static String loadPassword(KieServerConfig config, boolean hasEAPVault) throws EAPVaultException {
        String password;
        final String vaultName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_VAULT_NAME);

        if (vaultName == null) {
            password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
        } else {
            if (hasEAPVault) {
                password = KieVaultReader.decryptValue(vaultName);
            } else {
                throw new EAPVaultException("EAP Vault is not available");
            }
        }

        return password;
    }
}
