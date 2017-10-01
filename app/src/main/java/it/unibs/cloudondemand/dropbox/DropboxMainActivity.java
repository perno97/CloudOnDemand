package it.unibs.cloudondemand.dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.v2.users.FullAccount;
import com.squareup.picasso.Picasso;

import java.io.File;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.URI_to_Path;

public class DropboxMainActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE = 101;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!tokenExists()) {
            //No token
            //Back to LoginActivity to request
            Intent intent = new Intent(this, DropboxLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Retrieve access token from shared pref
        accessToken = retrieveAccessToken();
        // Retrieve user account info and print on screen
        getUserAccount();

        // Set on click listener on floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabOnClickListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK || data==null) return;
        //Check the request
        if(requestCode==IMAGE_REQUEST_CODE){
            File file =new File(URI_to_Path.getPath(getApplication(),data.getData()));
            if(file!=null) {
                new DropboxUploadFile(DropboxClient.getClient(accessToken), file, getApplicationContext()).execute();
            }
        }
    }

    private void getUserAccount() {
        new UserAccountTask(DropboxClient.getClient(accessToken), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                //Print account's info
                Log.d("User", account.getEmail());
                Log.d("User", account.getName().getDisplayName());
                Log.d("User", account.getAccountType().name());
                updateUI(account);
            }

            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account details.");
            }
        }).execute();
    }

    private FloatingActionButton.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Upload to Dropbox"), IMAGE_REQUEST_CODE);
        }
    };

    private boolean tokenExists() {
        return retrieveAccessToken() != null;
    }

    private String retrieveAccessToken() {
        //check if accessToken is stored on previous app launches
        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_dropbox_account), Context.MODE_PRIVATE);
        String accessToken = prefs.getString(getString(R.string.dropbox_access_token), null);
        if (accessToken == null) {
            Log.i("AccessToken Status", "No token found");
            return null;
        } else {
            //accessToken already exists
            Log.i("AccessToken Status", "Token exists");
            return accessToken;
        }
    }

    private void updateUI(FullAccount account) {
        ImageView profile = (ImageView) findViewById(R.id.imageView);
        TextView name = (TextView) findViewById(R.id.name_textView);
        TextView email = (TextView) findViewById(R.id.email_textView);

        name.setText(account.getName().getDisplayName());
        email.setText(account.getEmail());
        Picasso.with(this)
                .load(account.getProfilePhotoUrl())
                .resize(200, 200)
                .into(profile);
    }
}