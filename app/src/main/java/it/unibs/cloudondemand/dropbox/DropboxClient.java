package it.unibs.cloudondemand.dropbox;

/**
 * Classe necessaria per l'accesso alle informazioni dell'account utente.
 * Questa procedura è una network request gestita in modo asincrono, per questo necessito
 * di un Dropbox client
 */

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

class DropboxClient {
    /**
     * Metodo per recuperare le credenziali dell'utente
     * @param ACCESS_TOKEN ricevuto durante il login
     * @return
     */
    static DbxClientV2 getClient(String ACCESS_TOKEN) {
        // Creazione Dropbox Client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        return client;
    }
}
