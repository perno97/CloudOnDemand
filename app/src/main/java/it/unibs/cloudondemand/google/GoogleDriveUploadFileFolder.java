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

import it.unibs.cloudondemand.R;
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
            foldersTree.getCurrentFolderThis().setDriveFolder(driveFolderResult.getDriveFolder());
            // Upload the next file
            uploadNextFile();
        }
    };


    // Upload the next file or create the foldersTree in which is in
    private void uploadNextFile() {
        // Check if there is another file to upload in current folder
        if (!foldersTree.hasNextFile()) {
            FileTree<GoogleDriveCustomFolder> folder = foldersTree.nextFolder();
            // Check if current folder has another subfolder
            if(folder != null)
                // Create that subfolder
                createDriveFolder(folder.getParentFolder().getThisFolder().getDriveFolder(), folder.getCurrentFolder().getFolderName());
            else {
                // Reached up main folder without finding another folder... finished
                disconnect();
                Toast.makeText(this, "FINITO", Toast.LENGTH_SHORT).show();
            }

            return;
        }

        // Retrieve file to upload into this drive folder
        DriveFolder currentDriveFolder = foldersTree.getCurrentFolderThis().getDriveFolder();
        File currentFile = foldersTree.nextFile();

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
            Toast.makeText(this, R.string.unable_create_file_googledrive, Toast.LENGTH_SHORT).show();

            uploadNextFile();
            return;
        }

        // Retrieve created file and save it in data structure
        foldersTree.getCurrentFolderThis().setFileId(driveFile.getDriveId());

        uploadNextFile();
    }
}