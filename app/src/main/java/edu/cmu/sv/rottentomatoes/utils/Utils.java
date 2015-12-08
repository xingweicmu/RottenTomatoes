package edu.cmu.sv.rottentomatoes.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xingwei on 12/7/15.
 */
public class Utils {

    public static void closeStreamQuietly(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // ignore exception
        }
    }

}
