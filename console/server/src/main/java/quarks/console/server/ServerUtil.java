/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.console.server;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.security.ProtectionDomain;

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
     * @param warFileName the name of the war file to find the absolute path to
     * @return the absolute path to the warFileName argument as a String
     */
    public String getAbsoluteWarFilePath(String warFileName) {
        File warFilePath = getWarFilePath();
        if (warFilePath != null) {
        	File warFile = new File(warFilePath.getAbsolutePath() + "/" + warFileName);
        	if (warFile.exists()) {        	
        		return warFile.getAbsolutePath();
        	} else {
        		return "";
        	}
        }
        else {
            return "";
        }
    }
    
    /**
     * Looks for the absolute file path of the name of the warFileName argument when running from Eclipse
     * @param warFileName the name of the war file to find the absolute path to
     * @return the absolute path to the warFileName argument as a String
     */
    public String getEclipseWarFilePath(ProtectionDomain pDomain, String warFileName) {
        URL location = pDomain.getCodeSource().getLocation();
        File topQuarks = new File(location.getPath()).getParentFile().getParentFile().getParentFile();
        File warFile = new File(topQuarks, "./target/java8/console/webapps/" +warFileName);
        if (warFile.exists()) {
        	return warFile.getAbsolutePath();
        } else {
        	return "";
        }
	
    }

}
