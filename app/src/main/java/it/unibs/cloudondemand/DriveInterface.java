package it.unibs.cloudondemand;

import java.io.File;

/**
 * Created by Perno on 23/06/2017.
 */

public interface DriveInterface {
    void saveFile(File file);
    void getFile(String filename);

}
