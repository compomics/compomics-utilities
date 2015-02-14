package com.compomics.util.waiting;

/**
 * This class is used to pass actions like cancel while waiting.
 *
 * @author Marc Vaudel
 */
public interface WaitingActionListener {

    /**
     * Method called whenever the user pressed cancel.
     */
    public void cancelPressed();
}
