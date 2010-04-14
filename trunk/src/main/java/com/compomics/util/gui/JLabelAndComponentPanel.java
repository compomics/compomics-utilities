/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-dec-02
 * Time: 10:45:37
 */
package com.compomics.util.gui;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2009/07/30 10:20:39 $
 */

/**
 * This class implements a JPanel that lays out a set of JLabel and a JComponent
 * next to each other. <br />
 * Correct usage of this class is calling the empty constructor
 * and adding label-component pairs via the corresponding 'add' method. <br />
 * Do not call anything else unless you really want to break the functionality!
 *
 * @author Lennart Martens
 */
public class JLabelAndComponentPanel extends JPanel {
	// Class specific log4j logger for JLabelAndComponentPanel instances.
	Logger logger = Logger.getLogger(JLabelAndComponentPanel.class);

    /**
     * Constructor which allows the specification of the labels and components
     * to lay out.
     *
     * @param   aLabels JLabel[] with the labels.
     * @param   aComponents JComponent[] with the components.
     */
    public JLabelAndComponentPanel(JLabel[] aLabels, JComponent[] aComponents) {
        super();
        if(aLabels.length != aComponents.length) {
            throw new IllegalArgumentException("Unequal amounts of labels (" + aLabels.length + ") and textfields (" + aComponents.length + ")!");
        } else {

            this.setLayout(new GridBagLayout());
            for(int i = 0; i < aLabels.length; i++) {
                JLabel lLabel = aLabels[i];
                JPanel jpl = new JPanel();
                jpl.setLayout(new BoxLayout(jpl, BoxLayout.X_AXIS));
                jpl.add(Box.createRigidArea(new Dimension(10, lLabel.getHeight())));
                jpl.add(lLabel);
                jpl.add(Box.createHorizontalGlue());

                GridBagConstraints gbcL = new GridBagConstraints();
                gbcL.gridx = 0;
                gbcL.gridy = i;
                gbcL.gridwidth = 1;
                gbcL.gridheight = 1;
                gbcL.anchor = GridBagConstraints.WEST;

                JPanel jpColon = new JPanel();
                jpColon.add(new JLabel(" : "));

                GridBagConstraints gbcColon = new GridBagConstraints();
                gbcColon.gridx = 1;
                gbcColon.gridy = i;
                gbcColon.gridwidth = GridBagConstraints.RELATIVE;
                gbcColon.gridheight = 1;

                JPanel jpc = new JPanel();
                JComponent lComponent = aComponents[i];
                jpc.add(lComponent);

                GridBagConstraints gbcC = new GridBagConstraints();
                gbcC.gridx = 2;
                gbcC.gridy = i;
                gbcC.gridwidth = GridBagConstraints.REMAINDER;
                gbcC.gridheight = 1;

                this.add(jpl, gbcL);
                this.add(jpColon, gbcColon);
                this.add(jpc, gbcC);
            }
        }
    }
}
