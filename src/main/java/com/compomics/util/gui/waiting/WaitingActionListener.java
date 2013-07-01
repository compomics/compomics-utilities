/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.gui.waiting;

import java.awt.event.ActionListener;

/**
 * This class is used to pass actions while waiting like cancel.
 *
 * @author Marc
 */
public interface WaitingActionListener {
    
    /**
     * Method called whenever the user pressed cancel
     */
    public void cancelPressed();
    
}
