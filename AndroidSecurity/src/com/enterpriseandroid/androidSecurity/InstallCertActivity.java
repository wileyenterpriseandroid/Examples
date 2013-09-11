package com.enterpriseandroid.androidSecurity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.enterpriseandroid.androidSecurity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChain;
import android.util.Log;

/**
 * Installs a certificate into the system keychain using an Android
 * defined intent.
 */
public class InstallCertActivity extends Activity  {
	private static final String TAG = "ShowCertActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = KeyChain.createInstallIntent();
		byte[] p12;
		try {
			p12 = getkeystoreData();
			intent.putExtra(KeyChain.EXTRA_PKCS12, p12);
			startActivity(intent);
			Log.i(TAG, "done");
		} catch (IOException e) {
			Log.i(TAG, e.getMessage());
		}
	}

	private byte[] getkeystoreData() throws IOException {
		 InputStream is = getResources().openRawResource(R.raw.keystore2);
		 return readFully(is);
	}
	
	public byte[] readFully(InputStream input) throws IOException {
	    byte[] buffer = new byte[8192];
	    int bytesRead;
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    while ((bytesRead = input.read(buffer)) != -1)
	    {
	        output.write(buffer, 0, bytesRead);
	    }
	    return output.toByteArray();
	}
}
