package it.unibs.cloudondemand.google;

import android.content.Intent;
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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import it.unibs.cloudondemand.LoginActivity;

public class GoogleDrive extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 1;
    private String CONTENT_TYPE;
    private String CONTENT;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data=getIntent();
        CONTENT_TYPE=LoginActivity.CONTENT_STRING;//data.getStringExtra(LoginActivity.CONTENT_TYPE_EXTRA);
        CONTENT="Sono perno";//data.getStringExtra(LoginActivity.CONTENT_EXTRA);

        createGoogleClient();

        doLogin();
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
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        //TODO remove
        Toast.makeText(this, "Connection failed - Result : " + result.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    public void doLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(this, "loggato", Toast.LENGTH_SHORT).show();
            mGoogleApiClient.connect();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this, "non loggato", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(CONTENT_TYPE!=LoginActivity.CONTENT_FOLDER) {
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(driveContentsCallback);
        }
        else {
            //TODO
        }
    }

    //Called when file on Drive was created
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if(!driveContentsResult.getStatus().isSuccess()) {
                Toast.makeText(GoogleDrive.this, "Callback fallita", Toast.LENGTH_SHORT).show();
                Log.e("Google Drive", "Error while creating new file on Drive");
                return;
            }

            //Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();


            //Upload file or string into drive file
            new Thread(){
                @Override
                public void run() {
                    //Choose stream to use by content type
                    switch (CONTENT_TYPE) {
                        case LoginActivity.CONTENT_STRING :
                            Writer writer = new OutputStreamWriter(driveContents.getOutputStream());
                            try {
                                writer.write(CONTENT);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                try {
                                    writer.close();
                                }
                                catch (Exception e){

                                }
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle("nome")
                                    .setMimeType("text/plain")
                                    .setStarred(true)
                                    .build();

                            Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                    .createFile(mGoogleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);

                            break;
                        case LoginActivity.CONTENT_FILE :

                            break;
                    }

                }
            }.start();
        }
    };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if(!driveFileResult.getStatus().isSuccess())
                Toast.makeText(GoogleDrive.this, "file non creato", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(GoogleDrive.this, "file creato", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }
}