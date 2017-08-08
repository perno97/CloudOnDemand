package it.unibs.cloudondemand.google;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class StopServices extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static final String TAG = "StopServices";

    // Extra to obtain the service to stop
    public static final String SERVICE_EXTRA = "service";
    public static final int SERVICE_UPLOAD_STRING = 0;
    public static final int SERVICE_UPLOAD_FILE = 1;
    public static final int SERVICE_UPLOAD_FOLDER = 2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent toStop = null;

        switch (intent.getIntExtra(SERVICE_EXTRA, -1)) {
            case SERVICE_UPLOAD_STRING :
                toStop = new Intent(this, GoogleDriveUploadString.class);
                break;
            case SERVICE_UPLOAD_FILE :
                toStop = new Intent(this, GoogleDriveUploadFileSingle.class);
                break;
            case SERVICE_UPLOAD_FOLDER :
                toStop = new Intent(this, GoogleDriveUploadFileFolder.class);
                break;
        }

        Log.i(TAG, "Stop google service. Type : " + intent.getIntExtra(SERVICE_EXTRA, -1));
        // Stop the service
        stopService(toStop);

        // Finished
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public static Intent getStopIntent(Context context, int serviceExtra) {
        Intent intent = new Intent(context, StopServices.class);
        intent.putExtra(SERVICE_EXTRA, serviceExtra);
        return intent;
    }
}
