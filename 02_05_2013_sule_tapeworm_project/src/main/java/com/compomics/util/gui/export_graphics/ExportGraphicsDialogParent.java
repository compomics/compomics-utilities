
package com.compomics.util.gui.export_graphics;

import java.awt.Image;

/**
 * A simple interface that classes wanting to use the ExportGraphicsDialog have 
 * to implement. Basically to make it possible to send the path to the selected 
 * file back to the parent.
 * 
 * @author Harald Barsnes
 */
public interface ExportGraphicsDialogParent {

    /**
     * Set the default folder to use for exporting the graphics. Can be changed 
     * by the user later.
     * 
     * @param selectedFolder the default folder
     */
    public void setSelectedExportFolder(String selectedFolder);

    /**
     * Returns the default folder used for the export.
     * 
     * @return the default folder used for the export
     */
    public String getDefaultExportFolder();
    
    /**
     * Returns the normal icon.
     * 
     * @return the normal icon
     */
    public Image getNormalIcon();
    
    /**
     * Returns the waiting icon.
     * 
     * @return the waiting icon
     */
    public Image getWaitingIcon();
}
