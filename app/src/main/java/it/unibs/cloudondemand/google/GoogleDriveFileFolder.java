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
    private MyFile folder;

    private File currenFile;
    private DriveFolder currentDriveFolder;

    @Override
    public void startUploading() {
        // Initialize list of files to upload
        /*  TODO
        inizio creare tutte le cartelle su drive e metterle in un array
        array <pair> con file e indice cartella drive
         */
        File mainFolder = new File(getContent());
        folder = new MyFile(null, mainFolder);
        // Create base folder on Drive
        createDriveFolder(mainFolder.getName(), null);
    }




    // Send input to create a folder on drive (parent folder // root if parentFolder = null), retrieve it by callback (onDriveFolderCreated)
    private void createDriveFolder (String name, DriveFolder parentFolder) {
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
                createDriveFolder(folder.nextSubFolder().getThisFolder().getName(), folder.getThisDriveFolder());
            else {  // Go to parent folder next subdirectory
                MyFile subFolder = folder.nextParentSubFolder();
                if(subFolder == null)
                    Toast.makeText(this, "FINITO", Toast.LENGTH_SHORT).show();
                else
                    createDriveFolder(subFolder.getThisFolder().getName(), subFolder.getParentFolder().getThisDriveFolder());
            }

            return;
        }

        currentDriveFolder = folder.getCurrentDriveFolder();
        currenFile = folder.nextFile();
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
                        FileInputStream fileInputStream = new FileInputStream(currenFile);
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
                            .setTitle(currenFile.getName())
                            .setStarred(true)
                            .build();
                    // DA RIVEDERE
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
                Toast.makeText(GoogleDriveFileFolder.this, "File Creato", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            uploadFile();
        }
    };

    private class MyFile {
        private MyFile parentFolder;

        private File thisFolder;
        private DriveFolder thisDriveFolder;

        private MyFile[] subFolders;
        private int currentSubFolder = 0;

        private File[] files;
        private int currentFile = 0;

        private MyFile(MyFile parentFolder, File thisFolder) {
            this.parentFolder = parentFolder;
            this.thisFolder = thisFolder;
            this.subFolders = generateSubFolders(thisFolder);
            this.files = generateFiles(thisFolder);
        }

        private MyFile(MyFile parentFolder, File thisFolder, DriveFolder thisDriveFolder) {
            this.parentFolder = parentFolder;
            this.thisFolder = thisFolder;
            this.subFolders = generateSubFolders(thisFolder);
            this.files = generateFiles(thisFolder);
            this.thisDriveFolder = thisDriveFolder;
        }

        private MyFile[] generateSubFolders (File folder) {
            // List directories into the folder
            File[] directories = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            // Fill array with MyFile objects
            MyFile[] folders = new MyFile[directories.length];
            int i = 0;
            for (File directory : directories) {
                folders[i] = new MyFile(this, directory);
                i++;
            }

            return folders;
        }

        private File[] generateFiles (File folder) {
            // List files into the folder
            File[] files = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });

            return files;
        }

        //IMPORTANTE
        private MyFile getCurrentMyFile (MyFile myFile) {
            int i = currentSubFolder - 1;
            if(i == -1) {
                return this;
            }
            else {
                return getCurrentMyFile(subFolders[i]);
            }
        }
        private MyFile getCurrentMyFile () {
            return getCurrentMyFile(this);
        }

        private File nextFile () {
            if (getCurrentMyFile().files.length == getCurrentMyFile().currentFile)
                return null;
            else
                return getCurrentMyFile().files[currentFile++];
        }

        private MyFile nextSubFolder () {
            if (getCurrentMyFile().subFolders.length == getCurrentMyFile().currentSubFolder)
                return null;
            else
                return getCurrentMyFile().subFolders[currentSubFolder++];
        }

        private MyFile nextParentSubFolder () {
            if (!hasParentFolder())
                return null;
            MyFile parentFolder = getCurrentMyFile().parentFolder;
            return parentFolder.nextSubFolder();
        }

        // Set drive folder to thisDriveFolder if currentSubFolder is <0, else to subFolder[currentSubFolder-1]
        private void setDriveFolder (DriveFolder driveFolder) {
            MyFile currentMyFile = getCurrentMyFile();
            currentMyFile.thisDriveFolder = driveFolder;
        }

        private boolean hasParentFolder () {
            return parentFolder != null;
        }

        private boolean hasNextFile () {
            return files.length != currentFile;
        }

        private boolean hasNextSubFolder () {
            return subFolders.length != currentSubFolder;
        }

        private File getThisFolder () {
            return thisFolder;
        }

        private DriveFolder getThisDriveFolder () {
            return thisDriveFolder;
        }

        private MyFile getParentFolder () {
            return parentFolder;
        }

        private DriveFolder getCurrentDriveFolder () {
            return getCurrentMyFile().thisDriveFolder;
        }
    }
}