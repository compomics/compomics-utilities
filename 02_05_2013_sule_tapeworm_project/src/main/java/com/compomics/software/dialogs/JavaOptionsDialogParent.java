package com.compomics.software.dialogs;

import com.compomics.util.preferences.UtilitiesUserPreferences;

/**
 * Interface for parents of JavaOptionsDialog.
 *
 * @author Harald Barsnes
 */
public interface JavaOptionsDialogParent {

    /**
     * Restart the given tool with the new Java options.
     */
    public void restart();

    /**
     * Returns the utilities user preferences.
     *
     * @return the utilities user preferences
     */
    public UtilitiesUserPreferences getUtilitiesUserPreferences();
}
