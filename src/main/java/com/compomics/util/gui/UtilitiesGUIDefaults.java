package com.compomics.util.gui;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * This class contains a list of GUI defaults to be used across the tools 
 * using the utilities library to make sure that the tools have the same 
 * look and feel.
 *
 * @author Harald Barsnes
 */
public class UtilitiesGUIDefaults {

    /**
     * Empty default constructor
     */
    public UtilitiesGUIDefaults() {
    }

    /**
     * Sets the look and feel to the default utilities look and feel. First
     * tries to use Nimbus, if Nimbus is not supported then PlasticXPLookAndFeel
     * is used.
     * 
     * @exception IOException exception somehow thrown when starting from a zip file
     * @return true if the Nimbus look and feel is used, false otherwise
     */
    public static boolean setLookAndFeel() throws IOException {

        boolean nimbusLookAndFeelFound = false;

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusLookAndFeelFound = true;
                    break;
                }
            }
        } catch (Exception e) {
            // ignore error, use look and feel below
        }

        if (!nimbusLookAndFeelFound) {
            try {
                PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            } catch (Exception ex) {
                // this should not be possible...
                // use default look and feel
            }
        }

        return nimbusLookAndFeelFound;
    }
}
