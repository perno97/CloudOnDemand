package it.unibs.cloudondemand.google;

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

public abstract class GoogleDriveConnection extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleDriveConnection";

    public static final String SIGN_OUT_EXTRA = "signOut";
    private boolean signOut = false;

    private static final int clientConnectionType = GoogleApiClient.SIGN_IN_MODE_OPTIONAL;
    private static final int RC_SIGN_IN = 1;
    private static final int RC_RESOLUTION = 2;
    private GoogleApiClient mGoogleApiClient;
    private String content;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data=getIntent();
        content = data.getStringExtra(LoginActivity.CONTENT_EXTRA);


        createGoogleClient();


        if(!GoogleDriveUtil.isSignedIn(this)) {
            doSignIn();
        }
        else {
            // If signOut is true after is connected, do sign-out stuff
            if(data.getBooleanExtra(SIGN_OUT_EXTRA, false)) {
                signOut = true;
            }
            mGoogleApiClient.connect(clientConnectionType);
        }
    }

    private void createGoogleClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
        if(result.hasResolution()) {
            try {
                result.startResolutionForResult(this, RC_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Intent sender exception while trying to resolve error.", e.getCause());
            }
        }

        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established.
        // Display an error message
        Log.e(TAG, "Connection failed - Result : " + result.getErrorCode());
        Toast.makeText(this, R.string.unable_connect_googleservices, Toast.LENGTH_SHORT).show();
    }

    public void doSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN :
                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            case RC_RESOLUTION :
                if(resultCode == RESULT_OK)
                    mGoogleApiClient.connect(clientConnectionType);
                else
                    Log.e(TAG, "Tried with error to resolve onConnectionFailed");
                break;
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, connect to play services.
            GoogleSignInAccount account = result.getSignInAccount();
            // Save account name to shared preferences (Already signed in for future operations)
            if(account.getDisplayName()==null)
                GoogleDriveUtil.saveAccountSignedIn(this, "Google");    // This should never happen
            else
                GoogleDriveUtil.saveAccountSignedIn(this, account.getDisplayName());
            mGoogleApiClient.connect(clientConnectionType);
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this, R.string.unable_connect_account, Toast.LENGTH_SHORT).show();
            Log.e(TAG, result.getStatus().toString());
        }
    }




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
        StringBuilder msg=new StringBuilder("Connection to Play Services Suspended. Cause ");
        if(i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST)
            msg.append("NETWORK LOST");
        else if(i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED)
            msg.append("SERVICE DISCONNECTED");

        Log.i(TAG, msg.toString());
    }

    public void disconnect() {
        if(mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Disconnect to Play Services");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public String getContent() {
        return content;
    }
}