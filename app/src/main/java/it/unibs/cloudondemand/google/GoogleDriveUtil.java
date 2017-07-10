package it.unibs.cloudondemand.google;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;

public class GoogleDriveUtil {

    /**
     * Util method to retrieve intent to launch for upload.
     * @param context Context of activity that launch the intent.
     * @param contentType Type of content to upload.
     * @param content String, File path or Folder path.
     * @param signOut True if want to Sign-out before do something.
     * @return Intent to launch with startActivity(intent). Return null if content type is not found.
     */
    public static Intent getIntent(Context context, String contentType, String content, boolean signOut) {
        Intent intent=null;
        switch (contentType) {
            case LoginActivity.CONTENT_STRING :
                intent = new Intent(context, GoogleDriveString.class);
                intent.putExtra(LoginActivity.CONTENT_EXTRA, content);
                break;
            case LoginActivity.CONTENT_FILE :
                intent = new Intent(context, GoogleDriveFileSingle.class);
                intent.putExtra(LoginActivity.CONTENT_EXTRA, content);
                break;
            //TODO Edit when classes were created
            case LoginActivity.CONTENT_FOLDER :
                break;
        }
        if(signOut && intent!=null) //TODO Remove intent!=null when all classes are implemented
            intent.putExtra(GoogleDrive.SIGN_OUT_EXTRA, true);

        return intent;
    }

    /**
     * Util method to retrieve intent to launch for upload, signOut = false.
     * @param context Context of activity that launch the intent.
     * @param contentType Type of content to upload.
     * @param content String, File path or Folder path.
     * @return Intent to launch with startActivity(intent). Return null if content type is not found.
     */
    public static Intent getIntent(Context context, String contentType, String content) {
        return  getIntent(context, contentType, content, false);
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


}
