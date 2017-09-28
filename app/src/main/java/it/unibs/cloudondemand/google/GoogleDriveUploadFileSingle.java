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
    private static final int ROOT_ID = 0;

    private ProgressNotification mNotification;

    @Override
    public void startUploading() {
        // Start uploading the file into drive root dir.
        File file = new File(getContent());
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        // Initialize notification
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FILE);
        mNotification = new ProgressNotification(this, file.getName(), false, stopIntent);
        // Show initial notification
        showNotification(mNotification.getNotification());

        uploadFile(file, folder, ROOT_ID);
    }


    @Override
    public void fileProgress(int progress) {
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
                        .setContentTitle("Uploading file to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        return mBuilder.build();
    }
}
