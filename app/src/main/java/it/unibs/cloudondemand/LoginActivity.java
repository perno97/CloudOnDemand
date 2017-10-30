package it.unibs.cloudondemand;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

import it.unibs.cloudondemand.databaseManager.FileListDbHelper;
import it.unibs.cloudondemand.dropbox.DropboxMainActivity;
import it.unibs.cloudondemand.google.GoogleDriveUtil;
import it.unibs.cloudondemand.utils.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String CONTENT_TYPE_EXTRA = "content-type";
    public static final int CONTENT_FILE = 1;
    public static final int CONTENT_FOLDER = 2;

    public static final String CONTENT_EXTRA = "content";

    private int mContentType;
    private String mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mContent = intent.getStringExtra(CONTENT_EXTRA);
        mContentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, -1);

        //Set onClick listner for google sign in button
        findViewById(R.id.google_sign_in_new_account_button).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Show and edit google button if already signed in
        handleSignedInButtonGoogle();
    }

    public static Intent getIntent (Context context, int contentType, String content) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(CONTENT_TYPE_EXTRA, contentType);
        intent.putExtra(CONTENT_EXTRA, content);
        return intent;
    }


    // FOR GOOGLE BUTTONS
    //Show and edit google button if already signed in
    private void handleSignedInButtonGoogle() {
        if(GoogleDriveUtil.isSignedIn(this)) {
            //Edit and show Signed in Button
            SignInButton buttonSigned = (SignInButton) findViewById(R.id.google_signed_in_button);
            editGoogleButton(buttonSigned, getAccountName());
            buttonSigned.setVisibility(View.VISIBLE);
            buttonSigned.setOnClickListener(this);

            //Edit Sign in new account Button
            editGoogleButton((SignInButton) findViewById(R.id.google_sign_in_new_account_button), getString(R.string.button_google_another_account));
        }
    }

    private String getAccountName() {
        FileListDbHelper mDbHelper = new FileListDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return GoogleDriveUtil.getAccountName(db,GoogleDriveUtil.getAccountIdSignedIn(this));
    }

    // Util method to edit google sign in button text
    private void editGoogleButton(SignInButton button, String text) {
        for(int i=0; i < button.getChildCount(); i++) {
            View v = button.getChildAt(i);
            if(v instanceof TextView) {
                ((TextView) v).setText(text);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(!Utils.checkInternetConnections(this)) {
            Toast.makeText(this, R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        if(Utils.isConnectedWithMobile(this)) {
            Toast.makeText(this, R.string.warning_internet_with_mobile, Toast.LENGTH_SHORT).show();
        }

        switch (v.getId()) {
            case R.id.google_sign_in_new_account_button :
                //Check before if service is running
                if(GoogleDriveUtil.isUploadServiceRunning())
                    Toast.makeText(this, R.string.wait, Toast.LENGTH_SHORT).show();
                else
                    startService(GoogleDriveUtil.getIntent(this, mContentType, mContent, true));
                break;
            case R.id.google_signed_in_button :
                if(GoogleDriveUtil.isUploadServiceRunning())
                    Toast.makeText(this, R.string.wait, Toast.LENGTH_SHORT).show();
                else if(Utils.checkInternetConnections(this))

                    startService(GoogleDriveUtil.getIntent(this, mContentType, mContent));
                break;
            case R.id.dropbox_sign_in_button:
                startActivity(DropboxMainActivity.getIntent(this, mContent));
                break;
        }
        finish();
    }
}
