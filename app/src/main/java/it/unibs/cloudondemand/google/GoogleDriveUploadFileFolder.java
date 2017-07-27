package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
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

    // Folder tree structure
    private FileTree<GoogleDriveCustomFolder> foldersTree;

    // Last progress in fileProgress
    private int lastProgress = 0;

    @Override
    public void startUploading() {
        // Initialize file tree to upload
        File mainFolder = new File(getContent());
        foldersTree = new FileTree<>(new GoogleDriveCustomFolder(mainFolder));

        // Create main folder on Drive then start uploading
        createDriveFolder(null, mainFolder.getName());

        // Show initial notification
        showNotification(this, 0, "");
    }


    /**
     * Send input ro create a folder on drive. Created when is called the callback.
     * @param parentFolder Drive parent folder, if null parent is root drive folder.
     * @param name Name of folder to create.
     */
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

    /**
     * Callback : Folder on drive created
     */
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
            uploadNext();
        }
    };


    /**
     * Upload the next file or create the next folder if current is empty
     */
    private void uploadNext() {
        // Check if there is another file to upload in current folder
        if (!foldersTree.hasNextFile()) {
            FileTree<GoogleDriveCustomFolder> folder = foldersTree.nextFolder();
            // Check if current folder has another subfolder
            if(folder != null)
                // Create that subfolder
                createDriveFolder(folder.getParentFolder().getThisFolder().getDriveFolder(), folder.getCurrentFolder().getFolderName());
            else {
                // Finished
                disconnect();
            }

            return;
        }

        // Retrieve file to upload into this drive folder
        DriveFolder currentDriveFolder = foldersTree.getCurrentFolderThis().getDriveFolder();
        File currentFile = foldersTree.nextFile();

        // Edit notification
        lastProgress = 0;
        showNotification(this, 0, currentFile.getName());

        // Upload current file
        uploadFile(currentFile, currentDriveFolder);
    }

    @Override
    public void fileProgress(int progress) {
        if(lastProgress != progress)
            showNotification(this, progress);
        lastProgress = progress;
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Toast.makeText(this, "File non creato", Toast.LENGTH_SHORT).show();     //TODO FARE QUALCOSA
            return;
        }

        // Retrieve created file and save it in data structure
        foldersTree.getCurrentFolderThis().setFileId(driveFile.getDriveId());

        uploadNext();
    }

    @Override
    public Notification getFinalNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(GoogleDriveConnection.NOTIFICATION_ICON)
                        .setContentTitle("Uploading files to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        return mBuilder.build();
    }

    @Override
    public int getStopServiceExtra() {
        return StopServices.SERVICE_UPLOAD_FOLDER;
    }
}