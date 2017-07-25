package it.unibs.cloudondemand.google;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import it.unibs.cloudondemand.R;

public class GoogleDriveUploadFileSingle extends GoogleDriveUploadFile {
    private static final String TAG = "GoogleDriveUpSingleFile";

    // Inflate xml layout file
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
    }

    // Entry point
    @Override
    public void startUploading() {
        // Start creating new drive content and fill it in callback
        File file = new File(getContent());
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
        uploadFile(file, folder);

        // Set textview name to file path
        TextView textname = (TextView) findViewById(R.id.upload_textname);
        textname.setText("File : " + getContent());
    }

    // Update progress bar status
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
    }

    @Override
    public void onFileUploaded(DriveFile driveFile) {
        if (driveFile == null) {
            Toast.makeText(GoogleDriveUploadFileSingle.this, R.string.unable_create_file_googledrive, Toast.LENGTH_SHORT).show();
            return;
        }

        // Finished to upload and set textview id to driveId
        fileProgress(100);
        TextView textid = (TextView) findViewById(R.id.upload_textid);
        textid.setText("DriveID : " + driveFile.getDriveId());

        disconnect();
    }
}
