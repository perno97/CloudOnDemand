package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.content.Intent;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import java.io.File;
import java.util.ArrayList;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.FileTree;
import it.unibs.cloudondemand.utils.ProgressNotification;

public class GoogleDriveDownloadFileFolder extends GoogleDriveDownloadFile {

    private FileTree<GoogleDriveCustomFolder> foldersTree;

    private ProgressNotification mNotification;

    private ArrayList<String> driveIdToDownload;

    private ArrayList<String> pathToDownload;

    private int count;

    @Override
    public void startDownloading() {
        driveIdToDownload = getDriveIdArrayContent();
        pathToDownload = getPathArrayContent();

        // Initialize notification
        // Retrieve intent o launch when stop clicked
        Intent stopIntent = StopServices.getStopIntent(this, StopServices.SERVICE_UPLOAD_FOLDER);
        // Retrieve progress notification
        mNotification = new ProgressNotification(this, getString(R.string.googledrive_downloading_folder),"", false, stopIntent);
        // Show initial notification
        showNotification(mNotification.getNotification());

        count = 0;
        downloadFile(new File(pathToDownload.get(count)), driveIdToDownload.get(count));
    }

    @Override
    public void fileProgress(int percent) {

    }

    @Override
    public void onFileDownloaded(File file) {
        count++;
        downloadFile(new File(pathToDownload.get(count)), driveIdToDownload.get(count));
    }

    @Override
    public Notification getFinalNotification() {
        return null;
    }
}
