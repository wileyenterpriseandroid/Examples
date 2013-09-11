package com.enterpriseandroid.androidSecurity;

import com.enterpriseandroid.androidSecurity.R;
import com.enterpriseandroid.androidSecurity.util.AESEncryptionHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_action_activity);

        ListView selectActionListView =
                (ListView) findViewById(R.id.selectionActionList);

        selectActionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = (String) ((TextView)view).getText();
                String symmetricEncryption = getString(R.string.symmetric_encryption);
                String passwordHash = getString(R.string.password_hash);
                String secureConnection = getString(R.string.secure_connection);
                String accountManager = getString(R.string.account_manager);
                String installCertificate = getString(R.string.install_certificate);

                if (symmetricEncryption.equals(text)) {
                    start(MainActivity.this, AESEncryptionActivity.class);
                } else if (passwordHash.equals(text)) {
                    start(MainActivity.this, LoginActivity.class);
                } else if (secureConnection.equals(text)) {
                    start(MainActivity.this, SecureConnectionActivity.class);
                } else if (accountManager.equals(text)) {
                    start(MainActivity.this, AuthTokenActivity.class);
                } else if (installCertificate.equals(text)) {
                    start(MainActivity.this, InstallCertActivity.class);
                }
            }
        });

        selectActionListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getSelectedItem();
                Log.d("", item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("", "nothing");
            }
        });
    }

    public static void start(Activity activity, Class activityClass) {
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.setClass(activity, activityClass);
        activity.startActivity(i);
    }
}
