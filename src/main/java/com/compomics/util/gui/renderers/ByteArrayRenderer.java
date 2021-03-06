/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-feb-03
 * Time: 7:48:16
 */
package com.compomics.util.gui.renderers;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */
/**
 * ByteArrayRenderer.
 *
 * @author Lennart
 */
public class ByteArrayRenderer extends DefaultTableCellRenderer {

    /**
     * Empty default constructor
     */
    public ByteArrayRenderer() {
    }

    // Class specific log4j logger for ByteArrayRenderer instances.
    Logger logger = LogManager.getLogger(ByteArrayRenderer.class);

    /**
     * This byte[] is the actual data cached by the renderer.
     */
    private byte[] iData = null;

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        this.setFont(table.getFont());
        this.setText("(bytes)");
        if (value instanceof byte[]) {
            iData = (byte[]) value;
        }

        if (isSelected) {
            this.setBackground(table.getSelectionBackground());
        } else {
            this.setForeground(table.getForeground());
            this.setBackground(table.getBackground());
        }
        this.setForeground(Color.blue);

        if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            setBorder(noFocusBorder);
        }

        return this;
    }

    /**
     * This method returns the data that is currently cached by the renderer.
     *
     * @return byte[] with the data for this renderer.
     */
    public byte[] getData() {
        return this.iData;
    }
}
