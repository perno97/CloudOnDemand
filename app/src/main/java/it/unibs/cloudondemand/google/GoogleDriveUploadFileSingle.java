package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;

import java.io.File;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.StopServices;

public class GoogleDriveUploadFileSingle extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpSingleFile";

    private NotificationCompat.Builder mNotificationBuilder;
    private int lastProgress;

    // Entry point
    @Override
    public void startUploading() {
        // Start uploading the file into drive root dir.
        File file = new File(getContent());
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        // Start foreground notification
        lastProgress = 0;
        getNotificationManager().notify(getNotificationId(), buildNotification(this, 0, file.getName()));

        uploadFile(file, folder);
    }


    @Override
    public void fileProgress(int progress) {
        if(lastProgress != progress)
            getNotificationManager().notify(getNotificationId(), buildNotification(this, progress));
        lastProgress = progress;
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Toast.makeText(GoogleDriveUploadFileSingle.this, "File non Creato", Toast.LENGTH_SHORT).show();   //TODO spostare stringe nelle res
            return;
        }

        disconnect();
    }

    @Override
    public Notification getFinalNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(GoogleDriveConnection.NOTIFICATION_ICON)
                        .setContentTitle("Uploading file to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        return mBuilder.build();
    }
}
