package it.unibs.cloudondemand.google;

import android.Manifest;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.PermissionResultCallback;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveUploadFile extends GoogleDriveConnection {
    private static final String TAG = "GoogleDriveUpFile";

    @Override
    public void onConnected() {
        // Check if storage is readable and start upload
        if (Utils.isExternalStorageReadable()) {
            // Verify permission and after create new drive content when permission is granted
            Intent intent = PermissionRequest.getRequestPermissionIntent(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionResultCallback);
            startActivity(intent);
        }
        else {
            Toast.makeText(GoogleDriveUploadFile.this, R.string.unable_read_storage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to read external storage.");
        }
    }

    // Called when user chose to grant permission
    final private PermissionResultCallback permissionResultCallback = new PermissionResultCallback() {
        @Override
        public void onPermissionResult(int isGranted) {
            if (isGranted == PermissionRequest.PERMISSION_GRANTED)
                startUploading();
            else {
                // Permission denied, show to user and close activity
                Toast.makeText(GoogleDriveUploadFile.this, R.string.permission_read_storage, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Permission to read external storage denied");
                finish();
            }
        }
    };

    // Entry point subclasses (client connected, permission granted and storage readable)
    public abstract void startUploading();
}