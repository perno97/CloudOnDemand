package it.unibs.cloudondemand.google;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import java.io.File;
import java.io.FileFilter;

import it.unibs.cloudondemand.utils.FileTree;
import it.unibs.cloudondemand.utils.GenericFileTree;

class GoogleDriveFileTree extends FileTree<GoogleDriveFileTree.GoogleDriveCustomFolder>{
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

    public GoogleDriveFileTree[] generateSubFolders (File folder) {
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

    public GoogleDriveFileTree getCurrentFolder() {
        return getCurrentFolder(this);
    }

    public GoogleDriveFileTree nextCurrentSubFolder () {
        GoogleDriveFileTree currentGoogleDriveFileTree = getCurrentFolder();
        if (currentGoogleDriveFileTree.subFolders.length == currentGoogleDriveFileTree.currentSubFolder)
            return null;
        else
            return currentGoogleDriveFileTree.subFolders[++currentGoogleDriveFileTree.currentSubFolder];
    }

    private GoogleDriveFileTree nextSubFolder (GoogleDriveFileTree googleDriveFileTree) {
        if (googleDriveFileTree.hasNextSubFolder())
            return googleDriveFileTree.subFolders[++googleDriveFileTree.currentSubFolder];
        else
            return null;
    }

    GoogleDriveFileTree nextParentSubFolder () {
        if (!hasParentFolder())
            return null;
        return parentFolder.nextSubFolder(parentFolder);
    }

    void setCurrentDriveFolder (DriveFolder driveFolder) {
        GoogleDriveFileTree currentGoogleDriveFileTree = getCurrentFolder();
        currentGoogleDriveFileTree.thisFolder.setDriveFolder(driveFolder);
    }

    void setCurrentFileId (DriveId driveId) {
        GoogleDriveFileTree currentGoogleDriveFileTree = getCurrentFolder();
        currentGoogleDriveFileTree.thisFolder.setFileId(driveId);
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
    public class GoogleDriveCustomFolder implements GenericFileTree {
        private File folder;
        private DriveFolder driveFolder;
        private File[] files;
        private int currentFile;
        private DriveId[] filesId;

        public GoogleDriveCustomFolder(File folder) {
            this.folder = folder;
            this.files = generateFiles(folder);
            this.filesId = new DriveId[files.length];
            this.currentFile = -1;
        }

        public File[] generateFiles (File folder) {
            // List files into the folder
            File[] files = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });

            return files;
        }

        @Override
        public boolean hasNextFile () {
            return currentFile + 1 != files.length;
        }

        @Override
        public File nextFile () {
            return files[++currentFile];
        }

        public DriveFolder getDriveFolder () {
            return driveFolder;
        }

        public void setDriveFolder (DriveFolder driveFolder) {
            this.driveFolder = driveFolder;
        }

        @Override
        public String getFolderName () {
            return folder.getName();
        }

        @Override
        public void setFile(File file) {

        }

        public void setFileId (DriveId driveId) {
            filesId[currentFile] = driveId;
        }
    }
}
