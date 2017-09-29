package it.unibs.cloudondemand.databaseManager;


import android.provider.BaseColumns;

public class FileListContract {
    private FileListContract() {}

    public static class FileList implements BaseColumns {
        public static final String TABLE_NAME = "fileList";
        public static final String COLUMN_DRIVEID = "driveId";
        public static final String COLUMN_FILEPATH = "filePath";
        public static final String COLUMN_PARENTID = "parentId";
    }

    public static class FolderList implements BaseColumns{
        public static final String TABLE_NAME = "folderList";
        public static final String COLUMN_DRIVEID = "driveId";
        public static final String COLUMN_FOLDERPATH = "folderPath";
        public static final String COLUMN_PARENTID = "parentId";
    }
}
