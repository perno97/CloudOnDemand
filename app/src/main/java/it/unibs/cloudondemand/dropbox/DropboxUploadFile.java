package it.unibs.cloudondemand.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DropboxUploadFile extends AsyncTask {
    private DbxClientV2 dbxClient;
    private File file;
    private Context context;

    DropboxUploadFile(DbxClientV2 dbxClient, File file, Context context)
    {
        this.dbxClient=dbxClient;
        this.file=file;
        this.context=context;
    }

    @Override
    protected Object doInBackground(Object[] params)
    {
        try{
            //Upload to Dropbox
            InputStream inputStream=new FileInputStream(file);
            dbxClient.files().uploadBuilder(file.getName()).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            //Always overwrite existing file
            Log.d("Upload Status", "Login eseguito con successo");
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show();
    }

}