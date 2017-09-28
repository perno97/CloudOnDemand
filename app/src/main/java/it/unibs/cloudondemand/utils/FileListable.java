package it.unibs.cloudondemand.utils;

/**
 * Interface for file objects that are used into RowAdapter
 */
public interface FileListable {
    boolean isDirectory();
    String getName();
}
