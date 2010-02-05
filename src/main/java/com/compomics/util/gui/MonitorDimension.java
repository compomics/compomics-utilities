package com.compomics.util.gui;

import javax.swing.*;
import java.awt.*;

/**
 * <b>Created by IntelliJ IDEA.</b> User: Kenni Date: 9-jul-2006 Time: 12:31:49
 * <p/>
 * <br>This Class </br>
 */
public class MonitorDimension {
    /**
     * This dimension is used by all the methods.
     */
    private static Dimension lDimension;
    private static double lWidth;
    private static double lHeight;
    private static double percentage;

    /**
     * This method returns a Dimension with a percentual width and height in relation to the JFrame.
     * @param frame         Source dimension
     * @param aPercentage   Percentual width and height of the returning dimension.
     * @return              A Dimension instance with a percentual Dimension in relation to the JFrame.
     */
    public static Dimension getPercentualScreenDimension(JFrame frame, double aPercentage){
        clearDimension();
        Toolkit tlkt = frame.getToolkit();
        setDimension(tlkt, aPercentage);
        return lDimension;
    }

    /**
     * This method returns a Dimension with a percentual width and height in realtion to the JPanel.
     * @param jpan          Source Dimension
     * @param aPercentage   Percentual width and height of the returning dimension.
     * @return              A Dimension instance with a percentual Dimension in relation to the JPanel.
     */
    public static Dimension getPercentualScreenDimension(JPanel jpan, double aPercentage){
        clearDimension();
        Toolkit tlkt = jpan.getToolkit();
        setDimension(tlkt, aPercentage);
        return lDimension;
    }

    private static void setDimension(Toolkit tlkt, double aPercentage){
        lWidth = tlkt.getScreenSize().getWidth() * aPercentage;
        lHeight = tlkt.getScreenSize().getHeight() * aPercentage;
        lDimension.setSize(lWidth, lHeight);
    }

    private static void clearDimension(){
        lWidth = 0;
        lHeight = 0;
        lDimension.setSize(0, 0);
    }
}
