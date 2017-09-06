package it.unibs.cloudondemand.utils;

import android.os.Environment;

public class Utils {
    // Check if external storage is readable
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    // Check if external storage is readable and writable
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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
