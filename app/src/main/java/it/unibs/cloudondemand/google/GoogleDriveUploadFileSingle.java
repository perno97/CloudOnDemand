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
    private String filename;
    private int lastProgress;

    // Entry point
    @Override
    public void startUploading() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Start uploading the file into drive root dir.
        File file = new File(getContent());
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        // Start foreground notification
        filename = file.getName();
        lastProgress = 0;
        startForeground(NOTIFICATION_ID, buildNotification(0));

        uploadFile(file, folder);
    }

    private Notification buildNotification(int percent) {
        // Construct first time all the notification
        if(mNotificationBuilder == null) {
            mNotificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_file_folder)
                            .setContentTitle("Uploading files to Drive...")
                            .setContentText(filename + " ~ " + percent + "%")
                            .setOngoing(true);
        }
        else
            mNotificationBuilder.setContentText(filename + " ~ " + percent + "%");

        return mNotificationBuilder.build();
    }


    @Override
    public void fileProgress(int percent) {
        if(lastProgress != percent)
            mNotificationManager.notify(NOTIFICATION_ID, buildNotification(percent));
    }
    /* Update progress bar status
    @Override
    public void fileProgress(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.upload_progress_bar);
                progressBar.setProgress(percent);
                TextView textProtgress = (TextView) findViewById(R.id.upload_textprogress);
                textProtgress.setText(percent + "%");
            }
        });
    }   */

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
                        .setContentTitle("Uploading files to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        // Stop foreground and substitute notification
        stopForeground(true);
        mNotificationManager.cancel(NOTIFICATION_ID);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        disconnect();
    }
}
