package com.enterpriseandroid.syncadaptercontacts.svc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.util.Log;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;
import com.enterpriseandroid.syncadaptercontacts.ContactsApplication;
import com.enterpriseandroid.syncadaptercontacts.R;
import com.enterpriseandroid.syncadaptercontacts.sync.AccountMgr;
import com.enterpriseandroid.syncadaptercontacts.sync.InstallationId;
import com.google.gson.Gson;


public class RESTService {
    private static final String TAG = "REST";

    public static final String KEY_SYNC_MARKER = "SyncUtil.LAST_SYNC";

    // odd that these aren't defined elsewhere...
    public static enum HttpMethod { GET, PUT, POST, DELETE }

    public static final String HEADER_ENCODING = "Accept-Encoding";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String MIME_JSON = "application/json;charset=UTF-8";
    public static final String ENCODING_NONE = "identity";

    public static final int HTTP_READ_TIMEOUT = 30 * 1000; // ms
    public static final int HTTP_CONN_TIMEOUT = 30 * 1000; // ms


    private static class RestResponse {
        private final int code;
        private final String body;

        public RestResponse(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public int getCode() { return code; }

        public String getBody() { return body; }

        @Override
        public String toString() { return "(" + getCode() + "): " + getBody(); }
    }


    private final ContactsApplication ctxt;
    private final String userAgent;
    private final InstallationId id;

    public RESTService(ContactsApplication ctxt) {
        this.ctxt = ctxt;
        this.id = new InstallationId(ctxt);
        userAgent = ctxt.getString(R.string.app_name) + "/" + ctxt.getString(R.string.app_version);
    }

    public void sync(Account account, String auth) {
        AccountManager mgr = AccountManager.get(ctxt);
        String uri = ctxt.getServerUri() + "/sync";
        String iid = id.getInstallationId();

        // Deleting the account loses last update date.
        // Last sync is properly a property of the DB
        // and should go into a table along with the transaction id
        long lastUpdate = 0;
        String ts = mgr.getUserData(account, KEY_SYNC_MARKER);
        if (null != ts) {
            try { lastUpdate = Long.parseLong(ts); }
            catch (NumberFormatException e) { }
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sync started: " + iid + "=>" + uri + " @" + lastUpdate);
        }

        lastUpdate = syncContacts(uri, account, auth, iid, lastUpdate);

        if (0 < lastUpdate) {
            mgr.setUserData(account, KEY_SYNC_MARKER, String.valueOf(lastUpdate));
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sync complete @" + AccountMgr.acctStr(account) + "#" + auth + ": " + lastUpdate);
        }
    }

    private long syncContacts(String uri, Account account, String auth, String clientId, long lastUpdate) {
        ContentResolver cr = ctxt.getContentResolver();
        String xactId = UUID.randomUUID().toString();
        SyncUtil syncUtil = new SyncUtil();
        Gson gson = new Gson();

        long t = -1;
        try {
            syncUtil.beginUpdate(cr, xactId);

            List<Map<String, Object>> localUpdates = syncUtil.getLocalUpdates(cr, xactId);

            String payload = gson.toJson(syncUtil.createSyncRequest(
                    localUpdates,
                    account.name,
                    auth,
                    clientId,
                    lastUpdate));

            if (BuildConfig.DEBUG) { Log.d(TAG, "sync req: " + payload); }
            RestResponse response = sendRequest(HttpMethod.POST, uri, payload);
            String body = response.getBody();
            if (BuildConfig.DEBUG) { Log.d(TAG, "sync result: " + body); }
            if ( response.getCode() == HttpStatus.SC_OK) {
            	Map<String, Object> syncResultMap = gson.fromJson(body, SyncUtil.MAP_TYPE);
            	t = syncUtil.processUpdates(cr, syncResultMap);
            	syncUtil.finishUpdate(cr, localUpdates);
            	syncUtil.endUpdate(cr, xactId);
            }

        }
        catch (IOException e) { Log.w(TAG, "IO Exception: ", e); }

        return t;
    }


    private RestResponse sendRequest(HttpMethod method, String uri, String payload)
        throws IOException
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sending " + method + " @" + uri + ": " + payload);
        }
       
        HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        Exception e;
        try { return sendHttpRequest(conn, method, payload); }
        catch (IOException ioe) { e = ioe; }
        catch (NullPointerException npe) { e = npe; }
        finally {
            if (null != conn) {
                try { conn.disconnect(); } catch (Exception ex) { }
            }
        }

        Log.w(TAG, "request failed", e);
        throw new IOException("connection failed", e);
    }

    private RestResponse sendHttpRequest(HttpURLConnection conn, HttpMethod method, String payload)
            throws ProtocolException, IOException, UnsupportedEncodingException
    {
        conn.setReadTimeout(HTTP_READ_TIMEOUT);
        conn.setConnectTimeout(HTTP_CONN_TIMEOUT);
        conn.setRequestMethod(method.toString());
        conn.setRequestProperty(HEADER_USER_AGENT, userAgent);
        conn.setRequestProperty(HEADER_ENCODING, ENCODING_NONE);

        conn.setRequestProperty(HEADER_ACCEPT, MIME_JSON);
        conn.setDoInput(true);
        if (null != payload) {
            conn.setRequestProperty(HEADER_CONTENT_TYPE, MIME_JSON);
            conn.setFixedLengthStreamingMode(payload.length());
            conn.setDoOutput(true);

            conn.connect();
            Writer out = new OutputStreamWriter(
                    new BufferedOutputStream(conn.getOutputStream()),
                    "UTF-8");
            out.write(payload);
            out.flush();
        }
      
       int responseCode = conn.getResponseCode();
       String body = null;
       if (responseCode == HttpStatus.SC_OK) {
    	   body =  readResponse(conn.getInputStream());
       }
        Log.i(TAG, " response code: " + responseCode);
        Log.i(TAG, " response: body " + body);
        return new RestResponse(
        	   responseCode,
               body);
    }

    private String readResponse(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));

        StringBuilder buf = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null) { buf.append(line); }
         
        String body = buf.toString();
        

        if (BuildConfig.DEBUG) { Log.d(TAG, "response body: " + body); }

        return body;
    }
}
