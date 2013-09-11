package com.enterpriseandroid.androidSecurity;

import com.enterpriseandroid.androidSecurity.R;
import com.enterpriseandroid.androidSecurity.util.PasswordHelper;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
	private EditText mPassword;
	private EditText mUsername;
	private PasswordHelper mPasswordHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		Button button = (Button) findViewById(R.id.ok_button);
		mPassword = (EditText) findViewById(R.id.password_edit);
		mUsername = (EditText) findViewById(R.id.username_edit);
		mPasswordHelper = new PasswordHelper("android", "Password");
		button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	login();
		    }
		});
	}

	private void login() {
    	Log.i(TAG, " username: " + mUsername.getText() + " password: " + mPassword.getText());
		boolean b = mPasswordHelper.validatePassword(mUsername.getText().toString(), 
				mPassword.getText().toString());
		if ( b ) {
			Intent intent = new Intent(this, LoginSuccessfulActivity.class);
			startActivity(intent);			
		}
		Log.i(TAG, " result: " + b);
	}
}
