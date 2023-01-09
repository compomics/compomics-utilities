package com.compomics.util.gui;

/**
 * Objects used in CheckBoxCellRenderer.
 * 
 * @author Harald Barsnes
 */
public class CheckableItem {

    /**
     * The text to display.
     */
    public final String text;
    /**
     * Whether the item is selected or not.
     */
    public boolean selected;

    /**
     * Create a new CheckableItem.
     * 
     * @param text the text to display
     * @param selected whether the item is selected or not
     */
    public CheckableItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }

    @Override
    public String toString() {
        return text;
    }

}
