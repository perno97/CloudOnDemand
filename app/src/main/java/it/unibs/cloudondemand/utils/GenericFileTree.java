package it.unibs.cloudondemand.utils;

import java.io.File;

public interface GenericFileTree
{
        boolean hasNextFile ();

        File nextFile ();

        String getFolderName ();
    // USATO AL POSTO DEL COSTRUTTORE
    void setFile (File file);

}
