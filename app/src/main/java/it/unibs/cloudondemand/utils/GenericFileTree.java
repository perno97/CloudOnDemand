package it.unibs.cloudondemand.utils;

import java.io.File;

public interface GenericFileTree <T> {
    boolean hasNextFile ();
    File nextFile ();
    String getFolderName ();
    T[] getSubFolders ();
}
