package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;

import java.io.File;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.ProgressNotification;

public class GoogleDriveUploadFileSingle extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpSingleFile";

    // Notification showed while uploading
    private ProgressNotification mNotification;

    // Name of file to upload
    private String filename;

    @Override
    public void startUploading() {
        // Retrieve file to upload
        File file = new File(getContent());
        filename = file.getName();
        // Retrieve folder on witch upload the file
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        // Initialize notification
        // Retrieve intent o launch when stop clicked
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FILE);
        // Retrieve progress notification
        mNotification = new ProgressNotification(this, file.getName(), false, stopIntent);
        // Show initial notification
        showNotification(mNotification.getNotification());

        // Start uploading the file into drive root dir
        uploadFile(file, folder);
    }

    @Override
    public void fileProgress(int progress) {
        // Update notification
        showNotification(mNotification.editNotification(progress));
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Log.e(TAG, "File on Drive not created.");
            Toast.makeText(GoogleDriveUploadFileSingle.this, R.string.unable_create_file_googledrive, Toast.LENGTH_SHORT).show();

            return;
        }

        disconnect();
    }

    @Override
    public Notification getFinalNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(ProgressNotification.NOTIFICATION_ICON)
                        .setContentTitle(getString(R.string.googledrive_uploaded))
                        .setContentText(filename);

        return mBuilder.build();
    }
}
