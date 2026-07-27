package com.example.travelplanner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        try {
            KeyStore combinedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            combinedTrustStore.load(null, null);

            String cacertsPath = System.getProperty("java.home") + "/lib/security/cacerts";
            try (InputStream is = new java.io.FileInputStream(cacertsPath)) {
                KeyStore systemTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                systemTrustStore.load(is, "changeit".toCharArray());
                Enumeration<String> aliases = systemTrustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    if (systemTrustStore.isCertificateEntry(alias)) {
                        combinedTrustStore.setCertificateEntry(alias, systemTrustStore.getCertificate(alias));
                    }
                }
            } catch (Exception e) {
                log.warn("Could not load system cacerts: {}", e.getMessage());
            }

            try (InputStream is = new ClassPathResource("nbp-truststore.jks").getInputStream()) {
                KeyStore customTrustStore = KeyStore.getInstance("JKS");
                customTrustStore.load(is, "changeit".toCharArray());
                Enumeration<String> aliases = customTrustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    Certificate cert = customTrustStore.getCertificate(alias);
                    combinedTrustStore.setCertificateEntry("custom-" + alias, cert);
                    log.info("Loaded custom certificate '{}' into combined truststore", alias);
                }
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(combinedTrustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            log.info("Custom SSL truststore configured successfully");
        } catch (Exception e) {
            log.error("Failed to configure custom SSL truststore: {}", e.getMessage());
        }

        return new RestTemplate(new SimpleClientHttpRequestFactory());
    }
}
