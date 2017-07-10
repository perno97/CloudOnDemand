package it.unibs.cloudondemand.utils;

import android.os.Environment;

public class Utils {
    // Check if external storage is readable
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
