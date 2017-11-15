package it.unibs.cloudondemand.dropbox;

/**
 * Main Activity di Dropbox, mostra le informazioni dell'account dell'utente
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.v2.users.FullAccount;
import com.squareup.picasso.Picasso;

import java.io.File;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.R;

public class DropboxMainActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE = 101;
    private String accessToken;

    /**
     * Verifica che l'utente sia loggato, in caso contrario ritorna alla login activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String pathFileToUpload = intent.getStringExtra(LoginActivity.CONTENT_EXTRA);

        if (!tokenExists()) {
            Intent loginIntent = new Intent(this, DropboxLoginActivity.class);
            loginIntent.putExtra(LoginActivity.CONTENT_EXTRA, pathFileToUpload);
            startActivity(loginIntent);

            finish();
            return;
        }

        // Recupera access token da shared pref
        accessToken = retrieveAccessToken();
        // Recupera le credenziali utente e le mostra
        getUserAccount();

        // Inizio upload
        if(pathFileToUpload != null)
            new DropboxUploadFile(DropboxClient.getClient(accessToken), new File(pathFileToUpload), this).execute();
        else
            Log.i("DropboxMain", "No file passed to activity");
    }

    /**
     * Recupero delle credenziali dell'utente già loggato
     */
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

    /**
     * Verifica della presenza del token
     * @return boolean che attesta o meno la presenza del token
     */
    private boolean tokenExists() {
        return retrieveAccessToken() != null;
    }

    /**
     * Controlla se l'access token è già stato salvato in esecuzioni precedenti dell'applicazione
     * @return
     */
    private String retrieveAccessToken() {
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

    /**
     * Impostazione della UI con le informazioni dell'utente
     * @param account informazioni account
     */
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

    /**
     * Intent per avviare questa activity
     * @param context contesto richiamante
     * @param path content dell'intent
     * @return intent del chiamante
     */
    public static Intent getIntent(Context context, String path) {
        Intent intent = new Intent(context, DropboxMainActivity.class);
        intent.putExtra(LoginActivity.CONTENT_EXTRA, path);
        return intent;
    }
}