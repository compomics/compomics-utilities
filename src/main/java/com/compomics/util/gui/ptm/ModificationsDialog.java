package com.compomics.util.gui.ptm;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.pride.CvTerm;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * A dialog where the modification details can be modified.
 *
 * @author Harald Barsnes
 */
public class ModificationsDialog extends javax.swing.JDialog {

    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The color used for the sparkline bar chart plots.
     */
    private Color sparklineColor = new Color(110, 196, 97);
    /**
     * The color to use for the HTML tags for the selected rows, in HTML color
     * code.
     */
    private String selectedRowHtmlTagFontColor = "#FFFFFF";
    /**
     * The color to use for the HTML tags for the rows that are not selected, in
     * HTML color code.
     */
    private String notSelectedRowHtmlTagFontColor = "#0101DF";
    /**
     * The default mods table column header tooltips.
     */
    private ArrayList<String> defaultModsTableToolTips;
    /**
     * The user mods table column header tooltips.
     */
    private ArrayList<String> userModsTableToolTips;
    /**
     * The lines of the PTMs concerned by the search.
     */
    private ArrayList<Integer> searchPossibilities = new ArrayList<Integer>();
    /**
     * The search current selection.
     */
    private int searchCurrentSelection = 0;
    /**
     * The search text to display by default.
     */
    private final String searchWelcomeText = "(name or mass)";

    /**
     * Creates a new ModificationsDialog.
     *
     * @param parentFrame the parent frame
     * @param modal if the dialog is to be modal or not
     */
    public ModificationsDialog(Frame parentFrame, boolean modal) {
        super(parentFrame, modal);
        initComponents();

        // set up tables
        setUpTables();

        searchInputTxt.setText(searchWelcomeText);
        searchIndexLabel.setText("");
        searchPreviousButton.setEnabled(false);
        searchNextButton.setEnabled(false);

        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Set up the table properties.
     */
    private void setUpTables() {

        defaultModificationsTable.setAutoCreateRowSorter(true);
        userModificationsTable.setAutoCreateRowSorter(true);

        // make sure that the scroll panes are see-through
        defaultModsScrollPane.getViewport().setOpaque(false);
        userModsScrollPane.getViewport().setOpaque(false);

        defaultModificationsTable.getTableHeader().setReorderingAllowed(false);
        userModificationsTable.getTableHeader().setReorderingAllowed(false);

        // the index column
        defaultModificationsTable.getColumn(" ").setMaxWidth(50);
        defaultModificationsTable.getColumn(" ").setMinWidth(50);
        userModificationsTable.getColumn(" ").setMaxWidth(50);
        userModificationsTable.getColumn(" ").setMinWidth(50);

        defaultModificationsTable.getColumn("Short").setMaxWidth(100);
        defaultModificationsTable.getColumn("Short").setMinWidth(100);
        userModificationsTable.getColumn("Short").setMaxWidth(100);
        userModificationsTable.getColumn("Short").setMinWidth(100);

        defaultModificationsTable.getColumn("Residues").setMaxWidth(100);
        defaultModificationsTable.getColumn("Residues").setMinWidth(100);
        userModificationsTable.getColumn("Residues").setMaxWidth(100);
        userModificationsTable.getColumn("Residues").setMinWidth(100);

        defaultModificationsTable.getColumn("Type").setMaxWidth(50);
        defaultModificationsTable.getColumn("Type").setMinWidth(50);
        userModificationsTable.getColumn("Type").setMaxWidth(50);
        userModificationsTable.getColumn("Type").setMinWidth(50);

        defaultModificationsTable.getColumn("CV").setMaxWidth(100);
        defaultModificationsTable.getColumn("CV").setMinWidth(100);
        userModificationsTable.getColumn("CV").setMaxWidth(100);
        userModificationsTable.getColumn("CV").setMinWidth(100);

        // set up the ptm type color map
        HashMap<Integer, Color> ptmTypeColorMap = new HashMap<Integer, Color>();
        ptmTypeColorMap.put(PTM.MODMAX, Color.lightGray);
        ptmTypeColorMap.put(PTM.MODAA, sparklineColor);
        ptmTypeColorMap.put(PTM.MODC, Color.CYAN);
        ptmTypeColorMap.put(PTM.MODCAA, Color.MAGENTA);
        ptmTypeColorMap.put(PTM.MODCP, Color.RED);
        ptmTypeColorMap.put(PTM.MODCPAA, Color.ORANGE);
        ptmTypeColorMap.put(PTM.MODN, Color.YELLOW);
        ptmTypeColorMap.put(PTM.MODNAA, Color.PINK);
        ptmTypeColorMap.put(PTM.MODNP, Color.BLUE);
        ptmTypeColorMap.put(PTM.MODNPAA, Color.GRAY);

        // set up the ptm type tooltip map
        HashMap<Integer, String> ptmTypeTooltipMap = new HashMap<Integer, String>();
        ptmTypeTooltipMap.put(PTM.MODMAX, "max number of modification types");
        ptmTypeTooltipMap.put(PTM.MODAA, "Particular Amino Acid(s)");
        ptmTypeTooltipMap.put(PTM.MODC, "Protein C-term");
        ptmTypeTooltipMap.put(PTM.MODCAA, "Protein C-term - Particular Amino Acid(s)");
        ptmTypeTooltipMap.put(PTM.MODCP, "Peptide C-term");
        ptmTypeTooltipMap.put(PTM.MODCPAA, "Peptide C-term - Particular Amino Acid(s)");
        ptmTypeTooltipMap.put(PTM.MODN, "Protein N-term");
        ptmTypeTooltipMap.put(PTM.MODNAA, "Protein N-term - Particular Amino Acid(s)");
        ptmTypeTooltipMap.put(PTM.MODNP, "Peptide N-term");
        ptmTypeTooltipMap.put(PTM.MODNPAA, "Peptide N-term - Particular Amino Acid(s)");

        defaultModificationsTable.getColumn("Type").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, ptmTypeColorMap, ptmTypeTooltipMap));
        userModificationsTable.getColumn("Type").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, ptmTypeColorMap, ptmTypeTooltipMap));

        defaultModificationsTable.getColumn("CV").setCellRenderer(new HtmlLinksRenderer(selectedRowHtmlTagFontColor, notSelectedRowHtmlTagFontColor));
        userModificationsTable.getColumn("CV").setCellRenderer(new HtmlLinksRenderer(selectedRowHtmlTagFontColor, notSelectedRowHtmlTagFontColor));

        updateMassSparklines();

        // set up the table header tooltips
        defaultModsTableToolTips = new ArrayList<String>();
        defaultModsTableToolTips.add(null);
        defaultModsTableToolTips.add("Modification Name");
        defaultModsTableToolTips.add("Modification Short Name");
        defaultModsTableToolTips.add("Modification Mass Change");
        defaultModsTableToolTips.add("Modification Type");
        defaultModsTableToolTips.add("Affected Residues");
        defaultModsTableToolTips.add("CV Term Mapping");

        userModsTableToolTips = new ArrayList<String>();
        userModsTableToolTips.add(null);
        userModsTableToolTips.add("Modification Name");
        userModsTableToolTips.add("Modification Short Name");
        userModsTableToolTips.add("Modification Mass Change");
        userModsTableToolTips.add("Modification Type");
        userModsTableToolTips.add("Affected Residues");
        userModsTableToolTips.add("CV Term Mapping");

        ((TitledBorder) defaultModsPanel.getBorder()).setTitle("Default Modifications (" + defaultModificationsTable.getRowCount() + ")");
        ((TitledBorder) userModsPanel.getBorder()).setTitle("User Modifications (" + userModificationsTable.getRowCount() + ")");
    }

    /**
     * Update the max value of the mass sparklines.
     */
    private void updateMassSparklines() {

        // get the max absolute mass change
        double maxMassChange = 0;

        for (int i = 0; i < defaultModificationsTable.getRowCount(); i++) {
            if (Math.abs((Double) defaultModificationsTable.getValueAt(i, defaultModificationsTable.getColumn("Mass").getModelIndex())) > maxMassChange) {
                maxMassChange = Math.abs((Double) defaultModificationsTable.getValueAt(i, defaultModificationsTable.getColumn("Mass").getModelIndex()));
            }
        }

        for (int i = 0; i < userModificationsTable.getRowCount(); i++) {
            if (Math.abs((Double) userModificationsTable.getValueAt(i, userModificationsTable.getColumn("Mass").getModelIndex())) > maxMassChange) {
                maxMassChange = Math.abs((Double) userModificationsTable.getValueAt(i, userModificationsTable.getColumn("Mass").getModelIndex()));
            }
        }

        defaultModificationsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, -maxMassChange, maxMassChange));
        ((JSparklinesBarChartTableCellRenderer) defaultModificationsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 57, new DecimalFormat("0.0000"));

        userModificationsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, -maxMassChange, maxMassChange));
        ((JSparklinesBarChartTableCellRenderer) userModificationsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 57, new DecimalFormat("0.0000"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        defaultPtmPopupMenu = new javax.swing.JPopupMenu();
        viewDefaultPtmJMenuItem = new javax.swing.JMenuItem();
        userPtmPopupMenu = new javax.swing.JPopupMenu();
        editUserPtmJMenuItem = new javax.swing.JMenuItem();
        modificationsEditorPanel = new javax.swing.JPanel();
        modificationsSplitPane = new javax.swing.JSplitPane();
        defaultModsPanel = new javax.swing.JPanel();
        defaultModsScrollPane = new javax.swing.JScrollPane();
        defaultModificationsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String) defaultModsTableToolTips.get(realIndex);
                    }
                };
            }
        };
        searchIndexLabel = new javax.swing.JLabel();
        searchNextButton = new javax.swing.JButton();
        searchPreviousButton = new javax.swing.JButton();
        searchInputTxt = new javax.swing.JTextField();
        findJLabel = new javax.swing.JLabel();
        exportDefaultModsLabel = new javax.swing.JLabel();
        userModsPanel = new javax.swing.JPanel();
        userModsScrollPane = new javax.swing.JScrollPane();
        userModificationsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String) userModsTableToolTips.get(realIndex);
                    }
                };
            }
        };
        deleteUserPTM = new javax.swing.JButton();
        editUserPTM = new javax.swing.JButton();
        addUserPTM = new javax.swing.JButton();
        exportUserModsLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        modificationsHelpJButton = new javax.swing.JButton();

        viewDefaultPtmJMenuItem.setText("View");
        viewDefaultPtmJMenuItem.setToolTipText("View Details");
        viewDefaultPtmJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDefaultPtmJMenuItemActionPerformed(evt);
            }
        });
        defaultPtmPopupMenu.add(viewDefaultPtmJMenuItem);

        editUserPtmJMenuItem.setText("Edit");
        editUserPtmJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editUserPtmJMenuItemActionPerformed(evt);
            }
        });
        userPtmPopupMenu.add(editUserPtmJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Modification Details");
        setMinimumSize(new java.awt.Dimension(500, 500));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        modificationsEditorPanel.setBackground(new java.awt.Color(230, 230, 230));

        modificationsSplitPane.setBorder(null);
        modificationsSplitPane.setDividerLocation(380);
        modificationsSplitPane.setDividerSize(-1);
        modificationsSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        modificationsSplitPane.setResizeWeight(0.65);

        defaultModsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Default Modifications"));
        defaultModsPanel.setOpaque(false);

        defaultModificationsTable.setModel(new DefaultPTMTable());
        defaultModificationsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        defaultModificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                defaultModificationsTableMouseMoved(evt);
            }
        });
        defaultModificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                defaultModificationsTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                defaultModificationsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                defaultModificationsTableMouseReleased(evt);
            }
        });
        defaultModsScrollPane.setViewportView(defaultModificationsTable);

        searchIndexLabel.setFont(searchIndexLabel.getFont().deriveFont((searchIndexLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        searchIndexLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchIndexLabel.setText(" ");

        searchNextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/next_grey.png"))); // NOI18N
        searchNextButton.setToolTipText("Next");
        searchNextButton.setBorderPainted(false);
        searchNextButton.setContentAreaFilled(false);
        searchNextButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/next.png"))); // NOI18N
        searchNextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchNextButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchNextButtonMouseExited(evt);
            }
        });
        searchNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchNextButtonActionPerformed(evt);
            }
        });

        searchPreviousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/previous_grey.png"))); // NOI18N
        searchPreviousButton.setToolTipText("Previous");
        searchPreviousButton.setBorder(null);
        searchPreviousButton.setBorderPainted(false);
        searchPreviousButton.setContentAreaFilled(false);
        searchPreviousButton.setIconTextGap(0);
        searchPreviousButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/previous.png"))); // NOI18N
        searchPreviousButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchPreviousButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchPreviousButtonMouseExited(evt);
            }
        });
        searchPreviousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPreviousButtonActionPerformed(evt);
            }
        });

        searchInputTxt.setForeground(new java.awt.Color(204, 204, 204));
        searchInputTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        searchInputTxt.setText("(name or mass)");
        searchInputTxt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        searchInputTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                searchInputTxtMouseReleased(evt);
            }
        });
        searchInputTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchInputTxtKeyReleased(evt);
            }
        });

        findJLabel.setText("Find:");

        exportDefaultModsLabel.setText("<html><a href>Export to file</a></html>");
        exportDefaultModsLabel.setToolTipText("Export to tab delimited text file");
        exportDefaultModsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportDefaultModsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportDefaultModsLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exportDefaultModsLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout defaultModsPanelLayout = new javax.swing.GroupLayout(defaultModsPanel);
        defaultModsPanel.setLayout(defaultModsPanelLayout);
        defaultModsPanelLayout.setHorizontalGroup(
            defaultModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, defaultModsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defaultModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(defaultModsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                    .addGroup(defaultModsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(exportDefaultModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(findJLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchInputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchPreviousButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(searchNextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchIndexLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        defaultModsPanelLayout.setVerticalGroup(
            defaultModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, defaultModsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(defaultModsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(defaultModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(findJLabel)
                    .addComponent(searchInputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchIndexLabel)
                    .addComponent(searchPreviousButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchNextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportDefaultModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        modificationsSplitPane.setTopComponent(defaultModsPanel);

        userModsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("User Modifications"));
        userModsPanel.setOpaque(false);

        userModificationsTable.setModel(new UserPTMTable());
        userModificationsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        userModificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                userModificationsTableMouseMoved(evt);
            }
        });
        userModificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userModificationsTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                userModificationsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                userModificationsTableMouseReleased(evt);
            }
        });
        userModsScrollPane.setViewportView(userModificationsTable);

        deleteUserPTM.setText("Delete");
        deleteUserPTM.setToolTipText("Delete a user defined modification");
        deleteUserPTM.setEnabled(false);
        deleteUserPTM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUserPTMActionPerformed(evt);
            }
        });

        editUserPTM.setText("Edit");
        editUserPTM.setToolTipText("Edit a user defined modification");
        editUserPTM.setEnabled(false);
        editUserPTM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editUserPTMActionPerformed(evt);
            }
        });

        addUserPTM.setText("Add");
        addUserPTM.setToolTipText("Add a new user defined modification");
        addUserPTM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUserPTMActionPerformed(evt);
            }
        });

        exportUserModsLabel.setText("<html><a href>Export to file</a></html>");
        exportUserModsLabel.setToolTipText("Export to tab delimited text file");
        exportUserModsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportUserModsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportUserModsLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exportUserModsLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout userModsPanelLayout = new javax.swing.GroupLayout(userModsPanel);
        userModsPanel.setLayout(userModsPanelLayout);
        userModsPanelLayout.setHorizontalGroup(
            userModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userModsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(userModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userModsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                    .addGroup(userModsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(exportUserModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addUserPTM, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editUserPTM, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteUserPTM, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        userModsPanelLayout.setVerticalGroup(
            userModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userModsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userModsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(userModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(deleteUserPTM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editUserPTM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(userModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addUserPTM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportUserModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        modificationsSplitPane.setRightComponent(userModsPanel);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        modificationsHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        modificationsHelpJButton.setToolTipText("Help");
        modificationsHelpJButton.setBorder(null);
        modificationsHelpJButton.setBorderPainted(false);
        modificationsHelpJButton.setContentAreaFilled(false);
        modificationsHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                modificationsHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                modificationsHelpJButtonMouseExited(evt);
            }
        });
        modificationsHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationsHelpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout modificationsEditorPanelLayout = new javax.swing.GroupLayout(modificationsEditorPanel);
        modificationsEditorPanel.setLayout(modificationsEditorPanelLayout);
        modificationsEditorPanelLayout.setHorizontalGroup(
            modificationsEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modificationsEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modificationsSplitPane)
                    .addGroup(modificationsEditorPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(modificationsHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)))
                .addContainerGap())
        );
        modificationsEditorPanelLayout.setVerticalGroup(
            modificationsEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modificationsSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificationsEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(okButton)
                    .addComponent(modificationsHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 976, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(modificationsEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 673, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(modificationsEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Opens the default modification pop up menu.
     *
     * @param evt
     */
    private void defaultModificationsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultModificationsTableMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            defaultModificationsTable.setRowSelectionInterval(defaultModificationsTable.rowAtPoint(evt.getPoint()), defaultModificationsTable.rowAtPoint(evt.getPoint()));
            defaultPtmPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getClickCount() == 2 && defaultModificationsTable.getSelectedRow() != -1) {
            String ptmName = (String) defaultModificationsTable.getValueAt(defaultModificationsTable.getSelectedRow(), defaultModificationsTable.getColumn("Name").getModelIndex());
            PTM ptm = ptmFactory.getPTM(ptmName);
            new PtmDialog(this, ptm, false);
        }
    }//GEN-LAST:event_defaultModificationsTableMouseClicked

    /**
     * Changes the cursor into hand cursor.
     *
     * @param evt
     */
    private void defaultModificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultModificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_defaultModificationsTableMouseExited

    /**
     * Opens the link in a new browser.
     *
     * @param evt
     */
    private void defaultModificationsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultModificationsTableMouseReleased
        int row = defaultModificationsTable.rowAtPoint(evt.getPoint());
        int column = defaultModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == defaultModificationsTable.getColumn("CV").getModelIndex()) {
                // open protein link in web browser
                if (column == defaultModificationsTable.getColumn("CV").getModelIndex() && evt.getButton() == MouseEvent.BUTTON1
                        && defaultModificationsTable.getValueAt(row, column) != null
                        && ((String) defaultModificationsTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {

                    String link = (String) defaultModificationsTable.getValueAt(row, column);
                    link = link.substring(link.indexOf("\"") + 1);
                    link = link.substring(0, link.indexOf("\""));

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    BareBonesBrowserLaunch.openURL(link);
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }
            }
        }
    }//GEN-LAST:event_defaultModificationsTableMouseReleased

    /**
     * Changes the cursor to a hand cursor if over a link.
     *
     * @param evt
     */
    private void defaultModificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultModificationsTableMouseMoved
        int row = defaultModificationsTable.rowAtPoint(evt.getPoint());
        int column = defaultModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {

            if (column == defaultModificationsTable.getColumn("CV").getModelIndex() && defaultModificationsTable.getValueAt(row, column) != null) {

                String tempValue = (String) defaultModificationsTable.getValueAt(row, column);

                if (tempValue.lastIndexOf("<html>") != -1) {
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }

            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_defaultModificationsTableMouseMoved

    /**
     * Changes the cursor to a hand cursor.
     *
     * @param evt
     */
    private void searchNextButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchNextButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchNextButtonMouseEntered

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void searchNextButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchNextButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchNextButtonMouseExited

    /**
     * Find the next matching PTM.
     *
     * @param evt
     */
    private void searchNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchNextButtonActionPerformed
        if (searchCurrentSelection == searchPossibilities.size() - 1) {
            searchCurrentSelection = 0;
        } else {
            searchCurrentSelection++;
        }
        updatePtmSelection();
    }//GEN-LAST:event_searchNextButtonActionPerformed

    /**
     * Changes the cursor to a hand cursor.
     *
     * @param evt
     */
    private void searchPreviousButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPreviousButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchPreviousButtonMouseEntered

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void searchPreviousButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPreviousButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchPreviousButtonMouseExited

    /**
     * Find the previous matching PTM.
     *
     * @param evt
     */
    private void searchPreviousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPreviousButtonActionPerformed
        if (searchCurrentSelection == 0) {
            searchCurrentSelection = searchPossibilities.size() - 1;
        } else {
            searchCurrentSelection--;
        }
        updatePtmSelection();
    }//GEN-LAST:event_searchPreviousButtonActionPerformed

    /**
     * Start the PTM search.
     *
     * @param evt
     */
    private void searchInputTxtMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchInputTxtMouseReleased
        if (searchInputTxt.getText().equals(searchWelcomeText)) {
            searchInputTxt.selectAll();
        }
    }//GEN-LAST:event_searchInputTxtMouseReleased

    /**
     * Start the PTM search.
     *
     * @param evt
     */
    private void searchInputTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchInputTxtKeyReleased

        if (!searchInputTxt.getText().equalsIgnoreCase(searchWelcomeText)) {
            searchInputTxt.setForeground(Color.black);
        } else {
            searchInputTxt.setForeground(new Color(204, 204, 204));
        }
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT && searchNextButton.isEnabled()) {
            searchNextButtonActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_LEFT & searchPreviousButton.isEnabled()) {
            searchPreviousButtonActionPerformed(null);
        } else {
            String ptmName, mass;
            ArrayList<Integer> toAdd = new ArrayList<Integer>();
            searchPossibilities.clear();
            searchCurrentSelection = 0;
            String input = searchInputTxt.getText().trim().toLowerCase();
            if (!input.equals("")) {
                for (int row = 0; row < ptmFactory.getDefaultModifications().size(); row++) {
                    ptmName = ptmFactory.getDefaultModifications().get(row).toLowerCase();
                    mass = ptmFactory.getPTM(ptmName).getMass() + "";
                    if (mass.startsWith(input)) {
                        searchPossibilities.add(row);
                    } else if (ptmName.contains(input)) {
                        toAdd.add(row);
                    }
                }
                searchPossibilities.addAll(toAdd);
            }
        }
        if (searchPossibilities.size() > 1) {
            searchPreviousButton.setEnabled(true);
            searchNextButton.setEnabled(true);
            searchIndexLabel.setForeground(Color.BLACK);
            updatePtmSelection();
        } else if (searchPossibilities.size() == 1) {
            searchPreviousButton.setEnabled(false);
            searchNextButton.setEnabled(false);
            searchIndexLabel.setForeground(Color.BLACK);
            updatePtmSelection();
        } else {
            searchPreviousButton.setEnabled(false);
            searchNextButton.setEnabled(false);
            searchIndexLabel.setForeground(Color.RED);
            searchIndexLabel.setText("(no match)");
        }
    }//GEN-LAST:event_searchInputTxtKeyReleased

    /**
     * Opens the user mods pop up dialog or the PtmDialog.
     *
     * @param evt
     */
    private void userModificationsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userModificationsTableMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            userModificationsTable.setRowSelectionInterval(userModificationsTable.rowAtPoint(evt.getPoint()), userModificationsTable.rowAtPoint(evt.getPoint()));
            editUserPTM.setEnabled(true);
            deleteUserPTM.setEnabled(true);
            userPtmPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getClickCount() == 2 && userModificationsTable.getSelectedRow() != -1) {
            String ptmName = (String) userModificationsTable.getValueAt(userModificationsTable.getSelectedRow(), userModificationsTable.getColumn("Name").getModelIndex());
            PTM ptm = ptmFactory.getPTM(ptmName);
            PtmDialog ptmDialog = new PtmDialog(this, ptm, true);
            if (!ptmDialog.isCanceled()) {
                updateModifications();
            }
        }
    }//GEN-LAST:event_userModificationsTableMouseClicked

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void userModificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userModificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_userModificationsTableMouseExited

    /**
     * Opens the protein link in the browser.
     *
     * @param evt
     */
    private void userModificationsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userModificationsTableMouseReleased
        editUserPTM.setEnabled(userModificationsTable.getSelectedColumnCount() > 0);
        deleteUserPTM.setEnabled(userModificationsTable.getSelectedColumnCount() > 0);

        int row = userModificationsTable.rowAtPoint(evt.getPoint());
        int column = userModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == userModificationsTable.getColumn("CV").getModelIndex()) {
                // open protein link in web browser
                if (column == userModificationsTable.getColumn("CV").getModelIndex() && evt.getButton() == MouseEvent.BUTTON1
                        && ((String) userModificationsTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {

                    String link = (String) userModificationsTable.getValueAt(row, column);
                    link = link.substring(link.indexOf("\"") + 1);
                    link = link.substring(0, link.indexOf("\""));

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    BareBonesBrowserLaunch.openURL(link);
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }
            }
        }
    }//GEN-LAST:event_userModificationsTableMouseReleased

    /**
     * Change the cursor to a hand cursor when over a link.
     *
     * @param evt
     */
    private void userModificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userModificationsTableMouseMoved
        int row = userModificationsTable.rowAtPoint(evt.getPoint());
        int column = userModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {

            if (column == userModificationsTable.getColumn("CV").getModelIndex() && userModificationsTable.getValueAt(row, column) != null) {

                String tempValue = (String) userModificationsTable.getValueAt(row, column);

                if (tempValue.lastIndexOf("<html>") != -1) {
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }

            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_userModificationsTableMouseMoved

    /**
     * Delete the given user PTM.
     *
     * @param evt
     */
    private void deleteUserPTMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteUserPTMActionPerformed
        int row = userModificationsTable.getSelectedRow();
        String ptmName = (String) userModificationsTable.getValueAt(row, userModificationsTable.getColumn("Name").getModelIndex());

        int value = JOptionPane.showConfirmDialog(this, "Are you sure that you want to delete \'" + ptmName + "\'?", "Delete Modification?", JOptionPane.YES_NO_OPTION);

        if (value == JOptionPane.YES_OPTION) {
            ptmFactory.removeUserPtm(ptmName);
            updateModifications();
        }
    }//GEN-LAST:event_deleteUserPTMActionPerformed

    /**
     * Edit user PTM.
     *
     * @param evt
     */
    private void editUserPTMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editUserPTMActionPerformed
        int row = userModificationsTable.getSelectedRow();
        String ptmName = (String) userModificationsTable.getValueAt(row, userModificationsTable.getColumn("Name").getModelIndex());
        PTM ptm = ptmFactory.getPTM(ptmName);
        PtmDialog ptmDialog = new PtmDialog(this, ptm, true);
        if (!ptmDialog.isCanceled()) {
            updateModifications();
        }
    }//GEN-LAST:event_editUserPTMActionPerformed

    /**
     * Add user PTM.
     *
     * @param evt
     */
    private void addUserPTMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUserPTMActionPerformed
        if (ptmFactory.getUserModifications().size() < 30) {
            PtmDialog ptmDialog = new PtmDialog(this, null, true);
            if (!ptmDialog.isCanceled()) {
                updateModifications();
            }
        } else {
            JOptionPane.showMessageDialog(this, "In order to ensure compatibility with OMSSA, only 30 user modifications can be implemented. Please delete unused modifications.",
                    "Too many modifications", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addUserPTMActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void modificationsHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_modificationsHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void modificationsHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void modificationsHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificationsHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/ModificationEditor.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Modifications - Help", 500, 10);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsHelpJButtonActionPerformed

    /**
     * Edit a default PTM.
     *
     * @param evt
     */
    private void viewDefaultPtmJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDefaultPtmJMenuItemActionPerformed
        String ptmName = (String) defaultModificationsTable.getValueAt(defaultModificationsTable.getSelectedRow(), defaultModificationsTable.getColumn("Name").getModelIndex());
        PTM ptm = ptmFactory.getPTM(ptmName);
        PtmDialog ptmDialog = new PtmDialog(this, ptm, true);
        if (!ptmDialog.isCanceled()) {
            updateModifications();
        }
    }//GEN-LAST:event_viewDefaultPtmJMenuItemActionPerformed

    /**
     * Edit user PTM.
     *
     * @param evt
     */
    private void editUserPtmJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editUserPtmJMenuItemActionPerformed
        String ptmName = (String) userModificationsTable.getValueAt(userModificationsTable.getSelectedRow(), userModificationsTable.getColumn("Name").getModelIndex());
        PTM ptm = ptmFactory.getPTM(ptmName);
        PtmDialog ptmDialog = new PtmDialog(this, ptm, true);
        if (!ptmDialog.isCanceled()) {
            updateModifications();
        }
    }//GEN-LAST:event_editUserPtmJMenuItemActionPerformed

    /**
     * Save the PTM details.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        // save any changes to the factory
        try {
            ptmFactory.saveFactory();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while saving the modification.",
                    "Saving Error", JOptionPane.WARNING_MESSAGE);
        }

        dispose();
    }//GEN-LAST:event_formWindowClosing

    /**
     * Close the dialog and updated the PTMs in the dialog parent.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        formWindowClosing(null);
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void exportDefaultModsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportDefaultModsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_exportDefaultModsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void exportDefaultModsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportDefaultModsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_exportDefaultModsLabelMouseExited

    /**
     * Export the default modification to a user selected tab separated text
     * file.
     *
     * @param evt
     */
    private void exportDefaultModsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportDefaultModsLabelMouseReleased

        // get the file to send the output to
        final File selectedFile = Util.getUserSelectedFile(this, ".txt", "Tab separated text file (.txt)", "Export...", "user.home", "default modifications.txt", false);

        if (selectedFile != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                Util.tableToFile(defaultModificationsTable, "\t", null, true, writer);
                writer.close();
                JOptionPane.showMessageDialog(this, "Data copied to file:\n" + selectedFile.getAbsolutePath(), "Data Exported.", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "An error occurred when exporting the table content.", "Export Failed", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_exportDefaultModsLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void exportUserModsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportUserModsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_exportUserModsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void exportUserModsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportUserModsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_exportUserModsLabelMouseExited

    /**
     * Export the user modification to a user selected tab separated text file.
     *
     * @param evt
     */
    private void exportUserModsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportUserModsLabelMouseReleased

        // get the file to send the output to
        final File selectedFile = Util.getUserSelectedFile(this, ".txt", "Tab separated text file (.txt)", "Export...", "user.home", "user modifications.txt", false);

        if (selectedFile != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                Util.tableToFile(userModificationsTable, "\t", null, true, writer);
                writer.close();
                JOptionPane.showMessageDialog(this, "Data copied to file:\n" + selectedFile.getAbsolutePath(), "Data Exported.", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "An error occurred when exporting the table content.", "Export Failed", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_exportUserModsLabelMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addUserPTM;
    private javax.swing.JTable defaultModificationsTable;
    private javax.swing.JPanel defaultModsPanel;
    private javax.swing.JScrollPane defaultModsScrollPane;
    private javax.swing.JPopupMenu defaultPtmPopupMenu;
    private javax.swing.JButton deleteUserPTM;
    private javax.swing.JButton editUserPTM;
    private javax.swing.JMenuItem editUserPtmJMenuItem;
    private javax.swing.JLabel exportDefaultModsLabel;
    private javax.swing.JLabel exportUserModsLabel;
    private javax.swing.JLabel findJLabel;
    private javax.swing.JPanel modificationsEditorPanel;
    private javax.swing.JButton modificationsHelpJButton;
    private javax.swing.JSplitPane modificationsSplitPane;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel searchIndexLabel;
    private javax.swing.JTextField searchInputTxt;
    private javax.swing.JButton searchNextButton;
    private javax.swing.JButton searchPreviousButton;
    private javax.swing.JTable userModificationsTable;
    private javax.swing.JPanel userModsPanel;
    private javax.swing.JScrollPane userModsScrollPane;
    private javax.swing.JPopupMenu userPtmPopupMenu;
    private javax.swing.JMenuItem viewDefaultPtmJMenuItem;
    // End of variables declaration//GEN-END:variables

    /**
     * Table model for the default PTM table.
     */
    private class DefaultPTMTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return ptmFactory.getDefaultModifications().size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                case 2:
                    return "Short";
                case 3:
                    return "Mass";
                case 4:
                    return "Type";
                case 5:
                    return "Residues";
                case 6:
                    return "CV";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            String name = ptmFactory.getDefaultModificationsOrdered().get(row);
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return name;
                case 2:
                    return ptmFactory.getPTM(name).getShortName();
                case 3:
                    return ptmFactory.getPTM(name).getMass();
                case 4:
                    return ptmFactory.getPTM(name).getType();
                case 5:
                    String residues = "";
                    AminoAcidPattern aminoAcidPattern = ptmFactory.getPTM(name).getPattern();
                    if (aminoAcidPattern != null) {
                        for (Character residue : aminoAcidPattern.getAminoAcidsAtTarget()) {
                            if (!residues.equals("")) {
                                residues += ", ";
                            }
                            residues += residue;
                        }
                    }
                    return residues;
                case 6:
                    CvTerm cvTerm = ptmFactory.getPTM(name).getCvTerm();
                    if (cvTerm != null) {
                        if (cvTerm.getOntology().equalsIgnoreCase("UNIMOD")) {
                            return getUniModAccessionLink(cvTerm.getAccession());
                        } else { // psi-ms assumed
                            return getOlsAccessionLink(cvTerm.getAccession());
                        }
                    } else {
                        return null;
                    }
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
            return (new Double(0.0)).getClass();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    /**
     * Table model for the default PTM table.
     */
    private class UserPTMTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return ptmFactory.getUserModifications().size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                case 2:
                    return "Short";
                case 3:
                    return "Mass";
                case 4:
                    return "Type";
                case 5:
                    return "Residues";
                case 6:
                    return "CV";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            String name = ptmFactory.getUserModificationsOrdered().get(row);
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return name;
                case 2:
                    return ptmFactory.getPTM(name).getShortName();
                case 3:
                    return ptmFactory.getPTM(name).getMass();
                case 4:
                    return ptmFactory.getPTM(name).getType();
                case 5:
                    String residues = "";
                    AminoAcidPattern aminoAcidPattern = ptmFactory.getPTM(name).getPattern();
                    if (aminoAcidPattern != null) {
                        for (Character residue : aminoAcidPattern.getAminoAcidsAtTarget()) {
                            if (!residues.equals("")) {
                                residues += ", ";
                            }
                            residues += residue;
                        }
                    }
                    return residues;
                case 6:
                    CvTerm cvTerm = ptmFactory.getPTM(name).getCvTerm();
                    if (cvTerm != null) {
                        if (cvTerm.getOntology().equalsIgnoreCase("UNIMOD")) {
                            return getUniModAccessionLink(cvTerm.getAccession());
                        } else { // psi-ms assumed
                            return getOlsAccessionLink(cvTerm.getAccession());
                        }
                    } else {
                        return null;
                    }
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
            return (new Double(0.0)).getClass();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    /**
     * Returns a web link to the given PSI-MOD CV term at
     * http://www.ebi.ac.uk/ontology-lookup.
     *
     * @param modAccession the PSI-MOD accession number
     * @return the OLS web link
     */
    public String getOlsAccessionLink(String modAccession) {
        String accessionNumberWithLink = "<html><a href=\"http://www.ebi.ac.uk/ontology-lookup/?termId=" + modAccession + "\""
                + "\"><font color=\"" + selectedRowHtmlTagFontColor + "\">"
                + modAccession + "</font></a></html>";
        return accessionNumberWithLink;
    }

    /**
     * Returns a web link to the given Unimod CV term at
     * http://www.ebi.ac.uk/ontology-lookup.
     *
     * @param unimodAccession the Unimod accession number
     * @return the Unimod web link
     */
    public String getUniModAccessionLink(String unimodAccession) {
        String accessionNumber = unimodAccession.substring("Unimod:".length()); // remove 'Unimod:'
        String accessionNumberWithLink = "<html><a href=\"http://www.unimod.org/modifications_view.php?editid1=" + accessionNumber + "\""
                + "\"><font color=\"" + selectedRowHtmlTagFontColor + "\">"
                + unimodAccession + "</font></a></html>";
        return accessionNumberWithLink;
    }

    /**
     * Updates the modification lists and tables.
     */
    public void updateModifications() {
        DefaultTableModel dm = (DefaultTableModel) defaultModificationsTable.getModel();
        dm.fireTableDataChanged();
        dm = (DefaultTableModel) userModificationsTable.getModel();
        dm.fireTableDataChanged();
        editUserPTM.setEnabled(userModificationsTable.getSelectedColumnCount() > 0);
        deleteUserPTM.setEnabled(userModificationsTable.getSelectedColumnCount() > 0);
        updateMassSparklines();

        ((TitledBorder) userModsPanel.getBorder()).setTitle("User Modifications (" + userModificationsTable.getRowCount() + ")");
        userModsPanel.revalidate();
        userModsPanel.repaint();
    }

    /**
     * Updates the PTM selection in the default table.
     */
    public void updatePtmSelection() {
        int row = defaultModificationsTable.convertRowIndexToView(searchPossibilities.get(searchCurrentSelection));
        defaultModificationsTable.setRowSelectionInterval(row, row);
        searchIndexLabel.setText("(" + (searchCurrentSelection + 1) + " of " + searchPossibilities.size() + ")");
        defaultModificationsTable.scrollRectToVisible(defaultModificationsTable.getCellRect(row, 0, false));
    }
}
