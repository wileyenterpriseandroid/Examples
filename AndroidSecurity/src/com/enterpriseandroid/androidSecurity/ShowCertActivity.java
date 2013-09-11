package com.enterpriseandroid.androidSecurity;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import android.util.Log;

public class ShowCertActivity  extends Activity implements KeyChainAliasCallback {
	private static final String TAG = "ShowCertActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KeyChain.choosePrivateKeyAlias(this, this, // Callback
				new String[] {}, // Any key types.
				null, // Any issuers.
				"localhost", // Any host
				-1, // Any port
				"tomcat");
	}	

	@Override
	public void alias(String alias) {
		X509Certificate[] certs = getCertificateChain(alias);
		final PrivateKey privateKey = getPrivateKey(alias);
		final StringBuffer sb = new StringBuffer();
		for (X509Certificate cert : certs) {
			sb.append(cert.getIssuerDN());
			sb.append("\n");
		}
		Log.i(TAG, "certs:" + new String(sb));
		Log.i(TAG, "private key:"  + privateKey);
	}

	private X509Certificate[] getCertificateChain(String alias) {
		try {
			return KeyChain.getCertificateChain(this, alias);
		} catch (KeyChainException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PrivateKey getPrivateKey(String alias) {
		try {
			return KeyChain.getPrivateKey(this, alias);
		} catch (KeyChainException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}