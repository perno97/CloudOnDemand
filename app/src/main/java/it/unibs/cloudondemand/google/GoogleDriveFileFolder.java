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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class GoogleDriveFileFolder extends GoogleDriveFile {
    private static final String TAG = "GoogleDriveUpFolder";
    private File[] fileList;
    private HashMap<File,File> fileTree = new HashMap<>();
    private HashMap<File,File> folderTree = new HashMap<>();

    @Override
    public void startUploading() {
        // Initialize list of files to upload
        /*  TODO
        array cartelle da creare
        mappa file da caricare e cartella a cui appartiene
        array associato delle rispettive cartelle su drive
         */
        File folder = new File(getContent());
        initiateFileTree(folder);

        //Creates root folder
        createFolder(folder.getName(), Drive.DriveApi.getRootFolder(getGoogleApiClient()));
    }

    private void initiateFileTree(File folder) {
        File[] currentFileList = folder.listFiles();
        if(currentFileList.length == 0){
            Toast.makeText(this, "Nessun file nella cartella", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < currentFileList.length; i++) {
            if (currentFileList[i].isFile()) {
                fileTree.put(currentFileList[i], folder);
            } else {
                folderTree.put(currentFileList[i],folder);
                initiateFileTree(currentFileList[i]);
            }
        }
    }

    // Send input to create a folder on drive, retrieve it by callback (onDriveFolderCreated)
    private void createFolder (String name, DriveFolder parentFolder) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setStarred(true)
                .build();

        parentFolder.createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(onDriveFolderCreated);
    }

    private final ResultCallback<DriveFolder.DriveFolderResult> onDriveFolderCreated = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
            // Retrieve created folder
            //driveFolder = driveFolderResult.getDriveFolder();
            // Upload the file in fileList at currentFile position (fileList[currentFile])
            //uploadFile();
            createFolder();
        }
    };

    // Upload the file in fileList at currentFile position (fileList[currentFile])
    private void uploadFile() {
        // Finished to upload files in this folder
        if(currentFile == fileList.length) return;
        // Doesn't casually try to upload directory
        if(fileList[currentFile].isDirectory()) return;

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
                        FileInputStream fileInputStream = new FileInputStream(fileList[currentFile]);
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
            uploadFile();
        }
    };
}
