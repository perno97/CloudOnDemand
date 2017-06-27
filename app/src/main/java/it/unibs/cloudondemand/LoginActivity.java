package it.unibs.cloudondemand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import it.unibs.cloudondemand.google.GoogleDriveString;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String CONTENT_TYPE_EXTRA ="content-type";
    public static final String CONTENT_STRING="string";
    public static final String CONTENT_FILE="file";
    public static final String CONTENT_FOLDER="folder";

    public static final String CONTENT_EXTRA ="content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = new Intent(this, GoogleDriveString.class);
                startActivity(signInIntent);
                break;
            // ...
        }
    }


}
