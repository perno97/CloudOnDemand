package it.unibs.cloudondemand.google;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.databaseManager.FileListContract.FileList;
import it.unibs.cloudondemand.databaseManager.FileListDbHelper;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.Utils;

public abstract class GoogleDriveUploadFile extends GoogleDriveConnection {
    private static final String TAG = "GoogleDriveUpFile";

    // File to upload
    private File fileToUpload;
    // Drive folder in witch file need to be uploaded
    private DriveFolder driveFolder;


    private UploadFileAsyncTask uploadFileAsyncTask;

    // Database that contains files and folders already uploaded
    private SQLiteDatabase database;
    private FileListDbHelper mDbHelper;

    @Override
    public void onConnected() {
        // Initialize attributes
        mDbHelper = new FileListDbHelper(getApplicationContext());
        database = mDbHelper.getReadableDatabase();

        // Check if storage is readable and start upload
        if (Utils.isExternalStorageReadable()) {
            // Verify permission and after call startUploading when permission is granted
            Intent intent = PermissionRequest.getIntent(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionResultCallback);
            startActivity(intent);
        }
        else {
            Toast.makeText(GoogleDriveUploadFile.this, R.string.unable_read_storage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to read external storage.");
        }
    }

    // Called when user chose to grant permission
    final private PermissionRequest.PermissionRequestCallback permissionResultCallback = new PermissionRequest.PermissionRequestCallback() {
        @Override
        public void onPermissionResult(int isGranted) {
            if (isGranted == PermissionRequest.PERMISSION_GRANTED)
                startUploading();
            else {
                // Permission denied, show to user and close activity
                Toast.makeText(GoogleDriveUploadFile.this, R.string.requested_permission_read_storage, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Permission to read external storage denied");
                // Stop service
                disconnect();
            }
        }
    };

    // Entry point subclasses (client connected, permission granted and storage readable)
    public abstract void startUploading();

    // Called by subclasses when want to upload a file
    public void uploadFile (File file, DriveFolder folder) {
        this.fileToUpload = file;
        this.driveFolder = folder;

        // Delete file on drive if already exists
        deleteFileIfExists(fileToUpload);

        // Start to upload
        uploadFileAsyncTask = new UploadFileAsyncTask();
        uploadFileAsyncTask.execute();
    }

    /**
     * Upload the file in another thread
     */
    private class UploadFileAsyncTask extends AsyncTask<Void, Integer, DriveFolder.DriveFileResult> {

        @Override
        protected DriveFolder.DriveFileResult doInBackground(Void... voids) {
            // Get new drive content
            DriveApi.DriveContentsResult driveContentsResult= Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();

            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Error while creating new file on Drive");
                return null;
            }

            // Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();


            OutputStream outputStream = null;
            try {
                // Open file
                FileInputStream fileInputStream = new FileInputStream(fileToUpload);
                outputStream = driveContents.getOutputStream();

                byte[] buffer = new byte[8];
                long k = 0;
                long fileLength = fileToUpload.length();
                // Write on drive content stream with buffer of 8 bytes
                while (fileInputStream.read(buffer) != -1) {
                    publishProgress((int) (100*k/fileLength));
                    outputStream.write(buffer);
                    k+=8;
                }

            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found." + e.toString(), e.getCause());
            } catch (IOException e) {
                Log.e(TAG, "Exception while writing on driveConetents output stream." + e.toString(), e.getCause());
            } finally {
                // Close output stream
                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing streams." + e.toString(), e.getCause());
                }
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(fileToUpload.getName())
                    .setStarred(true)
                    .build();

            return driveFolder
                    .createFile(getGoogleApiClient(), changeSet, driveContents)
                    .await();
        }

        private int lastValue = 0;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(lastValue != values[0]) {
                // Call abstract method
                fileProgress(values[0]);
                lastValue = values[0];
            }
        }

        @Override
        protected void onPostExecute(DriveFolder.DriveFileResult driveFileResult) {
            // Finished to upload file
            if (!driveFileResult.getStatus().isSuccess()) {
                Log.e(TAG, "Unable to create file on drive folder. " + driveFileResult.getStatus().toString());

                onFileUploaded(null);
            } else {
                Log.i(TAG, "File on drive created. " + driveFileResult.getDriveFile().getDriveId());

                addFileToDatabase(driveFileResult.getDriveFile().getDriveId().encodeToString(), fileToUpload.getPath());
                onFileUploaded(driveFileResult.getDriveFile());
            }
        }

        @Override
        protected void onCancelled(DriveFolder.DriveFileResult driveFileResult) {
            Log.i(TAG, "User stop to upload files");
        }
    }

    @Override
    public void onDestroy() {
        // Stop async task if running when user stop the service
        if(uploadFileAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            uploadFileAsyncTask.cancel(true);

        // Close connection to DB
        mDbHelper.close();
        super.onDestroy();
    }

    // Called many times during the file upload.
    public abstract void fileProgress (int percent);

    // Called when a file has been uploaded. driveFile = null when file on drive wasn't created.
    public abstract void onFileUploaded (DriveFile driveFile);

    private void deleteFileIfExists(File file) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {FileList.COLUMN_DRIVEID, FileList.COLUMN_FILEPATH};

        // Filter results WHERE "title" = 'My Title'
        String selection = FileList.COLUMN_FILEPATH + " = ?";
        String[] selectionArgs = {file.getPath()};

        Cursor cursor = database.query(
                FileList.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if(cursor == null)
            return;

        if(cursor.getCount() == 0)
            return;

        cursor.moveToNext();
        String driveId = cursor.getString(cursor.getColumnIndex(FileList.COLUMN_DRIVEID));

        // Delete file from drive
        Log.i(TAG, "Deleting file with drive ID (if exists) : " + driveId);
        DriveFile toDelete = DriveId.decodeFromString(driveId).asDriveFile();
        toDelete.delete(getGoogleApiClient());
        cursor.close();

        // Delete from db the file deleted on drive
        selection = FileList.COLUMN_DRIVEID + " = ?";
        selectionArgs[0] = driveId;
        database.delete(FileList.TABLE_NAME, selection, selectionArgs);
    }

    private void addFileToDatabase(String driveId, String filePath){
        FileListDbHelper mDbHelper = new FileListDbHelper(getApplicationContext());

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FileList.COLUMN_DRIVEID, driveId);
        values.put(FileList.COLUMN_FILEPATH, filePath);

        // Insert the new row, returning the primary key value of the new row
        db.insert(FileList.TABLE_NAME, null, values);

        mDbHelper.close();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }
}