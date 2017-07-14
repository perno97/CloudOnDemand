package it.unibs.cloudondemand.google;

import com.google.android.gms.drive.DriveFolder;

import java.io.File;
import java.io.FileFilter;

class GoogleDriveCustomFile {
    private GoogleDriveCustomFile parentFolder;

    private File thisFolder;
    private DriveFolder thisDriveFolder;

    private GoogleDriveCustomFile[] subFolders;
    private int currentSubFolder;

    private File[] files;
    private int currentFile = 0;

    GoogleDriveCustomFile(GoogleDriveCustomFile parentFolder, File thisFolder) {
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
    private GoogleDriveCustomFile getCurrentFolder(GoogleDriveCustomFile googleDriveCustomFile) {
        int i = googleDriveCustomFile.currentSubFolder - 1;
        if(i == -1) {
            return googleDriveCustomFile;
        }
        else {
            return getCurrentFolder(subFolders[i]);
        }
    }

    GoogleDriveCustomFile getCurrentFolder() {
        return getCurrentFolder(this);
    }

    File nextFile () {
        if (files.length == currentFile)
            return null;
        else
            return files[currentFile++];
    }

    GoogleDriveCustomFile nextSubFolder () {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentFolder();
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

    GoogleDriveCustomFile nextParentSubFolder () {
        if (!hasParentFolder())
            return null;
        return parentFolder.nextSubFolder(parentFolder);
    }

    // Set drive folder to thisDriveFolder if currentSubFolder is <0, else to subFolder[currentSubFolder-1]
    void setDriveFolder (DriveFolder driveFolder) {
        GoogleDriveCustomFile currentGoogleDriveCustomFile = getCurrentFolder();
        currentGoogleDriveCustomFile.thisDriveFolder = driveFolder;
    }

    boolean hasParentFolder () {
        return parentFolder != null;
    }

    boolean hasNextFile () {
        return files.length != currentFile;
    }

    boolean hasNextSubFolder () {
        return subFolders.length != currentSubFolder;
    }

    File getThisFolder () {
        return thisFolder;
    }

    DriveFolder getThisDriveFolder () {
        return thisDriveFolder;
    }

    GoogleDriveCustomFile getParentFolder () {
        return parentFolder;
    }

    DriveFolder getCurrentDriveFolder () {
        return getCurrentFolder().thisDriveFolder;
    }
}
