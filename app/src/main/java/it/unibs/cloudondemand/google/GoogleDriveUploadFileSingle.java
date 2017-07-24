package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;

import java.io.File;

import it.unibs.cloudondemand.R;

public class GoogleDriveUploadFileSingle extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpSingleFile";

    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
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
        startForeground(NOTIFICATION_ID, buildNotification(0, file.getName()));
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        uploadFile(file, folder);
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

    @Override
    public void fileProgress(int progress) {
        if(lastProgress != progress)
            mNotificationManager.notify(NOTIFICATION_ID, buildNotification(progress));
        lastProgress = progress;
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Toast.makeText(GoogleDriveUploadFileSingle.this, "File non Creato", Toast.LENGTH_SHORT).show();   //TODO spostare stringe nelle res
            return;
        }

        // Construct final notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_file_folder)
                        .setContentTitle("Uploading file to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        // Stop foreground and substitute notification
        stopForeground(true);
        mNotificationManager.cancel(NOTIFICATION_ID);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        disconnect();
    }
}
