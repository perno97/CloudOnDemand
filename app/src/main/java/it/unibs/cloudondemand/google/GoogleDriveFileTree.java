package it.unibs.cloudondemand.google;

import com.google.android.gms.drive.DriveFolder;

import java.io.File;
import java.io.FileFilter;

class GoogleDriveFileTree {
    private GoogleDriveFileTree parentFolder;

    private GoogleDriveCustomFolder thisFolder;

    private GoogleDriveFileTree[] subFolders;
    private int currentSubFolder;


    GoogleDriveFileTree(GoogleDriveFileTree parentFolder, File thisFolder) {
        this.parentFolder = parentFolder;
        this.thisFolder = new GoogleDriveCustomFolder(thisFolder);
        this.subFolders = generateSubFolders(thisFolder);
        this.currentSubFolder = -1;
    }

    private GoogleDriveFileTree[] generateSubFolders (File folder) {
        // List directories into the folder
        File[] directories = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        // Fill array with GoogleDriveFileTree objects
        GoogleDriveFileTree[] folders = new GoogleDriveFileTree[directories.length];
        int i = 0;
        for (File directory : directories) {
            folders[i] = new GoogleDriveFileTree(this, directory);
            i++;
        }

        return folders;
    }



    //IMPORTANTE
    private GoogleDriveFileTree getCurrentFolder(GoogleDriveFileTree googleDriveFileTree) {
        int i = googleDriveFileTree.currentSubFolder;
        if(i == -1) {
            return googleDriveFileTree;
        }
        else {
            return getCurrentFolder(subFolders[i]);
        }
    }

    GoogleDriveFileTree getCurrentFolder() {
        return getCurrentFolder(this);
    }

    GoogleDriveFileTree nextCurrentSubFolder () {
        GoogleDriveFileTree currentGoogleDriveFileTree = getCurrentFolder();
        if (currentGoogleDriveFileTree.subFolders.length == currentGoogleDriveFileTree.currentSubFolder)
            return null;
        else
            return currentGoogleDriveFileTree.subFolders[++currentGoogleDriveFileTree.currentSubFolder];
    }

    private GoogleDriveFileTree nextSubFolder (GoogleDriveFileTree googleDriveFileTree) {
        if (hasNextSubFolder())
            return googleDriveFileTree.subFolders[++googleDriveFileTree.currentSubFolder];
        else
            return null;
    }

    GoogleDriveFileTree nextParentSubFolder () {
        if (!hasParentFolder())
            return null;
        return parentFolder.nextSubFolder(parentFolder);
    }

    // Set drive folder to thisDriveFolder if currentSubFolder is <0, else to subFolder[currentSubFolder-1]
    void setDriveFolder (DriveFolder driveFolder) {
        GoogleDriveFileTree currentGoogleDriveFileTree = getCurrentFolder();
        currentGoogleDriveFileTree.thisFolder.setDriveFolder(driveFolder);
    }

    File nextCurrentFile () {
        GoogleDriveFileTree currentGoogleDriveFileTree = getCurrentFolder();
        if (currentGoogleDriveFileTree.thisFolder.hasNextFile())
            return currentGoogleDriveFileTree.thisFolder.nextFile();
        else
            return null;
    }

    boolean hasParentFolder () {
        return parentFolder != null;
    }

    boolean hasNextFile () {
        return thisFolder.hasNextFile();
    }

    boolean hasNextSubFolder () {
        return currentSubFolder + 1 != subFolders.length;
    }

    String getFolderName () {
        return thisFolder.getFolderName();
    }

    DriveFolder getThisDriveFolder () {
        return thisFolder.getDriveFolder();
    }

    GoogleDriveFileTree getParentFolder () {
        return parentFolder;
    }

    DriveFolder getCurrentDriveFolder () {
        return getCurrentFolder().thisFolder.getDriveFolder();
    }

    // Element class of tree
    private class GoogleDriveCustomFolder {
        private File folder;
        private DriveFolder driveFolder;
        private File[] files;
        private int currentFile;

        private GoogleDriveCustomFolder(File folder) {
            this.folder = folder;
            files = generateFiles(folder);
            currentFile = -1;
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

        private boolean hasNextFile () {
            return currentFile + 1 != files.length;
        }

        private File nextFile () {
            return files[++currentFile];
        }

        private DriveFolder getDriveFolder () {
            return driveFolder;
        }

        private void setDriveFolder (DriveFolder driveFolder) {
            this.driveFolder = driveFolder;
        }

        private String getFolderName () {
            return folder.getName();
        }
    }
}
