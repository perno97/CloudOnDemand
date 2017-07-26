package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;

import java.io.File;

public class GoogleDriveUploadFileSingle extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpSingleFile";

    // Last progress in fileProgress
    private int lastProgress = 0;

    @Override
    public void startUploading() {
        // Start uploading the file into drive root dir.
        File file = new File(getContent());
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());

        // Show initial notification
        showNotification(this, 0, file.getName());

        uploadFile(file, folder);
    }


    @Override
    public void fileProgress(int progress) {
        // Update notification if lastProgress is different of current progress
        if(lastProgress != progress)
            showNotification(this, progress);

        lastProgress = progress;
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Log.e(TAG, "File on Drive not created.");
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
