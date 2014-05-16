package com.compomics.software.dialogs;

import com.compomics.util.preferences.UtilitiesUserPreferences;

/**
 * Interface for parents of JavaMemoryDialog.
 *
 * @author Harald Barsnes
 */
public interface JavaMemoryDialogParent {

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
