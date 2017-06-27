package it.unibs.cloudondemand;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "it.unibs.cloudondemand.MESSAGE";
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.riga, readFiles(path));
            ListView listView = (ListView) findViewById(R.id.listview);
            listView.setAdapter(adapter);
            }
        catch (IOException e) {
            Toast.makeText(this, "Errore lettura file", Toast.LENGTH_SHORT).show();
        }*/
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
    }

    /**Read files from external storage*/
    public static String [] readFiles(String strFile) throws IOException {
        File file = new File(strFile);

        if(file.exists()) {
            if (file.isFile()) {
                String[] fileList=new String[]{file.getName()};
                return fileList;
            }
            else if (file.isDirectory()) {
                File[] fileList=file.listFiles();
                String[] fileListName=new String[fileList.length];
                for (int i = 0; i<=fileList.length;i++)
                {
                    fileListName[i]=fileList[i].getName();
                }
                return fileListName;
            }
            else
            {
                return null;
            }
        }
        else
            throw new IOException();
    }
}


