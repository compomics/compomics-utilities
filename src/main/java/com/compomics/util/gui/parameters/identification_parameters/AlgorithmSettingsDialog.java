package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;

/**
 * Interface for an algorithm settings dialog.
 *
 * @author Marc Vaudel
 */
public interface AlgorithmSettingsDialog {
    
    /**
     * Indicates whether the user canceled the editing.
     *
     * @return true if cancel was pressed
     */
    public boolean isCancelled();
    
    /**
     * Returns the parameters as set by the user.
     * 
     * @return the parameters as set by the user
     */
    public IdentificationAlgorithmParameter getParameters();

}
