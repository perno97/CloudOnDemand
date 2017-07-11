package it.unibs.cloudondemand.google;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import it.unibs.cloudondemand.R;

public class GoogleDriveFileFolder extends GoogleDriveFile {
    private static final String TAG = "GoogleDriveUpFolder";
    private File[] fileList;
    private int currentFile;
    private DriveFolder driveFolder;
    @Override
    public void startUploading() {
        File folder = new File(getContent());
        fileList = folder.listFiles();
        currentFile = 0;

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folder.getName())
                .setStarred(true)
                .build();

        Drive.DriveApi.getRootFolder(getGoogleApiClient())
                .createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(onDriveFolderCreated);
    }

    private final ResultCallback<DriveFolder.DriveFolderResult> onDriveFolderCreated = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
            driveFolder = driveFolderResult.getDriveFolder();
            uploadFolder();
        }
    };

    private void uploadFolder() {
        if(currentFile == fileList.length) return;
        if(fileList[currentFile].isDirectory()) return;

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

            // Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();

            // Upload file into drive content
            new Thread() {
                @Override
                public void run() {
                    // Create stream based on which data need to be saved
                    OutputStream outputStream = null;

                    try {
                        // Open file
                        FileInputStream fileInputStream = new FileInputStream(fileList[currentFile]);
                        outputStream = driveContents.getOutputStream();
                        // Write on drive content stream with buffer of 8 bytes
                        byte[] buffer = new byte[8];
                        long k = 0;
                        while (fileInputStream.read(buffer) != -1) {
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
                            .setTitle(fileList[currentFile].getName())
                            .setStarred(true)
                            .build();

                    driveFolder.createFile(getGoogleApiClient(), changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
            }.start();
        }
    };

    // Called when file on drive was fully created
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if (!driveFileResult.getStatus().isSuccess()) {
                Toast.makeText(GoogleDriveFileFolder.this, "File non Creato", Toast.LENGTH_SHORT).show();   //TODO FARE QUALCOSA
                Log.e(TAG, "File not created");
            } else {
                Toast.makeText(GoogleDriveFileFolder.this, "File Creato", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            currentFile++;
            uploadFolder();
        }
    };
}
