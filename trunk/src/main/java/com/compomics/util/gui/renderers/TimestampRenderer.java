/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-feb-03
 * Time: 7:48:02
 */
package com.compomics.util.gui.renderers;
import org.apache.log4j.Logger;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class 
 *
 * @author Lennart
 */
public class TimestampRenderer extends DefaultTableCellRenderer {
	// Class specific log4j logger for TimestampRenderer instances.
	Logger logger = Logger.getLogger(TimestampRenderer.class);

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        if(value instanceof java.sql.Timestamp) {
            java.sql.Timestamp ts = (java.sql.Timestamp)value;
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String text = sdf.format(ts);
            this.setText(text);
            if(table != null) {
                this.setFont(table.getFont());
            }
        } else {
            logger.error("Got a " + value.getClass().getName() + " for the renderer instead of a java.sql.Timestamp.");
        }


        if (isSelected) {
           this.setForeground(table.getSelectionForeground());
           this.setBackground(table.getSelectionBackground());
        } else {
            this.setForeground(table.getForeground());
            this.setBackground(table.getBackground());
        }

        if (hasFocus) {
            setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
        } else {
            setBorder(noFocusBorder);
        }

        return this;
    }
}
