package it.unibs.cloudondemand.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileFilter;

public class Utils {
    // Check if external storage is readable
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    // Get array of subdirectories into a folder
    public static File[] getSubdirectories (File folder) {
        File[] directories = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        return directories;
    }
}
