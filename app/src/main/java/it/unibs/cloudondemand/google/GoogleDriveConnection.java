package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;

import java.util.ArrayList;
import java.util.HashMap;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.ProgressNotification;


/**
 * Google account connection service.
 */
public abstract class GoogleDriveConnection extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleDriveConnection";

    // Used to control running status of service
    static boolean isRunning;

    /**
     * Extra for calling intent (want to sign out by current account?)
     */
    public static final String SIGN_OUT_EXTRA = "signOut";
    // Used to check if user want to sign out
    private boolean signOut = false;

    // Google client
    private static final int clientConnectionType = GoogleApiClient.SIGN_IN_MODE_OPTIONAL;
    private GoogleApiClient mGoogleApiClient;

    // Intent content
    private Intent intent;

    // Foreground notification
    private static final int NOTIFICATION_ID = 1;
    private static final int LAST_NOTIFICATION_ID = 2;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize attributes
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start foreground notification
        startForeground(NOTIFICATION_ID, new Notification());

        // Initialize attributes
        this.intent = intent;
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

        return super.onStartCommand(intent, flags, startId);
    }

    // Not used because is a started service
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // An error has occurred
        Log.e(TAG, "Connection failed - Result code : " + result.getErrorCode());
        Toast.makeText(this, R.string.unable_connect_googleservices, Toast.LENGTH_SHORT).show();
    }

    /**
     * Start activity to sign in to google account.
     */
    private void doSignIn() {
        Intent intent = GoogleSignIn.getSignInIntent(this, signInCallback);
        startActivity(intent);
    }


    /**
     * Handle result of sign in request o user
     */
    private final GoogleSignIn.GoogleSignInCallback signInCallback = new GoogleSignIn.GoogleSignInCallback() {
        @Override
        public void onSignInResult(boolean isSignedIn) {
            if(isSignedIn)
                mGoogleApiClient.connect(clientConnectionType);
            else {
                Log.i(TAG, "Unable to sign in into google account.");
                Toast.makeText(GoogleDriveConnection.this, R.string.unable_connect_account, Toast.LENGTH_SHORT).show();
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
        isRunning = true;

        onConnected();
    }

    /**
     * Entry point for subclasses. Google client is connected.
     */
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
            Log.i(TAG, "Disconnect to Play Services.");
            mGoogleApiClient.disconnect();
        }

        // Stop service
        Log.i(TAG, "Finished service (Google).");
        isRunning = false;
        stopForeground(true);

        // Show last (cancelable) notification
        mNotificationManager.notify(LAST_NOTIFICATION_ID, getFinalNotification());

        stopSelf();
    }

    // Disconnect api client if is connected and user stop the service
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient.isConnected())
            disconnect();
    }

    /**
     * Should implement this to keep last notification with onGoing=false (need to be cancelable).
     * @return Last notification to show.
     */
    public abstract Notification getFinalNotification();

    /**
     * Show or update foreground notification.
     * @param notification New or updated notification.
     */
    public void showNotification(Notification notification) {
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Getter GoogleApiClient.
     * @return GoogleApiClient (may be connected).
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Getter intent content,
     * @return Content extra of intent.
     */
    public String getContent() {
        return intent.getStringExtra(LoginActivity.CONTENT_EXTRA);
    }

    public ArrayList<String> getDriveIdArrayContent(){
        return intent.getStringArrayListExtra(GoogleDriveDownloadFile.DRIVEID_EXTRA);
    }

    public ArrayList<String> getPathArrayContent(){
        return intent.getStringArrayListExtra(LoginActivity.CONTENT_EXTRA);
    }

    /**
     * Getter intent content only for download.
     * @return Content extra of intent.
     */
    public String getDownloadContent(){
        return intent.getStringExtra(GoogleDriveDownloadFile.DRIVEID_EXTRA);
    }
}