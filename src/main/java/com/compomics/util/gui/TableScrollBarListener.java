package com.compomics.util.gui;

import com.compomics.util.gui.tablemodels.SelfUpdatingTableModel;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JTable;

/**
 * AdjustmentListener for SelfUpdatingTableModels supporting scrolling in a more
 * efficient way.
 *
 * @author Harald Barsnes
 */
public class TableScrollBarListener implements AdjustmentListener {

    /**
     * The table to be monitored. Must have a SelfUpdatingTableModel as the
     * table model.
     */
    private final JTable table;

    /**
     * Constructor.
     *
     * @param table the table
     */
    public TableScrollBarListener(JTable table) {
        this.table = table;
    }

    public void adjustmentValueChanged(AdjustmentEvent evt) {
        ((SelfUpdatingTableModel) table.getModel()).setIsScrolling(evt.getValueIsAdjusting());
        table.revalidate();
        table.repaint();
    }
}
