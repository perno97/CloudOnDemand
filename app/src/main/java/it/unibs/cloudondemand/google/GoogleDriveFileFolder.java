package it.unibs.cloudondemand.google;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class GoogleDriveFileFolder extends GoogleDriveFile {
    private static final String TAG = "GoogleDriveUpFolder";
    private GoogleDriveCustomFile folder;

    private File currentFile;
    private DriveFolder currentDriveFolder;

    @Override
    public void startUploading() {
        // Initialize list of files to upload
        File mainFolder = new File(getContent());
        folder = new GoogleDriveCustomFile(null, mainFolder);
        // Create base folder on Drive
        createDriveFolder(null, mainFolder.getName());
    }




    // Send input to create a folder on drive (parent folder // root if parentFolder = null), retrieve it by callback (onDriveFolderCreated)
    private void createDriveFolder (DriveFolder parentFolder, String name) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setStarred(true)
                .build();

        if (parentFolder == null)
            parentFolder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        parentFolder
                .createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(onDriveFolderCreated);
    }

    private final ResultCallback<DriveFolder.DriveFolderResult> onDriveFolderCreated = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
            // Retrieve created folder
            folder.setDriveFolder(driveFolderResult.getDriveFolder());
            // Upload the file in fileList at currentFile position (fileList[currentFile])
            uploadFile();
        }
    };


    // Upload the file in fileList at currentFile position (fileList[currentFile])
    private void uploadFile() {
        if (!folder.hasNextFile()) {
            if(folder.hasNextSubFolder())
                createDriveFolder(folder.getCurrentMyFile().getThisDriveFolder(), folder.nextSubFolder().getThisFolder().getName());
            else {  // Go to parent folder next subdirectory
                GoogleDriveCustomFile subFolder = folder.nextParentSubFolder();
                if(subFolder == null)
                    Toast.makeText(this, "FINITO", Toast.LENGTH_SHORT).show();
                else
                    createDriveFolder(subFolder.getParentFolder().getThisDriveFolder(), subFolder.getThisFolder().getName());
            }

            return;
        }

        currentDriveFolder = folder.getCurrentDriveFolder();
        currentFile = folder.nextFile();

        // Start creating new drive content and fill it in callback with fileList[currentFile]
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
                        FileInputStream fileInputStream = new FileInputStream(currentFile);
                        outputStream = driveContents.getOutputStream();
                        // Write on drive content stream with buffer of 8 bytes
                        byte[] buffer = new byte[8];
                        while (fileInputStream.read(buffer) != -1) {
                            outputStream.write(buffer);
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
                            .setTitle(currentFile.getName())
                            .setStarred(true)
                            .build();

                    currentDriveFolder
                            .createFile(getGoogleApiClient(), changeSet, driveContents)
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
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            uploadFile();
        }
    };
}