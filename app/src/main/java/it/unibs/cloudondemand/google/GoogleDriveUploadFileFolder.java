package it.unibs.cloudondemand.google;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.util.Arrays;

import it.unibs.cloudondemand.utils.FileTree;

public class GoogleDriveUploadFileFolder extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpFolder";
    private FileTree<GoogleDriveCustomFolder> foldersTree;

    @Override
    public void startUploading() {
        // Initialize list of files to upload
        File mainFolder = new File(getContent());
        foldersTree = new FileTree<>(new GoogleDriveCustomFolder(mainFolder));
        // Create main folder on Drive then start uploading
        createDriveFolder(null, mainFolder.getName());
    }

    // Send input to create a foldersTree on drive (parent foldersTree // root if parentFolder = null), retrieve it by callback (onDriveFolderCreated)
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
            if(!driveFolderResult.getStatus().isSuccess()) {
                Log.e(TAG, "Unable to create drive folder.");
            }

            Log.i(TAG, "Folder on drive created. " + driveFolderResult.getDriveFolder().getDriveId());
            // Retrieve created folder and save it in data structure
            foldersTree.getCurrentThisFolder().setDriveFolder(driveFolderResult.getDriveFolder());
            // Upload the next file
            uploadNextFile();
        }
    };


    // Upload the next file or create the foldersTree in which is in
    private void uploadNextFile() {
        // Check if there is another file to upload in current folder
        if (!foldersTree.getCurrentFolder().hasNextFile()) {
            // Check if current folder has another subfolder
            if(foldersTree.getCurrentFolder().hasNextSubFolder())
                // Create that subfolder
                createDriveFolder(foldersTree.getCurrentThisFolder().getDriveFolder(), foldersTree.getCurrentFolder().nextSubFolder().getFolderName());
            else {
                // Go to parent's foldersTree next subdirectory
                FileTree<GoogleDriveCustomFolder> subFolder = foldersTree.getCurrentFolder().nextParentSubFolder();
                FileTree<GoogleDriveCustomFolder> thisFolder = foldersTree.getCurrentFolder();
                // Go up to main directory
                while(subFolder == null && thisFolder.hasParentFolder()) {
                    thisFolder = thisFolder.getParentFolder();
                    subFolder = thisFolder.nextParentSubFolder();
                }

                // Found another folder in the tree
                if(subFolder != null) {
                    createDriveFolder(subFolder.getParentFolder().getThisFolder().getDriveFolder(), subFolder.getFolderName());
                    return;
                }

                // Reached up main folder without finding another folder... finished
                disconnect();
                Toast.makeText(this, "FINITO", Toast.LENGTH_SHORT).show();
            }

            return;
        }

        // Retrieve file to upload into this drive folder
        DriveFolder currentDriveFolder = foldersTree.getCurrentThisFolder().getDriveFolder();
        File currentFile = foldersTree.getCurrentFolder().nextFile();

        // Upload current file
        uploadFile(currentFile, currentDriveFolder);
    }

    @Override
    public void fileProgress(int percent) {
        //TODO
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Toast.makeText(this, "File non creato", Toast.LENGTH_SHORT).show();     //TODO FARE QUALCOSA
            return;
        }

        // Retrieve created file and save it in data structure
        foldersTree.getCurrentThisFolder().setFileId(driveFile.getDriveId());

        uploadNextFile();
    }
}