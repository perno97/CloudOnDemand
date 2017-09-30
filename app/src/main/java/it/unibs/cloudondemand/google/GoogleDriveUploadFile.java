package it.unibs.cloudondemand.google;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.databaseManager.FileListContract.FileList;
import it.unibs.cloudondemand.databaseManager.FileListDbHelper;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveUploadFile extends GoogleDriveConnection {
    private static final String TAG = "GoogleDriveUpFile";

    // File to upload
    private File fileToUpload;
    // Drive folder where file must be uploaded
    private DriveFolder driveFolder;
    // File's parent identificator
    private int parentId;


    private UploadFileAsyncTask uploadFileAsyncTask;

    @Override
    public void onConnected() {

        // Check if storage is readable and start upload
        if (Utils.isExternalStorageReadable()) {
            // Verify permission and after call startUploading when permission is granted
            Intent intent = PermissionRequest.getIntent(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionResultCallback);
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
                // Stop service
                disconnect();
            }
        }
    };

    // Entry point subclasses (client connected, permission granted and storage readable)
    public abstract void startUploading();

    // Called by subclasses when want to upload a file
    public void uploadFile (File file, DriveFolder folder, int parentId) {
        this.fileToUpload = file;
        this.driveFolder = folder;
        this.parentId = parentId;

        // Delete file on drive if already exists
        GoogleDriveUtil.deleteFileIfExists(getApplicationContext(),fileToUpload, getGoogleApiClient());

        // Start to upload
        uploadFileAsyncTask = new UploadFileAsyncTask();
        uploadFileAsyncTask.execute();
    }

    /**
     * Upload the file in another thread
     */
    private class UploadFileAsyncTask extends AsyncTask<Void, Integer, DriveFolder.DriveFileResult> {

        @Override
        protected DriveFolder.DriveFileResult doInBackground(Void... voids) {
            // Get new drive content
            DriveApi.DriveContentsResult driveContentsResult= Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();

            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Error while creating new file on Drive");
                return null;
            }

            // Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();


            OutputStream outputStream = null;
            try {
                // Open file
                FileInputStream fileInputStream = new FileInputStream(fileToUpload);
                outputStream = driveContents.getOutputStream();

                byte[] buffer = new byte[8];
                long k = 0;
                long fileLength = fileToUpload.length();
                // Write on drive content stream with buffer of 8 bytes
                while (fileInputStream.read(buffer) != -1) {
                    publishProgress((int) (100*k/fileLength));
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
                    .setTitle(fileToUpload.getName())
                    .setStarred(true)
                    .build();

            return driveFolder
                    .createFile(getGoogleApiClient(), changeSet, driveContents)
                    .await();
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
        protected void onPostExecute(DriveFolder.DriveFileResult driveFileResult) {
            // Finished to upload file
            if (!driveFileResult.getStatus().isSuccess()) {
                Log.e(TAG, "Unable to create file on drive folder. " + driveFileResult.getStatus().toString());

                onFileUploaded(null);
            } else {
                Log.i(TAG, "File on drive created. " + driveFileResult.getDriveFile().getDriveId());

                GoogleDriveUtil.addFileToDatabase(getApplicationContext(), driveFileResult.getDriveFile().getDriveId().encodeToString(), fileToUpload.getPath(), parentId);
                onFileUploaded(driveFileResult.getDriveFile());
            }
        }

        @Override
        protected void onCancelled(DriveFolder.DriveFileResult driveFileResult) {
            Log.i(TAG, "User stop to upload files");
        }
    }

    @Override
    public void onDestroy() {
        // Stop async task if running when user stop the service
        if(uploadFileAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            uploadFileAsyncTask.cancel(true);

        super.onDestroy();
    }

    // Called many times during the file upload.
    /**
     * Used by subclasses to retrieve the percent value of progress of  the upload.
     * @param percent Progress value.
     */
    public abstract void fileProgress (int percent);

    // Called when a file has been uploaded. driveFile = null when file on drive wasn't created.

    /**
     * Used by subclasses to know when a file was uploaded.
     * @param driveFile Drive file uploaded.
     */
    public abstract void onFileUploaded (DriveFile driveFile);
}