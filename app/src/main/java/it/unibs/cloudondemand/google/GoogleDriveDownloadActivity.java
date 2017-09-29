package it.unibs.cloudondemand.google;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import it.unibs.cloudondemand.MainActivity;
import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.FileListable;
import it.unibs.cloudondemand.utils.RowAdapter;

public class GoogleDriveDownloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_download);

        HashMap<String, String> itemsList = GoogleDriveUtil.getDatabase(getApplicationContext());
        if(itemsList == null)
            Toast.makeText(this, "Nessun file caricato", Toast.LENGTH_SHORT).show();
        else
            showList(itemsList);
    }

    private void showList(HashMap<String,String> list){
        // Fill listview
        ListView listView = (ListView) findViewById(R.id.select_download_listview);

        ArrayList<FileListable> fileList = new ArrayList<>(list.size());
        fileList.add(new CustomFileDrive(null, "null"));
        for(String key : list.keySet()) {
            CustomFileDrive fileDrive = new CustomFileDrive(key, list.get(key));
            fileList.add(fileDrive);
        }

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
                Toast.makeText(GoogleDriveDownloadActivity.this, "Clicked on = path : " + fileDrive.path + " / driveid : " + fileDrive.driveId, Toast.LENGTH_SHORT).show();
            }
        }
    };

    //TODO bisognerebbe usare filetree
    private class CustomFileDrive implements FileListable {
        String driveId;
        String path;

        private CustomFileDrive(String driveId, String path) {
            this.driveId = driveId;
            this.path = path;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public String getName() {
            return path.substring(path.lastIndexOf('/'));
        }
    }
}
