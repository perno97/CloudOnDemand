package it.unibs.cloudondemand.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

import it.unibs.cloudondemand.R;

public class Utils {
    /**
     * Check if external storage is mounted and readable.
     * @return True if mounted and readable, False otherwise.
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Check if internet connection is available.
     * @param context A context.
     * @return True if connected, False otherwise.
     */
    public static boolean checkInternetConnections(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    /**
     * Check if internet connection is over mobile data.
     * @param context A context.
     * @return True if is connected whit mobile, False otherwise.
     */
    public static boolean isConnectedWithMobile(Context context) {
        if(!checkInternetConnections(context))
            return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
    }

    // Extensions of files
    private static String[] imageExtensions = {"png", "jpg", "jpeg", "gif", "tif", "svg"};
    private static String[] audioExtensions = {"mp3", "m4a", "mpa", "wav", "wma", "aac"};
    private static String[] videoExtensions = {"mp4", "3g2", "3gp", "avi", "flv", "m4v", "mov", "mpg", "wmv"};
    private static String[] textExtensions = {"txt", "csv", "log", "html", "htm", "js", "css", "php", "xml", "c", "cpp", "py", "sh"};
    private static String[] wordExtensions = {"doc", "docx", "odt"};
    private static String[] spreadsheetExtensions = {"xls", "xlsx", "xlr"};
    private static String[] pdfExtensions = {"pdf"};
    private static String[] compressedArchiveExtensions = {"zip", "rar", "tar", "gz", "7z"};

    // Used by some methods to check extension
    private static boolean isXFile (String fileExtension, String[] extensions) {
        fileExtension = fileExtension.toLowerCase();
        for (String extension : extensions) {
            if(fileExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isImageFile (String fileExtension) {
        return isXFile(fileExtension, imageExtensions);
    }
    private static boolean isAudioFile (String fileExtension) {
        return isXFile(fileExtension, audioExtensions);
    }
    private static boolean isVideoFile (String fileExtension) {
        return isXFile(fileExtension, videoExtensions);
    }
    private static boolean isTextFile (String fileExtension) {
        return isXFile(fileExtension, textExtensions);
    }
    private static boolean isWordFile (String fileExtension) {
        return isXFile(fileExtension, wordExtensions);
    }
    private static boolean isSpreadsheetFile (String fileExtension) {
        return isXFile(fileExtension, spreadsheetExtensions);
    }
    private static boolean isPdfFile (String fileExtension) {
        return isXFile(fileExtension, pdfExtensions);
    }
    private static boolean isCompressedArchiveFile (String fileExtension) {
        return isXFile(fileExtension, compressedArchiveExtensions);
    }

    /**
     * Return an icon for the file with a specific extension.
     * @param extension Extension of file.
     * @return Drawable resource id of icon.
     */
    static int getFileIcon (String extension) {
        int drawableResource;

        if (Utils.isImageFile(extension))
            drawableResource = R.drawable.ic_file_image;

        else if (Utils.isAudioFile(extension))
            drawableResource = R.drawable.ic_file_music;

        else if (Utils.isVideoFile(extension))
            drawableResource = R.drawable.ic_file_video;

        else if (Utils.isTextFile(extension))
            drawableResource = R.drawable.ic_file_document;

        else if (Utils.isWordFile(extension))
            drawableResource = R.drawable.ic_file_word;

        else if (Utils.isSpreadsheetFile(extension))
            drawableResource = R.drawable.ic_file_excel;

        else if (Utils.isTextFile(extension))
            drawableResource = R.drawable.ic_file_document;

        else if (Utils.isCompressedArchiveFile(extension))
            drawableResource = R.drawable.ic_file_compressed_archive;

        else if (Utils.isPdfFile(extension))
            drawableResource = R.drawable.ic_file_pdf;

        else
            drawableResource = R.drawable.ic_file_empty;

        return drawableResource;
    }
}
