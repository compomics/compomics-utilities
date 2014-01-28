package com.compomics.util.gui;

import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.export.graphics.ExportGraphicsDialog;
import com.compomics.util.gui.export.graphics.ExportGraphicsDialogParent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.*;
import javax.swing.*;
import no.uib.jsparklines.data.XYDataPoint;
import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.TextAnchor;

/**
 * A dialog for creating and displaying Venn diagrams. (Work in progress...)
 *
 * @author Harald Barsnes
 */
public class VennDiagramDialog extends javax.swing.JDialog implements ExportGraphicsDialogParent {

    /**
     * The supported Venn diagram types.
     */
    public enum VennDiagramType {

        ONE_WAY, TWO_WAY, THREE_WAY, FOUR_WAY
    }
    /**
     * The current Venn diagram type.
     */
    private VennDiagramType currentVennDiagramType = VennDiagramType.THREE_WAY;
    /**
     * Map each dataset tooltip back to the given dataset.
     */
    private HashMap<String, String> tooltipToDatasetMap;
    /**
     * The current Venn diagram results/data.
     */
    private HashMap<String, ArrayList<String>> vennDiagramResults;
    /**
     * The current Venn diagram group names.
     */
    private HashMap<String, String> groupNames;
    /**
     * The chart panel.
     */
    private ChartPanel chartPanel;
    /**
     * The dialog parent.
     */
    private Frame dialogParent;
    /**
     * The last selected folder.
     */
    private String lastSelectedFolder = "user.home";
    /**
     * The normal icon for the parent dialog.
     */
    private Image normalIcon;
    /**
     * The icon to use when busy.
     */
    private Image waitingIcon;
    /**
     * The font size to use for the values.
     */
    private int fontSizeValues = 17;
    /**
     * The font size to use for the legend.
     */
    private int fontSizeLegend = 14;
    /**
     * If true, the legend is shown.
     */
    private boolean showLegend = true;
    /**
     * The legend location of Dataset A in a one to three way Venn diagram.
     */
    private XYDataPoint legendDatasetAThreeWay = new XYDataPoint(0.86, 0.86);
    /**
     * The legend location of Dataset B in a one to three way Venn diagram.
     */
    private XYDataPoint legendDatasetBThreeWay = new XYDataPoint(1.15, 0.86);
    /**
     * The legend location of Dataset C in a one to three way Venn diagram.
     */
    private XYDataPoint legendDatasetCThreeWay = new XYDataPoint(1.0, 1.22);
    /**
     * The legend location of Dataset A in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetAFourWay = new XYDataPoint(0.13, 0.53);
    /**
     * The legend location of Dataset B in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetBFourWay = new XYDataPoint(0.23, 0.43);
    /**
     * The legend location of Dataset C in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetCFourWay = new XYDataPoint(0.33, 0.33);
    /**
     * The legend location of Dataset D in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetDFourWay = new XYDataPoint(0.43, 0.23);

    /**
     * Creates a new XYPlottingDialog.
     *
     * @param dialogParent the dialog parent
     * @param modal
     * @param normalIcon the normal icon for the parent dialog
     * @param waitingIcon the icon to use when busy
     */
    public VennDiagramDialog(java.awt.Frame dialogParent, Image normalIcon, Image waitingIcon, boolean modal) {
        super(dialogParent, modal);
        initComponents();
        this.dialogParent = dialogParent;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;

        // default dataset colors
        int alphaLevel = 150;
        datasetAColorJPanel.setBackground(new java.awt.Color(0, 0, 255, alphaLevel));
        datasetBColorJPanel.setBackground(new java.awt.Color(0, 255, 0, alphaLevel));
        datasetCColorJPanel.setBackground(new java.awt.Color(255, 255, 0, alphaLevel));
        datasetDColorJPanel.setBackground(new java.awt.Color(255, 0, 0, alphaLevel));

        setUpGUI();
        setLocationRelativeTo(dialogParent);

        plotLayeredPaneComponentResized(null);
        setVisible(true);

        updatePlot();
    }

    /**
     * Main method. For testing purposes only.
     *
     * @param args
     */
    public static void main(String args[]) {

        // set the look and feel
        try {
            UtilitiesGUIDefaults.setLookAndFeel();
        } catch (Exception e) {
            // ignore
        }

        new VennDiagramDialog(new JFrame(), null, null, true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {

        ArrayList<String> a = new ArrayList<String>();
        ArrayList<String> b = new ArrayList<String>();
        ArrayList<String> c = new ArrayList<String>();
        ArrayList<String> d = new ArrayList<String>();

        String datasetA = datasetATextArea.getText();
        if (datasetA.trim().length() > 0) {
            String[] lines = datasetA.split("\n");
            a.addAll(Arrays.asList(lines));
        }

        String datasetB = datasetBTextArea.getText();
        if (datasetB.trim().length() > 0) {
            String[] lines = datasetB.split("\n");
            b.addAll(Arrays.asList(lines));
        }

        String datasetC = datasetCTextArea.getText();
        if (datasetC.trim().length() > 0) {
            String[] lines = datasetC.split("\n");
            c.addAll(Arrays.asList(lines));
        }

        String datasetD = datasetDTextArea.getText();
        if (datasetD.trim().length() > 0) {
            String[] lines = datasetD.split("\n");
            d.addAll(Arrays.asList(lines));
        }

        groupNames = new HashMap<String, String>();

        groupNames.put("a", datasetATextField.getText());
        groupNames.put("b", datasetBTextField.getText());
        groupNames.put("c", datasetCTextField.getText());
        groupNames.put("d", datasetDTextField.getText());

        vennDiagramResults = vennDiagramMaker(a, b, c, d);

        if (b.isEmpty() && c.isEmpty() && d.isEmpty()) {
            currentVennDiagramType = VennDiagramType.ONE_WAY;
        } else if (c.isEmpty() && d.isEmpty()) {
            currentVennDiagramType = VennDiagramType.TWO_WAY;
        } else if (d.isEmpty()) {
            currentVennDiagramType = VennDiagramType.THREE_WAY;
        } else {
            currentVennDiagramType = VennDiagramType.FOUR_WAY;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plotPopupMenu = new javax.swing.JPopupMenu();
        exportPlotMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        legendCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        legendLocationMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        fontSizeMenu = new javax.swing.JMenu();
        valueFontSizeMenuItem = new javax.swing.JMenuItem();
        legendFontSizeMenuItem = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        plotLayeredPane = new javax.swing.JLayeredPane();
        xyPlotPanel = new javax.swing.JPanel();
        plotPanel = new javax.swing.JPanel();
        plotOptionsJButton = new javax.swing.JButton();
        plotHelpJButton = new javax.swing.JButton();
        contextMenuPlotBackgroundPanel = new javax.swing.JPanel();
        dataPanel = new javax.swing.JPanel();
        datasetAScrollPane = new javax.swing.JScrollPane();
        datasetATextArea = new javax.swing.JTextArea();
        datasetBScrollPane = new javax.swing.JScrollPane();
        datasetBTextArea = new javax.swing.JTextArea();
        datasetCScrollPane = new javax.swing.JScrollPane();
        datasetCTextArea = new javax.swing.JTextArea();
        datasetDScrollPane = new javax.swing.JScrollPane();
        datasetDTextArea = new javax.swing.JTextArea();
        datasetATextField = new javax.swing.JTextField();
        datasetBTextField = new javax.swing.JTextField();
        datasetCTextField = new javax.swing.JTextField();
        datasetDTextField = new javax.swing.JTextField();
        datasetAColorJPanel = new javax.swing.JPanel();
        datasetBColorJPanel = new javax.swing.JPanel();
        datasetCColorJPanel = new javax.swing.JPanel();
        datasetDColorJPanel = new javax.swing.JPanel();

        exportPlotMenuItem.setText("Export");
        exportPlotMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPlotMenuItemActionPerformed(evt);
            }
        });
        plotPopupMenu.add(exportPlotMenuItem);
        plotPopupMenu.add(jSeparator1);

        legendCheckBoxMenuItem.setSelected(true);
        legendCheckBoxMenuItem.setText("Show Legend");
        legendCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legendCheckBoxMenuItemActionPerformed(evt);
            }
        });
        plotPopupMenu.add(legendCheckBoxMenuItem);

        legendLocationMenuItem.setText("Legend Location");
        legendLocationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legendLocationMenuItemActionPerformed(evt);
            }
        });
        plotPopupMenu.add(legendLocationMenuItem);
        plotPopupMenu.add(jSeparator2);

        fontSizeMenu.setText("Font Size");

        valueFontSizeMenuItem.setText("Values");
        valueFontSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFontSizeMenuItemActionPerformed(evt);
            }
        });
        fontSizeMenu.add(valueFontSizeMenuItem);

        legendFontSizeMenuItem.setText("Legend");
        legendFontSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legendFontSizeMenuItemActionPerformed(evt);
            }
        });
        fontSizeMenu.add(legendFontSizeMenuItem);

        plotPopupMenu.add(fontSizeMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Statistics");
        setModal(true);
        setPreferredSize(new java.awt.Dimension(600, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));
        backgroundPanel.setPreferredSize(new java.awt.Dimension(600, 600));

        plotLayeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                plotLayeredPaneComponentResized(evt);
            }
        });

        xyPlotPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Venn Diagram"));
        xyPlotPanel.setOpaque(false);
        xyPlotPanel.setLayout(new javax.swing.BoxLayout(xyPlotPanel, javax.swing.BoxLayout.PAGE_AXIS));

        plotPanel.setBackground(new java.awt.Color(255, 255, 255));
        plotPanel.setLayout(new javax.swing.BoxLayout(plotPanel, javax.swing.BoxLayout.LINE_AXIS));
        xyPlotPanel.add(plotPanel);

        xyPlotPanel.setBounds(0, 0, 580, 360);
        plotLayeredPane.add(xyPlotPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        plotOptionsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/contextual_menu_gray.png"))); // NOI18N
        plotOptionsJButton.setToolTipText("Plot Options");
        plotOptionsJButton.setBorder(null);
        plotOptionsJButton.setBorderPainted(false);
        plotOptionsJButton.setContentAreaFilled(false);
        plotOptionsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/contextual_menu_black.png"))); // NOI18N
        plotOptionsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                plotOptionsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                plotOptionsJButtonMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                plotOptionsJButtonMouseReleased(evt);
            }
        });
        plotOptionsJButton.setBounds(550, 5, 10, 19);
        plotLayeredPane.add(plotOptionsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        plotHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        plotHelpJButton.setToolTipText("Help");
        plotHelpJButton.setBorder(null);
        plotHelpJButton.setBorderPainted(false);
        plotHelpJButton.setContentAreaFilled(false);
        plotHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        plotHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                plotHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                plotHelpJButtonMouseExited(evt);
            }
        });
        plotHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotHelpJButtonActionPerformed(evt);
            }
        });
        plotHelpJButton.setBounds(570, 0, 10, 19);
        plotLayeredPane.add(plotHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPlotBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPlotBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPlotBackgroundPanel);
        contextMenuPlotBackgroundPanel.setLayout(contextMenuPlotBackgroundPanelLayout);
        contextMenuPlotBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPlotBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuPlotBackgroundPanelLayout.setVerticalGroup(
            contextMenuPlotBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        contextMenuPlotBackgroundPanel.setBounds(550, 0, 30, 19);
        plotLayeredPane.add(contextMenuPlotBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        dataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Data"));
        dataPanel.setOpaque(false);

        datasetATextArea.setTabSize(4);
        datasetATextArea.setText("1\n2\n3\n4\n5");
        datasetATextArea.setToolTipText("");
        datasetATextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetATextAreaKeyReleased(evt);
            }
        });
        datasetAScrollPane.setViewportView(datasetATextArea);

        datasetBTextArea.setTabSize(4);
        datasetBTextArea.setText("3\n5\n6");
        datasetBTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetBTextAreaKeyReleased(evt);
            }
        });
        datasetBScrollPane.setViewportView(datasetBTextArea);

        datasetCTextArea.setColumns(2);
        datasetCTextArea.setTabSize(4);
        datasetCTextArea.setText("3\n6\n7");
        datasetCTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetCTextAreaKeyReleased(evt);
            }
        });
        datasetCScrollPane.setViewportView(datasetCTextArea);

        datasetDTextArea.setColumns(2);
        datasetDTextArea.setTabSize(4);
        datasetDTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetDTextAreaKeyReleased(evt);
            }
        });
        datasetDScrollPane.setViewportView(datasetDTextArea);

        datasetATextField.setText("Dataset A");
        datasetATextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetATextFieldKeyReleased(evt);
            }
        });

        datasetBTextField.setText("Dataset B");
        datasetBTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetBTextFieldKeyReleased(evt);
            }
        });

        datasetCTextField.setText("Dataset C");
        datasetCTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetCTextFieldKeyReleased(evt);
            }
        });

        datasetDTextField.setText("Dataset D");
        datasetDTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                datasetDTextFieldKeyReleased(evt);
            }
        });

        datasetAColorJPanel.setBackground(new java.awt.Color(0, 0, 255));
        datasetAColorJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        datasetAColorJPanel.setToolTipText("The color used for Peptide A");
        datasetAColorJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                datasetAColorJPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout datasetAColorJPanelLayout = new javax.swing.GroupLayout(datasetAColorJPanel);
        datasetAColorJPanel.setLayout(datasetAColorJPanelLayout);
        datasetAColorJPanelLayout.setHorizontalGroup(
            datasetAColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );
        datasetAColorJPanelLayout.setVerticalGroup(
            datasetAColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        datasetBColorJPanel.setBackground(new java.awt.Color(0, 255, 0));
        datasetBColorJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        datasetBColorJPanel.setToolTipText("The color used for Peptide A");
        datasetBColorJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                datasetBColorJPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout datasetBColorJPanelLayout = new javax.swing.GroupLayout(datasetBColorJPanel);
        datasetBColorJPanel.setLayout(datasetBColorJPanelLayout);
        datasetBColorJPanelLayout.setHorizontalGroup(
            datasetBColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );
        datasetBColorJPanelLayout.setVerticalGroup(
            datasetBColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        datasetCColorJPanel.setBackground(new java.awt.Color(255, 255, 0));
        datasetCColorJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        datasetCColorJPanel.setToolTipText("The color used for Peptide A");
        datasetCColorJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                datasetCColorJPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout datasetCColorJPanelLayout = new javax.swing.GroupLayout(datasetCColorJPanel);
        datasetCColorJPanel.setLayout(datasetCColorJPanelLayout);
        datasetCColorJPanelLayout.setHorizontalGroup(
            datasetCColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );
        datasetCColorJPanelLayout.setVerticalGroup(
            datasetCColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        datasetDColorJPanel.setBackground(new java.awt.Color(255, 0, 0));
        datasetDColorJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        datasetDColorJPanel.setToolTipText("The color used for Peptide A");
        datasetDColorJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                datasetDColorJPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout datasetDColorJPanelLayout = new javax.swing.GroupLayout(datasetDColorJPanel);
        datasetDColorJPanel.setLayout(datasetDColorJPanelLayout);
        datasetDColorJPanelLayout.setHorizontalGroup(
            datasetDColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );
        datasetDColorJPanelLayout.setVerticalGroup(
            datasetDColorJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout dataPanelLayout = new javax.swing.GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datasetAScrollPane)
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(datasetATextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasetAColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datasetBScrollPane)
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(datasetBTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasetBColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datasetCScrollPane)
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(datasetCTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasetCColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datasetDScrollPane)
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(datasetDTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasetDColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(datasetATextField)
                    .addComponent(datasetAColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetBTextField)
                    .addComponent(datasetCTextField)
                    .addComponent(datasetDTextField)
                    .addComponent(datasetBColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetCColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetDColorJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datasetAScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetBScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetCScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetDScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        dataPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {datasetATextField, datasetBTextField, datasetCTextField, datasetDTextField});

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .addComponent(dataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(plotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void plotOptionsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotOptionsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_plotOptionsJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void plotOptionsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotOptionsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_plotOptionsJButtonMouseExited

    /**
     * Show the options for the plot.
     *
     * @param evt
     */
    private void plotOptionsJButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotOptionsJButtonMouseReleased
        plotPopupMenu.show(plotOptionsJButton, evt.getX(), evt.getY());
    }//GEN-LAST:event_plotOptionsJButtonMouseReleased

    /**
     * Export the plot to file.
     *
     * @param evt
     */
    private void exportPlotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPlotMenuItemActionPerformed
        new ExportGraphicsDialog(this, this, true, chartPanel);
    }//GEN-LAST:event_exportPlotMenuItemActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void plotHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void plotHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void plotHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/VennDiagramDialog.html"), // @TODO: setup the help page
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Statistics - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        System.exit(0); // @TODO: remove method when testing is done!
    }//GEN-LAST:event_formWindowClosing

    /**
     * Resize the layered pane.
     *
     * @param evt
     */
    private void plotLayeredPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_plotLayeredPaneComponentResized
        // resize the layered panels
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // move the icons
                plotLayeredPane.getComponent(0).setBounds(
                        plotLayeredPane.getWidth() - plotLayeredPane.getComponent(0).getWidth() - 22,
                        -2,
                        plotLayeredPane.getComponent(0).getWidth(),
                        plotLayeredPane.getComponent(0).getHeight());

                plotLayeredPane.getComponent(1).setBounds(
                        plotLayeredPane.getWidth() - plotLayeredPane.getComponent(1).getWidth() - 10,
                        -4,
                        plotLayeredPane.getComponent(1).getWidth(),
                        plotLayeredPane.getComponent(1).getHeight());

                plotLayeredPane.getComponent(2).setBounds(
                        plotLayeredPane.getWidth() - plotLayeredPane.getComponent(2).getWidth() - 5,
                        -3,
                        plotLayeredPane.getComponent(2).getWidth(),
                        plotLayeredPane.getComponent(2).getHeight());

                // resize the plot area
                plotLayeredPane.getComponent(3).setBounds(0, 0, plotLayeredPane.getWidth(), plotLayeredPane.getHeight());

                if (getCurrentVennDiagramType() != VennDiagramType.FOUR_WAY) {
                    int min = Math.min(plotLayeredPane.getWidth(), plotLayeredPane.getHeight());
                    plotPanel.setMaximumSize(new Dimension(min, min));
                    plotPanel.setPreferredSize(new Dimension(min, min));
                    updatePlot();
                } else {
                    plotPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                    updatePlot();
                }

                plotLayeredPane.revalidate();
                plotLayeredPane.repaint();
            }
        });
    }//GEN-LAST:event_plotLayeredPaneComponentResized

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetATextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetATextAreaKeyReleased
        setUpGUI();
        updatePlot();
        plotLayeredPaneComponentResized(null);
    }//GEN-LAST:event_datasetATextAreaKeyReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetCTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetCTextAreaKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetCTextAreaKeyReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetDTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetDTextAreaKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetDTextAreaKeyReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetATextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetATextFieldKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetATextFieldKeyReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetBTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetBTextFieldKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetBTextFieldKeyReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetCTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetCTextFieldKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetCTextFieldKeyReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetDTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetDTextFieldKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetDTextFieldKeyReleased

    /**
     * Change the color of dataset A.
     *
     * @param evt
     */
    private void datasetAColorJPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datasetAColorJPanelMouseClicked
        Color newColor = JColorChooser.showDialog(this, "Pick a Color", datasetAColorJPanel.getBackground());

        if (newColor != null) {
            datasetAColorJPanel.setBackground(newColor);
            updatePlot();
        }
    }//GEN-LAST:event_datasetAColorJPanelMouseClicked

    /**
     * Change the color of dataset B.
     *
     * @param evt
     */
    private void datasetBColorJPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datasetBColorJPanelMouseClicked
        Color newColor = JColorChooser.showDialog(this, "Pick a Color", datasetBColorJPanel.getBackground());

        if (newColor != null) {
            datasetBColorJPanel.setBackground(newColor);
            updatePlot();
        }
    }//GEN-LAST:event_datasetBColorJPanelMouseClicked

    /**
     * Change the color of dataset C.
     *
     * @param evt
     */
    private void datasetCColorJPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datasetCColorJPanelMouseClicked
        Color newColor = JColorChooser.showDialog(this, "Pick a Color", datasetCColorJPanel.getBackground());

        if (newColor != null) {
            datasetCColorJPanel.setBackground(newColor);
            updatePlot();
        }
    }//GEN-LAST:event_datasetCColorJPanelMouseClicked

    /**
     * Change the color of dataset D.
     *
     * @param evt
     */
    private void datasetDColorJPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datasetDColorJPanelMouseClicked
        Color newColor = JColorChooser.showDialog(this, "Pick a Color", datasetDColorJPanel.getBackground());

        if (newColor != null) {
            datasetDColorJPanel.setBackground(newColor);
            updatePlot();
        }
    }//GEN-LAST:event_datasetDColorJPanelMouseClicked

    /**
     * Turn the legend on/off.
     *
     * @param evt
     */
    private void legendCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legendCheckBoxMenuItemActionPerformed
        showLegend = legendCheckBoxMenuItem.isSelected();
        updatePlot();
    }//GEN-LAST:event_legendCheckBoxMenuItemActionPerformed

    /**
     * Allow the user to change the value font size.
     *
     * @param evt
     */
    private void valueFontSizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFontSizeMenuItemActionPerformed
        String value = JOptionPane.showInputDialog(this, "Values Font Size:", fontSizeValues);

        if (value != null) {
            try {
                fontSizeValues = Integer.parseInt(value);
                updatePlot();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Font size has to be an integer!", "Font Error", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_valueFontSizeMenuItemActionPerformed

    /**
     * Allow the user to change the legend font size.
     *
     * @param evt
     */
    private void legendFontSizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legendFontSizeMenuItemActionPerformed
        String value = JOptionPane.showInputDialog(this, "Legend Font Size:", fontSizeLegend);

        if (value != null) {
            try {
                fontSizeLegend = Integer.parseInt(value);
                updatePlot();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Font size has to be an integer!", "Font Error", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_legendFontSizeMenuItemActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void datasetBTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_datasetBTextAreaKeyReleased
        datasetATextAreaKeyReleased(null);
    }//GEN-LAST:event_datasetBTextAreaKeyReleased

    /**
     * Open the legend location dialog.
     *
     * @param evt
     */
    private void legendLocationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legendLocationMenuItemActionPerformed
        new VennDiagramLegendLocationDialog(this, true);
    }//GEN-LAST:event_legendLocationMenuItemActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JPanel contextMenuPlotBackgroundPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel datasetAColorJPanel;
    private javax.swing.JScrollPane datasetAScrollPane;
    private javax.swing.JTextArea datasetATextArea;
    private javax.swing.JTextField datasetATextField;
    private javax.swing.JPanel datasetBColorJPanel;
    private javax.swing.JScrollPane datasetBScrollPane;
    private javax.swing.JTextArea datasetBTextArea;
    private javax.swing.JTextField datasetBTextField;
    private javax.swing.JPanel datasetCColorJPanel;
    private javax.swing.JScrollPane datasetCScrollPane;
    private javax.swing.JTextArea datasetCTextArea;
    private javax.swing.JTextField datasetCTextField;
    private javax.swing.JPanel datasetDColorJPanel;
    private javax.swing.JScrollPane datasetDScrollPane;
    private javax.swing.JTextArea datasetDTextArea;
    private javax.swing.JTextField datasetDTextField;
    private javax.swing.JMenuItem exportPlotMenuItem;
    private javax.swing.JMenu fontSizeMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JCheckBoxMenuItem legendCheckBoxMenuItem;
    private javax.swing.JMenuItem legendFontSizeMenuItem;
    private javax.swing.JMenuItem legendLocationMenuItem;
    private javax.swing.JButton plotHelpJButton;
    private javax.swing.JLayeredPane plotLayeredPane;
    private javax.swing.JButton plotOptionsJButton;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JPopupMenu plotPopupMenu;
    private javax.swing.JMenuItem valueFontSizeMenuItem;
    private javax.swing.JPanel xyPlotPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Update the plot.
     */
    public void updatePlot() {

        plotPanel.removeAll();
        tooltipToDatasetMap = new HashMap<String, String>();

        DefaultXYZDataset xyzDataset = new DefaultXYZDataset();

        JFreeChart chart = ChartFactory.createBubbleChart(null, "X", "Y", xyzDataset, PlotOrientation.VERTICAL, false, true, false);
        XYPlot plot = chart.getXYPlot();

        if (currentVennDiagramType == VennDiagramType.ONE_WAY) {
            plot.getRangeAxis().setRange(0.86, 1.24);
            plot.getDomainAxis().setRange(0.85, 1.25);
        } else if (currentVennDiagramType == VennDiagramType.TWO_WAY) {
            plot.getRangeAxis().setRange(0.86, 1.24);
            plot.getDomainAxis().setRange(0.85, 1.25);
        } else if (currentVennDiagramType == VennDiagramType.THREE_WAY) {
            plot.getRangeAxis().setRange(0.86, 1.24);
            plot.getDomainAxis().setRange(0.85, 1.25);
        } else {
            plot.getRangeAxis().setRange(-0.04, 0.6);
            plot.getDomainAxis().setRange(-0.08, 0.7);
        }

        plot.getRangeAxis().setVisible(false);
        plot.getDomainAxis().setVisible(false);


        double radius = 0.1;
        Ellipse2D ellipse = new Ellipse2D.Double(1 - radius, 1 - radius, radius + radius, radius + radius);
        XYShapeAnnotation xyShapeAnnotation = new XYShapeAnnotation(ellipse, new BasicStroke(2f), new Color(140, 140, 140, 150), datasetAColorJPanel.getBackground()); // @TODO: make it possible set the line color and width?
        plot.addAnnotation(xyShapeAnnotation);

        if (currentVennDiagramType == VennDiagramType.TWO_WAY || currentVennDiagramType == VennDiagramType.THREE_WAY) {
            ellipse = new Ellipse2D.Double(1 - radius + 0.1, 1 - radius, radius + radius, radius + radius);
            xyShapeAnnotation = new XYShapeAnnotation(ellipse, new BasicStroke(2f), new Color(140, 140, 140, 150), datasetBColorJPanel.getBackground());
            plot.addAnnotation(xyShapeAnnotation);
        }

        if (currentVennDiagramType == VennDiagramType.THREE_WAY) {
            ellipse = new Ellipse2D.Double(1 - radius + 0.05, 1 - radius + 0.1, radius + radius, radius + radius);
            xyShapeAnnotation = new XYShapeAnnotation(ellipse, new BasicStroke(2f), new Color(140, 140, 140, 150), datasetCColorJPanel.getBackground());
            plot.addAnnotation(xyShapeAnnotation);
        }

        XYTextAnnotation anotation;

        if (currentVennDiagramType == VennDiagramType.ONE_WAY) {

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 1.0, 1.0);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAThreeWay.getX(), legendDatasetAThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }

        } else if (currentVennDiagramType == VennDiagramType.TWO_WAY) {

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 0.96, 1.0);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("b").size(), 1.14, 1.0);
            anotation.setToolTipText(groupNames.get("b"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "b");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ab").size(), 1.05, 1.0);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ab");

            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAThreeWay.getX(), legendDatasetAThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("b"), legendDatasetBThreeWay.getX(), legendDatasetBThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }

        } else if (currentVennDiagramType == VennDiagramType.THREE_WAY) {

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 0.96, 0.97);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("b").size(), 1.14, 0.97);
            anotation.setToolTipText(groupNames.get("b"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "b");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ab").size(), 1.05, 0.97);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ab");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("c").size(), 1.05, 1.14);
            anotation.setToolTipText(groupNames.get("c"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "c");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ac").size(), 0.99, 1.065);
            anotation.setToolTipText("<html>" + groupNames.get("a") + "  &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ac");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bc").size(), 1.11, 1.065);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bc");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abc").size(), 1.05, 1.036);
            anotation.setToolTipText("<html>" + groupNames.get("a") + "  &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abc");


            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAThreeWay.getX(), legendDatasetAThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("b"), legendDatasetBThreeWay.getX(), legendDatasetBThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("c"), legendDatasetCThreeWay.getX(), legendDatasetCThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }

        } else if (currentVennDiagramType == VennDiagramType.FOUR_WAY) {

            XYBoxAnnotation anotation2 = new XYBoxAnnotation(0, 0, 0.2, 0.5, new BasicStroke(2), Color.LIGHT_GRAY, datasetAColorJPanel.getBackground());
            plot.addAnnotation(anotation2);

            anotation2 = new XYBoxAnnotation(0.1, 0, 0.3, 0.4, new BasicStroke(2), Color.LIGHT_GRAY, datasetBColorJPanel.getBackground());
            plot.addAnnotation(anotation2);

            anotation2 = new XYBoxAnnotation(0, 0.1, 0.4, 0.3, new BasicStroke(2), Color.LIGHT_GRAY, datasetCColorJPanel.getBackground());
            plot.addAnnotation(anotation2);

            anotation2 = new XYBoxAnnotation(0, 0, 0.5, 0.2, new BasicStroke(2), Color.LIGHT_GRAY, datasetDColorJPanel.getBackground());
            plot.addAnnotation(anotation2);

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 0.15, 0.45);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ab").size(), 0.15, 0.35);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ab");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abc").size(), 0.15, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abc");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abcd").size(), 0.15, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abcd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abd").size(), 0.15, 0.05);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ac").size(), 0.05, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ac");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("acd").size(), 0.05, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "acd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ad").size(), 0.05, 0.05);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ad");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("b").size(), 0.25, 0.35);
            anotation.setToolTipText("<html>" + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "b");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bc").size(), 0.25, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bc");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bcd").size(), 0.25, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bcd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bd").size(), 0.25, 0.05);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("c").size(), 0.35, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "c");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("cd").size(), 0.35, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "cd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("d").size(), 0.45, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "d");


            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAFourWay.getX(), legendDatasetAFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("b"), legendDatasetBFourWay.getX(), legendDatasetBFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("c"), legendDatasetCFourWay.getX(), legendDatasetCFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("d"), legendDatasetDFourWay.getX(), legendDatasetDFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }
        }

        // set up the renderer
        XYBubbleRenderer renderer = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_RANGE_AXIS);
        renderer.setBaseToolTipGenerator(new StandardXYZToolTipGenerator());
        plot.setRenderer(renderer);

        // make all datapoints semitransparent
        plot.setForegroundAlpha(0.5f);

        // remove space before/after the domain axis
        plot.getDomainAxis().setUpperMargin(0);
        plot.getDomainAxis().setLowerMargin(0);

        plot.setRangeGridlinePaint(Color.black);

        // hide unwanted chart details
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        chart.getPlot().setOutlineVisible(false);

        // set background color
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);
        chartPanel = new ChartPanel(chart);

        // disable the pop up menu
        chartPanel.setPopupMenu(null);

        chartPanel.setBackground(Color.WHITE);

        // add the plot to the chart
        plotPanel.add(chartPanel);

        plotPanel.revalidate();

        plotPanel.repaint();

        // add chart mouse listener
        chartPanel.addChartMouseListener(
                new ChartMouseListener() {
                    public void chartMouseClicked(ChartMouseEvent cme) {
                        mouseClickedInChart(cme);
                    }

                    public void chartMouseMoved(ChartMouseEvent cme) {
                        mouseMovedInChart(cme);
                    }
                });


        // add more chart mouse listeners
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
    }

    /**
     * Handles mouse clicks in the chart panel.
     *
     * @param event
     */
    public void mouseClickedInChart(ChartMouseEvent event) {

        ArrayList<ChartEntity> entities = getEntitiesForPoint(event.getTrigger().getPoint().x, event.getTrigger().getPoint().y);

        if (entities.isEmpty()) {
            return;
        }

        boolean dataPointFound = false;
        String dataPointTooltip = "";

        for (ChartEntity tempEntity : entities) {
            if (tempEntity instanceof XYAnnotationEntity) {
                if (((XYAnnotationEntity) tempEntity).getToolTipText() != null) {
                    dataPointFound = true;
                    dataPointTooltip = ((XYAnnotationEntity) tempEntity).getToolTipText();
                }
            }
        }

        if (dataPointFound) {
            String dataset = tooltipToDatasetMap.get(dataPointTooltip);
            JOptionPane.showMessageDialog(this, dataPointTooltip + ":\n" + vennDiagramResults.get(dataset), "Selected Values", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Handles mouse movements in the chart panel.
     *
     * @param event
     */
    public void mouseMovedInChart(ChartMouseEvent event) {

        ArrayList<ChartEntity> entities = getEntitiesForPoint(event.getTrigger().getPoint().x, event.getTrigger().getPoint().y);

        boolean dataPointFound = false;

        for (ChartEntity tempEntity : entities) {
            if (tempEntity instanceof XYAnnotationEntity) {
                if (((XYAnnotationEntity) tempEntity).getToolTipText() != null) {
                    dataPointFound = true;
                }
            }
        }

        if (dataPointFound) {
            chartPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else {
            chartPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Returns a list of the entities at the given x, y view location.
     *
     * @param viewX the x location
     * @param viewY the y location
     * @return a list of the entities
     */
    public ArrayList<ChartEntity> getEntitiesForPoint(int viewX, int viewY) {

        ArrayList<ChartEntity> entitiesForPoint = new ArrayList<ChartEntity>();
        ChartRenderingInfo info = chartPanel.getChartRenderingInfo();

        if (info != null) {
            Insets insets = chartPanel.getInsets();
            double x = (viewX - insets.left) / chartPanel.getScaleX();
            double y = (viewY - insets.top) / chartPanel.getScaleY();
            EntityCollection allEntities = info.getEntityCollection();
            int numEntities = allEntities.getEntityCount();

            for (int i = 0; i < numEntities; i++) {
                ChartEntity entity = allEntities.getEntity(i);
                if (entity.getArea().contains(x, y)) {
                    entitiesForPoint.add(entity);
                }
            }
        }

        return entitiesForPoint;
    }

    public void setSelectedExportFolder(String selectedFolder) {
        lastSelectedFolder = selectedFolder;
    }

    public String getDefaultExportFolder() {
        return lastSelectedFolder;
    }

    /**
     * Create the Venn diagram groupings based on the provided data.
     *
     * @param groupA
     * @param groupB
     * @param groupC
     * @param groupD
     * @return the Venn diagram groupings
     */
    public HashMap<String, ArrayList<String>> vennDiagramMaker(ArrayList<String> groupA, ArrayList<String> groupB, ArrayList<String> groupC, ArrayList<String> groupD) {

        HashMap<String, ArrayList<String>> tempVennDiagramResults = new HashMap<String, ArrayList<String>>();

        ArrayList<String> a = new ArrayList<String>();
        ArrayList<String> b = new ArrayList<String>();
        ArrayList<String> c = new ArrayList<String>();
        ArrayList<String> d = new ArrayList<String>();

        ArrayList<String> ab = new ArrayList<String>();
        ArrayList<String> ac = new ArrayList<String>();
        ArrayList<String> ad = new ArrayList<String>();
        ArrayList<String> bc = new ArrayList<String>();
        ArrayList<String> bd = new ArrayList<String>();
        ArrayList<String> cd = new ArrayList<String>();

        ArrayList<String> abc = new ArrayList<String>();
        ArrayList<String> abd = new ArrayList<String>();
        ArrayList<String> acd = new ArrayList<String>();
        ArrayList<String> bcd = new ArrayList<String>();

        ArrayList<String> abcd = new ArrayList<String>();

        ArrayList<String> allDataPoints = new ArrayList<String>();

        for (String temp : groupA) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupB = groupB.contains(temp);
                boolean inGroupC = groupC.contains(temp);
                boolean inGroupD = groupD.contains(temp);

                if (!inGroupB && !inGroupC && !inGroupD) {
                    a.add(temp);
                } else {
                    if (inGroupB && !inGroupC && !inGroupD) {
                        ab.add(temp);
                    } else if (!inGroupB && inGroupC && !inGroupD) {
                        ac.add(temp);
                    } else if (!inGroupB && !inGroupC && inGroupD) {
                        ad.add(temp);
                    } else if (inGroupB && inGroupC && !inGroupD) {
                        abc.add(temp);
                    } else if (inGroupB && !inGroupC && inGroupD) {
                        abd.add(temp);
                    } else if (!inGroupB && inGroupC && inGroupD) {
                        acd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }

        for (String temp : groupB) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupA = groupA.contains(temp);
                boolean inGroupC = groupC.contains(temp);
                boolean inGroupD = groupD.contains(temp);

                if (!inGroupA && !inGroupC && !inGroupD) {
                    b.add(temp);
                } else {
                    if (inGroupA && !inGroupC && !inGroupD) {
                        ab.add(temp);
                    } else if (!inGroupA && inGroupC && !inGroupD) {
                        bc.add(temp);
                    } else if (!inGroupA && !inGroupC && inGroupD) {
                        bd.add(temp);
                    } else if (inGroupA && inGroupC && !inGroupD) {
                        abc.add(temp);
                    } else if (inGroupA && !inGroupC && inGroupD) {
                        abd.add(temp);
                    } else if (!inGroupA && inGroupC && inGroupD) {
                        bcd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }


        for (String temp : groupC) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupA = groupA.contains(temp);
                boolean inGroupB = groupB.contains(temp);
                boolean inGroupD = groupD.contains(temp);

                if (!inGroupA && !inGroupB && !inGroupD) {
                    c.add(temp);
                } else {
                    if (inGroupA && !inGroupB && !inGroupD) {
                        ac.add(temp);
                    } else if (!inGroupA && inGroupB && !inGroupD) {
                        bc.add(temp);
                    } else if (!inGroupA && !inGroupB && inGroupD) {
                        cd.add(temp);
                    } else if (inGroupA && inGroupB && !inGroupD) {
                        abc.add(temp);
                    } else if (inGroupA && !inGroupB && inGroupD) {
                        acd.add(temp);
                    } else if (!inGroupA && inGroupB && inGroupD) {
                        bcd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }

        for (String temp : groupD) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupA = groupA.contains(temp);
                boolean inGroupB = groupB.contains(temp);
                boolean inGroupC = groupC.contains(temp);

                if (!inGroupA && !inGroupB && !inGroupC) {
                    d.add(temp);
                } else {
                    if (inGroupA && !inGroupB && !inGroupC) {
                        ad.add(temp);
                    } else if (!inGroupA && inGroupB && !inGroupC) {
                        bd.add(temp);
                    } else if (!inGroupA && !inGroupB && inGroupC) {
                        cd.add(temp);
                    } else if (inGroupA && inGroupB && !inGroupC) {
                        abd.add(temp);
                    } else if (inGroupA && !inGroupB && inGroupC) {
                        acd.add(temp);
                    } else if (!inGroupA && inGroupB && inGroupC) {
                        bcd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }


        // add the results to the hashmap
        tempVennDiagramResults.put("a", a);
        tempVennDiagramResults.put("b", b);
        tempVennDiagramResults.put("c", c);
        tempVennDiagramResults.put("d", d);

        tempVennDiagramResults.put("ab", ab);
        tempVennDiagramResults.put("ac", ac);
        tempVennDiagramResults.put("ad", ad);
        tempVennDiagramResults.put("bc", bc);
        tempVennDiagramResults.put("bd", bd);
        tempVennDiagramResults.put("cd", cd);

        tempVennDiagramResults.put("abc", abc);
        tempVennDiagramResults.put("abd", abd);
        tempVennDiagramResults.put("acd", abd);
        tempVennDiagramResults.put("bcd", bcd);

        tempVennDiagramResults.put("abcd", abcd);


        boolean debug = false;

        if (debug) {

            System.out.print("a: ");
            for (String temp : a) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("b: ");
            for (String temp : b) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("c: ");
            for (String temp : c) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("d: ");
            for (String temp : d) {
                System.out.print(temp + ", ");
            }
            System.out.println();


            System.out.print("ab: ");
            for (String temp : ab) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("ac: ");
            for (String temp : ac) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("ad: ");
            for (String temp : ad) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("bc: ");
            for (String temp : bc) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("bd: ");
            for (String temp : bd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("cd: ");
            for (String temp : cd) {
                System.out.print(temp + ", ");
            }
            System.out.println();


            System.out.print("abc: ");
            for (String temp : abc) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("abd: ");
            for (String temp : abd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("bcd: ");
            for (String temp : bcd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("acd: ");
            for (String temp : acd) {
                System.out.print(temp + ", ");
            }
            System.out.println();


            System.out.print("abcd: ");
            for (String temp : abcd) {
                System.out.print(temp + ", ");
            }
            System.out.println();
        }

        return tempVennDiagramResults;

    }

    public Image getNormalIcon() {
        return Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/compomics-utilities.png"));
    }

    public Image getWaitingIcon() {
        return Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/compomics-utilities.png"));
    }

    /**
     * Returns the chart panel.
     *
     * @return the chart panel
     */
    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    /**
     * Returns a standard map of the group names. Keys: a, b, c and d.
     *
     * @return a standard map of the group names
     */
    public HashMap<String, String> getGroupNames() {
        return groupNames;
    }

    /**
     * Returns the legend location of Dataset A in a three way Venn diagram.
     *
     * @return the legendDatasetAThreeWay
     */
    public XYDataPoint getLegendDatasetAThreeWay() {
        return legendDatasetAThreeWay;
    }

    /**
     * Set the legend location of Dataset A in a three way Venn diagram.
     *
     * @param legendDatasetAThreeWay the legendDatasetAThreeWay to set
     */
    public void setLegendDatasetAThreeWay(XYDataPoint legendDatasetAThreeWay) {
        this.legendDatasetAThreeWay = legendDatasetAThreeWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset B in a three way Venn diagram.
     *
     * @return the legendDatasetBThreeWay
     */
    public XYDataPoint getLegendDatasetBThreeWay() {
        return legendDatasetBThreeWay;
    }

    /**
     * Set the legend location of Dataset B in a three way Venn diagram.
     *
     * @param legendDatasetBThreeWay the legendDatasetBThreeWay to set
     */
    public void setLegendDatasetBThreeWay(XYDataPoint legendDatasetBThreeWay) {
        this.legendDatasetBThreeWay = legendDatasetBThreeWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset C in a three way Venn diagram.
     *
     * @return the legendDatasetCThreeWay
     */
    public XYDataPoint getLegendDatasetCThreeWay() {
        return legendDatasetCThreeWay;
    }

    /**
     * Set the legend location of Dataset C in a three way Venn diagram.
     *
     * @param legendDatasetCThreeWay the legendDatasetCThreeWay to set
     */
    public void setLegendDatasetCThreeWay(XYDataPoint legendDatasetCThreeWay) {
        this.legendDatasetCThreeWay = legendDatasetCThreeWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset A in a four way Venn diagram.
     *
     * @return the legendDatasetAFourWay
     */
    public XYDataPoint getLegendDatasetAFourWay() {
        return legendDatasetAFourWay;
    }

    /**
     * Set the legend location of Dataset A in a four way Venn diagram.
     *
     * @param legendDatasetAFourWay the legendDatasetAFourWay to set
     */
    public void setLegendDatasetAFourWay(XYDataPoint legendDatasetAFourWay) {
        this.legendDatasetAFourWay = legendDatasetAFourWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset B in a four way Venn diagram.
     *
     * @return the legendDatasetBFourWay
     */
    public XYDataPoint getLegendDatasetBFourWay() {
        return legendDatasetBFourWay;
    }

    /**
     * Set the legend location of Dataset B in a four way Venn diagram.
     *
     * @param legendDatasetBFourWay the legendDatasetBFourWay to set
     */
    public void setLegendDatasetBFourWay(XYDataPoint legendDatasetBFourWay) {
        this.legendDatasetBFourWay = legendDatasetBFourWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset C in a four way Venn diagram.
     *
     * @return the legendDatasetCFourWay
     */
    public XYDataPoint getLegendDatasetCFourWay() {
        return legendDatasetCFourWay;
    }

    /**
     * Set the legend location of Dataset C in a four way Venn diagram.
     *
     * @param legendDatasetCFourWay the legendDatasetCFourWay to set
     */
    public void setLegendDatasetCFourWay(XYDataPoint legendDatasetCFourWay) {
        this.legendDatasetCFourWay = legendDatasetCFourWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset D in a four way Venn diagram.
     *
     * @return the legendDatasetDFourWay
     */
    public XYDataPoint getLegendDatasetDFourWay() {
        return legendDatasetDFourWay;
    }

    /**
     * Set the legend location of Dataset D in a four way Venn diagram.
     *
     * @param legendDatasetDFourWay the legendDatasetDFourWay to set
     */
    public void setLegendDatasetDFourWay(XYDataPoint legendDatasetDFourWay) {
        this.legendDatasetDFourWay = legendDatasetDFourWay;
        updatePlot();
    }

    /**
     * Returns the current Venn diagram type.
     *
     * @return the currentVennDiagramType
     */
    public VennDiagramType getCurrentVennDiagramType() {
        return currentVennDiagramType;
    }
}