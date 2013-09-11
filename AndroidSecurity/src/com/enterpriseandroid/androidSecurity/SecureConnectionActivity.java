package com.enterpriseandroid.androidSecurity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import com.enterpriseandroid.androidSecurity.R;
import com.enterpriseandroid.androidSecurity.util.HttpsClientHelper;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * A very simple activity that can invoke a request on a secure mHost.
 */
public class SecureConnectionActivity extends Activity {
    public static final String TAG = "SecureConnection";

    /**
     * Visit this host in the Android browser to make certain
     * Android can access the DNS name. If needed, specify
     * the DNS host to the emulator using:
     *
     * -dns-server <dns_ip>
     *
     * from /etc/resolv.conf
     */
    public static final String SECURE_HOST = "jibedev.net";
//    public static final String SECURE_HOST = "your_dns_hostname_with_root_domain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecureConnectionTask scTask = new SecureConnectionTask(this, new Handler());
        scTask.execute();
    }

    public static final class SecureConnectionTask extends
            AsyncTask <String, Integer, String>
    {
        private final Activity mContext;
        private final String mUser = "exampleUser";
        private final String mPass = "examplePassword";
        // assume the ch6 service is running locally, and connect to localhost.
        private final String mHost = SECURE_HOST;

        private final Handler mHandler;

        private SecureConnectionTask(Activity context, Handler handler) {
            mContext = context;
            mHandler = handler;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpGet httpGet =
                    new HttpGet("https://" +
                            mHost + ":" + HttpsClientHelper.SECURE_PORT +
                            "/springServiceContacts/Contacts");
            try {
                UsernamePasswordCredentials credentials =
                        new UsernamePasswordCredentials(mUser, mPass);

                HttpClient httpClient = HttpsClientHelper.
                        getHttpClient(mContext.getResources());

                AuthScope as = new AuthScope(mHost,
                        HttpsClientHelper.SECURE_PORT);
                ((AbstractHttpClient) httpClient).getCredentialsProvider().
                        setCredentials(as, credentials);

                BasicHttpContext basicContext = new BasicHttpContext();
                BasicScheme basicAuth = new BasicScheme();
                basicContext.setAttribute("preemptive-auth", basicAuth);

                HttpResponse response =
                        httpClient.execute(httpGet, basicContext);
                response.getStatusLine();

                successfulRequest();

            } catch (IOException e) {
                failedRequest(e);
            } catch (UnrecoverableKeyException e) {
                failedRequest(e);
            } catch (CertificateException e) {
                failedRequest(e);
            } catch (NoSuchAlgorithmException e) {
                failedRequest(e);
            } catch (KeyStoreException e) {
                failedRequest(e);
            } catch (KeyManagementException e) {
                failedRequest(e);
            } finally {
                mContext.finish();
            }

            return null;
        }

        private void successfulRequest() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    toast(mContext.getString(R.string.succesful_request));
                }
            });
        }

        private void failedRequest(Exception e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    toast(mContext.getString(R.string.failed_request));
                }
            });

            if (e == null) {
                Log.d(TAG, "null exception???");
            }
            e.printStackTrace();
        }

        private void toast(String mesg) {
            Toast t = Toast.makeText(mContext, mesg, Toast.LENGTH_LONG);
            t.show();
            Log.d(TAG, mesg);
        }
    }
}
