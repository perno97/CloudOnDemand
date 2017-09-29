package it.unibs.cloudondemand.utils;

import java.io.File;

/**
 * Interface to be implemented into custom folder classes of FileTree
 * @param <T> The same class that has implemented this.
 */
public interface GenericFileTree <T> {
    boolean hasNextFile ();
    File nextFile ();
    File getFolder ();
    T[] getSubFolders ();
}
