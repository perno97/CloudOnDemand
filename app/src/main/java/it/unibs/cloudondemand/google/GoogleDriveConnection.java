package it.unibs.cloudondemand.google;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;

import static android.app.Activity.RESULT_OK;

public abstract class GoogleDriveConnection extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleDriveConnection";

    public static final String SIGN_OUT_EXTRA = "signOut";
    private boolean signOut = false;

    private static final int clientConnectionType = GoogleApiClient.SIGN_IN_MODE_OPTIONAL;
    private GoogleApiClient mGoogleApiClient;

    private String content;

    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GoogleDriveConnection() {
        super("GoogleDriveSync");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // Initialize attributes
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startForeground(NOTIFICATION_ID, new Notification());

        if(intent != null)
            content = intent.getStringExtra(LoginActivity.CONTENT_EXTRA);

        mGoogleApiClient = createGoogleClient();

        // Connect to google services
        if(!GoogleDriveUtil.isSignedIn(this)) {
            doSignIn();
        }
        else {
            // If signOut is true after is connected, do sign-out stuff
            if(intent.getBooleanExtra(SIGN_OUT_EXTRA, false)) {
                signOut = true;
            }
            mGoogleApiClient.connect(clientConnectionType);
        }
    }

    private GoogleApiClient createGoogleClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        return mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // An error has occurred
        // and try to resolve it
        /*  TODO Cercare di ripristinare il codice
        if(result.hasResolution()) {
            try {
                result.startResolutionForResult(this, RC_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Intent sender exception while trying to resolve error.", e.getCause());
            }
        }*/

        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established.
        // Display an error message
        Log.e(TAG, "Connection failed - Result : " + result.getErrorCode());
        Toast.makeText(this, R.string.unable_connect_googleservices, Toast.LENGTH_SHORT).show();
    }

    private void doSignIn() {
        Intent intent = GoogleSignIn.getSignInIntent(this, signInCallback);
        startActivity(intent);
    }

    // Handle result of sign in request o user
    private final GoogleSignIn.GoogleSignInCallback signInCallback = new GoogleSignIn.GoogleSignInCallback() {
        @Override
        public void onSignInResult(boolean isSignedIn) {
            if(isSignedIn)
                mGoogleApiClient.connect(clientConnectionType);
            else {
                Log.i(TAG, "Unable to sign in into google account.");
                Toast.makeText(GoogleDriveConnection.this, "Impossibile connettersi all'account", Toast.LENGTH_SHORT).show();  //TODO cambiare
            }
        }
    };


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Verify if client want to Sign-out
        if(signOut) {
            Log.i(TAG, "Sign-out from Google Account");
            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            // Make it false to prevent infinite loop
                            signOut = false;
                            // Delete account name from shared preferences
                            GoogleDriveUtil.saveAccountSignedIn(GoogleDriveConnection.this, "");
                            // Restart with new Sign-in
                            createGoogleClient();
                            doSignIn();
                        }
                    });
            return;
        }

        Log.i(TAG, "Connected to Play Services.");

        onConnected();
    }
    //Entry point for class extended
    public abstract void onConnected();

    @Override
    public void onConnectionSuspended(int i) {
        StringBuilder msg = new StringBuilder("Connection to Play Services Suspended. Cause ");
        if(i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST)
            msg.append("NETWORK LOST");
        else if(i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED)
            msg.append("SERVICE DISCONNECTED");

        Log.i(TAG, msg.toString());
    }

    /**
     * Disconnect from google service.
     */
    public void disconnect() {
        if(mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Disconnect to Play Services");
            mGoogleApiClient.disconnect();
        }

        // Stop service
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public String getContent() {
        return content;
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public int getNotificationId() {
        return NOTIFICATION_ID;
    }
}