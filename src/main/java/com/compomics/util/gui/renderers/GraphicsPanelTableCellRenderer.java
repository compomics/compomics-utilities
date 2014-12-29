package com.compomics.util.gui.renderers;

import com.compomics.util.gui.spectrum.GraphicsPanel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer for GraphicsPanels. 
 * <br>
 * Draws miniature versions of the graphics panels with reduced padding.
 * All graphics panels are also rescaled to use the same x-axis range.
 *
 * @author Harald Barsnes
 */
public class GraphicsPanelTableCellRenderer extends JLabel implements TableCellRenderer {

    /**
     * The minimum x-axis value.
     */
    private double minXAxisValue;
    /**
     * The maxium x-axis value.
     */
    private double maxXAxisValue;

    /**
     * A reference to a standard table cell renderer.
     */
    private TableCellRenderer delegate = new DefaultTableCellRenderer();

    /**
     * Create a new GraphicsPanelTableCellRenderer with a given x-axis range
     * to ensure that all spectra in the column have the same range.
     *
     * @param minXAxisValue the minimum x-axis value
     * @param maxXAxisValue the maximum x-axis value
     */
    public GraphicsPanelTableCellRenderer(double minXAxisValue, double maxXAxisValue) {
        this.minXAxisValue = minXAxisValue;
        this.maxXAxisValue = maxXAxisValue;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JComponent c = (JComponent) delegate.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        // respect focus and hightlighting
        setBorder(c.getBorder());
        setOpaque(c.isOpaque());
        setBackground(c.getBackground());

        // turn on miniature, reduce the padding, and rescale
        GraphicsPanel graphicsPanel = (GraphicsPanel) value;
        graphicsPanel.setMaxPadding(2);
        graphicsPanel.setMiniature(true);
        graphicsPanel.rescale(minXAxisValue, maxXAxisValue, true);

        // respect cell highlighting
        if (isSelected) {
            graphicsPanel.setBackground(c.getBackground());
        } else {
            graphicsPanel.setBackground(Color.WHITE);
        }

        // add border when cell is selected
        if (hasFocus) {
            graphicsPanel.setBorder(c.getBorder());
        } else {
            graphicsPanel.setBorder(null);
        }

        return graphicsPanel;
    }
}
