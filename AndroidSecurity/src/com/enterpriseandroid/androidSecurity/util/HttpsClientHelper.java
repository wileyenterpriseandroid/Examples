package com.enterpriseandroid.androidSecurity.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.enterpriseandroid.androidSecurity.R;

import android.content.res.Resources;

/**
 * Creates an Https client that loads a key-store that can supply an application
 * defined root certificate authority to validate the client connection.
 *
 * This client can also authenticate using the system key store which contains
 * standard CAs as well.
 */
public class HttpsClientHelper {
    public static final int SECURE_PORT = 443;
    public static String CAPASSWORD = "your_ca_keystore_password";

	public static HttpClient getHttpClient(Resources resources)
			throws KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException,
            KeyStoreException, CertificateException,
			IOException
    {
		KeyStore caRootStore = KeyStore.getInstance("BKS");
        // Contains your application's root CA and allows use of
        // certificates that you sign with that CA.
		InputStream in = resources.openRawResource(R.raw.your_ownca_keystore);
		caRootStore.load(in, CAPASSWORD.toCharArray());

        // Use unencrypted factory for http port 80
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http",
                PlainSocketFactory.getSocketFactory(), 80));

        // Use a secure socket factory for 443, but this socket
        // factory will consider our "root" trust store when
        // making its connection.
		SSLSocketFactory sslSocketFactory = new SSLSocketFactory(caRootStore);
        sslSocketFactory.setHostnameVerifier(
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https",
                sslSocketFactory, SECURE_PORT));
		HttpParams params = new BasicHttpParams();
		ClientConnectionManager cm =
                new ThreadSafeClientConnManager(params, schemeRegistry);

		HttpClient client = new DefaultHttpClient(cm, params);
        return client;
	}
}
