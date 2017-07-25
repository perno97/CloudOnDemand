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

    private static final int NOTIFICATION_ID = 1;
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
        getNotificationManager().notify(NOTIFICATION_ID, buildNotification(0, file.getName()));

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
                    //.addAction()
                    .setOngoing(true);

            // Add action button to stop service
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, StopServices.class), PendingIntent.FLAG_UPDATE_CURRENT);
            if(Build.VERSION.SDK_INT > 23) {
                NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop", pendingIntent).build();
                mNotificationBuilder.addAction(stopAction);
            }
            else {
                mNotificationBuilder.addAction(R.drawable.ic_close, "Stop", pendingIntent);
            }
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
            ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, buildNotification(progress));
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

        // Substitute notification
        getNotificationManager().cancel(NOTIFICATION_ID);
        getNotificationManager().notify(NOTIFICATION_ID, mBuilder.build());

        disconnect();
    }
}
