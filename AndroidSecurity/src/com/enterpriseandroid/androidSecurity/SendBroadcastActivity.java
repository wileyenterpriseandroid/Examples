package com.enterpriseandroid.androidSecurity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SendBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent secureIntent = new Intent("RECEIVE_BROADCAST");
        String receivePermission =
                "com.enterpriseandroid.androidSecurity.RECEIVE_BROADCAST";
        sendBroadcast(secureIntent, receivePermission);
    }
}
