package it.unibs.cloudondemand.dropbox;

/**
 * Classe per il caricamento di file nel cloud Dropbox
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import it.unibs.cloudondemand.R;
import it.unibs.cloudondemand.utils.ProgressNotification;

public class DropboxUploadFile extends AsyncTask {
    private DbxClientV2 dbxClient;
    private File file;
    private Context context;

    // Foreground notification
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    /**
     * Upload di un file
     * @param dbxClient client Dropbox
     * @param file file da caricare
     * @param context
     */
    DropboxUploadFile(DbxClientV2 dbxClient, File file, Context context)
    {
        this.dbxClient=dbxClient;
        this.file=file;
        this.context=context;
        // Inzializzazione attributi
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Operazioni eseguite in Background
     * @param params
     * @return
     */
    @Override
    protected Object doInBackground(Object[] params)
    {
        try{
            InputStream inputStream = new FileInputStream(file);

            // Mostra notifica
            ProgressNotification progressNotification = new ProgressNotification(context, context.getString(R.string.dropbox_uploading_file), file.getName(), true);
            mNotificationManager.notify(NOTIFICATION_ID, progressNotification.getNotification());

            // Inizio upload (Sempre overwrite dei file già caricati)
            dbxClient.files().uploadBuilder(file.getPath()).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);

            Log.d("Upload Status", "Login eseguito con successo");
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Notifica di avvenuto caricamento
     * @param o
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        mNotificationManager.notify(NOTIFICATION_ID, new Notification.Builder(context).setContentTitle(context.getString(R.string.dropbox_uploaded)).setContentText(file.getName()).setSmallIcon(ProgressNotification.NOTIFICATION_ICON).build());
    }

}