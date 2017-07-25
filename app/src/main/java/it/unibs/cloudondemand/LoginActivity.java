package it.unibs.cloudondemand;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

import it.unibs.cloudondemand.google.GoogleDriveConnection;
import it.unibs.cloudondemand.google.GoogleDriveUploadFileFolder;
import it.unibs.cloudondemand.google.GoogleDriveUploadFileSingle;
import it.unibs.cloudondemand.google.GoogleDriveUploadString;
import it.unibs.cloudondemand.google.GoogleDriveUtil;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String CONTENT_TYPE_EXTRA ="content-type";
    public static final String CONTENT_STRING="string";
    public static final String CONTENT_FILE="file";
    public static final String CONTENT_FOLDER="folder";

    public static final String CONTENT_EXTRA ="content";

    private String mContentType;
    private String mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mContent = intent.getStringExtra(CONTENT_EXTRA);
        mContentType = intent.getStringExtra(CONTENT_TYPE_EXTRA);

        //Set onClick listner for google sign in button
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Show and edit google button if already signed in
        handleSignedInAccounts();
    }

    //Show and edit google button if already signed in
    private void handleSignedInAccounts() {
        if(GoogleDriveUtil.isSignedIn(this)) {
            //Edit and show Signed in Button
            SignInButton buttonSigned = (SignInButton) findViewById(R.id.google_signed_in_button);
            editGoogleButton(buttonSigned, GoogleDriveUtil.getAccountName(this));
            buttonSigned.setVisibility(View.VISIBLE);
            buttonSigned.setOnClickListener(this);
            //Edit Sign in Button
            editGoogleButton((SignInButton) findViewById(R.id.google_sign_in_button), getString(R.string.button_google_another_account));
        }
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
        switch (v.getId()) {
            case R.id.google_sign_in_button :
                //Check before if service is running
                if(GoogleDriveUtil.isServiceRunning())
                    Toast.makeText(this, "Wait", Toast.LENGTH_SHORT).show();    //TODO res/strings
                else
                    startService(GoogleDriveUtil.getIntent(this, mContentType, mContent, true));
                break;
            case R.id.google_signed_in_button :
                if(GoogleDriveUtil.isServiceRunning())
                    Toast.makeText(this, "Wait", Toast.LENGTH_SHORT).show();    //TODO res/strings
                else
                    startService(GoogleDriveUtil.getIntent(this, mContentType, mContent));
                break;
            // ...
        }
        finish();
    }

    public static Intent getIntent (Context context, String contentType, String content) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(CONTENT_TYPE_EXTRA, contentType);
        intent.putExtra(CONTENT_EXTRA, content);
        return intent;
    }
}
