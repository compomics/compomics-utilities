package com.compomics.util.gui.ptm;

/**
 * An interface that has to be implemented by all classes using the PtmDialog.
 *
 * @author Harald Barsnes
 */
public interface PtmDialogParent {

    /**
     * Update the PTM information in the parent.
     */
    public void updateModifications();
}
