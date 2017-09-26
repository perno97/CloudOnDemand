package it.unibs.cloudondemand.google;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.File;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.databaseManager.FileListContract;
import it.unibs.cloudondemand.databaseManager.FileListDbHelper;

public class GoogleDriveUtil {

    /**
     * Util method to retrieve intent to launch for upload.
     * @param context Context of activity that launch the intent.
     * @param contentType Type of content to upload.
     * @param content String, File path or Folder path.
     * @param signOut True if want to Sign-out before do something.
     * @return Intent to launch with startActivity(intent). Return null if content type is not found.
     */
    public static Intent getIntent(Context context, int contentType, String content, boolean signOut) {
        Intent intent = null;
        switch (contentType) {
            case LoginActivity.CONTENT_STRING :
                intent = new Intent(context, GoogleDriveUploadString.class);
                break;
            case LoginActivity.CONTENT_FILE :
                intent = new Intent(context, GoogleDriveUploadFileSingle.class);
                break;
            case LoginActivity.CONTENT_FOLDER :
                intent = new Intent(context, GoogleDriveUploadFileFolder.class);
                break;
        }
        intent.putExtra(LoginActivity.CONTENT_EXTRA, content);

        if(signOut)
            intent.putExtra(GoogleDriveConnection.SIGN_OUT_EXTRA, true);

        return intent;
    }

    /**
     * Util method to retrieve intent to launch for upload, signOut = false.
     * @param context Context of activity that launch the intent.
     * @param contentType Type of content to upload.
     * @param content String, File path or Folder path.
     * @return Intent to launch with startActivity(intent). Return null if content type is not found.
     */
    public static Intent getIntent(Context context, int contentType, String content) {
        return  getIntent(context, contentType, content, false);
    }

    /**
     * Check if an upload service is running.
     * @return True if an upload service is running, False otherwise.
     */
    public static boolean isUploadServiceRunning() {//TODO funziona anche per download?
        return GoogleDriveConnection.isRunning;
    }

    // Read account name from shared preferences and verify if user is signed in
    public static boolean isSignedIn(Context context) {
        return !getAccountName(context).equals("");
    }

    // Read account name from shared preferences
    public static String getAccountName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_pref_account), Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.saved_account_google), "");
    }

    // Save account name to shared preferences (Already signed in for future operations)
    static void saveAccountSignedIn(Context context, String accountName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_pref_account), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.saved_account_google), accountName);
        editor.apply();
    }

    public static void addFileToDatabase(Context context, String driveId, String filePath){
        FileListDbHelper mDbHelper = new FileListDbHelper(context);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FileListContract.FileList.COLUMN_DRIVEID, driveId);
        values.put(FileListContract.FileList.COLUMN_FILEPATH, filePath);

        // Insert the new row, returning the primary key value of the new row
        db.insert(FileListContract.FileList.TABLE_NAME, null, values);

        mDbHelper.close();
    }

    public static void deleteFileIfExists(Context context, File file, GoogleApiClient mGoogleApiClient) {
        FileListDbHelper mDbHelper = new FileListDbHelper(context);
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        String[] projection = {FileListContract.FileList.COLUMN_DRIVEID};

        String selection = FileListContract.FileList.COLUMN_FILEPATH + " = ?";
        String[] selectionArgs = {file.getPath()};

        Cursor cursor = database.query(
                FileListContract.FileList.TABLE_NAME,
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
        String driveId = cursor.getString(cursor.getColumnIndex(FileListContract.FileList.COLUMN_DRIVEID));

        // Delete file from drive
        DriveFile toDelete = DriveId.decodeFromString(driveId).asDriveFile();
        toDelete.delete(mGoogleApiClient);
        cursor.close();

        // Delete from db the file deleted on drive
        selection = FileListContract.FileList.COLUMN_DRIVEID + " = ?";
        selectionArgs[0] = driveId;
        database.delete(FileListContract.FileList.TABLE_NAME, selection, selectionArgs);

        mDbHelper.close();
    }

    public static void databaseToString(Context context){
        FileListDbHelper mDbHelper = new FileListDbHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

    }


}
