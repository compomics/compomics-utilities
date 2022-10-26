package com.compomics.util.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * JComobox supporting multiple selection. Based on
 * https://java-swing-tips.blogspot.com/2016/07/select-multiple-jcheckbox-in-jcombobox.html.
 *
 * @author Harald Barsnes
 *
 * @param <E> a checkable item
 */
public class CheckedComboBox<E extends CheckableItem> extends JComboBox<E> {

    /**
     * Whether the pop up is visible.
     */
    private boolean keepOpen;
    /**
     * The action listener for the combo box.
     */
    private transient ActionListener listener;

    /**
     * Create a new CheckedComboBox.
     */
    public CheckedComboBox() {
        super();
    }

    /**
     * Create a new CheckedComboBox using the given model.
     *
     * @param aModel the model
     */
    public CheckedComboBox(ComboBoxModel<E> aModel) {
        super(aModel);
    }

    /**
     * Create a new CheckedComboBox using the given model.
     *
     * @param m the model
     */
    public CheckedComboBox(E[] m) {
        super(m);
    }

    @Override
    public void updateUI() {

        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();
        listener = e -> {
            if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };

        setRenderer(new CheckBoxCellRenderer());
        addActionListener(listener);

        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");

        getActionMap().put("checkbox-select", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof BasicComboPopup) {
                    BasicComboPopup pop = (BasicComboPopup) a;
                    updateItem(pop.getList().getSelectedIndex());
                }
            }
        });
    }

    /**
     * Update the item the given index.
     *
     * @param index the index of the item
     */
    private void updateItem(int index) {

        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
            removeItemAt(index);
            insertItemAt(item, index);
            setSelectedItem(item);
        }

    }

    @Override
    public void setPopupVisible(boolean v) {

        if (keepOpen) {
            keepOpen = false;
        } else {
            super.setPopupVisible(v);
        }

    }
}
