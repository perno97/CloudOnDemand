package it.unibs.cloudondemand.utils;

import java.io.File;
import java.io.FileFilter;

import it.unibs.cloudondemand.google.GoogleDriveCustomFolder;

public class FileTree <T extends GenericFileTree<T>> {

    private FileTree<T> parentFolder;
    private T thisFolder;
    private FileTree<T>[] subFolders;

    private int currentSubFolder;

    public FileTree(FileTree<T> parentFolder, T thisFolder){
        this.parentFolder = parentFolder;
        this.thisFolder = thisFolder;
        this.subFolders = generateSubFolders(thisFolder);
        this.currentSubFolder = -1;
    }

    private FileTree<T>[] generateSubFolders(T folder) {
        // List directories into the folder
        T[] subFolders = folder.getSubFolders();

        FileTree[] folders=new FileTree[subFolders.length];
        for(int i = 0; i < subFolders.length; i++) {
            folders[i] = new FileTree(this, subFolders[i]);
        }
        return folders;
    }

    private FileTree<T> getCurrentFolder(FileTree<T> fileTree){
        int i = fileTree.currentSubFolder;
        if (i == -1)
            return fileTree;
        else
            return getCurrentFolder(subFolders[i]);
    }

    public FileTree<T> getCurrentFolder() {
        return getCurrentFolder(this);
    }

    public T getCurrentThisFolder() {
        FileTree<T> currentFileTree = getCurrentFolder();
        return currentFileTree.thisFolder;
    }

    public FileTree<T> nextCurrentSubFolder(){
        FileTree currentFileTree = getCurrentFolder();
        if (currentFileTree.subFolders.length == currentFileTree.currentSubFolder)
            return null;
        else
            return currentFileTree.subFolders[++currentFileTree.currentSubFolder];
    }

    private FileTree<T> nextSubFolder (FileTree fileTree) {
        if (fileTree.hasNextSubFolder())
            return fileTree.subFolders[++fileTree.currentSubFolder];
        else
            return null;
    }

    public FileTree<T> nextParentSubFolder () {
        if (!hasParentFolder())
            return null;
        return parentFolder.nextSubFolder(parentFolder);
    }

    public File nextCurrentFile () {
        FileTree currentFileTree = getCurrentFolder();
        if (currentFileTree.thisFolder.hasNextFile())
            return currentFileTree.thisFolder.nextFile();
        else
            return null;
    }

    public boolean hasParentFolder () {
        return parentFolder != null;
    }

    public boolean hasNextSubFolder () {
        return currentSubFolder + 1 != subFolders.length;
    }

    public String getFolderName () {
        return thisFolder.getFolderName();
    }

    public FileTree<T> getParentFolder () {
        return parentFolder;
    }

    public T getThisFolder() {
        return thisFolder;
    }

    public boolean hasNextFile () {
        return thisFolder.hasNextFile();
    }
}