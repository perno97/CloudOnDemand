package it.unibs.cloudondemand.databaseManager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class FileListDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_TABLE_FILES =
            "CREATE TABLE " + FileListContract.FileList.TABLE_NAME + " (" +
                    FileListContract.FileList.COLUMN_DRIVEID + " TEXT PRIMARY KEY," +
                    FileListContract.FileList.COLUMN_FILEPATH + " TEXT)";

    private static final String SQL_CREATE_TABLE_FOLDERS =
            "CREATE TABLE " + FileListContract.FolderList.TABLE_NAME + " (" +
                    FileListContract.FolderList.COLUMN_DRIVEID + " TEXT PRIMARY KEY," +
                    FileListContract.FolderList.COLUMN_FOLDERPATH + " TEXT)";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FileList.db";

    public FileListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_FILES);
        db.execSQL(SQL_CREATE_TABLE_FOLDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nada
    }
}
