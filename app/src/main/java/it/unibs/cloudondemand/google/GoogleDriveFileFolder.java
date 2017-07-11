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
import java.util.ArrayList;

import it.unibs.cloudondemand.utils.Utils;

public class GoogleDriveFileFolder extends GoogleDriveFile {
    private static final String TAG = "GoogleDriveUpFolder";
    private File[] fileList;
    private int currentFile;
    private ArrayList<DriveFolder> driveFolders = new ArrayList<>();
    private int currentDriveFolder;

    private File[] subdirectories;
    private int currentSubdirectory;
    private DriveFolder intoDriveFolder;

    @Override
    public void startUploading() {
        // Initialize list of files to upload
        /*  TODO
        inizio creare tutte le cartelle su drive e metterle in un array
        array <pair> con file e indice cartella drive
         */
        File mainFolder = new File(getContent());
        fileList = mainFolder.listFiles();
        currentFile = 0;

        /* TESTING */
        currentDriveFolder = 0;
        subdirectories = Utils.getSubdirectories(mainFolder);
        currentSubdirectory = 0;
        intoDriveFolder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
        initializeArrays();
        Log.i("SADADSAASDASD", "Fatto");
        //createFolder(folder.getName());
    }

    private void initializeArrays (DriveFolder driveFolder) {
        if(currentSubdirectory == subdirectories.length) return;

        createDriveFolder(subdirectories[currentSubdirectory].getName(), intoDriveFolder);
        currentSubdirectory++;
    }


    // Send input to create a folder on drive (parent folder), retrieve it by callback (onDriveFolderCreated)
    private void createDriveFolder (String name, DriveFolder parentFolder) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setStarred(true)
                .build();

        parentFolder
                .createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(onDriveFolderCreated);
    }

    private final ResultCallback<DriveFolder.DriveFolderResult> onDriveFolderCreated = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
            // Retrieve created folder
            driveFolders.add(currentDriveFolder,driveFolderResult.getDriveFolder());
            currentDriveFolder++;
            /* TESTING */
            initializeArrays(driveFolderResult.getDriveFolder());
            // Upload the file in fileList at currentFile position (fileList[currentFile])
            //uploadFile();
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

        currentFile++;
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
                    // DA RIVEDERE
                    driveFolders.get(currentDriveFolder)
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
                Toast.makeText(GoogleDriveFileFolder.this, "File Creato", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            uploadFile();
        }
    };
}