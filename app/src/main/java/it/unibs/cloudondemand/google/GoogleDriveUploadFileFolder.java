package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
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
    private FileTree<GoogleDriveCustomFolder> foldersTree;

    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private int lastProgress;

    @Override
    public void startUploading() {
        // Initialize list of files to upload
        File mainFolder = new File(getContent());
        foldersTree = new FileTree<>(new GoogleDriveCustomFolder(mainFolder));
        // Create main folder on Drive then start uploading
        createDriveFolder(null, mainFolder.getName());

        // Start foreground notification
        startForeground(NOTIFICATION_ID, buildNotification(0, ""));
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private Notification buildNotification(int progress, String filename) {
        // Construct first time the notification
        if(mNotificationBuilder == null) {
            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_file_folder)
                    .setContentTitle("Uploading files to Drive...")
                    .setContentText(filename)
                    .setProgress(100, progress, false)
                    .setOngoing(true);
        }
        else
            if(filename == null)
                mNotificationBuilder.setProgress(100, progress, false);
            else
                mNotificationBuilder.setProgress(100, progress, false)
                                    .setContentText(filename);

        return mNotificationBuilder.build();
    }

    private Notification buildNotification(int progress) {
        return buildNotification(progress, null);
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
                // Finished
                disconnect();

                // Construct final notification
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_file_folder)
                                .setContentTitle("Uploading files to Drive...") //TODO mettere dentro res/values
                                .setContentText("Finito");

                // Stop foreground and substitute notification
                stopForeground(true);
                mNotificationManager.cancel(NOTIFICATION_ID);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }

            return;
        }

        // Retrieve file to upload into this drive folder
        DriveFolder currentDriveFolder = foldersTree.getCurrentFolderThis().getDriveFolder();
        File currentFile = foldersTree.nextFile();

        // Edit notification
        lastProgress = 0;
        mNotificationManager.notify(NOTIFICATION_ID, buildNotification(0, currentFile.getName()));

        // Upload current file
        uploadFile(currentFile, currentDriveFolder);
    }

    @Override
    public void fileProgress(int progress) {
        if(lastProgress != progress)
            mNotificationManager.notify(NOTIFICATION_ID, buildNotification(progress));
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

        uploadNextFile();
    }
}