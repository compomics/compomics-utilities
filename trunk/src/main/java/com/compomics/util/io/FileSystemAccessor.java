package com.compomics.util.io;
import org.apache.log4j.Logger;

import com.compomics.util.enumeration.OperatingSystemEnum;

import java.io.File;

/**
 * This class serves as a fast OS independent access point to the file system.
 *
 * Created by IntelliJ IDEA.
 * User: kenny
 * Date: Feb 22, 2010
 * Time: 1:39:58 PM
 */
public class FileSystemAccessor {

    // Class specific log4j logger for FileSystemAccessor instances.
    Logger logger = Logger.getLogger(FileSystemAccessor.class);

    /**
     * Empty constructor.
     */
    public FileSystemAccessor() {
    }

    /**
     * Get the operating system.
     * @return This method returns the underlying operating system as a OperatingSystemEnum instance.
     */
    public static OperatingSystemEnum getOperatingSystem(){
        String lOSName = System.getProperty("os.name").toLowerCase();
        if(lOSName.indexOf("win") > -1){
            return OperatingSystemEnum.WINDOWS;
        }else if(lOSName.indexOf("uni") > -1){
            return OperatingSystemEnum.UNIX;
        }else if(lOSName.indexOf("mac") > -1){
            return OperatingSystemEnum.MACOS;
        }else return null;
    }

    /**
     * Returns the user home folder.
     *
     * @return the user home folder
     */
    public static File getHomeFolder(){
        String lHomeDirectory = System.getProperty("user.home");
        File lFile = new File(lHomeDirectory);
        return lFile;
    }

    /**
     * Returns the absolute path of the underlying file system of the given class.
     * @param aClassname The classname of which the parent folder needs to be returned (e.g.: "FileSystemAccessor.class")
     * @return The full path to the parent classname. (e.g.: /home/user/java/compomics/utilities/)
     */
    public static String getPathOfClass(String aClassname){
        String path;
        String lFileSeparator = System.getProperties().get("file.separator").toString();
        // PropertiesManager serves as a class of the Utilities libary to find the resources
        path = PropertiesManager.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        path = path.substring(5, path.lastIndexOf(lFileSeparator) + 1);
        path = path.replace("%20", " ");
        path = path.replace("%5b", "[");
        path = path.replace("%5d", "]");

        return path;
    }
}
