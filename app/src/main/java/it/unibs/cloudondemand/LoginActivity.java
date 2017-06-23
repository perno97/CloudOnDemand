package it.unibs.cloudondemand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import it.unibs.cloudondemand.google.LoginGoogle;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //Sign in when the user clicks on Google sign-in button
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        onClick(findViewById(R.id.sign_in_button));

    }

    @Override
    public void onClick(View v)
    {
            Intent signInIntent = new Intent(this, LoginGoogle.class);
            startActivity(signInIntent);
    }
}
