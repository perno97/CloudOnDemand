package it.unibs.cloudondemand.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PermissionRequest extends AppCompatActivity {
    public static final String PERMISSION_EXTRA = "permission";
    public static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
    public static final int PERMISSION_DENIED = PackageManager.PERMISSION_DENIED;

    private static final int RC_PERMISSION = 1;
    private static PermissionRequestCallback permissionCallback;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get from intent the permission to verify
        Intent data = getIntent();
        String permission = data.getStringExtra(PERMISSION_EXTRA);
        // Check and call back the result of permission
        verifyPermission(permission);
    }

    private void verifyPermission(String permission) {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            
            // Request always for permission when is not already granted
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    RC_PERMISSION);
        }
        else {
            // Permission is already granted
            permissionCallback.onPermissionResult(PERMISSION_GRANTED);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSION :
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    //Return permission
                    permissionCallback.onPermissionResult(grantResults[0]);
                }
                else {
                    permissionCallback.onPermissionResult(PERMISSION_DENIED);
                }
                break;
        }
        finish();
    }

    // Constuctor of intent
    public static Intent getRequestPermissionIntent(Context context, String permission, PermissionRequestCallback permissionResultCallback) {
        permissionCallback = permissionResultCallback;
        Intent intent = new Intent(context, PermissionRequest.class);
        intent.putExtra(PERMISSION_EXTRA, permission);
        return intent;
    }

    public interface PermissionRequestCallback {
        void onPermissionResult(int isGranted);
    }
}
