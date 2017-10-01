package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.ProgressNotification;

public class GoogleDriveDownloadFileSingle extends GoogleDriveDownloadFile {
    private static final String TAG = "GoogleDriveDwSingleFile";

    // Notification showed while downloading
    private ProgressNotification mNotification;

    // Name of file to download
    private String filename = "Error";

    @Override
    public void startDownloading() {
        // Retrieve extras from intent
        String driveId = getDownloadContent();
        File destinationPath = new File(getContent());
        filename = destinationPath.getName();

        // Initialize notification
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FILE);
        mNotification = new ProgressNotification(this, getString(R.string.googledrive_downloading_file),destinationPath.getName(), false, stopIntent);
        // Show initial notification
        showNotification(mNotification.getNotification());

        downloadFile(destinationPath, driveId);
    }

    @Override
    public void onFileDownloaded(File file) {
        if (file == null) {
            Log.e(TAG, "File from Drive not downloaded.");
            Toast.makeText(this, "Unable download file", Toast.LENGTH_SHORT).show();    //TODO res/strings
        }
        else {
            filename = file.getName();
        }

        disconnect();
    }

    @Override
    public void fileProgress(int percent) {
        showNotification(mNotification.editNotification(percent));
    }

    @Override
    public Notification getFinalNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(ProgressNotification.NOTIFICATION_ICON)
                        .setContentTitle(getString(R.string.googledrive_downloaded))
                        .setContentText(filename);

        return mBuilder.build();
    }
}

