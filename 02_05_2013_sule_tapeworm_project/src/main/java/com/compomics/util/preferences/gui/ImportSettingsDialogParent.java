package com.compomics.util.preferences.gui;

import com.compomics.util.preferences.IdFilter;

/**
 * Interface implemented by parents of the ImportSettingsDialog.
 *
 * @author Harald Barsnes
 */
public interface ImportSettingsDialogParent {

    /**
     * Sets the identification filter used.
     *
     * @param idFilter the identification filter used
     */
    public void setIdFilter(IdFilter idFilter);
    
    /**
     * Returns the identification filter used.
     *
     * @return the identification filter used
     */
    public IdFilter getIdFilter();
    /**
     * Sets the filter settings field to the given text.
     *
     * @param text the text to set
     */
    public void updateFilterSettingsField(String text);
}
