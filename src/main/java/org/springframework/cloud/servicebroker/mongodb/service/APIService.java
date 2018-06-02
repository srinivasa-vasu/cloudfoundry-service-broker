package org.springframework.cloud.servicebroker.mongodb.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Service
public class APIService extends RestTemplate {

    public APIService() {
        setRequestFactory(new TrustEverythingClientHttpRequestFactory());
        setErrorHandler(new NoErrorsResponseErrorHandler());
    }

    private static final class NoErrorsResponseErrorHandler
            extends DefaultResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) {
            return false;
        }

    }

    private static final class TrustEverythingClientHttpRequestFactory
            extends SimpleClientHttpRequestFactory {

        @Override
        protected HttpURLConnection openConnection(URL url, Proxy proxy)
                throws IOException {
            HttpURLConnection connection = super.openConnection(url, proxy);

            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

                httpsConnection.setSSLSocketFactory(
                        getSslContext(new TrustEverythingTrustManager())
                                .getSocketFactory());
                httpsConnection
                        .setHostnameVerifier(new TrustEverythingHostNameVerifier());
            }

            return connection;
        }

        private static SSLContext getSslContext(TrustManager trustManager) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[] { trustManager }, null);
                return sslContext;
            }
            catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);

            }

        }
    }

    private static final class TrustEverythingHostNameVerifier
            implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }

    }

    private static final class TrustEverythingTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }
}
