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
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveDownloadFile extends GoogleDriveConnection {

    // File to upload
    private File destinationPath;
    // Drive folder in witch file need to be uploaded
    private DriveFile driveFile;

    private static final String TAG = "GoogleDriveDownFile";
    public static final int CONTENT_FOLDER = 1;
    public static final int CONTENT_FILE = 0;

    public static final String DRIVEID_EXTRA = "drive-id";

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

    private class DownloadFileAsyncTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if(destinationPath.isDirectory())
                return null;
            else {
                // Open file
                DriveApi.DriveContentsResult driveContentsResult =
                        driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return null;
                }
                // Retrieve file dimension
                DriveResource.MetadataResult metadataResult = driveFile.getMetadata(getGoogleApiClient()).await();
                Metadata metadata = metadataResult.getMetadata();
                long fileLength = metadata.getFileSize();

                FileOutputStream outputStream = null;
                DriveContents driveContents = driveContentsResult.getDriveContents();

                try {
                    InputStream reader = driveContents.getInputStream();
                    outputStream = new FileOutputStream(destinationPath);

                    long k = 0;
                    byte[] buffer = new byte[8];
                    // Write on file stream with buffer of 8 bytes
                    while (reader.read(buffer) != -1) {
                        publishProgress((int) (100*k/fileLength));
                        //Check if last data is < of 1 byte
                        if(k+8 > fileLength) {
                            byte[] buffer2 = new byte[(int) (fileLength - k)];
                            for (int i = 0; i < buffer2.length; i++) {
                                buffer2[i] = buffer[i];
                            }
                            outputStream.write(buffer2);
                        }
                        else
                            outputStream.write(buffer);
                        k += 8;
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

        private int lastValue = 0;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(lastValue != values[0]) {
                // Call abstract method
                lastValue = values[0];
                fileProgress(lastValue);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onFileDownloaded();
        }
    }

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

    public static Intent getIntent(Context context, int contentType, String destinationPath, String driveId){
        Intent intent = null;
        switch (contentType){
            case CONTENT_FILE:
                intent = new Intent(context, GoogleDriveDownloadFileSingle.class);
                break;
            case CONTENT_FOLDER:
                intent = new Intent(context, GoogleDriveDownloadFileFolder.class);
                break;
        }
        intent.putExtra(DRIVEID_EXTRA, driveId);
        intent.putExtra(LoginActivity.CONTENT_EXTRA, destinationPath);
        return intent;
    }
}
