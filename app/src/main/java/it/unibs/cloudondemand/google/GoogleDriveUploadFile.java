package it.unibs.cloudondemand.google;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveUploadFile extends GoogleDriveConnection {
    private static final String TAG = "GoogleDriveUpFile";

    private File fileToUpload;
    private DriveFolder driveFolder;

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
    final private PermissionRequest.PermissionRequestCallback permissionResultCallback = new PermissionRequest.PermissionRequestCallback() {
        @Override
        public void onPermissionResult(int isGranted) {
            if (isGranted == PermissionRequest.PERMISSION_GRANTED)
                startUploading();
            else {
                // Permission denied, show to user and close activity
                Toast.makeText(GoogleDriveUploadFile.this, R.string.requested_permission_read_storage, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Permission to read external storage denied");
                finish();
            }
        }
    };

    // Entry point subclasses (client connected, permission granted and storage readable)
    public abstract void startUploading();

    // Called by subclasses when want to upload a file
    public void uploadFile (File file, DriveFolder folder) {
        this.fileToUpload = file;
        this.driveFolder = folder;
        // Start creating new drive content and fill it in callback
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    // Called when new content on Drive was created
    // upload content
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Error while creating new file on Drive");
                return;
            }

            new UploadFileAsyncTask().execute(driveContentsResult);
        }
    };

    private class UploadFileAsyncTask extends AsyncTask<DriveApi.DriveContentsResult, Integer, DriveFolder.DriveFileResult> {

        @Override
        protected DriveFolder.DriveFileResult doInBackground(DriveApi.DriveContentsResult... driveContentsResult) {
            // Create stream based on which data need to be saved
            OutputStream outputStream = null;

            // Get content of new file
            final DriveContents driveContents = driveContentsResult[0].getDriveContents();

            final File file = fileToUpload;
            final DriveFolder folder = driveFolder;

            try {
                // Open file
                FileInputStream fileInputStream = new FileInputStream(file);
                outputStream = driveContents.getOutputStream();

                byte[] buffer = new byte[8];
                long k = 0;
                long fileLenght = file.length();
                // Write on drive content stream with buffer of 8 bytes
                while (fileInputStream.read(buffer) != -1) {
                    publishProgress((int) (100*k/fileLenght));
                    outputStream.write(buffer);
                    k+=8;
                }

            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found." + e.toString(), e.getCause());
            } catch (IOException e) {
                Log.e(TAG, "Exception while writing on driveConetents output stream." + e.toString(), e.getCause());
            } finally {
                // Close output stream
                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing streams." + e.toString(), e.getCause());
                }
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(file.getName())
                    .setStarred(true)
                    .build();

            return folder
                    .createFile(getGoogleApiClient(), changeSet, driveContents)
                    .await();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            fileProgress(values[0]);
        }

        @Override
        protected void onPostExecute(DriveFolder.DriveFileResult driveFileResult) {
            // Finished to upload file
            if (!driveFileResult.getStatus().isSuccess()) {
                Log.e(TAG, "Unable to create file on drive folder.");

                onFileUploaded(null);
            } else {
                Log.i(TAG, "File on drive created. " + driveFileResult.getDriveFile().getDriveId());

                onFileUploaded(driveFileResult.getDriveFile());
            }
        }
    }

    // Called many times during the file upload.
    public abstract void fileProgress (int percent);

    // Called when a file has been uploaded. driveFile = null when file on drive wasn't created.
    public abstract void onFileUploaded (DriveFile driveFile);
}