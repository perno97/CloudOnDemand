package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import it.unibs.cloudondemand.utils.ProgressNotification;

public class GoogleDriveDownloadFileSingle extends GoogleDriveDownloadFile {
    private static final String TAG = "GoogleDriveDwSingleFile";

    // Notification showed while downloading
    private ProgressNotification mNotification;

    // Name of file to download
    private String filename = "Error";

    @Override
    public void startDownloading() {
        // Start downloading the file into device root dir.
        String driveId = getContent();
        File destinationPath = Environment.getExternalStorageDirectory();//TODO cambiare destinazione

        // Initialize notification
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FILE);
        mNotification = new ProgressNotification(this, "Qualcosa", false, stopIntent);  //TODO Metodo in downloadfile per sapere il nome del file
        // Show initial notification
        showNotification(mNotification.getNotification());

        downloadFile(destinationPath, driveId);
    }

    //TODO utilizzare questo poi
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
    public void onFileDownloaded() {
        filename = "Completato";

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
                        .setContentTitle("Download su drive completato")    //TODO res/strings
                        .setContentText(filename);

        return mBuilder.build();
    }
}

