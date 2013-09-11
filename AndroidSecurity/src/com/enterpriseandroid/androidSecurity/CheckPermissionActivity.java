package com.enterpriseandroid.androidSecurity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Checks that the caller has permission to access a delegated
 * URI.
 */
public class CheckPermissionActivity extends Activity {
    public static final String URI_PARAMETER = "";

    private static final String LOG_TAG = "checkPermissions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent activityIntent = getIntent();
        String uriParameter =
                activityIntent.getStringExtra(URI_PARAMETER);

        Uri uriParam = Uri.parse(uriParameter);
        int checkCallingUriPermissions =
                checkCallingUriPermission(uriParam,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
        checkGranted(checkCallingUriPermissions,
                "Uri: " + uriParam.toString());

        String cameraPermission = "android.permission.CAMERA";
        int checkCameraPermission = checkCallingPermission(cameraPermission);
        checkGranted(checkCameraPermission, cameraPermission);

        String internetPermission = "android.permission.INTERNET";
        int checkUriPermission = checkCallingPermission(internetPermission);
        checkGranted(checkUriPermission, internetPermission);
    }

    private void checkGranted(int checkPermission, String mesg) {
        if (checkPermission ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Permission Denied: " + mesg);
        }  else if (checkPermission ==
                PackageManager.PERMISSION_DENIED) {
            Log.d(LOG_TAG, "Permission Denied: " + mesg);
        }
    }
}
