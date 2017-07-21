package it.unibs.cloudondemand;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import it.unibs.cloudondemand.utils.FileListable;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.PermissionResultCallback;
import it.unibs.cloudondemand.utils.RowAdapter;
import it.unibs.cloudondemand.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private FileAdaptable currentPath = new FileAdaptable(initialPath);
    private final ArrayList<FileListable> currentFileList = new ArrayList<>();

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
                RowAdapter adapter = new RowAdapter(MainActivity.this, currentFileList);
                listView.setAdapter(adapter);
            }
            catch (IOException e) {
                Toast.makeText(MainActivity.this, "Errore lettura file", Toast.LENGTH_SHORT).show();
            }

            // Add on item click listener to listview items
            listView.setOnItemClickListener(onItemClick);
            listView.setOnItemLongClickListener(onItemLongClick);
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
            if (currentPath.isDirectory()) {
                // Clear string array
                currentFileList.clear();
                // Add first directory (back) /..
                currentFileList.add(new FileAdaptable(currentPath.getParentFile()));

                // Fill first with name of folders
                File[] folderList = currentPath.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                Arrays.sort(folderList );
                for (File file : folderList )
                {
                    currentFileList.add(new FileAdaptable(file));
                }

                // Fill first with name of files
                File[] fileList = currentPath.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile();
                    }
                });
                Arrays.sort(fileList);
                for (File file : fileList)
                {
                    currentFileList.add(new FileAdaptable(file));
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
                    currentPath = new FileAdaptable(currentPath.getParentFile());
                    refreshListView = true;
                }
            }
            else {
                // Retrieve file of item clicked
                File selected = (File) parent.getAdapter().getItem(position);

                // Check if is directory or file
                if (selected.isDirectory()) {
                    currentPath = new FileAdaptable(selected);
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

    private final AdapterView.OnItemLongClickListener onItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // Retrieve file of item clicked
            File selected = (File) parent.getAdapter().getItem(position);

            // Check if is directory
            if (selected.isDirectory()) {
                onFileClick(selected);
                return true;
            }

            return false;
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
                        if(selected.isDirectory())
                            sendIntent(LoginActivity.CONTENT_FOLDER, selected.getPath());
                        else
                            sendIntent(LoginActivity.CONTENT_FILE, selected.getPath());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User doesn't want to upload the selected file/folder
                    }
                })
                .create();

        alertDialog.show();
    }

    // Start another activity
    private void sendIntent (String contentType, String content) {
        startActivity(LoginActivity.getIntent(this, contentType, content));
    }

    // Custom File class to use into listview adapter
    private class FileAdaptable extends File implements FileListable {
        private FileAdaptable(String path) {
            super(path);
        }

        private FileAdaptable(File file) {
            super(file.getPath());
        }
    }
}


