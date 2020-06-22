package org.kie.server.services.taskassigning.planning.util;

import java.io.FileOutputStream;
import java.security.KeyStore;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class KeyStoreUtil {

    private KeyStoreUtil() {
    }

    /**
     * Helper method for facilitating the generation of the KeyStore with the values used by this tests.
     * @param keyStoreLocation the keystore location.
     * @param entryAlias the alias for the keystore entry to generate.
     * @param entryAliasPassword the password for protecting the entry defined by the alias.
     * @param valueToCipherAndStore the value to cipher and store in the alias name entryAlias.
     * @param keyStorePassword the password for protecting the keystore.
     * @throws Exception exceptions are thrown if any cryptographic error is produced or the keystore can't be created, etc.
     */
    public static void makeNewKeystoreEntry(String keyStoreLocation,
                                             String entryAlias,
                                             String entryAliasPassword,
                                             String valueToCipherAndStore,
                                             String keyStorePassword) throws Exception {

        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(null, keyStorePassword.toCharArray());

        SecretKeyFactory factoryBPE = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey generatedSecret =
                factoryBPE.generateSecret(new PBEKeySpec(
                        valueToCipherAndStore.toCharArray()));

        KeyStore.PasswordProtection entryPasswordProtection = new KeyStore.PasswordProtection(entryAliasPassword.toCharArray());

        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(generatedSecret);
        ks.setEntry(entryAlias, secret, entryPasswordProtection);

        FileOutputStream fos = new java.io.FileOutputStream(keyStoreLocation);
        ks.store(fos, keyStorePassword.toCharArray());
    }
}
