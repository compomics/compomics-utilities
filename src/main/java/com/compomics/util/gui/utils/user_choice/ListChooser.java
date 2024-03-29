package com.compomics.util.gui.utils.user_choice;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Dialog for choosing an item in a list.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class ListChooser extends javax.swing.JDialog {

    /**
     * Empty default constructor.
     */
    public ListChooser() {
    }

    /**
     * The list of items the user will choose from.
     */
    public ArrayList<String> items;

    /**
     * Boolean indicating whether the selection has been cancelled.
     */
    private boolean canceled = false;
    /**
     * Table column header tooltips.
     */
    private final ArrayList<String> tableToolTips = new ArrayList<>();

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
    protected ListChooser(
            java.awt.Frame parent,
            ArrayList<String> items,
            String dialogTitle,
            String panelTitle,
            String instructionsLabel,
            boolean multipleSelection
    ) {

        super(parent, true);

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No item to select.");
        }

        initComponents();
        this.items = items;

        setUpGui(dialogTitle, panelTitle, instructionsLabel, multipleSelection);
        setLocationRelativeTo(parent);

    }

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
    protected ListChooser(
            javax.swing.JDialog parent,
            ArrayList<String> items,
            String dialogTitle,
            String panelTitle,
            String instructionsLabel,
            boolean multipleSelection
    ) {

        super(parent, true);

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No item to select.");
        }

        initComponents();
        this.items = items;
        setUpGui(dialogTitle, panelTitle, instructionsLabel, multipleSelection);
        setLocationRelativeTo(parent);

    }

    /**
     * Constructor. Null values will be replaced by default.
     *
     * @param dialogTitle the title to give to the dialog.
     * @param panelTitle the title to give to the panel containing the table.
     * @param instructionsLabel the instructions label on top of the table.
     * @param multipleSelection boolean indicating whether the user should be
     * allowed to select multiple items.
     */
    private void setUpGui(
            String dialogTitle,
            String panelTitle,
            String instructionsLabel,
            boolean multipleSelection
    ) {

        if (dialogTitle != null) {
            setTitle(dialogTitle);
        } else {
            setTitle("Selection");
        }

        if (panelTitle != null) {
            ((TitledBorder) itemsPanel.getBorder()).setTitle(panelTitle);
        }

        if (instructionsLabel != null) {
            itemsLabel.setText(instructionsLabel);
        }

        // make sure that the scroll panes are see-through
        itemsTableScrollPane.getViewport().setOpaque(false);

        if (multipleSelection) {
            itemsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

    }

    /**
     * Formats the table.
     */
    protected abstract void formatTable();

    /**
     * Returns the tooltips used for the column headers.
     *
     * @return the tooltips used for the column headers
     */
    protected ArrayList<String> getTableTooltips() {
        return tableToolTips;
    }

    /**
     * Returns the JTable containing the items.
     *
     * @return the JTable containing the items
     */
    protected JTable getTable() {
        return itemsTable;
    }

    /**
     * Updates the table content.
     */
    public void setUpTable() {
        formatTable();
        ((DefaultTableModel) itemsTable.getModel()).fireTableDataChanged();
        itemsTable.setRowSelectionInterval(0, 0);
    }

    /**
     * Returns a boolean indicating whether the selection was canceled by the
     * user.
     *
     * @return a boolean indicating whether the selection was canceled by the
     * user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the item selected by the user.
     *
     * @return the item selected by the user
     */
    public String getSelectedItem() {
        int row = itemsTable.getSelectedRow();
        String itemName = items.get(row);
        return itemName;
    }

    /**
     * Returns the items selected by the user in a list.
     *
     * @return the items selected by the user in a list
     */
    public HashSet<String> getSelectedItems() {

        return Arrays.stream(itemsTable.getSelectedRows())
                .mapToObj(row -> items.get(row))
                .collect(Collectors.toCollection(HashSet::new));

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        itemsPanel = new javax.swing.JPanel();
        itemsLabel = new javax.swing.JLabel();
        itemsTableScrollPane = new javax.swing.JScrollPane();
        itemsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String) tableToolTips.get(realIndex);
                    }
                };
            }
        };

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        itemsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Possibilities"));
        itemsPanel.setOpaque(false);

        itemsLabel.setText("Please select one of the following items:");

        itemsTable.setModel(new DefaultItemsTableModel());
        itemsTableScrollPane.setViewportView(itemsTable);

        javax.swing.GroupLayout itemsPanelLayout = new javax.swing.GroupLayout(itemsPanel);
        itemsPanel.setLayout(itemsPanelLayout);
        itemsPanelLayout.setHorizontalGroup(
            itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(itemsPanelLayout.createSequentialGroup()
                        .addComponent(itemsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(itemsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE))
                .addContainerGap())
        );
        itemsPanelLayout.setVerticalGroup(
            itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(itemsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(itemsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(itemsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(itemsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Cancel the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel itemsLabel;
    private javax.swing.JPanel itemsPanel;
    private javax.swing.JTable itemsTable;
    private javax.swing.JScrollPane itemsTableScrollPane;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Table model for the items.
     */
    private class DefaultItemsTableModel extends DefaultTableModel {

        public DefaultItemsTableModel() {
        }

        @Override
        public int getRowCount() {

            if (items == null) {
                return 0;
            }

            return items.size();

        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {

            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                default:
                    return "";
            }

        }

        @Override
        public Object getValueAt(int row, int column) {

            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return items.get(row);
                default:
                    return "";
            }

        }

        @Override
        public Class getColumnClass(int columnIndex) {

            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }

            return String.class;

        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

    }

}
