package com.enterpriseandroid.androidSecurity;

import com.enterpriseandroid.androidSecurity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginSuccessfulActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_successful);
		createGoBackButton();
	}
	

}
