package com.compomics.util.gui.searchsettings;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.util.ArrayList;

/**
 * Interface for parents of the SearchSettingsDialog.
 * 
 * @author Harald Barsnes
 */
public interface SearchSettingsDialogParent {

    /**
     * The horizontal padding used before and after the text in the titled
     * borders. (Needed to make it look as good in Java 7 as it did in Java
     * 6...)
     */
    public static String TITLED_BORDER_HORIZONTAL_PADDING = "";

    /**
     * Returns the last selected folder.
     *
     * @return the last selected folder
     */
    public String getLastSelectedFolder();

    /**
     * Set the last selected folder.
     *
     * @param lastSelectedFolder
     */
    public void setLastSelectedFolder(String lastSelectedFolder);

    /**
     * Returns the user modifications file.
     *
     * @return the user modifications file
     */
    public File getUserModificationsFile();

    /**
     * Returns the search parameters.
     *
     * @return the search parameters
     */
    public SearchParameters getSearchParameters();

    /**
     * Set the search parameters.
     *
     * @param searchParameters
     */
    public void setSearchParameters(SearchParameters searchParameters);

    /**
     * Returns the list of modifications used.
     *
     * @return the list of modifications used
     */
    public ArrayList<String> getModificationUse();
}
