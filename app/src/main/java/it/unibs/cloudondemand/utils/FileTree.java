package it.unibs.cloudondemand.utils;

import java.io.File;
import java.io.FileFilter;

import it.unibs.cloudondemand.google.GoogleDriveCustomFolder;

public class FileTree <T extends GenericFileTree<T>> {
    // Parent folder of thisFolder
    private FileTree<T> parentFolder;

    // (Node of the tree)
    private T thisFolder;

    // SubFolders of thisFolder (Leafs of the tree)
    private FileTree<T>[] subFolders;
    // Used to remember the sub folder on which it's working
    private int currentSubFolder;

    /**
     * Create the main folder instance.
     * @param thisFolder Folder node.
     */
    public FileTree(T thisFolder){
        this.parentFolder = null;
        this.thisFolder = thisFolder;
        this.subFolders = generateSubFolders(thisFolder);
        this.currentSubFolder = -1;
    }

    // Constructor call generateSubFolders to fill subFolders array (recursion done by method that call the constructor)
    /**
     * Create all the tree recursively by the first calling.
     * @param parentFolder Parent folder of thisFolder.
     * @param thisFolder Folder node.
     */
    private FileTree(FileTree<T> parentFolder, T thisFolder){
        this.parentFolder = parentFolder;
        this.thisFolder = thisFolder;
        this.subFolders = generateSubFolders(thisFolder);
        this.currentSubFolder = -1;
    }

    // Called recursively by the constructor
    /**
     * Generate array of folder param sub folders.
     * @param folder Parent folder.
     * @return Array of sub folders of the folder.
     */
    private FileTree<T>[] generateSubFolders(T folder) {
        // List directories into the folder
        T[] subFolders = folder.getSubFolders();

        FileTree<T>[] folders=new FileTree[subFolders.length];
        for(int i = 0; i < subFolders.length; i++) {
            folders[i] = new FileTree(this, subFolders[i]);
        }
        return folders;
    }

    /**
     * Recursive method to get the working folder.
     * @param fileTree Current node.
     * @return Working folder.
     */
    private FileTree<T> getCurrentFolder(FileTree<T> fileTree){
        int i = fileTree.currentSubFolder;
        if (i == -1)
            return fileTree;
        else
            return getCurrentFolder(fileTree.subFolders[i]);
    }

    /**
     * WARNING : Invoke this on main folder node object.
     * @return Working folder tree object.
     */
    public FileTree<T> getCurrentFolder() {
        return getCurrentFolder(this);
    }

    /**
     * Invoke this on main folder node object.
     * @return Working folder node of tree.
     */
    public T getCurrentThisFolder() {
        FileTree<T> currentFileTree = getCurrentFolder();
        return currentFileTree.thisFolder;
    }

    /**
     * Get next subfolder of this node.
     * @return Tree object of subfolder or null if there isn't next subfolder.
     */
    public FileTree<T> nextSubFolder() {
        if (!hasNextSubFolder())
            return null;
        else
            return subFolders[++currentSubFolder];
    }

    /**
     * Get next subfolder of this parent folder.
     * @return Tree object of parent subfolder or null if there isn't next subfolder.
     */
    public FileTree<T> nextParentSubFolder () {
        if (!hasParentFolder())
            return null;
        return parentFolder.nextSubFolder(parentFolder);
    }

    /**
     * Get next subfolder of node passed by parameter.
     * @param fileTree Working file tree.
     * @return Tree object of subfolder or null if there isn't next subfolder.
     */
    private FileTree<T> nextSubFolder (FileTree fileTree) {
        if (fileTree.hasNextSubFolder())
            return fileTree.subFolders[++fileTree.currentSubFolder];
        else
            return null;
    }

    /**
     * Get next file of this node.
     * @return File requested or null if there isn't another file in this node folder.
     */
    public File nextFile () {
        if (thisFolder.hasNextFile())
            return thisFolder.nextFile();
        else
            return null;
    }

    /**
     * Check if this node has the parent folder (isn't main directory).
     * @return True if has parent folder, False otherwise.
     */
    public boolean hasParentFolder () {
        return parentFolder != null;
    }

    /**
     * Check if this node has another subfolder.
     * @return True if has another subfolder, False otherwise.
     */
    public boolean hasNextSubFolder () {
        return currentSubFolder + 1 != subFolders.length;
    }

    /**
     * Check if this node folder has another file.
     * @return True if has another file, False otherwise.
     */
    public boolean hasNextFile () {
        return thisFolder.hasNextFile();
    }

    /**
     * Getter for this node folder name.
     * @return Name of the folder
     */
    public String getFolderName () {
        return thisFolder.getFolderName();
    }

    /**
     * Getter parent folder of this node.
     * @return Parent folder or null if this node is main directory).
     */
    public FileTree<T> getParentFolder () {
        return parentFolder;
    }

    /**
     * Getter this folder object for custom implementations.
     * @return This folder (node).
     */
    public T getThisFolder() {
        return thisFolder;
    }
}