package com.webtrekk.webtrekksdk.Utils;

import android.annotation.TargetApi;
import android.net.http.X509TrustManagerExtensions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class PinConnectionValidator {

    @Nullable
    private final Set<String> validPins;

    public PinConnectionValidator(@Nullable Set<String> validPins) {
        this.validPins = validPins;
    }

    private TrustChecker trustChecker = getTrustChecker();

    public void validatePinning(@NonNull HttpsURLConnection conn) throws SSLException {

        // Don't validate if no valid pins are provided
        if (validPins == null || validPins.isEmpty()) {
            WebtrekkLogging.log("PinConnectionValidator: Warning - No public pins provided. Pinning will be ignored.");
            return;
        }

        StringBuilder certChainMsg = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            List<X509Certificate> trustedChain = trustedChain(conn);
            for (Certificate cert : trustedChain) {
                byte[] publicKey = cert.getPublicKey().getEncoded();
                md.update(publicKey, 0, publicKey.length);
                String pin = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                certChainMsg.append("    sha256/")
                        .append(pin).append(" : ")
                        .append(cert.getPublicKey().toString())
                        .append("\n");
                if (validPins.contains(pin)) {
                    return;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SSLException(e);
        }
        throw new SSLPeerUnverifiedException("Certificate pinning " +
                "failure\n  Peer certificate chain:\n" + certChainMsg);
    }

    @NonNull
    private List<X509Certificate> trustedChain(@NonNull HttpsURLConnection conn)
            throws SSLException {

        Certificate[] serverCerts = conn.getServerCertificates();
        X509Certificate[] untrustedCerts = Arrays.copyOf(serverCerts, serverCerts.length,
                X509Certificate[].class);
        String host = conn.getURL().getHost();

        try {
            return trustChecker.checkServerTrusted(untrustedCerts, "RSA", host);
        } catch (CertificateException e) {
            throw new SSLException(e);
        }
    }

    @NonNull
    private TrustChecker getTrustChecker() {
        final X509TrustManager finalX509TrustManager = findTrustManager();

        if (finalX509TrustManager != null &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return new TrustChecker() {

                X509TrustManagerExtensions extensions = new X509TrustManagerExtensions(finalX509TrustManager);

                @Override
                @TargetApi(17)
                public List<X509Certificate> checkServerTrusted(
                        X509Certificate[] chain, String authType, String host)
                        throws CertificateException {
                    return extensions.checkServerTrusted(chain, authType, host);
                }
            };
        }

        // Implement for other versions below API 17
        return new TrustChecker() {
            @Override
            public List<X509Certificate> checkServerTrusted(
                    X509Certificate[] chain, String authType, String host) throws CertificateException {
                try {
                    finalX509TrustManager.checkServerTrusted(chain, authType);
                    return Arrays.asList(chain);
                } catch (CertificateException | NullPointerException e) {
                    throw new CertificateException("No valid certificate checker");
                }
            }
        };
    }

    @Nullable
    private X509TrustManager findTrustManager() {
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Trust", e.getMessage(), e);
        } catch (KeyStoreException e) {
            Log.e("Trust", e.getMessage(), e);
        }

        if (trustManagerFactory != null) {
            // Find first X509TrustManager in the TrustManagerFactory
            X509TrustManager x509TrustManager = null;
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManager;
                    break;
                }
            }
            return x509TrustManager;
        }

        return null;
    }

    interface TrustChecker {
        List<X509Certificate> checkServerTrusted(
                X509Certificate[] chain,
                String authType,
                String host
        ) throws CertificateException;
    }
}
