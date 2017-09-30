package it.unibs.cloudondemand.google;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveDownloadFile extends GoogleDriveConnection {

    // File to upload
    private File destinationPath;
    // Drive folder in witch file need to be uploaded
    private DriveFile driveFile;

    private static final String TAG = "GoogleDriveDownFile";

    @Override
    public void onConnected() {

        // Check if storage is readable and start download
        if (Utils.isExternalStorageWritable()) {
            // Verify permission and after call startDownloading when permission is granted
            Intent intent = PermissionRequest.getIntent(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionResultCallback);
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
        this.driveFile = DriveId.decodeFromString(driveIdFileToDownload).asDriveFile();

        // TODO Delete file if already exists

        // Start download
        DownloadFileAsyncTask downloadFileAsyncTask = new DownloadFileAsyncTask();
        downloadFileAsyncTask.execute();
    }

    private class DownloadFileAsyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if(destinationPath.isDirectory())
                return null;
            else {
                DriveApi.DriveContentsResult driveContentsResult =
                        driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, downloadProgressListener).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return null;
                }

                FileOutputStream outputStream = null;
                DriveContents driveContents = driveContentsResult.getDriveContents();

                try {
                    InputStream reader = driveContents.getInputStream();
                    outputStream = new FileOutputStream(destinationPath);

                    byte[] buffer = new byte[8];
                    // Write on file stream with buffer of 8 bytes
                    while (reader.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }
                } catch (FileNotFoundException e){
                    Log.e(TAG, "File not found." + e.toString(), e.getCause());
                } catch (IOException e){
                    Log.e(TAG, "Exception while writing on driveConetents output stream." + e.toString(), e.getCause());
                } finally {
                    // Close output stream
                    try {
                        if (outputStream != null)
                            outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while closing streams." + e.toString(), e.getCause());
                    }
                    //Discard changes in drive contents and close
                    driveContents.discard(getGoogleApiClient());
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onFileDownloaded();
        }
    }

    private DriveFile.DownloadProgressListener downloadProgressListener = new DriveFile.DownloadProgressListener() {
        @Override
        public void onProgress(long byteDownloaded, long byteExpeted) {
            fileProgress((int) ( byteDownloaded/byteExpeted * 100));
        }
    };

    /**
     * Used by subclasses to retrieve the percent value of progress of the download.
     * @param percent Progress value.
     */
    public abstract void fileProgress (int percent);

    /**
     * Used by subclasses to know when a file was downloaded.
     * @param file File downloaded. //TODO implement this
     */
    public abstract void onFileDownloaded (File file);
    public abstract void onFileDownloaded();

    public abstract void startDownloading();
}
