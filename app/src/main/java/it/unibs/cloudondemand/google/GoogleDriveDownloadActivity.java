package it.unibs.cloudondemand.google;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.databaseManager.FileListDbHelper;
import it.unibs.cloudondemand.utils.FileListable;
import it.unibs.cloudondemand.utils.RowAdapter;

public class GoogleDriveDownloadActivity extends AppCompatActivity {
    private final boolean FILE = false;
    private final boolean DIRECTORY = true;
    private HashMap<String, String> listFiles;
    private HashMap<String, String> listFolders;
    private boolean signOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileListDbHelper mDbHelper = new FileListDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Intent intent = getIntent();
        signOut = intent.getBooleanExtra(GoogleDriveConnection.SIGN_OUT_EXTRA, false);

        setContentView(R.layout.activity_google_drive_download);

        String accountId = GoogleDriveUtil.getAccountIdSignedIn(this);

        listFiles = GoogleDriveUtil.getFiles(db, accountId);
        listFolders = GoogleDriveUtil.getFolders(db, accountId);
        if (listFiles == null && listFolders == null)
            Toast.makeText(this, "Nessun file caricato", Toast.LENGTH_SHORT).show();
        else
            showList();
    }

    private void showList(){
        // Fill listview
        ListView listView = (ListView) findViewById(R.id.select_download_listview);

        ArrayList<FileListable> fileList = new ArrayList<>(listFiles.size());
        fileList.add(new CustomFileDrive(null, "null", false));
        if(listFiles != null) {
            for(String key : listFiles.keySet()) {
                CustomFileDrive fileDrive = new CustomFileDrive(key, listFiles.get(key), FILE);
                fileList.add(fileDrive);
            }
        }

        if(listFolders != null) {
            for(String key : listFolders.keySet()) {
                CustomFileDrive fileDrive = new CustomFileDrive(key, listFolders.get(key), DIRECTORY);
                fileList.add(fileDrive);
            }
        }

        // Create adapter and set to listview
        RowAdapter adapter = new RowAdapter(this, fileList);
        listView.setAdapter(adapter);

        // Add on item click listener to listview items
        listView.setOnItemClickListener(onItemClick);
    }

    // Callback click on listview item
    private final AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Check if clicked on back directory
            if (position == 0) {
                Toast.makeText(GoogleDriveDownloadActivity.this, "Non puoi tornare indietro!", Toast.LENGTH_SHORT).show();
            }
            else {
                // Retrieve drive file of item clicked
                CustomFileDrive fileDrive = (CustomFileDrive) parent.getAdapter().getItem(position);

                //Check wether it's a file or a directory
                if(fileDrive.isDirectory())
                    //downloadFolder(fileDrive.path, fileDrive.driveId);
                    Toast.makeText(GoogleDriveDownloadActivity.this, "Funzione disponibile a breve", Toast.LENGTH_SHORT).show();//TODO rimuovere
                else
                    downloadFile(fileDrive.path, fileDrive.driveId);
            }
        }
    };

    //TODO bisognerebbe usare filetree
    private class CustomFileDrive implements FileListable {
        String driveId;
        String path;
        boolean type;

        private CustomFileDrive(String driveId, String path, boolean type) {
            this.driveId = driveId;
            this.path = path;
            this.type = type;
        }

        @Override
        public boolean isDirectory() {
            return type;
        }

        @Override
        public String getName() {
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

    private void downloadFile(String destinationPath, String driveId){
        startService(GoogleDriveDownloadFile.getIntentFile(getApplicationContext(),  destinationPath, driveId, signOut));
    }

    private void downloadFolder(String destinationPath, String driveId){
        HashMap<String, String> toDownload = null;

        for(String key : listFiles.keySet()) {
            if(listFiles.get(key).startsWith(destinationPath)){
                toDownload.put(key,listFiles.get(key));
            }
        }

        if(toDownload == null)
            Toast.makeText(this, "Cartella vuota", Toast.LENGTH_SHORT).show();
        else
            startService(GoogleDriveDownloadFile.getIntentFolder(getApplicationContext(), toDownload));
    }
}
