package com.compomics.util.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Arrays;
import javax.swing.JFrame;

/**
 * A simple class used to be able to show a JDialog without a parent frame in
 * the OS task bar.
 *
 * @author Harald Barsnes
 */
public class DummyFrame extends JFrame {

    /**
     * Constructor.
     *
     * @param title the frame title
     * @param relativeImageIconPath the relative path to the default icon for
     * the frame
     */
    public DummyFrame(String title, String relativeImageIconPath) {
        super(title);
        setUndecorated(true);
        setVisible(true);
        setLocationRelativeTo(null);
        setIconImages(Arrays.asList(Toolkit.getDefaultToolkit().getImage(getClass().getResource(relativeImageIconPath))));
    }

    /**
     * Constructor.
     *
     * @param title the frame title
     * @param imageIcon the default image icon for the frame
     */
    public DummyFrame(String title, Image imageIcon) {
        super(title);
        setUndecorated(true);
        setVisible(true);
        setLocationRelativeTo(null);
        setIconImages(Arrays.asList(imageIcon));
    }

    /**
     * Update the frame title and return the frame.
     *
     * @param title the new title
     * @return the updated dummy frame
     */
    public DummyFrame setNewTitle(String title) {
        this.setTitle(title);
        return this;
    }
}
