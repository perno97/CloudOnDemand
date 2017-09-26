package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.databaseManager.FileListContract;
import it.unibs.cloudondemand.databaseManager.FileListContract.FolderList;
import it.unibs.cloudondemand.databaseManager.FileListDbHelper;
import it.unibs.cloudondemand.utils.FileTree;
import it.unibs.cloudondemand.utils.ProgressNotification;

public class GoogleDriveUploadFileFolder extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpFolder";

    // Folder tree structure
    private FileTree<GoogleDriveCustomFolder> foldersTree;

    // Folder that is going to be created
    private File folderToCreate;

    private ProgressNotification mNotification;

    @Override
    public void startUploading() {
        // Initialize file tree to upload
        File mainFolder = new File(getContent());
        foldersTree = new FileTree<>(new GoogleDriveCustomFolder(mainFolder));

        // Create main folder on Drive then start uploading
        createDriveFolder(null, mainFolder);


        // Initialize notification
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FOLDER);
        mNotification = new ProgressNotification(this, "", false, stopIntent);
        // Show initial notification
        showNotification(mNotification.getNotification());
    }


    /** TODO Edit comments
     * Send input ro create a folder on drive. Created when is called the callback.
     * @param parentFolder Drive parent folder, if null parent is root drive folder.
     * @param folder Folder to create.
     */
    private void createDriveFolder (DriveFolder parentFolder, File folder) {
        // Delete folder on drive if already exists
        GoogleDriveUtil.deleteFolderIfExists(getApplicationContext(), folder, getGoogleApiClient());

        // Folder doesn't exist on Drive, so create it
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folder.getName())
                .setStarred(true)
                .build();

        if (parentFolder == null)
            parentFolder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        folderToCreate = folder;

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
                return;
            }
            DriveFolder createdDriveFolder = driveFolderResult.getDriveFolder();

            Log.i(TAG, "Folder on drive created. " +  createdDriveFolder.getDriveId());

            // Save folder into database
            GoogleDriveUtil.addFolderToDatabase(getApplicationContext(), createdDriveFolder.getDriveId().encodeToString(), folderToCreate.getPath());

            // Retrieve created folder and save it in data structure
            foldersTree.getCurrentFolderThis().setDriveFolder(createdDriveFolder);
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
            FileTree<GoogleDriveCustomFolder> folderNode = foldersTree.nextFolder();
            // Check if current folder has another subfolder
            if(folderNode != null)
                // Create that subfolder
                createDriveFolder(folderNode.getParentFolder().getThisNode().getDriveFolder(), folderNode.getCurrentFolder().getFolder());
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
        showNotification(mNotification.editNotification(0, currentFile.getName()));

        // Upload current file
        uploadFile(currentFile, currentDriveFolder);
    }

    @Override
    public void fileProgress(int progress) {
        showNotification(mNotification.editNotification(progress));
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Toast.makeText(this, R.string.unable_create_file_googledrive, Toast.LENGTH_SHORT).show();

            uploadNext();
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
                        .setSmallIcon(ProgressNotification.NOTIFICATION_ICON)
                        .setContentTitle("Uploading files to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        return mBuilder.build();
    }
}