package com.compomics.util.io;

import com.compomics.util.enumeration.OperatingSystemEnum;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: kenny
 * Date: Feb 22, 2010
 * Time: 1:39:58 PM
 * <p/>
 * This class serves as a fast OS independent access point to the file system.
 */
public class FileSystemAccessor {

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


    public static File getHomeFolder(){
        File lFile = null;
        String lHomeDirectory = System.getProperty("user.home");
        lFile = new File(lHomeDirectory);

        return lFile;
    }
}
