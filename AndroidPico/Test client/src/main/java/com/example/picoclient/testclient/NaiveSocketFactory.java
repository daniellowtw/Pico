package com.example.picoclient.testclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class NaiveSocketFactory {
    private static SSLSocketFactory sf;
    /**
     * Returns a SSL Factory instance that accepts all server certificates.
     *
     * SSLSocket sock = (SSLSocket) getSocketFactory.createSocket(host, 443);
     * @return An SSL-specific socket factory.
     **/
    public static final SSLSocketFactory getSocketFactory() {
        if (sf == null) {
            try {
                TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(new KeyManager[0], tm, new SecureRandom());
                sf = (SSLSocketFactory) context
                        .getSocketFactory();

            } catch (KeyManagementException e) {
                System.out.println("No SSL algorithm support: " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                System.out.println(e.getMessage());
            }
        }
        return sf;
    }
}
