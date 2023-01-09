package com.compomics.util.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;

/**
 * Cell renderer for JComboBox supporting multiple selection. Based on
 * https://java-swing-tips.blogspot.com/2016/07/select-multiple-jcheckbox-in-jcombobox.html.
 *
 * @author Harald Barsnes
 *
 * @param <E> the checkable item
 */
public class CheckBoxCellRenderer<E extends CheckableItem> implements ListCellRenderer<E> {

    /**
     * The JLabel representing the text to display.
     */
    private final JLabel label = new JLabel(" ");
    /**
     * The JCheckBox with the check box.
     */
    private final JCheckBox check = new JCheckBox(" ");

    @Override
    public Component getListCellRendererComponent(JList list, CheckableItem value, int index, boolean isSelected, boolean cellHasFocus) {

        // centrally allign the text
        label.setHorizontalAlignment(SwingConstants.CENTER);

        if (index < 0) {

            label.setText(getCheckedItemString(list.getModel()));
            return label;

        } else {

            check.setText(Objects.toString(value, ""));
            check.setSelected(value.selected);
            check.setOpaque(isSelected);

            if (isSelected) {
                check.setBackground(list.getSelectionBackground());
                check.setForeground(list.getSelectionForeground());
            } else {
                check.setBackground(list.getBackground());
                check.setForeground(list.getForeground());
            }

            return check;

        }
    }

    /**
     * Returns the checked items as a string.
     *
     * @param model the list model
     * @return the checked items as a string
     */
    private String getCheckedItemString(ListModel model) {

        List<String> sl = new ArrayList<>();

        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (o instanceof CheckableItem && ((CheckableItem) o).selected) {
                sl.add(o.toString());
            }
        }

        if (sl.isEmpty()) {
            return "--- Select ---";
        }

        return sl.stream().sorted().collect(Collectors.joining(", "));
    }

    /**
     * Returns the checked items as an array of strings.
     *
     * @param model the list model
     * @return the checked items as an array of strings
     */
    public ArrayList<String> getCheckedItems(ListModel model) {

        ArrayList<String> sl = new ArrayList<>();

        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (o instanceof CheckableItem && ((CheckableItem) o).selected) {
                sl.add(o.toString());
            }
        }

        return sl;
    }

    /**
     * Checks the item with the given name in the list.
     *
     * @param model the list model
     * @param itemName the name of the item to check
     */
    public void setCheckedItem(ListModel model, String itemName) {

        for (int i = 0; i < model.getSize(); i++) {

            Object o = model.getElementAt(i);

            if (o instanceof CheckableItem && ((CheckableItem) o).text.equalsIgnoreCase(itemName)) {
                ((CheckableItem) o).selected = true;
            }

        }

    }
}
