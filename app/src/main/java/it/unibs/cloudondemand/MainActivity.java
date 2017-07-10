package it.unibs.cloudondemand;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.PermissionResultCallback;
import it.unibs.cloudondemand.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private File currentPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
    private File[] currentFileList;
    private final ArrayList<String> currentFileListString = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if external storage is readable
        if (Utils.isExternalStorageReadable()) {
            // Verify permission and after fill listview
            Intent intent = PermissionRequest.getRequestPermissionIntent(this, android.Manifest.permission.READ_EXTERNAL_STORAGE, permissionResultCallback);
            startActivity(intent);
        }
        else {
                Toast.makeText(this, "Errore lettura file", Toast.LENGTH_SHORT).show();
        }

    }

    // Called when user chose to grant permission
    private final PermissionResultCallback permissionResultCallback = new PermissionResultCallback() {
        @Override
        public void onPermissionResult(int isGranted) {
            // Blocks code if permission is denied
            if(isGranted == PermissionRequest.PERMISSION_DENIED)
                return;

            // Fill listview
            ListView listView = (ListView) findViewById(R.id.listview);
            try {
                readFiles();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row, R.id.riga, currentFileListString);
                listView.setAdapter(adapter);
            }
            catch (IOException e) {
                Toast.makeText(MainActivity.this, "Errore lettura file", Toast.LENGTH_SHORT).show();
            }

            // Add on item click listener to listview items
            listView.setOnItemClickListener(onItemClick);
        }
    };

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        if(message.isEmpty())
            Toast.makeText(this, "Inserisci una stringa non vuota", Toast.LENGTH_LONG ).show();
        else
        {
            intent.putExtra(LoginActivity.CONTENT_TYPE_EXTRA, LoginActivity.CONTENT_STRING);
            intent.putExtra(LoginActivity.CONTENT_EXTRA, message);
            startActivity(intent);
        }
    }

    /**Read files from external storage*/ //TODO gestire IOEXceotion
    public void readFiles() throws IOException {
        if(currentPath.exists()) {
            if (currentPath.isFile()) {
                // Open popup when clicked on file ... listview doesn't change
                /*
                String[] fileList=new String[]{file.getName()};
                return fileList; */
            }
            else if (currentPath.isDirectory()) {
                currentFileList=currentPath.listFiles();
                // Clear string array
                currentFileListString.clear();
                // Add first directory (back) /..
                currentFileListString.add("/..");
                // Fill it with name of files
                for (int i = 0; i<currentFileList.length;i++)
                {
                    File temp = currentFileList[i];
                    if (temp.isDirectory())
                        currentFileListString.add(currentFileList[i].getName() + "/");
                    else
                        currentFileListString.add(currentFileList[i].getName());
                }
            }
        }
        else {
            throw new FileNotFoundException();
        }
    }

    // Callback click su listview item
    private final AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            boolean refreshListView = false;

            if (position == 0) {
                currentPath = currentPath.getParentFile();
                refreshListView = true;
            }
            else {
                File selected = currentFileList[position-1];
                if (selected.isDirectory()) {
                    currentPath = selected;
                    refreshListView = true;
                }
                else if (selected.isFile()) {
                    Toast.makeText(MainActivity.this, "E' un file", Toast.LENGTH_SHORT).show();
                }
            }


            if (refreshListView) {
                //TODO rimuovere try catch
                try {
                    // Update array with new path
                    readFiles();
                    // Notify update to listview
                    ((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}


