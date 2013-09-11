package com.enterpriseandroid.androidSecurity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;



import com.enterpriseandroid.androidSecurity.R;
import com.enterpriseandroid.androidSecurity.util.AESEncryptionHelper;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AESEncryptionActivity extends BaseActivity {
	private static String DATA_FILE_NAME = "data";
	private static String KEY_FILE_NAME = "key";
	private static final String TAG = "AESEncryptionActivity";
	private EditText mText;

	//shared secret
    byte[] keyBytes = "abcdefghijklmnop".getBytes();
    byte[] ivBytes =  new byte[] {
            	0x00, 0x01, 0x02, 0x03,
            	0x04, 0x05, 0x06, 0x07,
            	0x08, 0x09, 0x0a, 0x0b,
            	0x0c, 0x0d, 0x0e, 0x0f};

	
	private AESEncryptionHelper encryptionHelper;
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		Log.i(TAG, "externalStorage available:" + mExternalStorageAvailable);
		Log.i(TAG, "externalStorage writeable:" + mExternalStorageWriteable);

		try {
			encryptionHelper = new AESEncryptionHelper(keyBytes, ivBytes);
		} catch (Exception e) {
	
		}
				
		setContentView(R.layout.aes_encryption_input);
		createGoBackButton();
		createSaveButton();
		mText = (EditText) findViewById(R.id.editText);
		mText.setText("good morning");
		initKey();
		loadData();
	}

	
	private void createSaveButton() {
		Button button = (Button) findViewById(R.id.save_button);
		button.setOnClickListener(new View.OnClickListener() {
		    @Override
			public void onClick(View v) {
		    	saveData();
		    }
		});		
	}
	
	
    @Override
    protected void onResume() {
        super.onResume();
        initKey();
        loadData();
    }	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	saveData();
    }	
	
	private void saveData() {
		try {
			Log.i(TAG, " store text: " + mText.getText());
			File file = getFileFromStorage();
			//FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] data = mText.getText().toString().getBytes();
			data = encryptData(data);
			fos.write(data);
			fos.flush();
			fos.close();
			data = decryptData(data);
			Log.i(TAG, " decrypted text: " + new String(data));
		} catch (IOException e) {
			Log.e(TAG, "save failure", e);
		}
	
	}
	
	/***
	 * If we need to support large size file, we need to encrypt and decrypt data chunk by chunk 
	 * This code assume that entire content can be read in by once. 
	 */
	private void loadData() {
		try {
			//FileInputStream fis = openFileInput(FILE_NAME);
			File file = getFileFromStorage();
			byte[] data = new byte[(int)file.length()];		
			InputStream fis = new FileInputStream(file);

			fis.read(data);
			data = decryptData(data);
			Log.i(TAG, " load text size: " + data.length);
			mText.setText(new String(data));
			Log.i(TAG, " load text: " + new String(data));
			fis.close();
		} catch (FileNotFoundException e) {		
			// when we run the app first, we do not have the file. So we do nothing for this exception
		}catch (IOException e) {			
			Log.e(TAG, "loadData failure", e);
		}
		
	}
	
	private File getFileFromStorage() {
		Log.i(TAG, "extern file dir: " + getExternalFilesDir(null));
		                           
		return new File(getExternalFilesDir(null), DATA_FILE_NAME);
	}
	
	private byte[] encryptData(byte[] data) {
		try {
			encryptionHelper = new AESEncryptionHelper(keyBytes, ivBytes);
			data = encryptionHelper.encrypt(data);
			Log.i(TAG, " encrypted data: " + bytesToHex(data));
			setText(R.id.message, "Encrypted data: " + bytesToHex(data));
		} catch (Exception e) {
			Log.e(TAG, "encryptData failure", e);
		}
		return data;
	}
	
	private byte[] decryptData(byte[] data) {
		try {
			encryptionHelper = new AESEncryptionHelper(keyBytes, ivBytes);
			data = encryptionHelper.decrypt(data);
		} catch (Exception e) {
			Log.e(TAG, "decryptData failure", e);
		}	
		return data;
	}
	
	private void loadKey() {
		try {
			FileInputStream fis = openFileInput(KEY_FILE_NAME);
			fis.read(keyBytes);
			fis.read(ivBytes);
			fis.close();
		} catch (IOException e) {			
			Log.e(TAG, "decryptData failure", e);
		}
		
	}
	
	private void saveKey() {
		try{ 
			FileOutputStream fos = openFileOutput(KEY_FILE_NAME, Context.MODE_PRIVATE);
			fos.write(keyBytes);
			fos.write(ivBytes);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Log.e(TAG, "saveKey failure", e);
		}	
	}
	
	private void initKey() {
		
		File keyFile = new File(getFilesDir (), KEY_FILE_NAME);
		if ( !keyFile.exists()) {
			Log.i(TAG, " key does not exit");
			new Random().nextBytes(keyBytes);
			new Random().nextBytes(ivBytes);
			saveKey();
		} else {
			Log.i(TAG, " load from key file");
			loadKey();
		}
	}
	
	private String bytesToHex(byte[] in) {
	    final StringBuilder builder = new StringBuilder();
	    for(byte b : in) {
	        builder.append(String.format("%02x", b));
	    }
	    return builder.toString();
	}
	
	private void setText(int id, String msg) {
        TextView tv = (TextView) this.findViewById(id);
        tv.setText(msg);
    }
     
}
