package it.unibs.cloudondemand.google;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import java.io.File;
import java.io.FileFilter;

import it.unibs.cloudondemand.utils.GenericFileTree;

public class GoogleDriveCustomFolder implements GenericFileTree<GoogleDriveCustomFolder> {
    private File folder;
    private DriveFolder driveFolder;
    private File[] files;
    private int currentFile;
    private DriveId[] filesId;

    GoogleDriveCustomFolder(File folder) {
        this.folder = folder;
        this.files = generateFiles(folder);
        this.filesId = new DriveId[files.length];
        this.currentFile = -1;
    }

    private File[] generateFiles (File folder) {
        // List files into the folder
        return folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }

    @Override
    public boolean hasNextFile () {
        return currentFile + 1 != files.length;
    }

    @Override
    public File nextFile () {
        return files[++currentFile];
    }

    DriveFolder getDriveFolder () {
        return driveFolder;
    }

    void setDriveFolder (DriveFolder driveFolder) {
        this.driveFolder = driveFolder;
    }

    @Override
    public String getFolderName () {
        return folder.getName();
    }


    void setFileId (DriveId driveId) {
        filesId[currentFile] = driveId;
    }

    @Override
    public GoogleDriveCustomFolder[] getSubFolders() {
        // List directories into the folder
        File[] directories = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        GoogleDriveCustomFolder[] folders = new GoogleDriveCustomFolder[directories.length];
        for (int i = 0; i < directories.length; i++) {
            folders[i] = new GoogleDriveCustomFolder(directories[i]);
        }

        return folders;
    }
}
