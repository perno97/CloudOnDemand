package it.unibs.cloudondemand.google;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import it.unibs.cloudondemand.R;

public class GoogleDriveFile extends GoogleDrive {
    private static final String TAG = "GoogleDriveUpFile";

    private static final int PERMISSION_READ_STORAGE = 1;

    @Override
    public void onConnected() {
        verifyPermission();
    }

    private void verifyPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSION_READ_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_STORAGE :
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // Check if storage is readable and start upload
                    if(isExternalStorageReadable())
                        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                                .setResultCallback(driveContentsCallback);
                    else {
                        Toast.makeText(this, R.string.unable_read_storage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to read external storage.");
                    }

                } else {
                    //Permission denied, show to user and close activity
                    Toast.makeText(this, R.string.permission_read_storage, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permission to read external storage denied");
                    finish();
                }
                break;
        }
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //Called when new content on Drive was created
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Error while creating new file on Drive");
                return;
            }

            //Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();

            //Upload file or string into drive file
            new Thread() {
                @Override
                public void run() {
                    //Create stream based on which data need to be saved
                    OutputStream outputStream=null;

                    try {
                        //Open file
                        //FileInputStream fileInputStream = new FileInputStream(file);
                        FileInputStream fileInputStream = new FileInputStream("/storage/emulated/0/open_gapps_log.txt");
                        outputStream = driveContents.getOutputStream();
                        int buffer;
                        while((buffer = fileInputStream.read()) != -1) {
                            outputStream.write(buffer);
                        }
                    }
                    catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found." + e.toString() , e.getCause());
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Exception while writing on driveConetents output stream." + e.toString(), e.getCause());
                    } finally {
                        try {
                            if(outputStream!=null)
                                outputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while closing streams." + e.toString(), e.getCause());
                        }
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("prova.txt")
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .build();

                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
            }.start();
        }
    };


    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if(!driveFileResult.getStatus().isSuccess()) {
                Toast.makeText(GoogleDriveFile.this, "File non Creato", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "File not created");
            }
            else {
                Toast.makeText(GoogleDriveFile.this, "File Creato", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            getGoogleApiClient().disconnect();
        }
    };
}
