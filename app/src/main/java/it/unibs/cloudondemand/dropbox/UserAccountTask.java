package it.unibs.cloudondemand.dropbox;

/**
 * Classe che rappresenta i dettagli della richiesta per l'account utente
 */

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

class UserAccountTask extends AsyncTask<Void, Void, FullAccount> {
    private DbxClientV2 dbxClient;
    private TaskDelegate delegate;
    private Exception error;

    /**
     * Restituisce le info dell'account all'activity che le richiede
     */
    interface TaskDelegate {
        void onAccountReceived(FullAccount account);
        void onError(Exception error);
    }

    /**
     * Costruttore
     * @param dbxClient client Dropbox
     * @param delegate
     */
    UserAccountTask(DbxClientV2 dbxClient, TaskDelegate delegate) {
        this.dbxClient =dbxClient;
        this.delegate = delegate;
    }

    /**
     * Recupero delle informazioni degli utenti salvati
     * @param params
     * @return
     */
    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            return dbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
            error = e;
        }
        return null;
    }

    /**
     * Azioni da eseguire dopo aver recuperato le info degli account
     * @param account account estratti
     */
    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account != null && error == null){
            delegate.onAccountReceived(account);
        }
        else {
            delegate.onError(error);
        }
    }
}
