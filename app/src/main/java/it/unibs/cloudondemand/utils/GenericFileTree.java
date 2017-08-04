package it.unibs.cloudondemand.utils;

import java.io.File;

public interface GenericFileTree <T> {
    boolean hasNextFile ();
    File nextFile ();
    File getFolder ();
    T[] getSubFolders ();
}
