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

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;


/**
 * Google account connection service.
 */
public abstract class GoogleDriveConnection extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleDriveConnection";

    // Used to control running status of service    //TODO NON FUNZIONA
    static boolean isRunning;

    // Extra of calling intent
    public static final String SIGN_OUT_EXTRA = "signOut";
    private boolean signOut = false;

    // Google client
    private static final int clientConnectionType = GoogleApiClient.SIGN_IN_MODE_OPTIONAL;
    private GoogleApiClient mGoogleApiClient;

    // Intent content
    private String content;

    // Foreground notification
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

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
                Toast.makeText(GoogleDriveConnection.this, R.string.unable_connect_account, Toast.LENGTH_SHORT).show();  //TODO cambiare
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
     * Entry point for class extended
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
        stopForeground(false);
        stopSelf();
    }

    // Disconnect api client if is connected and user stop the service
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient.isConnected())
            disconnect();

        // Show last notification
        mNotificationManager.notify(NOTIFICATION_ID, getFinalNotification());
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
        return content;
    }

    // ------------
    // NOTIFICATION
    // ------------

    /**
     * Should implement this to keep last notification with onGoing=false (cancelable).
     * @return Last notification to show.
     */
    public abstract Notification getFinalNotification();

    /**
     * Return the integer that determinate which service is running.
     * @return A StopServices constant.
     */
    public abstract int getStopServiceExtra();

    // Small icon for notification
    public static final int NOTIFICATION_ICON = R.mipmap.ic_launcher;

    // Build new notification or edit old
    private Notification buildNotification(int progress, String contentText, boolean indeterminateProgress) {
        // Construct first time the notification
        if(mNotificationBuilder == null) {

            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(NOTIFICATION_ICON)
                    .setContentTitle("Uploading files to Drive...") //TODO res/strings
                    .setContentText(contentText)
                    .setProgress(100, progress, indeterminateProgress)
                    //.addAction()
                    .setOngoing(true);

            // Intent to launch when stop pressed
            Intent stopIntent = new Intent(this, StopServices.class);
            stopIntent.putExtra(StopServices.SERVICE_EXTRA, getStopServiceExtra());

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // Add pending intent to notification builder
            if(Build.VERSION.SDK_INT > 23) {
                NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop", pendingIntent).build();
                mNotificationBuilder.addAction(stopAction);
            }
            else {
                mNotificationBuilder.addAction(R.drawable.ic_close, "Stop", pendingIntent);
            }
        }
        else
            // Edit already created notification
            if(contentText == null)
                mNotificationBuilder.setProgress(100, progress, false);
            else
                mNotificationBuilder.setProgress(100, progress, false)
                        .setContentText(contentText);

        return mNotificationBuilder.build();
    }

    /**
     * Show or update notification with determinate progress bar.
     * @param progress Percent progress.
     * @param contentText Text of notification content.
     */
    public void showNotification(int progress, String contentText) {
        Notification notification = buildNotification(progress, contentText, false);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Update notification with determinate progress bar (doesn't change content text).
     * @param progress Percent progress.
     */
    public void showNotification(int progress) {
        Notification notification = buildNotification(progress, null, false);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Show or update notification.
     * @param progress Percent progress.
     * @param indeterminateProgress True if want indeterminate progress bar.
     * @param contentText Text of notification content.
     */
    public void showNotification(int progress, boolean indeterminateProgress, String contentText) {
        Notification notification = buildNotification(progress, contentText, indeterminateProgress);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Update notification (doesn't change content text).
     * @param progress Percent progress.
     * @param indeterminateProgress True if want indeterminate progress bar.
     */
    public void showNotification(int progress, boolean indeterminateProgress) {
        Notification notification = buildNotification(progress, null, indeterminateProgress);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}