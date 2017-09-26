package it.unibs.cloudondemand.google;

import android.Manifest;
import android.app.Notification;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveDownloadFile extends GoogleDriveConnection {

    // File to upload
    private File destinationPath;
    // Drive folder in witch file need to be uploaded
    private DriveFile driveFile;

    private static final String TAG = "GoogleDriveUpFile";

    @Override
    public void onConnected() {

        // Check if storage is readable and start upload
        if (Utils.isExternalStorageWritable()) {
            // Verify permission and after call startUploading when permission is granted
            Intent intent = PermissionRequest.getRequestPermissionIntent(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionResultCallback);
            startActivity(intent);
        }
        else {
            Toast.makeText(GoogleDriveDownloadFile.this, R.string.unable_write_storage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to read/write external storage.");
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
                Toast.makeText(GoogleDriveDownloadFile.this, R.string.requested_permission_write_storage, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Permission to read/write external storage denied");
                // Stop service
                disconnect();
            }
        }
    };

    // Called by subclasses when want to download a file
    public void downloadFile (File destinationPath, String driveIdFileToDownload) {
        this.destinationPath = destinationPath;
        this.driveFile = DriveId.decodeFromString(driveIdFileToDownload).asDriveFile();//TODO controllare esista

        // TODO Delete file if already exists

        // Start download
        DownloadFileAsyncTask downloadFileAsyncTask = new DownloadFileAsyncTask();
        downloadFileAsyncTask.execute();
    }

    private class DownloadFileAsyncTask extends AsyncTask<Void, Void, File>{

        @Override
        protected File doInBackground(Void... voids) {
            String contents = null;
            if(destinationPath.isDirectory())
                return destinationPath;
            else {
                DriveApi.DriveContentsResult driveContentsResult =
                        driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return null;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(driveContents.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    contents = builder.toString();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while reading from the stream", e);
                }

                if (contents == null)
                    return null;

                driveContents.discard(getGoogleApiClient());
                return new File(destinationPath, contents);
            }
        }

        @Override
        protected void onPostExecute(File file) {
            onFileDownloaded(file);
        }

        /*
        TODO mostrare progress bar
        private int lastValue = 0;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(lastValue != values[0]) {
                // Call abstract method
                fileProgress(values[0]);
                lastValue = values[0];
            }
        }
        */
    }

    // Called many times during the file upload.
    //public abstract void fileProgress (int percent);

    public abstract void onFileDownloaded(File file);

    public abstract void startDownloading();

    @Override
    public Notification getFinalNotification() {
        return null;
    }
}
