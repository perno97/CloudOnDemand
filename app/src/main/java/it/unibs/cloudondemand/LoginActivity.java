package it.unibs.cloudondemand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

import it.unibs.cloudondemand.google.GoogleDrive;
import it.unibs.cloudondemand.google.GoogleDriveString;
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
                startActivity(GoogleDriveUtil.getIntent(this, mContentType, mContent, true));
                break;
            case R.id.google_signed_in_button :
                startActivity(GoogleDriveUtil.getIntent(this, mContentType, mContent));
                break;
            // ...
        }
    }

}
