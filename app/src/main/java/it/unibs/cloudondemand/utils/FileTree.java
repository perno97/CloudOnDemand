package it.unibs.cloudondemand.utils;

import java.io.File;
import java.io.FileFilter;

public class FileTree<T>{

    private FileTree parentFolder;
    private FileTreeNode<T> thisFolder;
    private FileTree[] subFolders;

    private int currentSubFolder;

    public FileTree(FileTree parentFolder, File thisFolder){
        this.parentFolder = parentFolder;
        this.thisFolder = new FileTreeNode<>(thisFolder);
        this.subFolders = generateSubFolders(thisFolder);
        this.currentSubFolder = -1;
    }

    public FileTree() {
    }

    public FileTree[] generateSubFolders(File folder) {
        // List directories into the folder
        File[] directories = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        FileTree[] folders=new FileTree[directories.length];
        for(int i = 0; i < directories.length; i++){
            folders[i]=new FileTree(this, directories[i]);
        }
        return folders;
    }

    private FileTree getCurrentFolder(FileTree fileTree){
        int i = fileTree.currentSubFolder;
        if (i == -1)
            return fileTree.parentFolder;
        else
            return getCurrentFolder(subFolders[i]);
    }

    public FileTree getCurrentFolder() {
        return getCurrentFolder(this);
    }

    public FileTree nextCurrentSubFolder(){
        FileTree currentFileTree = getCurrentFolder();
        if (currentFileTree.subFolders.length == currentFileTree.currentSubFolder)
            return null;
        else
            return currentFileTree.subFolders[++currentFileTree.currentSubFolder];
    }

    private FileTree nextSubFolder (FileTree fileTree) {
        if (fileTree.hasNextSubFolder())
            return fileTree.subFolders[++fileTree.currentSubFolder];
        else
            return null;
    }

    FileTree nextParentSubFolder () {
        if (!hasParentFolder())
            return null;
        return parentFolder.nextSubFolder(parentFolder);
    }

    File nextCurrentFile () {
        FileTree currentFileTree = getCurrentFolder();
        if (currentFileTree.thisFolder.hasNextFile())
            return currentFileTree.thisFolder.nextFile();
        else
            return null;
    }

    boolean hasParentFolder () {
        return parentFolder != null;
    }

    boolean hasNextSubFolder () {
        return currentSubFolder + 1 != subFolders.length;
    }

    private String getFolderName () {
        return thisFolder.getFolderName();
    }

    FileTree getParentFolder () {
        return parentFolder;
    }

    private boolean hasNextFile () {
        return thisFolder.hasNextFile();
    }

    private class FileTreeNode<E> implements GenericFileTree {
        private E data;

        private FileTreeNode (File file) {
            setFile(file);
        }

        @Override
        public boolean hasNextFile() {
            return false;
        }

        @Override
        public File nextFile() {
            return null;
        }

        @Override
        public String getFolderName() {
            return null;
        }

        @Override
        public void setFile(File file) {

        }
    }
}
