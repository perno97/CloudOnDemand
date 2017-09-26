package it.unibs.cloudondemand.google;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class GoogleDriveDownloadFileSingle extends GoogleDriveDownloadFile {
    private static final String TAG = "GoogleDriveDwSingleFile";

    @Override
    public void startDownloading() {
        // Start downloading the file into device root dir.
        String driveId = getContent();
        File  destinationPath = Environment.getExternalStorageDirectory();//TODO cambiare destinazione

        /* TODO mettere notifica
        // Initialize notification
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FILE);
        mNotification = new ProgressNotification(this, file.getName(), false, stopIntent);
        // Show initial notification
        showNotification(mNotification.getNotification());
        */
        downloadFile(destinationPath, driveId);
    }

    @Override
    public void onFileDownloaded(File file) {
        if(file != null) {
            Toast.makeText(this, "DOWNLOAD RIUSCITO", Toast.LENGTH_SHORT).show();//TODO usare string res
        }
        else {
            Log.e(TAG, "File not created.");
            Toast.makeText(this, "DOWNLOAD FALLITO", Toast.LENGTH_SHORT).show();//TODO usare string res
        }
        disconnect();
    }
}
