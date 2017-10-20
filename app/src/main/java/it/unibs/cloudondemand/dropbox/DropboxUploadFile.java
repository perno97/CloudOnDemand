package it.unibs.cloudondemand.dropbox;

import android.app.Notification;
import android.app.NotificationManager;
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

import it.unibs.cloudondemand.utils.ProgressNotification;

public class DropboxUploadFile extends AsyncTask {
    private DbxClientV2 dbxClient;
    private File file;
    private Context context;

    // Foreground notification
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    DropboxUploadFile(DbxClientV2 dbxClient, File file, Context context)
    {
        this.dbxClient=dbxClient;
        this.file=file;
        this.context=context;
        // Initialize attributes
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected Object doInBackground(Object[] params)
    {
        try{
            InputStream inputStream = new FileInputStream(file);

            // Show notification
            ProgressNotification progressNotification = new ProgressNotification(context, "Upload su Dropbox", file.getName(), true);
            mNotificationManager.notify(NOTIFICATION_ID, progressNotification.getNotification());

            // Start upload (Always overwrite existing file)
            dbxClient.files().uploadBuilder(file.getPath()).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);

            // Show last notification
            mNotificationManager.notify(NOTIFICATION_ID, new Notification.Builder(context).setContentTitle("Upload su Dropbox completato").setContentText(file.getName()).setSmallIcon(ProgressNotification.NOTIFICATION_ICON).build());
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