package com.enterpriseandroid.androidSecurity.util;

import org.apache.commons.codec.digest.DigestUtils;

import android.util.Log;

public class PasswordHelper {
	private static final String TAG = "PasswordHelper";
	
	private String passwordHash;
	
	public PasswordHelper(String username, String password) {
		Log.i(TAG, "*****username:" + username + " password:" + password);
		String salt = generateSalt(username);
		Log.i(TAG, "*****salt:" + salt );
		this.passwordHash = DigestUtils.shaHex(password + salt);
		Log.i(TAG, " hash:" + passwordHash);
	}
	
	private String generateSalt(String s) {
		StringBuffer buf = new StringBuffer();
		for( int i=0; i< s.length(); i++) {
			if ( i % 2 ==0 ) {
				buf.append(s.charAt(i));
			}
		}
		return buf.toString();
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}

	public boolean validatePassword(String username, String password) {
		Log.i(TAG, "username:" + username + " password:" + password);
		String salt = generateSalt(username);
		Log.i(TAG, " salt:" + salt );
		Log.i(TAG, " hash:" + passwordHash);

		Log.i(TAG, "validate hash:" + DigestUtils.shaHex(password + salt));
		return passwordHash.equals(DigestUtils.shaHex(password + salt));
	}
}
