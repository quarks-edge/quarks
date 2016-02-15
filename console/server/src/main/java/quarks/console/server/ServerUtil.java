/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.console.server;

import java.io.File;
import java.io.FilenameFilter;

public class ServerUtil {

	/**
	 *  The public constructor of this utility class for use by the HttpServer class.
	 */
    public ServerUtil() {

    }
    /**
     * Returns the path to the jar file for this package
     * @return a String representing the path to the jar file of this package
     */
    private String getPath() {
        return getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    /**
     * Returns a file object representing the parent's parent directory of the jar file.
     * @return a File object
     */
    private File getTopDirFilePath() {
        File jarFile = new File(getPath());
        return jarFile.getParentFile().getParentFile().getParentFile();
    }

    // create new filename filter
    FilenameFilter fileNameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if (name.equals("webapps")) {
                return true;
            }
            else {
                return false;
            }
        }
    };
    /**
     * Returns the File object representing the "webapps" directory
     * @return a File object or null if the "webapps" directory is not found
     */
    private File getWarFilePath() {
        File[] foundFiles = getTopDirFilePath().listFiles(fileNameFilter);
        if (foundFiles.length == 1) {
            return foundFiles[0];
        }
        return null;
    }
    
    /**
     * Looks for the absolute file path of the name of the warFileName argument
     * @param warFileName the name of the war file to find the absolute path of
     * @return the absolute path to the warFileName argument as a String
     */
    public String getAbsoluteWarFilePath(String warFileName) {
        File warFile = getWarFilePath();
        if (warFile != null) {
            return warFile.getAbsolutePath() + "/" + warFileName;
        }
        else {
            return "";
        }
    }

}
