package it.unibs.cloudondemand.google;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import it.unibs.cloudondemand.databaseManager.FileListDbHelper;

/**
 * Sign in to google account activity
 */
public class GoogleSignIn extends AppCompatActivity {
    private static final String TAG = "GoogleSignIn";

    private static GoogleSignInCallback signInCallback;

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = createGoogleClient();

        doSignIn();
    }

    /**
     * Construct GoogleApiClient.
     * @return Created client.
     */
    private GoogleApiClient createGoogleClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        return new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .build();
    }

    /**
     * Start account chooser dialog.
     */
    private void doSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
        }
    }

    /**
     * Handle sign in result.
     * @param result Sign in result.
     */
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, connect to play services.
            GoogleSignInAccount account = result.getSignInAccount();
            // Save account name to shared preferences (Already signed in for future operations)
            if(account != null)
                saveAccountSignedIn(account);

            // Return result of sign in
            signInCallback.onSignInResult(true);
        } else {
            // Signed out, show unauthenticated UI.
            Log.e(TAG, "Unable to connect to google account. Result : " + result.getStatus().toString());

            // Return result of sign in
            signInCallback.onSignInResult(false);
        }

        finish();
    }

    private void saveAccountSignedIn(GoogleSignInAccount account) {
        FileListDbHelper mDbHelper = new FileListDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        GoogleDriveUtil.saveAccountSignedIn(db,account.getId(),account.getDisplayName());
        GoogleDriveUtil.saveAccountSignedIn(this,account.getId());
    }


    /**
     * Contructor intent to start GoogleSignIn activity.
     * @param context Caller context.
     * @param callback Callback called when sign in is finished.
     * @return Intent to call.
     */
    public static Intent getSignInIntent(Context context, GoogleSignInCallback callback) {
        signInCallback = callback;
        return new Intent(context, GoogleSignIn.class);
    }

    /**
     * Callback interface for sign in activity
     */
    interface GoogleSignInCallback {
        void onSignInResult(boolean isSignedIn);
    }
}
