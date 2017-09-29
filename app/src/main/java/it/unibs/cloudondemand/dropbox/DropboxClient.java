package it.unibs.cloudondemand.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

/*Getting user account information is a network request, that is handled asynchronously
so i need a Dropbox client*/

public class DropboxClient {
    public static DbxClientV2 getClient(String ACCESS_TOKEN) { //ACCESS_TOKEN is recived during the login
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        return client;
    }
}
