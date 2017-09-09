package it.unibs.cloudondemand.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

public class Utils {
    // Check if external storage is readable
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
    
    // Check if internet connection is available. True if connected, False otherwise.
    public static boolean checkInternetConnections(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    // Check if internet connection is over mobile data. True if is connected whit mobile, False otherwise.
    public static boolean isConnectedWithMobile(Context context) {
        if(!checkInternetConnections(context))
            return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
    }

    // Check extension of files
    private static String[] imageExtensions = {"png", "jpg", "jpeg", "gif", "tif", "svg"};
    private static String[] audioExtensions = {"mp3", "m4a", "mpa", "wav", "wma", "aac"};
    private static String[] videoExtensions = {"mp4", "3g2", "3gp", "avi", "flv", "m4v", "mov", "mpg", "wmv"};
    private static String[] textExtensions = {"txt", "csv", "log", "html", "htm", "js", "css", "php", "xml", "c", "cpp", "py", "sh"};
    private static String[] wordExtensions = {"doc", "docx", "odt"};
    private static String[] spreadsheetExtensions = {"xls", "xlsx", "xlr"};
    private static String[] pdfExtensions = {"pdf"};
    private static String[] compressedArchiveExtensions = {"zip", "rar", "tar", "gz", "7z"};

    private static boolean isXFile (String fileExtension, String[] extensions) {
        fileExtension = fileExtension.toLowerCase();
        for (String extension : extensions) {
            if(fileExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    static boolean isImageFile (String fileExtension) {
        return isXFile(fileExtension, imageExtensions);
    }
    static boolean isAudioFile (String fileExtension) {
        return isXFile(fileExtension, audioExtensions);
    }
    static boolean isVideoFile (String fileExtension) {
        return isXFile(fileExtension, videoExtensions);
    }
    static boolean isTextFile (String fileExtension) {
        return isXFile(fileExtension, textExtensions);
    }
    static boolean isWordFile (String fileExtension) {
        return isXFile(fileExtension, wordExtensions);
    }
    static boolean isSpreadsheetFile (String fileExtension) {
        return isXFile(fileExtension, spreadsheetExtensions);
    }
    static boolean isPdfFile (String fileExtension) {
        return isXFile(fileExtension, pdfExtensions);
    }
    static boolean isCompressedArchiveFile (String fileExtension) {
        return isXFile(fileExtension, compressedArchiveExtensions);
    }
}
