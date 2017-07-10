package it.unibs.cloudondemand;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
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
    private static final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private File currentPath = new File(initialPath);
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
            sendIntent(LoginActivity.CONTENT_STRING, message);
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
                File[] currentFileList=currentPath.listFiles();
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

    // Callback click on listview item
    private final AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Store if the listview should to be refreshed
            boolean refreshListView = false;

            // Check if clicked on back directory
            if (position == 0) {
                // Check if is possible to go back (not if is already in initial dir)
                if (!currentPath.getAbsolutePath().equals(initialPath)) {
                    currentPath = currentPath.getParentFile();
                    refreshListView = true;
                }
            }
            else {
                // Retrieve file name of item clicked
                String currentFileName = ((ArrayAdapter<String>) parent.getAdapter()).getItem(position);
                // File on which user clicked
                File selected = new File(currentPath.getPath() + File.separator + currentFileName);

                // Check if is directory or file
                if (selected.isDirectory()) {
                    currentPath = selected;
                    refreshListView = true;
                }
                else if (selected.isFile()) {
                    onFileClick(selected);
                }
            }


            if (refreshListView) {
                //TODO rimuovere try catch
                try {
                    // Update array with new path
                    readFiles();
                    // Notify update to listview (update UI)
                    ((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // Called when user click on a file in path finder UI
    private void onFileClick (final File selected) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Vuoi caricare il file selezionato?")   //TODO spostare in values/string
                .setTitle("Carica")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendIntent(LoginActivity.CONTENT_FILE, selected.getPath());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User doesn't want to upload the selected file
                    }
                })
                .create();

        alertDialog.show();
    }

    // Start another activity
    private void sendIntent (String contentType, String content) {
        startActivity(LoginActivity.getIntent(this, contentType, content));
    }
}


