package com.enterpriseandroid.androidSecurity;

import com.enterpriseandroid.androidSecurity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	protected void createGoBackButton() {
		Button button = (Button) findViewById(R.id.back_button);
		button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	goBack();
		    }
		});		
	}
	
	protected void goBack() {		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);					
	}
	
}
