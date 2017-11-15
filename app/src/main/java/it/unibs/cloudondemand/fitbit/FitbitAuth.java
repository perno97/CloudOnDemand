package it.unibs.cloudondemand.fitbit;

/**
 * Classe per l'autenticazione mediante Fitbit
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Iterator;

import it.unibs.cloudondemand.R;

public class FitbitAuth extends FitbitConnection {
    private static final String TAG = "FitbitAuth";

    private static final String SCOPE = "profile";

    private FitbitToken token;

    /**
     * Impostazione iniziale del layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitbit_auth);
    }

    /**
     * Inoltro della richiesta di autenticazione se presente un token
     * @param token Token acquired.
     */
    @Override
    public void onTokenAcquired(FitbitToken token) {
        if(token == null) {
            Toast.makeText(this, getString(R.string.unable_connect_account), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.token = token;

        // Make API request
        makeAPIRequestGet(token, "https://api.fitbit.com/1/user/" + token.getUserId() + "/profile.json", profileResponse);
    }

    /**
     * Gestione della richiesta dei dati del profilo
     */
    private OnAPIResponse profileResponse = new OnAPIResponse() {
        @Override
        public void onResponse(JSONObject response, URL urlRequest) {
            // Check if is a valid response
            if (response == null) {
                Toast.makeText(FitbitAuth.this, "Error occurred while calling fitbit server.", Toast.LENGTH_SHORT).show();
                return;
            }

            TextView mTextView = (TextView) findViewById(R.id.fitbit_response_text);
            mTextView.setText("");
            try {
                JSONObject user = response.getJSONObject("user");
                Iterator<String> keys = user.keys();
                String key = keys.next();
                while (keys.hasNext()) {
                    mTextView.append(key + " : " + user.getString(key) + "\n");
                    key = keys.next();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error occurred while reading user json object. " + e.toString());
                mTextView.setText(R.string.unable_connect_account);
            }
        }
    };

    /**
     * Intent per avviare questa activity
     * @param context contesto richiamante
     * @return intent del chiamante
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, FitbitAuth.class);
    }

    @Override
    public String getScopes() {
        return SCOPE;
    }
}
