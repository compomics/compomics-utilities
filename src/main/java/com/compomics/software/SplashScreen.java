package com.compomics.software;

import java.io.File;
import java.util.ArrayList;

/**
 * Class that takes care of converting an svg file to a png file using the
 * command line version of apache batik.
 *
 * @author Harald Barsnes
 */
public class SplashScreen {

    /**
     * Constructor. Requires three arguments, 1) the target folder of the tool
     * with the figure to convert, 2) the jar file folder of the tool with the
     * figure to convert, and 3) the figure file name (without .svg or .png).
     *
     * @param args the arguments: 1) target folder, 2) jar file folder, 3)
     * figure file name (without .svg or .png)
     */
    public SplashScreen(String args[]) {

        // get the locations and the figure file names
        String targetFolder = args[0]; // target folder
        String jarFolder = args[1]; // jar file folder
        String svgFileName = args[2]; // splash screen file name (without svg/png)

        // set the locations needed
        String splashScreenLocation = targetFolder + "/classes/" + svgFileName + ".svg";
        String splashScreenEndLocation = jarFolder + "/resources/conf/" + svgFileName + ".png";
        String pomFileFolder = targetFolder.substring(0, targetFolder.length() - "target".length());
        String splashScreenCodePath = pomFileFolder + "splashscreen/";

        // build the command line
        ArrayList process_name_array = new ArrayList();
        process_name_array.add("java");
        process_name_array.add("-jar");
        process_name_array.add("batik-rasterizer.jar");
        process_name_array.add("-dpi");
        process_name_array.add("60000");
        process_name_array.add("-h");
        process_name_array.add("2200");
        process_name_array.add("-d");
        process_name_array.add(splashScreenEndLocation);
        process_name_array.add(splashScreenLocation);

        // execute the command line
        ProcessBuilder pb = new ProcessBuilder(process_name_array);
        pb.directory(new File(splashScreenCodePath));
        try {
            Process p = pb.start();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * The main method. Use in the pom file starting the process.
     *
     * @param args the arguments: 1) target folder, 2) jar file folder, 3)
     * figure file name (without .svg or .png)
     */
    public static void main(String args[]) {
        new SplashScreen(args);
    }
}
