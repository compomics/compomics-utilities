package com.compomics.util.gui.waiting;

/**
 * This class is used to pass actions while waiting like cancel.
 *
 * @author Marc Vaudel
 */
public interface WaitingActionListener {

    /**
     * Method called whenever the user pressed cancel.
     */
    public void cancelPressed();
}
