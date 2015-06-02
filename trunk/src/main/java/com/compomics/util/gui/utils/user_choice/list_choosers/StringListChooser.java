package com.compomics.util.gui.utils.user_choice.list_choosers;

import com.compomics.util.gui.utils.user_choice.ListChooser;
import java.util.ArrayList;
import javax.swing.table.TableColumnModel;

/**
 * Dialog for choosing an item in a list of String.
 *
 * @author Marc Vaudel
 */
public class StringListChooser extends ListChooser {

    /**
     * Constructor. Null values will be replaced by default.
     *
     * @param parent the parent frame
     * @param items list of items for the user to select
     * @param dialogTitle the title to give to the dialog.
     * @param panelTitle the title to give to the panel containing the table.
     * @param instructionsLabel the instructions label on top of the table.
     * @param multipleSelection boolean indicating whether the user should be
     * allowed to select multiple items.
     */
    public StringListChooser(java.awt.Frame parent, ArrayList<String> items, String dialogTitle, String panelTitle, String instructionsLabel, boolean multipleSelection) {
        super(parent, items, dialogTitle, panelTitle, instructionsLabel, multipleSelection);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No item to select.");
        }
        setUpTable();
        setVisible(true);
    }

    @Override
    protected void formatTable() {
        TableColumnModel tableColumnModel = getTable().getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(50);
    }
}
