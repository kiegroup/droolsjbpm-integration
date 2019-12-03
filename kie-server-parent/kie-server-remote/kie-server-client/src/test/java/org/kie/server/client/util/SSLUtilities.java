package org.kie.server.client.util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class SSLUtilities {

    private static HostnameVerifier verifier;

    private static TrustManager[] trustManagers;

    public static void trustAllHostnames() {
        if (verifier == null) {
            verifier = new AllValidHostnameVerifier();
        }
        HttpsURLConnection.setDefaultHostnameVerifier(verifier);
    }

    public static void trustAllHttpsCertificates() {
        SSLContext context;

        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new AllValidTrustManager()};
        }
        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, trustManagers, new SecureRandom());
        } catch (GeneralSecurityException gse) {
            throw new IllegalStateException(gse.getMessage());
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    public static class AllValidHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return (true);
        } 
    } 

    public static class AllValidTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }
}
