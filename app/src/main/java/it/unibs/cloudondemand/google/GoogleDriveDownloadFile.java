package it.unibs.cloudondemand.google;

import android.Manifest;
import android.app.Notification;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.databaseManager.FileListDbHelper;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveDownloadFile extends GoogleDriveConnection {

    // File to upload
    private File destinationPath;
    // Drive folder in witch file need to be uploaded
    private DriveFile driveFile;

    private static final String TAG = "GoogleDriveUpFile";

    // Database that contains files and folders already uploaded
    private SQLiteDatabase database;
    private FileListDbHelper mDbHelper;

    @Override
    public void onConnected() {
        // Initialize attributes
        mDbHelper = new FileListDbHelper(getApplicationContext());
        database = mDbHelper.getReadableDatabase();

        // Check if storage is readable and start upload
        if (Utils.isExternalStorageReadable()) {
            // Verify permission and after call startUploading when permission is granted
            Intent intent = PermissionRequest.getRequestPermissionIntent(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionResultCallback);
            startActivity(intent);
        }
        else {
            Toast.makeText(GoogleDriveDownloadFile.this, R.string.unable_read_storage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to read external storage.");
        }
    }

    // Called when user chose to grant permission
    final private PermissionRequest.PermissionRequestCallback permissionResultCallback = new PermissionRequest.PermissionRequestCallback() {
        @Override
        public void onPermissionResult(int isGranted) {
            if (isGranted == PermissionRequest.PERMISSION_GRANTED)
                startDownloading();
            else {
                // Permission denied, show to user and close activity
                Toast.makeText(GoogleDriveDownloadFile.this, R.string.requested_permission_read_storage, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Permission to read external storage denied");
                // Stop service
                disconnect();
            }
        }
    };

    // Called by subclasses when want to upload a file
    public void downloadFile (File destinationPath, String driveIdFileToDownload) {
        this.destinationPath = destinationPath;
        DriveFile fileToDownload = DriveId.decodeFromString(driveIdFileToDownload).asDriveFile();
        this.driveFile = fileToDownload;

        // TODO Delete file if already exists (pin file?)

        // Start download
        downloadFileAsyncTask = new GoogleDriveUploadFile.DownloadFileAsyncTask();
        downloadFileAsyncTask.execute();
    }

    private class DownloadFileAsyncTask extends AsyncTask<Void, Integer, >{

        @Override
        protected void doInBackground(Void... voids) {
            //TODO ottenere file da drive
        }

        private int lastValue = 0;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(lastValue != values[0]) {
                // Call abstract method
                fileProgress(values[0]);
                lastValue = values[0];
            }
        }

        @Override
        protected void onPostExecute()
    }

    // Called many times during the file upload.
    public abstract void fileProgress (int percent);

    public abstract void startDownloading();
    @Override
    public Notification getFinalNotification() {
        return null;
    }
}
