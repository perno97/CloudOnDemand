package it.unibs.cloudondemand;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.BoolRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import it.unibs.cloudondemand.google.GoogleDriveDownloadActivity;
import it.unibs.cloudondemand.utils.FileListable;
import it.unibs.cloudondemand.utils.PermissionRequest;
import it.unibs.cloudondemand.utils.RowAdapter;
import it.unibs.cloudondemand.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private FileAdaptable currentPath = new FileAdaptable(initialPath);
    private final ArrayList<FileListable> currentFileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if external storage is readable before filling listview
        if (Utils.isExternalStorageReadable()) {
            // Verify permission and after fill file manager listview
            Intent intent = PermissionRequest.getIntent(this, android.Manifest.permission.READ_EXTERNAL_STORAGE, permissionResultCallback);
            startActivity(intent);
        }
        else {
                Toast.makeText(this, R.string.requested_permission_read_storage, Toast.LENGTH_SHORT).show();
        }

    }

    // Called when user click a button button
    public void onClick(View view) {
        switch (view.getId()) {
            // Send string button
            case R.id.button :
                EditText editText = (EditText) findViewById(R.id.editText);
                String message = editText.getText().toString();
                if(message.isEmpty())
                    Toast.makeText(this, R.string.insert_not_empty_string, Toast.LENGTH_LONG ).show();
                else
                {
                    sendIntent(LoginActivity.CONTENT_STRING, message);
                }
                break;
            // Download button
            case R.id.button2 :
                Intent intent = new Intent(this, GoogleDriveDownloadActivity.class);
                startActivity(intent);
                break;
        }
    }

    // Called when user chose to grant permission
    private final PermissionRequest.PermissionRequestCallback permissionResultCallback = new PermissionRequest.PermissionRequestCallback() {
        @Override
        public void onPermissionResult(int isGranted) {
            // Blocks code if permission is denied
            if(isGranted == PermissionRequest.PERMISSION_DENIED)
                return;

            // Fill listview
            ListView listView = (ListView) findViewById(R.id.listview);

            readFiles();
            RowAdapter adapter = new RowAdapter(MainActivity.this, currentFileList);
            listView.setAdapter(adapter);

            // Add on item click listener to listview items
            listView.setOnItemClickListener(onItemClick);
            listView.setOnItemLongClickListener(onItemLongClick);
        }
    };

    // List files into currentPath
    public void readFiles() {
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
            Log.e(TAG, "File not found on external storage directory.");
            Toast.makeText(this, R.string.unable_read_storage, Toast.LENGTH_SHORT).show();
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
                // Update array with new path
                readFiles();
                // Notify update to listview (update UI)
                ((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();
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
                .setMessage(R.string.dialog_upload_content)
                .setTitle(R.string.dialog_upload_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selected.isDirectory())
                            sendIntent(LoginActivity.CONTENT_FOLDER, selected.getPath());
                        else
                            sendIntent(LoginActivity.CONTENT_FILE, selected.getPath());
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User doesn't want to upload the selected file/folder
                    }
                })
                .create();

        alertDialog.show();
    }

    // Start another activity
    private void sendIntent (int contentType, String content) {
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


