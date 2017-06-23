package it.unibs.cloudondemand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import it.unibs.cloudondemand.google.Login;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "it.unibs.cloudondemand.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        if(message.isEmpty())
            Toast.makeText(this, "Inserisci una stringa non vuota", Toast.LENGTH_LONG ).show();
        else
        {
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }

        Login log=new Login(this);
        log.doLogin();


    }
}

