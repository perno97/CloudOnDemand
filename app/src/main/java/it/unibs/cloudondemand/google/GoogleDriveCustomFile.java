package it.unibs.cloudondemand.google;

import com.google.android.gms.drive.DriveFolder;

import java.io.File;
import java.io.FileFilter;

public class GoogleDriveCustomFile {
    private GoogleDriveCustomFile parentFolder;

    private File thisFolder;
    private DriveFolder thisDriveFolder;

    private GoogleDriveCustomFile[] subFolders;
    private int currentSubFolder;

    private File[] files;
    private int currentFile = 0;

    public GoogleDriveCustomFile(GoogleDriveCustomFile parentFolder, File thisFolder) {
        this.parentFolder = parentFolder;
        this.thisFolder = thisFolder;
        this.subFolders = generateSubFolders(thisFolder);
        this.currentSubFolder = 0;
        this.files = generateFiles(thisFolder);
        this.currentFile = 0;
    }

    private GoogleDriveCustomFile[] generateSubFolders (File folder) {
        // List directories into the folder
        File[] directories = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        // Fill array with GoogleDriveCustomFile objects
        GoogleDriveCustomFile[] folders = new GoogleDriveCustomFile[directories.length];
        int i = 0;
        for (File directory : directories) {
            folders[i] = new GoogleDriveCustomFile(this, directory);
            i++;
        }

        return folders;
    }

    private File[] generateFiles (File folder) {
        // List files into the folder
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        return files;
    }

    //IMPORTANTE
    private GoogleDriveCustomFile getCurrentMyFile (GoogleDriveCustomFile googleDriveCustomFile) {
        int i = googleDriveCustomFile.currentSubFolder - 1;
        if(i == -1) {
            return googleDriveCustomFile;
        }
        else {
            return getCurrentMyFile(subFolders[i]);
        }
    }

    public GoogleDriveCustomFile getCurrentMyFile () {
        return getCurrentMyFile(this);
    }

    public File nextFile () {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentMyFile();
        if (currentGoogleDriveCustomFile.files.length == currentGoogleDriveCustomFile.currentFile)
            return null;
        else
            return currentGoogleDriveCustomFile.files[currentGoogleDriveCustomFile.currentFile++];
    }

    public GoogleDriveCustomFile nextSubFolder () {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentMyFile();
        if (currentGoogleDriveCustomFile.subFolders.length == currentGoogleDriveCustomFile.currentSubFolder)
            return null;
        else
            return currentGoogleDriveCustomFile.subFolders[currentGoogleDriveCustomFile.currentSubFolder++];
    }

    private GoogleDriveCustomFile nextSubFolder (GoogleDriveCustomFile googleDriveCustomFile) {
        if (googleDriveCustomFile.subFolders.length == googleDriveCustomFile.currentSubFolder)
            return null;
        else
            return googleDriveCustomFile.subFolders[googleDriveCustomFile.currentSubFolder++];
    }

    public GoogleDriveCustomFile nextParentSubFolder () {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentMyFile();
        if (!currentGoogleDriveCustomFile.hasParentFolder())
            return null;
        GoogleDriveCustomFile parentFolder = currentGoogleDriveCustomFile.parentFolder;
        return parentFolder.nextSubFolder(parentFolder);
    }

    // Set drive folder to thisDriveFolder if currentSubFolder is <0, else to subFolder[currentSubFolder-1]
    public void setDriveFolder (DriveFolder driveFolder) {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentMyFile();
        currentGoogleDriveCustomFile.thisDriveFolder = driveFolder;
    }

    public boolean hasParentFolder () {
        return parentFolder != null;
    }

    public boolean hasNextFile () {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentMyFile();
        return currentGoogleDriveCustomFile.files.length != currentGoogleDriveCustomFile.currentFile;
    }

    public boolean hasNextSubFolder () {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentMyFile();
        return currentGoogleDriveCustomFile.subFolders.length != currentGoogleDriveCustomFile.currentSubFolder;
    }

    public File getThisFolder () {
        return thisFolder;
    }

    public DriveFolder getThisDriveFolder () {
        return thisDriveFolder;
    }

    public GoogleDriveCustomFile getParentFolder () {
        return parentFolder;
    }

    public DriveFolder getCurrentDriveFolder () {
        return getCurrentMyFile().thisDriveFolder;
    }
}
