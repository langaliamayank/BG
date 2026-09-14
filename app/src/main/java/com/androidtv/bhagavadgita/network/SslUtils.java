package com.androidtv.bhagavadgita.network;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class SslUtils {

    /**
     * Builds an OkHttpClient.Builder trusting the system's CAs PLUS
     * ISRG Root X1 explicitly, for older Android devices whose OS
     * trust store predates Let's Encrypt's root.
     */
    public static OkHttpClient.Builder withIsrgRootTrust(OkHttpClient.Builder builder, InputStream isrgRootX1Pem) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate isrgRoot = cf.generateCertificate(isrgRootX1Pem);

            // Build a KeyStore containing the device's existing system CAs + our added root
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("isrg-root-x1", isrgRoot);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            TrustManager[] trustManagers = tmf.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers: " + trustManagers.length);
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);

        } catch (Exception e) {
            e.printStackTrace();
            // Falls back to default OkHttp behavior if this setup fails
        }

        return builder;
    }
}