package com.compomics.util.gui;

import com.compomics.util.Util;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.export.graphics.ExportGraphicsDialog;
import com.compomics.util.gui.tablemodels.SelfUpdatingTableModel;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.preferences.LastSelectedFolder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import no.uib.jsparklines.data.XYDataPoint;
import no.uib.jsparklines.data.StartIndexes;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import no.uib.jsparklines.renderers.util.AreaRenderer;
import no.uib.jsparklines.renderers.util.GradientColorCoding;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.*;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.*;
import umontreal.iro.lecuyer.gof.KernelDensity;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.KernelDensityGen;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.RandomStream;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.jfree.chart.annotations.XYTextAnnotation;

/**
 * A dialog that makes it straightforward to inspect compare the values of two
 * columns in a table in a XY plot. Currently supported data types for the plots
 * are Integer, Double, XYDataPoint and StartIndexes. For XYDataPoint the x
 * value is used, while for StartIndexes the first index is used.
 *
 * @author Harald Barsnes
 */
public class XYPlottingDialog extends javax.swing.JDialog implements VisibleTableColumnsDialogParent {

    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The table model.
     */
    private TableModel tableModel;
    /**
     * The chart panel.
     */
    private ChartPanel chartPanel;
    /**
     * The bubble size.
     */
    private double bubbleSize = 1.0;
    /**
     * The bubble scaling factor in percent. 1.0 means no scaling.
     */
    private double bubbleScalingFactor = 1.0;
    /**
     * The number if bins for the histograms.
     */
    private int numberOfBins = 100;
    /**
     * If true, the user defined bin size will be used instead of the automatic
     * one.
     */
    private boolean userDefinedBinSize = false;
    /**
     * The dialog parent.
     */
    private Frame dialogParent;
    /**
     * If true, the plot is currently being updated.
     */
    private boolean isPlotting = false;
    /**
     * If true, the mouse is currently being dragged.
     */
    private boolean mouseDragged = false;
    /**
     * The starting location of the mouse dragging.
     */
    private Point dragStart;
    /**
     * The ending location of the mouse dragging.
     */
    private Point dragEnd;
    /**
     * The currently selected data points. The keys are the series keys, while
     * the elements are the lists of data indexes.
     */
    private HashMap<Integer, ArrayList<Integer>> selectedDataPoints;
    /**
     * If true, selection is currently being performed.
     */
    private boolean selectionActive = false; // @TODO: can this be combined with mouseDragged??
    /**
     * The data point to model row number map.
     */
    private HashMap<String, Integer> dataPointToRowNumber;
    /**
     * The index of the selected rows.
     */
    private ArrayList<Integer> selectedModelRows;
    /**
     * The cell renderers for the table.
     */
    private HashMap<Integer, TableCellRenderer> cellRenderers;
    /**
     * The maximum column widths for the table.
     */
    private HashMap<Integer, Integer> maxColumnWidths;
    /**
     * The minimum column widths for the table.
     */
    private HashMap<Integer, Integer> minColumnWidths;
    /**
     * The table column header tool tips.
     */
    private ArrayList<String> tableToolTips;
    /**
     * The color gradient to use.
     */
    private GradientColorCoding.ColorGradient colorGradient = GradientColorCoding.ColorGradient.BlueWhiteGreen;
    /**
     * The color used for the bars in the histograms.
     */
    private Color histogramColor = new Color(110, 196, 97);
    /**
     * The last selected folder.
     */
    private LastSelectedFolder lastSelectedFolder;
    /**
     * The normal icon for the parent dialog.
     */
    private Image normalIcon;
    /**
     * The icon to use when busy.
     */
    private Image waitingIcon;
    /**
     * The table columns.
     */
    private ArrayList<TableColumn> allTableColumns;
    /**
     * Boolean indicators of which columns to show.
     */
    private HashMap<Integer, Boolean> visibleColumns;
    /**
     * If true, gradient color coding is used.
     */
    private boolean useGradientColorCoding = false;
    /**
     * If true, the regression line is shown.
     */
    private boolean showRegressionLine = true;
    /**
     * The data filters.
     */
    private HashMap<String, String> filters = new HashMap<String, String>();
    /**
     * The column names.
     */
    private Vector<String> colummnNames;
    /**
     * The rows remaining after applying the data filters.
     */
    private ArrayList<Integer> rowsAfterDataFiltering = new ArrayList<Integer>();

    /**
     * Creates a new XYPlottingDialog.
     *
     * @param dialogParent the dialog parent
     * @param table the table to display the xy plot for
     * @param tableToolTips the table tooltips
     * @param modal if the dialog is to be modal or not
     * @param normalIcon the normal icon for the parent dialog
     * @param waitingIcon the icon to use when busy
     */
    public XYPlottingDialog(java.awt.Frame dialogParent, JTable table, ArrayList<String> tableToolTips,
            Image normalIcon, Image waitingIcon, boolean modal) {
        super(dialogParent, modal);
        initComponents();
        this.dialogParent = dialogParent;
        this.tableModel = table.getModel();
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;

        cellRenderers = new HashMap<Integer, TableCellRenderer>();
        maxColumnWidths = new HashMap<Integer, Integer>();
        minColumnWidths = new HashMap<Integer, Integer>();

        if (table.getRowCount() > 0) {
            for (int i = 0; i < table.getColumnCount(); i++) {
                cellRenderers.put(i, table.getCellRenderer(0, i));
                minColumnWidths.put(i, table.getColumn(table.getColumnName(i)).getMinWidth());
                maxColumnWidths.put(i, table.getColumn(table.getColumnName(i)).getMaxWidth());
            }
        }

        this.tableToolTips = tableToolTips;

        setUpGUI();
        updatePlot();
        setLocationRelativeTo(dialogParent);
        backgroundPanelComponentResized(null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {

        // @TODO: only show the values of the supported types for each drop down menu...
        colummnNames = new Vector<String>();
        Vector<String> colummnNamesExtended = new Vector<String>();
        colummnNamesExtended.add(0, "[user defined]");

        int columnCount = tableModel.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            if (tableModel.getColumnName(i).trim().length() == 0) {
                colummnNames.add("(column " + (i + 1) + ")");
                colummnNamesExtended.add("(column " + (i + 1) + ")");
            } else {
                colummnNames.add(tableModel.getColumnName(i));
                colummnNamesExtended.add(tableModel.getColumnName(i));
            }
        }

        xAxisComboBox.setModel(new DefaultComboBoxModel(colummnNames));
        yAxisComboBox.setModel(new DefaultComboBoxModel(colummnNames));
        colorsComboBox.setModel(new DefaultComboBoxModel(colummnNames));
        bubbleSizeComboBox.setModel(new DefaultComboBoxModel(colummnNamesExtended));

        selectedValuesTable.setModel(tableModel);

        allTableColumns = new ArrayList<TableColumn>();
        visibleColumns = new HashMap<Integer, Boolean>();

        for (int i = 0; i < selectedValuesTable.getColumnCount(); i++) {
            allTableColumns.add(selectedValuesTable.getColumn(selectedValuesTable.getColumnName(i)));
            visibleColumns.put(i, true);
        }

        selectedValuesScrollPane.getViewport().setOpaque(false);
        selectedValuesTable.getTableHeader().setReorderingAllowed(false);

        if (tableModel instanceof SelfUpdatingTableModel) {
            SelfUpdatingTableModel.addSortListener(selectedValuesTable, new ProgressDialogX(dialogParent,
                    normalIcon,
                    waitingIcon,
                    true));

            // add scrolling listeners
            //SelfUpdatingTableModel.addScrollListeners(selectedValuesTable, selectedValuesScrollPane, selectedValuesScrollPane.getVerticalScrollBar()); // @TODO: should work, but has no effect it seems...
        } else {
            selectedValuesTable.setAutoCreateRowSorter(true);
        }

        Iterator<Integer> iterator = cellRenderers.keySet().iterator();

        while (iterator.hasNext()) {
            Integer columnIndex = iterator.next();
            selectedValuesTable.getColumn(selectedValuesTable.getColumnName(columnIndex)).setCellRenderer(cellRenderers.get(columnIndex));
        }

        iterator = minColumnWidths.keySet().iterator();

        while (iterator.hasNext()) {
            Integer columnIndex = iterator.next();
            selectedValuesTable.getColumn(selectedValuesTable.getColumnName(columnIndex)).setMinWidth(minColumnWidths.get(columnIndex));
        }

        iterator = maxColumnWidths.keySet().iterator();

        while (iterator.hasNext()) {
            Integer columnIndex = iterator.next();
            selectedValuesTable.getColumn(selectedValuesTable.getColumnName(columnIndex)).setMaxWidth(maxColumnWidths.get(columnIndex));
        }

        // correct the color for the upper right corner
        JPanel selectedValuesCorner = new JPanel();
        selectedValuesCorner.setBackground(selectedValuesTable.getTableHeader().getBackground());
        selectedValuesScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, selectedValuesCorner);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plotTypeButtonGroup = new javax.swing.ButtonGroup();
        dragButtonGroup = new javax.swing.ButtonGroup();
        plotPopupMenu = new javax.swing.JPopupMenu();
        exportPlotMenuItem = new javax.swing.JMenuItem();
        regressionLineCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        selectedValuesTablePopupMenu = new javax.swing.JPopupMenu();
        exportSelectedValuesMenuItem = new javax.swing.JMenuItem();
        hideColumnsMenuItem = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        plotTypePanel = new javax.swing.JPanel();
        densityPlotRadioButton = new javax.swing.JRadioButton();
        histogramRadioButton = new javax.swing.JRadioButton();
        xyPlotRadioButton = new javax.swing.JRadioButton();
        xAxisPanel = new javax.swing.JPanel();
        xAxisComboBox = new javax.swing.JComboBox();
        xAxisLabel = new javax.swing.JLabel();
        yAxisComboBox = new javax.swing.JComboBox();
        yAxisLabel = new javax.swing.JLabel();
        colorLabel = new javax.swing.JLabel();
        colorsComboBox = new javax.swing.JComboBox();
        bubbleSizeLabel = new javax.swing.JLabel();
        bubbleSizeComboBox = new javax.swing.JComboBox();
        binSizeSpinner = new javax.swing.JSpinner();
        binsLabel = new javax.swing.JLabel();
        dragSettingsPanel = new javax.swing.JPanel();
        dragToSelectRadioButton = new javax.swing.JRadioButton();
        dragToZoomRadioButton = new javax.swing.JRadioButton();
        logAcisPanel = new javax.swing.JPanel();
        xAxisLogCheckBox = new javax.swing.JCheckBox();
        yAxisLogCheckBox = new javax.swing.JCheckBox();
        sizeLogCheckBox = new javax.swing.JCheckBox();
        selectedValuesLayeredPane = new javax.swing.JLayeredPane();
        selectedValuesPanel = new javax.swing.JPanel();
        selectedValuesScrollPane = new javax.swing.JScrollPane();
        selectedValuesTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) tableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        selectedValuesTableOptionsJButton = new javax.swing.JButton();
        selectedValuesTableHelpJButton = new javax.swing.JButton();
        contextMenuSelectedValuesTableBackgroundPanel = new javax.swing.JPanel();
        plotLayeredPane = new javax.swing.JLayeredPane();
        xyPlotPanel = new javax.swing.JPanel();
        plotPanel = new javax.swing.JPanel();
        plotOptionsJButton = new javax.swing.JButton();
        plotHelpJButton = new javax.swing.JButton();
        contextMenuPlotBackgroundPanel = new javax.swing.JPanel();
        filterPanel = new javax.swing.JPanel();
        editFiltersButton = new javax.swing.JButton();

        exportPlotMenuItem.setText("Export");
        exportPlotMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPlotMenuItemActionPerformed(evt);
            }
        });
        plotPopupMenu.add(exportPlotMenuItem);

        regressionLineCheckBoxMenuItem.setSelected(true);
        regressionLineCheckBoxMenuItem.setText("Regression Line");
        regressionLineCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regressionLineCheckBoxMenuItemActionPerformed(evt);
            }
        });
        plotPopupMenu.add(regressionLineCheckBoxMenuItem);

        exportSelectedValuesMenuItem.setText("Export");
        exportSelectedValuesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSelectedValuesMenuItemActionPerformed(evt);
            }
        });
        selectedValuesTablePopupMenu.add(exportSelectedValuesMenuItem);

        hideColumnsMenuItem.setText("Hide/Show Columns");
        hideColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideColumnsMenuItemActionPerformed(evt);
            }
        });
        selectedValuesTablePopupMenu.add(hideColumnsMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Statistics (beta)");
        setMinimumSize(new java.awt.Dimension(800, 700));

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));
        backgroundPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                backgroundPanelComponentResized(evt);
            }
        });

        plotTypePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plot Type"));
        plotTypePanel.setOpaque(false);

        plotTypeButtonGroup.add(densityPlotRadioButton);
        densityPlotRadioButton.setText("Density");
        densityPlotRadioButton.setIconTextGap(10);
        densityPlotRadioButton.setOpaque(false);
        densityPlotRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                densityPlotRadioButtonActionPerformed(evt);
            }
        });

        plotTypeButtonGroup.add(histogramRadioButton);
        histogramRadioButton.setText("Histogram");
        histogramRadioButton.setIconTextGap(10);
        histogramRadioButton.setOpaque(false);
        histogramRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histogramRadioButtonActionPerformed(evt);
            }
        });

        plotTypeButtonGroup.add(xyPlotRadioButton);
        xyPlotRadioButton.setSelected(true);
        xyPlotRadioButton.setText("XY Plot");
        xyPlotRadioButton.setIconTextGap(10);
        xyPlotRadioButton.setOpaque(false);
        xyPlotRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyPlotRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout plotTypePanelLayout = new javax.swing.GroupLayout(plotTypePanel);
        plotTypePanel.setLayout(plotTypePanelLayout);
        plotTypePanelLayout.setHorizontalGroup(
            plotTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plotTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xyPlotRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(histogramRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(densityPlotRadioButton)
                .addContainerGap())
        );
        plotTypePanelLayout.setVerticalGroup(
            plotTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plotTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plotTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(densityPlotRadioButton)
                    .addComponent(histogramRadioButton)
                    .addComponent(xyPlotRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        xAxisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Values"));
        xAxisPanel.setOpaque(false);

        xAxisComboBox.setMaximumRowCount(30);
        xAxisComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xAxisComboBoxActionPerformed(evt);
            }
        });

        xAxisLabel.setText("<html>\n<a href>X Axis</a>\n</html>");
        xAxisLabel.setToolTipText("Click to swap x and y axis");
        xAxisLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xAxisLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                xAxisLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xAxisLabelMouseExited(evt);
            }
        });

        yAxisComboBox.setMaximumRowCount(30);
        yAxisComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yAxisComboBoxActionPerformed(evt);
            }
        });

        yAxisLabel.setText("<html>\n<a href>Y Axis</a>\n</html>");
        yAxisLabel.setToolTipText("Click to swap x and y axis");
        yAxisLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yAxisLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                yAxisLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                yAxisLabelMouseExited(evt);
            }
        });

        colorLabel.setText("<html> <a href>Color</a> </html>");
        colorLabel.setToolTipText("Click to enable gradient colors");
        colorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                colorLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                colorLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                colorLabelMouseExited(evt);
            }
        });

        colorsComboBox.setMaximumRowCount(30);
        colorsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorsComboBoxActionPerformed(evt);
            }
        });

        bubbleSizeLabel.setText("<html> <a href>Size</a> </html>");
        bubbleSizeLabel.setToolTipText("Click to change the bubble scaling factor");
        bubbleSizeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bubbleSizeLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bubbleSizeLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bubbleSizeLabelMouseExited(evt);
            }
        });

        bubbleSizeComboBox.setMaximumRowCount(30);
        bubbleSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bubbleSizeComboBoxActionPerformed(evt);
            }
        });

        binSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        binSizeSpinner.setToolTipText("The size of the bubbles relative to the x-axis");
        binSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                binSizeSpinnerStateChanged(evt);
            }
        });

        binsLabel.setText("Bins");

        javax.swing.GroupLayout xAxisPanelLayout = new javax.swing.GroupLayout(xAxisPanel);
        xAxisPanel.setLayout(xAxisPanelLayout);
        xAxisPanelLayout.setHorizontalGroup(
            xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xAxisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yAxisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bubbleSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(binsLabel))
                .addGap(18, 18, 18)
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yAxisComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bubbleSizeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorsComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xAxisComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(binSizeSpinner))
                .addContainerGap())
        );
        xAxisPanelLayout.setVerticalGroup(
            xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xAxisComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xAxisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yAxisComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yAxisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bubbleSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bubbleSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(binSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(binsLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dragSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select vs. Zoom"));
        dragSettingsPanel.setOpaque(false);

        dragButtonGroup.add(dragToSelectRadioButton);
        dragToSelectRadioButton.setSelected(true);
        dragToSelectRadioButton.setText("Drag to Select");
        dragToSelectRadioButton.setIconTextGap(15);
        dragToSelectRadioButton.setOpaque(false);

        dragButtonGroup.add(dragToZoomRadioButton);
        dragToZoomRadioButton.setText("Drag to Zoom");
        dragToZoomRadioButton.setIconTextGap(15);
        dragToZoomRadioButton.setOpaque(false);

        javax.swing.GroupLayout dragSettingsPanelLayout = new javax.swing.GroupLayout(dragSettingsPanel);
        dragSettingsPanel.setLayout(dragSettingsPanelLayout);
        dragSettingsPanelLayout.setHorizontalGroup(
            dragSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dragSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dragToSelectRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dragToZoomRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dragSettingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {dragToSelectRadioButton, dragToZoomRadioButton});

        dragSettingsPanelLayout.setVerticalGroup(
            dragSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dragSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dragSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dragToSelectRadioButton)
                    .addComponent(dragToZoomRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        logAcisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logarithmic Values"));
        logAcisPanel.setOpaque(false);

        xAxisLogCheckBox.setText("X Axis");
        xAxisLogCheckBox.setToolTipText("Use logarithmic axis");
        xAxisLogCheckBox.setIconTextGap(15);
        xAxisLogCheckBox.setOpaque(false);
        xAxisLogCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xAxisLogCheckBoxActionPerformed(evt);
            }
        });

        yAxisLogCheckBox.setText("Y Axis");
        yAxisLogCheckBox.setToolTipText("Use logarithmic axis");
        yAxisLogCheckBox.setIconTextGap(15);
        yAxisLogCheckBox.setOpaque(false);
        yAxisLogCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yAxisLogCheckBoxActionPerformed(evt);
            }
        });

        sizeLogCheckBox.setText("Size");
        sizeLogCheckBox.setToolTipText("Use logarithmic scaling for the size");
        sizeLogCheckBox.setIconTextGap(15);
        sizeLogCheckBox.setOpaque(false);
        sizeLogCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sizeLogCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout logAcisPanelLayout = new javax.swing.GroupLayout(logAcisPanel);
        logAcisPanel.setLayout(logAcisPanelLayout);
        logAcisPanelLayout.setHorizontalGroup(
            logAcisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logAcisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xAxisLogCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(9, 9, 9)
                .addComponent(yAxisLogCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(sizeLogCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );
        logAcisPanelLayout.setVerticalGroup(
            logAcisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logAcisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logAcisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xAxisLogCheckBox)
                    .addComponent(yAxisLogCheckBox)
                    .addComponent(sizeLogCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        selectedValuesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Values"));
        selectedValuesPanel.setOpaque(false);

        selectedValuesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                selectedValuesTableMouseReleased(evt);
            }
        });
        selectedValuesTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                selectedValuesTableKeyReleased(evt);
            }
        });
        selectedValuesScrollPane.setViewportView(selectedValuesTable);

        javax.swing.GroupLayout selectedValuesPanelLayout = new javax.swing.GroupLayout(selectedValuesPanel);
        selectedValuesPanel.setLayout(selectedValuesPanelLayout);
        selectedValuesPanelLayout.setHorizontalGroup(
            selectedValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedValuesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedValuesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
                .addContainerGap())
        );
        selectedValuesPanelLayout.setVerticalGroup(
            selectedValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedValuesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedValuesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addContainerGap())
        );

        selectedValuesPanel.setBounds(0, 0, 1070, 190);
        selectedValuesLayeredPane.add(selectedValuesPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        selectedValuesTableOptionsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/contextual_menu_gray.png"))); // NOI18N
        selectedValuesTableOptionsJButton.setToolTipText("Table Options");
        selectedValuesTableOptionsJButton.setBorder(null);
        selectedValuesTableOptionsJButton.setBorderPainted(false);
        selectedValuesTableOptionsJButton.setContentAreaFilled(false);
        selectedValuesTableOptionsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/contextual_menu_black.png"))); // NOI18N
        selectedValuesTableOptionsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                selectedValuesTableOptionsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                selectedValuesTableOptionsJButtonMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                selectedValuesTableOptionsJButtonMouseReleased(evt);
            }
        });
        selectedValuesTableOptionsJButton.setBounds(1025, 5, 10, 19);
        selectedValuesLayeredPane.add(selectedValuesTableOptionsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        selectedValuesTableHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        selectedValuesTableHelpJButton.setToolTipText("Help");
        selectedValuesTableHelpJButton.setBorder(null);
        selectedValuesTableHelpJButton.setBorderPainted(false);
        selectedValuesTableHelpJButton.setContentAreaFilled(false);
        selectedValuesTableHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        selectedValuesTableHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                selectedValuesTableHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                selectedValuesTableHelpJButtonMouseExited(evt);
            }
        });
        selectedValuesTableHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedValuesTableHelpJButtonActionPerformed(evt);
            }
        });
        selectedValuesTableHelpJButton.setBounds(1035, 0, 10, 19);
        selectedValuesLayeredPane.add(selectedValuesTableHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuSelectedValuesTableBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuSelectedValuesTableBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuSelectedValuesTableBackgroundPanel);
        contextMenuSelectedValuesTableBackgroundPanel.setLayout(contextMenuSelectedValuesTableBackgroundPanelLayout);
        contextMenuSelectedValuesTableBackgroundPanelLayout.setHorizontalGroup(
            contextMenuSelectedValuesTableBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuSelectedValuesTableBackgroundPanelLayout.setVerticalGroup(
            contextMenuSelectedValuesTableBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        contextMenuSelectedValuesTableBackgroundPanel.setBounds(1010, 0, 30, 19);
        selectedValuesLayeredPane.add(contextMenuSelectedValuesTableBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        xyPlotPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("XY Plot"));
        xyPlotPanel.setOpaque(false);

        plotPanel.setBackground(new java.awt.Color(255, 255, 255));
        plotPanel.setLayout(new javax.swing.BoxLayout(plotPanel, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout xyPlotPanelLayout = new javax.swing.GroupLayout(xyPlotPanel);
        xyPlotPanel.setLayout(xyPlotPanelLayout);
        xyPlotPanelLayout.setHorizontalGroup(
            xyPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xyPlotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
                .addContainerGap())
        );
        xyPlotPanelLayout.setVerticalGroup(
            xyPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xyPlotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addContainerGap())
        );

        xyPlotPanel.setBounds(0, 0, 810, 440);
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
        plotOptionsJButton.setBounds(770, 5, 10, 19);
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
        plotHelpJButton.setBounds(780, 0, 10, 19);
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

        contextMenuPlotBackgroundPanel.setBounds(770, 0, 30, 19);
        plotLayeredPane.add(contextMenuPlotBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filters"));
        filterPanel.setOpaque(false);

        editFiltersButton.setText("Edit Data Filters");
        editFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFiltersButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editFiltersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editFiltersButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedValuesLayeredPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addComponent(plotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(xAxisPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dragSettingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(logAcisPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(plotTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addComponent(plotTypePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xAxisPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logAcisPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dragSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(plotLayeredPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedValuesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
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
     * Update the plot.
     *
     * @param evt
     */
    private void xAxisComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisComboBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_xAxisComboBoxActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void yAxisComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yAxisComboBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_yAxisComboBoxActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void colorsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorsComboBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_colorsComboBoxActionPerformed

    /**
     * Change the bubble plot size.
     *
     * @param evt
     */
    private void binSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_binSizeSpinnerStateChanged
        numberOfBins = (Integer) binSizeSpinner.getValue();
        userDefinedBinSize = true;
        updatePlot();
    }//GEN-LAST:event_binSizeSpinnerStateChanged

    /**
     * Update the highlights in the plot.
     *
     * @param evt
     */
    private void selectedValuesTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_selectedValuesTableKeyReleased
        chartPanel.getChart().fireChartChanged();
    }//GEN-LAST:event_selectedValuesTableKeyReleased

    /**
     * Update the highlights in the plot.
     *
     * @param evt
     */
    private void selectedValuesTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedValuesTableMouseReleased
        chartPanel.getChart().fireChartChanged();
    }//GEN-LAST:event_selectedValuesTableMouseReleased

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void xAxisLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisLogCheckBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_xAxisLogCheckBoxActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void yAxisLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yAxisLogCheckBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_yAxisLogCheckBoxActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void densityPlotRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_densityPlotRadioButtonActionPerformed
        histogramRadioButtonActionPerformed(null);
    }//GEN-LAST:event_densityPlotRadioButtonActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void histogramRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histogramRadioButtonActionPerformed

        yAxisLabel.setEnabled(xyPlotRadioButton.isSelected());
        yAxisComboBox.setEnabled(xyPlotRadioButton.isSelected());
        colorLabel.setEnabled(xyPlotRadioButton.isSelected());
        colorsComboBox.setEnabled(xyPlotRadioButton.isSelected());
        bubbleSizeLabel.setEnabled(xyPlotRadioButton.isSelected());
        bubbleSizeComboBox.setEnabled(xyPlotRadioButton.isSelected());
        dragToSelectRadioButton.setEnabled(xyPlotRadioButton.isSelected());
        dragToZoomRadioButton.setEnabled(xyPlotRadioButton.isSelected());
        regressionLineCheckBoxMenuItem.setEnabled(xyPlotRadioButton.isSelected());
        binsLabel.setEnabled(histogramRadioButton.isSelected());
        binSizeSpinner.setEnabled(histogramRadioButton.isSelected());

        updatePlot();
    }//GEN-LAST:event_histogramRadioButtonActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void xAxisLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xAxisLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_xAxisLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void xAxisLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xAxisLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_xAxisLabelMouseExited

    /**
     * Switch the x and y axis.
     *
     * @param evt
     */
    private void xAxisLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xAxisLabelMouseClicked
        String xAxis = (String) xAxisComboBox.getSelectedItem();
        String yAxis = (String) yAxisComboBox.getSelectedItem();

        xAxisComboBox.setSelectedItem(yAxis);
        yAxisComboBox.setSelectedItem(xAxis);
    }//GEN-LAST:event_xAxisLabelMouseClicked

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void yAxisLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yAxisLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_yAxisLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void yAxisLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yAxisLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_yAxisLabelMouseExited

    /**
     * Switch the x and y axis.
     *
     * @param evt
     */
    private void yAxisLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yAxisLabelMouseClicked
        xAxisLabelMouseClicked(null);
    }//GEN-LAST:event_yAxisLabelMouseClicked

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void xyPlotRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyPlotRadioButtonActionPerformed
        histogramRadioButtonActionPerformed(null);
    }//GEN-LAST:event_xyPlotRadioButtonActionPerformed

    /**
     * Update the bubble size.
     *
     * @param evt
     */
    private void bubbleSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bubbleSizeComboBoxActionPerformed
        if (bubbleSizeComboBox.getSelectedIndex() == 0) {
            String value = JOptionPane.showInputDialog(this, "Select the bubble size.", bubbleSize);
            if (value != null) {
                try {
                    bubbleSize = new Double(value);
                    updatePlot();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Bubble size has to be a number.", "Bubble Size Error", JOptionPane.ERROR_MESSAGE);
                    bubbleSizeComboBoxActionPerformed(null);
                }
            }
        } else {
            updatePlot();
        }
    }//GEN-LAST:event_bubbleSizeComboBoxActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void bubbleSizeLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bubbleSizeLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bubbleSizeLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void bubbleSizeLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bubbleSizeLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_bubbleSizeLabelMouseExited

    /**
     * Open a dialog where the user can select the bubble scaling factor.
     *
     * @param evt
     */
    private void bubbleSizeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bubbleSizeLabelMouseClicked
        String value = JOptionPane.showInputDialog(this, "Select the bubble scaling factor.\nA value of 1 means no scaling.", bubbleScalingFactor);
        if (value != null) {
            try {
                double tempBubbleScalingFactor = new Double(value);
                if (tempBubbleScalingFactor > 0) {
                    bubbleScalingFactor = tempBubbleScalingFactor;
                    updatePlot();
                } else {
                    JOptionPane.showMessageDialog(this, "The bubble scaling factor has to be bigger than 0.", "Bubble Scaling Error", JOptionPane.ERROR_MESSAGE);
                    bubbleSizeComboBoxActionPerformed(null);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Bubble scaling factor has to be a number.", "Bubble Scaling Error", JOptionPane.ERROR_MESSAGE);
                bubbleSizeComboBoxActionPerformed(null);
            }
        }
    }//GEN-LAST:event_bubbleSizeLabelMouseClicked

    /**
     * Resize the layered panes.
     *
     * @param evt
     */
    private void backgroundPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_backgroundPanelComponentResized

        // resize the layered panels
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // move the icons
                selectedValuesLayeredPane.getComponent(0).setBounds(
                        selectedValuesLayeredPane.getWidth() - selectedValuesLayeredPane.getComponent(0).getWidth() - 22,
                        -2,
                        selectedValuesLayeredPane.getComponent(0).getWidth(),
                        selectedValuesLayeredPane.getComponent(0).getHeight());

                selectedValuesLayeredPane.getComponent(1).setBounds(
                        selectedValuesLayeredPane.getWidth() - selectedValuesLayeredPane.getComponent(1).getWidth() - 10,
                        -4,
                        selectedValuesLayeredPane.getComponent(1).getWidth(),
                        selectedValuesLayeredPane.getComponent(1).getHeight());

                selectedValuesLayeredPane.getComponent(2).setBounds(
                        selectedValuesLayeredPane.getWidth() - selectedValuesLayeredPane.getComponent(2).getWidth() - 5,
                        -3,
                        selectedValuesLayeredPane.getComponent(2).getWidth(),
                        selectedValuesLayeredPane.getComponent(2).getHeight());

                // resize the table area
                selectedValuesLayeredPane.getComponent(3).setBounds(0, 0, selectedValuesLayeredPane.getWidth(), selectedValuesLayeredPane.getHeight());
                selectedValuesLayeredPane.revalidate();
                selectedValuesLayeredPane.repaint();

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
                plotLayeredPane.revalidate();
                plotLayeredPane.repaint();
            }
        });
    }//GEN-LAST:event_backgroundPanelComponentResized

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void selectedValuesTableOptionsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedValuesTableOptionsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_selectedValuesTableOptionsJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void selectedValuesTableOptionsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedValuesTableOptionsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_selectedValuesTableOptionsJButtonMouseExited

    /**
     * Show the options for the selected values table.
     *
     * @param evt
     */
    private void selectedValuesTableOptionsJButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedValuesTableOptionsJButtonMouseReleased
        selectedValuesTablePopupMenu.show(selectedValuesTableOptionsJButton, evt.getX(), evt.getY());
    }//GEN-LAST:event_selectedValuesTableOptionsJButtonMouseReleased

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
        new ExportGraphicsDialog(this, getNormalIcon(), getWaitingIcon(), true, chartPanel, lastSelectedFolder);
    }//GEN-LAST:event_exportPlotMenuItemActionPerformed

    /**
     * Export the table to file.
     *
     * @param evt
     */
    private void exportSelectedValuesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSelectedValuesMenuItemActionPerformed
        final File selectedFile = Util.getUserSelectedFile(this, ".txt", "(tab separated text file)", "Export Selected Values", lastSelectedFolder.getLastSelectedFolder(), false);
        final XYPlottingDialog finalRef = this;

        if (selectedFile != null) {

            progressDialog = new ProgressDialogX(this, dialogParent,
                    normalIcon,
                    waitingIcon,
                    true);
            progressDialog.setPrimaryProgressCounterIndeterminate(true);
            progressDialog.setTitle("Exporting Table. Please Wait...");

            new Thread(new Runnable() {
                public void run() {
                    try {
                        progressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            new Thread("TableExportThread") {
                @Override
                public void run() {
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                        Util.tableToFile(selectedValuesTable, "\t", progressDialog, true, writer);
                        writer.close();

                        boolean processCancelled = progressDialog.isRunCanceled();
                        progressDialog.setRunFinished();

                        if (!processCancelled) {
                            JOptionPane.showMessageDialog(finalRef, "Data copied to file:\n" + selectedFile.getAbsolutePath(), "Data Exported.", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (IOException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(null, "An error occurred when exporting the table content.", "Export Failed", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }//GEN-LAST:event_exportSelectedValuesMenuItemActionPerformed

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
        new HelpDialog(this, getClass().getResource("/helpFiles/StatisticsDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Statistics - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void selectedValuesTableHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedValuesTableHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_selectedValuesTableHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void selectedValuesTableHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedValuesTableHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_selectedValuesTableHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void selectedValuesTableHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedValuesTableHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/StatisticsDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                "Statistics - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_selectedValuesTableHelpJButtonActionPerformed

    /**
     * Show the VisibleTableColumnsDialog.
     *
     * @param evt
     */
    private void hideColumnsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideColumnsMenuItemActionPerformed
        new VisibleTableColumnsDialog(this, this, isPlotting);
    }//GEN-LAST:event_hideColumnsMenuItemActionPerformed

    /**
     * Enable/disable the gradient color coding.
     *
     * @param evt
     */
    private void colorLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorLabelMouseClicked
        useGradientColorCoding = !useGradientColorCoding;
        updatePlot();

        if (useGradientColorCoding) {
            colorLabel.setToolTipText("Click to disable gradient colors");
        } else {
            colorLabel.setToolTipText("Click to enable gradient colors");
        }
    }//GEN-LAST:event_colorLabelMouseClicked

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void colorLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_colorLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void colorLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_colorLabelMouseExited

    /**
     * Show/hide the regression line.
     *
     * @param evt
     */
    private void regressionLineCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regressionLineCheckBoxMenuItemActionPerformed
        showRegressionLine = regressionLineCheckBoxMenuItem.isSelected();
        updatePlot(); // @TODO: maybe not needed to redo the whole plot to show/hide the regression line?
    }//GEN-LAST:event_regressionLineCheckBoxMenuItemActionPerformed

    /**
     * Update the plot.
     *
     * @param evt
     */
    private void sizeLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeLogCheckBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_sizeLogCheckBoxActionPerformed

    /**
     * Edit the filters.
     *
     * @param evt
     */
    private void editFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editFiltersButtonActionPerformed
        new XYPlotFiltersDialog(this, true);
    }//GEN-LAST:event_editFiltersButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JSpinner binSizeSpinner;
    private javax.swing.JLabel binsLabel;
    private javax.swing.JComboBox bubbleSizeComboBox;
    private javax.swing.JLabel bubbleSizeLabel;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JComboBox colorsComboBox;
    private javax.swing.JPanel contextMenuPlotBackgroundPanel;
    private javax.swing.JPanel contextMenuSelectedValuesTableBackgroundPanel;
    private javax.swing.JRadioButton densityPlotRadioButton;
    private javax.swing.ButtonGroup dragButtonGroup;
    private javax.swing.JPanel dragSettingsPanel;
    private javax.swing.JRadioButton dragToSelectRadioButton;
    private javax.swing.JRadioButton dragToZoomRadioButton;
    private javax.swing.JButton editFiltersButton;
    private javax.swing.JMenuItem exportPlotMenuItem;
    private javax.swing.JMenuItem exportSelectedValuesMenuItem;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JMenuItem hideColumnsMenuItem;
    private javax.swing.JRadioButton histogramRadioButton;
    private javax.swing.JPanel logAcisPanel;
    private javax.swing.JButton plotHelpJButton;
    private javax.swing.JLayeredPane plotLayeredPane;
    private javax.swing.JButton plotOptionsJButton;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JPopupMenu plotPopupMenu;
    private javax.swing.ButtonGroup plotTypeButtonGroup;
    private javax.swing.JPanel plotTypePanel;
    private javax.swing.JCheckBoxMenuItem regressionLineCheckBoxMenuItem;
    private javax.swing.JLayeredPane selectedValuesLayeredPane;
    private javax.swing.JPanel selectedValuesPanel;
    private javax.swing.JScrollPane selectedValuesScrollPane;
    private javax.swing.JTable selectedValuesTable;
    private javax.swing.JButton selectedValuesTableHelpJButton;
    private javax.swing.JButton selectedValuesTableOptionsJButton;
    private javax.swing.JPopupMenu selectedValuesTablePopupMenu;
    private javax.swing.JCheckBox sizeLogCheckBox;
    private javax.swing.JComboBox xAxisComboBox;
    private javax.swing.JLabel xAxisLabel;
    private javax.swing.JCheckBox xAxisLogCheckBox;
    private javax.swing.JPanel xAxisPanel;
    private javax.swing.JPanel xyPlotPanel;
    private javax.swing.JRadioButton xyPlotRadioButton;
    private javax.swing.JComboBox yAxisComboBox;
    private javax.swing.JLabel yAxisLabel;
    private javax.swing.JCheckBox yAxisLogCheckBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Update the plot.
     */
    public void updatePlot() {

        if (!isPlotting) {
            selectedDataPoints = new HashMap<Integer, ArrayList<Integer>>();
            dataPointToRowNumber = new HashMap<String, Integer>();
            selectedModelRows = new ArrayList<Integer>();

            isPlotting = true;

            progressDialog = new ProgressDialogX(this, dialogParent,
                    normalIcon,
                    waitingIcon,
                    true);
            progressDialog.setPrimaryProgressCounterIndeterminate(true);
            progressDialog.setTitle("Loading Data. Please Wait...");

            new Thread(new Runnable() {
                public void run() {
                    try {
                        progressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            new Thread("XYPlottingThread") {
                @Override
                public void run() {

                    plotPanel.removeAll();

                    // setup the dataset
                    String xAxisName = (String) xAxisComboBox.getSelectedItem();
                    String yAxisName = (String) yAxisComboBox.getSelectedItem();
                    double maxBubbleSize = 0.0;
                    double xAxisRange = 1.0;

                    // apply the data filters
                    filterData();

                    boolean selfUpdating = true;

                    if (tableModel instanceof SelfUpdatingTableModel) {
                        SelfUpdatingTableModel selfUpdatingTableModel = (SelfUpdatingTableModel) tableModel;
                        selfUpdating = selfUpdatingTableModel.isSelfUpdating();
                        selfUpdatingTableModel.setSelfUpdating(false);
                    }

                    if (histogramRadioButton.isSelected() || densityPlotRadioButton.isSelected()) {

                        ((TitledBorder) xyPlotPanel.getBorder()).setTitle(xAxisName);
                        xyPlotPanel.revalidate();
                        xyPlotPanel.repaint();

                        progressDialog.setPrimaryProgressCounterIndeterminate(false);
                        progressDialog.setMaxPrimaryProgressCounter(tableModel.getRowCount());
                        progressDialog.setValue(0);

                        int xAxisIndex = xAxisComboBox.getSelectedIndex();
                        double[] values = new double[tableModel.getRowCount()];

                        for (int index = 0; index < tableModel.getRowCount(); index++) {
                            progressDialog.increasePrimaryProgressCounter();

                            if (rowsAfterDataFiltering.contains(index)) {

                                // @TODO: support more data types!!
                                if (tableModel.getValueAt(index, xAxisIndex) instanceof XYDataPoint) {
                                    values[index] = ((XYDataPoint) tableModel.getValueAt(index, xAxisIndex)).getX();
                                } else if (tableModel.getValueAt(index, xAxisIndex) instanceof Integer) {
                                    values[index] = ((Integer) tableModel.getValueAt(index, xAxisIndex)).doubleValue();
                                } else if (tableModel.getValueAt(index, xAxisIndex) instanceof Double) {
                                    values[index] = ((Double) tableModel.getValueAt(index, xAxisIndex)).doubleValue();
                                } else if (tableModel.getValueAt(index, xAxisIndex) instanceof StartIndexes) {
                                    if (((StartIndexes) tableModel.getValueAt(index, xAxisIndex)).getIndexes().size() > 0) {
                                        values[index] = ((StartIndexes) tableModel.getValueAt(index, xAxisIndex)).getIndexes().get(0);
                                    }
                                }

                                // @TODO: what about null values?
                            }
                        }

                        XYPlot plot;
                        JFreeChart chart;

                        if (densityPlotRadioButton.isSelected()) {

                            NormalKernelDensityEstimator kernelEstimator = new NormalKernelDensityEstimator();
                            ArrayList list = kernelEstimator.estimateDensityFunction(values);

                            XYSeriesCollection lineChartDataset = new XYSeriesCollection();
                            XYSeries tempSeries = new XYSeries("1");

                            double[] xValues = (double[]) list.get(0);
                            double[] yValues = (double[]) list.get(1);

                            for (int i = 0; i < xValues.length; i++) {
                                tempSeries.add(xValues[i], yValues[i]);
                            }

                            lineChartDataset.addSeries(tempSeries);

                            AreaRenderer renderer = new AreaRenderer();
                            renderer.setOutline(true);
                            renderer.setSeriesFillPaint(0, histogramColor);
                            renderer.setSeriesOutlinePaint(0, histogramColor.darker());

                            chart = ChartFactory.createXYLineChart(null, xAxisName, "Density", lineChartDataset, PlotOrientation.VERTICAL, false, true, false);
                            plot = chart.getXYPlot();
                            plot.setRenderer(renderer);

                        } else { // histogram

                            HistogramDataset dataset = new HistogramDataset();
                            dataset.setType(HistogramType.FREQUENCY);

                            if (!userDefinedBinSize) {
                                numberOfBins = getNumberOfBins(values);
                                binSizeSpinner.setValue(numberOfBins);
                            }

                            userDefinedBinSize = false;

                            dataset.addSeries(xAxisName, values, numberOfBins);

                            chart = ChartFactory.createHistogram(null, xAxisName, "Frequency", dataset, PlotOrientation.VERTICAL, false, true, false);
                            plot = chart.getXYPlot();

                            // set up the chart renderer
                            XYBarRenderer renderer = new XYBarRenderer();
                            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
                            renderer.setShadowVisible(false);
                            renderer.setSeriesPaint(0, histogramColor);
                            plot.setRenderer(renderer);
                        }

                        // linear or logarithmic axis
                        if (xAxisLogCheckBox.isSelected()) {
                            plot.setDomainAxis(new LogAxis(plot.getDomainAxis().getLabel()));
                        }
                        if (yAxisLogCheckBox.isSelected()) {
                            plot.setRangeAxis(new LogAxis(plot.getRangeAxis().getLabel()));
                        }

                        chartPanel = new ChartPanel(chart);
                        chartPanel.setBorder(null);
                        chart.setBorderVisible(false);

                        // hide unwanted chart details
                        plot.setOutlineVisible(false);

                        plot.setBackgroundPaint(Color.WHITE);

                        chartPanel.setBackground(Color.WHITE);
                        chart.setBackgroundPaint(Color.WHITE);

                        plotPanel.add(chartPanel);
                        plotPanel.revalidate();
                        plotPanel.repaint();

                    } else { // xy plot

                        ((TitledBorder) xyPlotPanel.getBorder()).setTitle(xAxisName + " vs. " + yAxisName);
                        xyPlotPanel.revalidate();
                        xyPlotPanel.repaint();

                        DefaultXYZDataset xyzDataset = new DefaultXYZDataset();

                        ArrayList<String> datasetNames = new ArrayList<String>();
                        HashMap<String, ArrayList<Integer>> datasets = new HashMap<String, ArrayList<Integer>>();

                        int colorIndex = colorsComboBox.getSelectedIndex();
                        double minColorValue = Double.MAX_VALUE;
                        double maxColorValue = Double.MIN_VALUE;

                        progressDialog.setPrimaryProgressCounterIndeterminate(false);
                        progressDialog.setMaxPrimaryProgressCounter(tableModel.getRowCount() * 2);
                        progressDialog.setValue(0);

                        for (int i = 0; i < tableModel.getRowCount(); i++) {

                            progressDialog.increasePrimaryProgressCounter();

                            if (rowsAfterDataFiltering.contains(i)) {

                                ArrayList<Integer> tempArray;
                                if (!datasets.containsKey(tableModel.getValueAt(i, colorIndex).toString())) {
                                    tempArray = new ArrayList<Integer>();
                                    datasetNames.add(tableModel.getValueAt(i, colorIndex).toString());
                                } else {
                                    tempArray = datasets.get(tableModel.getValueAt(i, colorIndex).toString());
                                }
                                tempArray.add(i);
                                datasets.put(tableModel.getValueAt(i, colorIndex).toString(), tempArray);

                                // get the min and max values for the color column
                                if (tableModel.getValueAt(i, colorIndex) instanceof XYDataPoint) {
                                    double tempColorValue = ((XYDataPoint) tableModel.getValueAt(i, colorIndex)).getX();
                                    if (tempColorValue > maxColorValue) {
                                        maxColorValue = tempColorValue;
                                    }
                                    if (tempColorValue < minColorValue) {
                                        minColorValue = tempColorValue;
                                    }
                                } else if (tableModel.getValueAt(i, colorIndex) instanceof Integer) {
                                    int tempColorValue = (Integer) tableModel.getValueAt(i, colorIndex);
                                    if (tempColorValue > maxColorValue) {
                                        maxColorValue = tempColorValue;
                                    }
                                    if (tempColorValue < minColorValue) {
                                        minColorValue = tempColorValue;
                                    }
                                } else if (tableModel.getValueAt(i, colorIndex) instanceof Double) {
                                    double tempColorValue = (Double) tableModel.getValueAt(i, colorIndex);
                                    if (tempColorValue > maxColorValue) {
                                        maxColorValue = tempColorValue;
                                    }
                                    if (tempColorValue < minColorValue) {
                                        minColorValue = tempColorValue;
                                    }
                                } else if (tableModel.getValueAt(i, colorIndex) instanceof StartIndexes) {
                                    if (((StartIndexes) tableModel.getValueAt(i, colorIndex)).getIndexes().size() > 0) {
                                        double tempColorValue = ((StartIndexes) tableModel.getValueAt(i, colorIndex)).getIndexes().get(0);
                                        if (tempColorValue > maxColorValue) {
                                            maxColorValue = tempColorValue;
                                        }
                                        if (tempColorValue < minColorValue) {
                                            minColorValue = tempColorValue;
                                        }
                                    }
                                }
                            }
                        }

                        int xAxisIndex = xAxisComboBox.getSelectedIndex();
                        int yAxisIndex = yAxisComboBox.getSelectedIndex();
                        int bubbleSizeIndex = bubbleSizeComboBox.getSelectedIndex();
                        HashMap<Integer, Color> datasetColors = new HashMap<Integer, Color>();

                        progressDialog.setPrimaryProgressCounterIndeterminate(false);
                        progressDialog.setMaxPrimaryProgressCounter(tableModel.getRowCount());
                        progressDialog.setValue(0);

                        int datasetCounter = 0;
                        double minXValue = Double.MAX_VALUE;
                        double maxXValue = Double.MIN_VALUE;
                        double sx = 0, sy = 0, sxx = 0, sxy = 0, syy = 0;
                        SimpleRegression simpleRegression = new SimpleRegression();

                        // @TODO: add the option of filtering the data based on the values in one or more columns?
                        //        for example remove all non-validated proteins or show only coverage > 50%?
                        // split the data into the datasets
                        for (String dataset : datasetNames) {

                            double[][] tempDataXYZ = new double[3][datasets.get(dataset).size()];

                            int counter = 0;

                            for (Integer index : datasets.get(dataset)) {

                                progressDialog.increasePrimaryProgressCounter();

                                // @TODO: support more data types!!
                                if (tableModel.getValueAt(index, xAxisIndex) instanceof XYDataPoint) {
                                    tempDataXYZ[0][counter] = ((XYDataPoint) tableModel.getValueAt(index, xAxisIndex)).getX();
                                } else if (tableModel.getValueAt(index, xAxisIndex) instanceof Integer) {
                                    tempDataXYZ[0][counter] = (Integer) tableModel.getValueAt(index, xAxisIndex);
                                } else if (tableModel.getValueAt(index, xAxisIndex) instanceof Double) {
                                    tempDataXYZ[0][counter] = (Double) tableModel.getValueAt(index, xAxisIndex);
                                } else if (tableModel.getValueAt(index, xAxisIndex) instanceof StartIndexes) {
                                    if (((StartIndexes) tableModel.getValueAt(index, xAxisIndex)).getIndexes().size() > 0) {
                                        tempDataXYZ[0][counter] = ((StartIndexes) tableModel.getValueAt(index, xAxisIndex)).getIndexes().get(0);
                                    }
                                }

                                if (tableModel.getValueAt(index, yAxisIndex) instanceof XYDataPoint) {
                                    tempDataXYZ[1][counter] = ((XYDataPoint) tableModel.getValueAt(index, yAxisIndex)).getX();
                                } else if (tableModel.getValueAt(index, yAxisIndex) instanceof Integer) {
                                    tempDataXYZ[1][counter] = (Integer) tableModel.getValueAt(index, yAxisIndex);
                                } else if (tableModel.getValueAt(index, yAxisIndex) instanceof Double) {
                                    tempDataXYZ[1][counter] = (Double) tableModel.getValueAt(index, yAxisIndex);
                                } else if (tableModel.getValueAt(index, yAxisIndex) instanceof StartIndexes) {
                                    if (((StartIndexes) tableModel.getValueAt(index, yAxisIndex)).getIndexes().size() > 0) {
                                        tempDataXYZ[1][counter] = ((StartIndexes) tableModel.getValueAt(index, yAxisIndex)).getIndexes().get(0);
                                    }
                                }

                                // get the min and max x values
                                if (tempDataXYZ[0][counter] > maxXValue) {
                                    maxXValue = tempDataXYZ[0][counter];
                                }
                                if (tempDataXYZ[0][counter] < minXValue) {
                                    minXValue = tempDataXYZ[0][counter];
                                }

                                // get the regression line data
                                simpleRegression.addData(tempDataXYZ[0][counter], tempDataXYZ[1][counter]);

                                if (bubbleSizeIndex == 0) {
                                    tempDataXYZ[2][counter] = bubbleSize;
                                } else {
                                    if (tableModel.getValueAt(index, bubbleSizeIndex - 1) instanceof XYDataPoint) {
                                        tempDataXYZ[2][counter] = ((XYDataPoint) tableModel.getValueAt(index, bubbleSizeIndex - 1)).getX();
                                    } else if (tableModel.getValueAt(index, bubbleSizeIndex - 1) instanceof Integer) {
                                        tempDataXYZ[2][counter] = ((Integer) tableModel.getValueAt(index, bubbleSizeIndex - 1));
                                    } else if (tableModel.getValueAt(index, bubbleSizeIndex - 1) instanceof Double) {
                                        tempDataXYZ[2][counter] = ((Double) tableModel.getValueAt(index, bubbleSizeIndex - 1));
                                    } else if (tableModel.getValueAt(index, bubbleSizeIndex - 1) instanceof StartIndexes) {
                                        if (((StartIndexes) tableModel.getValueAt(index, bubbleSizeIndex - 1)).getIndexes().size() > 0) {
                                            tempDataXYZ[2][counter] = (((StartIndexes) tableModel.getValueAt(index, bubbleSizeIndex - 1)).getIndexes().get(0));
                                        } else {
                                            tempDataXYZ[2][counter] = 0;
                                        }
                                    }
                                }

                                // add log scaling if selected
                                if (sizeLogCheckBox.isSelected()) {
                                    tempDataXYZ[2][counter] = Math.log(tempDataXYZ[2][counter]) / Math.log(2);
                                }

                                // add the bubble scaling factor
                                tempDataXYZ[2][counter] = tempDataXYZ[2][counter] * bubbleScalingFactor;

                                // store the maximum bubble size
                                if (tempDataXYZ[2][counter] > maxBubbleSize) {
                                    maxBubbleSize = tempDataXYZ[2][counter];
                                }

                                dataPointToRowNumber.put(datasetCounter + "_" + counter++, index);
                            }

                            // set the datasetcolor
                            int tableRowIndex = datasets.get(dataset).get(0);

                            Object tempObject = tableModel.getValueAt(tableRowIndex, colorIndex);

                            // get the color to use if using gradient color coding
                            if (tempObject instanceof Integer) {
                                datasetColors.put(datasetCounter, GradientColorCoding.findGradientColor(((Integer) tempObject).doubleValue(), minColorValue, maxColorValue, colorGradient, false));
                            } else if (tempObject instanceof Double) {
                                datasetColors.put(datasetCounter, GradientColorCoding.findGradientColor((Double) tempObject, minColorValue, maxColorValue, colorGradient, false));
                            } else if (tempObject instanceof XYDataPoint) {
                                datasetColors.put(datasetCounter, GradientColorCoding.findGradientColor(((XYDataPoint) tempObject).getX(), minColorValue, maxColorValue, colorGradient, false));
                            } else if (tempObject instanceof StartIndexes) {
                                if (((StartIndexes) tempObject).getIndexes().size() > 0) {
                                    datasetColors.put(datasetCounter, GradientColorCoding.findGradientColor(
                                            ((StartIndexes) tempObject).getIndexes().get(0).doubleValue(), minColorValue, maxColorValue, colorGradient, false));
                                } else {
                                    datasetColors.put(datasetCounter, GradientColorCoding.findGradientColor(minColorValue, minColorValue, maxColorValue, colorGradient, false));
                                }
                            } else {
                                datasetColors.put(datasetCounter, GradientColorCoding.findGradientColor(minColorValue, minColorValue, maxColorValue, colorGradient, false));
                            }

                            xyzDataset.addSeries(dataset, tempDataXYZ);
                            datasetCounter++;
                        }

                        progressDialog.setPrimaryProgressCounterIndeterminate(true);

                        // create the plot
                        JFreeChart chart = ChartFactory.createBubbleChart(null, xAxisName, yAxisName, xyzDataset, PlotOrientation.VERTICAL, false, true, false);
                        XYPlot plot = chart.getXYPlot();

                        // add the regression line
                        if (showRegressionLine) {
                            XYSeriesCollection regressionData = new XYSeriesCollection();
                            XYSeries regr = new XYSeries("RegressionLine");
                            regr.add(minXValue, simpleRegression.predict(minXValue));
                            regr.add(maxXValue, simpleRegression.predict(maxXValue));
                            regressionData.addSeries(regr);

                            // show the r squared value
                            plot.addAnnotation(new XYTextAnnotation("R2=" + Util.roundDouble(simpleRegression.getRSquare(), 2),
                                    maxXValue * 0.93, simpleRegression.predict(maxXValue) * 0.99));

                            StandardXYItemRenderer regressionRenderer = new StandardXYItemRenderer();
                            regressionRenderer.setBaseSeriesVisibleInLegend(false);
                            regressionRenderer.setSeriesPaint(0, Color.GRAY);
                            plot.setDataset(1, regressionData);
                            plot.setRenderer(1, regressionRenderer);
                        }

                        // set up the renderer
                        XYBubbleRenderer renderer = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_DOMAIN_AXIS) {
                            @Override
                            public Stroke getItemOutlineStroke(int row, int column) {

                                boolean selectedInTable = false;
                                int[] selectedRows = selectedValuesTable.getSelectedRows();

                                for (int tableRowIndex : selectedRows) {
                                    if (dataPointToRowNumber.get(row + "_" + column).intValue()
                                            == selectedValuesTable.convertRowIndexToModel(tableRowIndex)) {
                                        selectedInTable = true;
                                    }
                                }

                                if (selectedInTable) {
                                    BasicStroke stroke = (BasicStroke) super.getItemOutlineStroke(row, column);
                                    return new BasicStroke(stroke.getLineWidth() * 10f);
                                } else {
                                    if (!selectedDataPoints.isEmpty()) {
                                        if (selectedDataPoints.containsKey(row) && selectedDataPoints.get(row).contains(column)) {
                                            BasicStroke stroke = (BasicStroke) super.getItemOutlineStroke(row, column);
                                            return new BasicStroke(stroke.getLineWidth() * 1.2f);
                                        } else {
                                            return super.getItemOutlineStroke(row, column);
                                        }
                                    } else {
                                        return super.getItemOutlineStroke(row, column);
                                    }
                                }
                            }

                            @Override
                            public Paint getItemOutlinePaint(int row, int column) {

                                boolean selectedInTable = false;
                                int[] selectedRows = selectedValuesTable.getSelectedRows();

                                for (int tableRowIndex : selectedRows) {
                                    if (dataPointToRowNumber.get(row + "_" + column).intValue()
                                            == selectedValuesTable.convertRowIndexToModel(tableRowIndex)) {
                                        selectedInTable = true;
                                    }
                                }

                                if (selectedInTable) {
                                    return Color.BLUE; // @TODO: should not be hard coded here!!
                                } else {
                                    if (!selectedDataPoints.isEmpty()) {
                                        if (selectedDataPoints.containsKey(row) && selectedDataPoints.get(row).contains(column)) {
                                            return Color.BLACK;
                                        } else {
                                            return Color.LIGHT_GRAY;
                                        }
                                    } else {
                                        return super.getItemOutlinePaint(row, column);
                                    }
                                }
                            }

                            @Override
                            public Paint getItemPaint(int row, int column) {
                                if (!selectedDataPoints.isEmpty()) {
                                    Color tempColor = (Color) super.getItemPaint(row, column);
                                    if (selectedDataPoints.containsKey(row) && selectedDataPoints.get(row).contains(column)) {
                                        return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), 255);
                                    } else {
                                        return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), 30); // @TODO: should not be hard coded here?
                                    }
                                } else {
                                    return super.getItemPaint(row, column);
                                }
                            }

                            @Override
                            public Paint getItemFillPaint(int row, int column) {
                                if (!selectedDataPoints.isEmpty()) {
                                    Color tempColor = (Color) super.getItemFillPaint(row, column);
                                    if (selectedDataPoints.containsKey(row) && selectedDataPoints.get(row).contains(column)) {
                                        return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), 255);
                                    } else {
                                        return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), 30); // @TODO: should not be hard coded here?
                                    }
                                } else {
                                    return super.getItemFillPaint(row, column);
                                }
                            }
                        };

                        // set the colors for the data series
                        boolean isIntegerColorRenderer = false;

                        if (cellRenderers.containsKey(Integer.valueOf(colorsComboBox.getSelectedIndex()))) {
                            if (cellRenderers.get(Integer.valueOf(colorsComboBox.getSelectedIndex())) instanceof JSparklinesIntegerColorTableCellRenderer) {
                                JSparklinesIntegerColorTableCellRenderer integerColorRenderer
                                        = (JSparklinesIntegerColorTableCellRenderer) cellRenderers.get(Integer.valueOf(colorsComboBox.getSelectedIndex()));
                                HashMap<Integer, Color> colors = integerColorRenderer.getColors();

                                for (int i = 0; i < datasetNames.size(); i++) {
                                    Integer datasetInteger = Integer.valueOf(datasetNames.get(i));
                                    renderer.setSeriesPaint(i, colors.get(Integer.valueOf(datasetInteger)));
                                }

                                isIntegerColorRenderer = true;
                            }
                        }

                        if (!isIntegerColorRenderer && useGradientColorCoding) {
                            for (int i = 0; i < datasetNames.size(); i++) {
                                renderer.setSeriesPaint(i, datasetColors.get(i));
                            }
                        }

                        renderer.setBaseToolTipGenerator(new StandardXYZToolTipGenerator());
                        plot.setRenderer(renderer);

                        // make all datapoints semitransparent
                        plot.setForegroundAlpha(0.5f);

                        // remove space before/after the domain axis
                        plot.getDomainAxis().setUpperMargin(0);
                        plot.getDomainAxis().setLowerMargin(0);

                        plot.setRangeGridlinePaint(Color.black);

                        // linear or logarithmic axis
                        if (xAxisLogCheckBox.isSelected()) {
                            plot.setDomainAxis(new LogAxis(plot.getDomainAxis().getLabel()));
                        }
                        if (yAxisLogCheckBox.isSelected()) {
                            plot.setRangeAxis(new LogAxis(plot.getRangeAxis().getLabel()));
                        }

                        // store the x axis range to see of the bubbles are too big
                        xAxisRange = plot.getDomainAxis().getUpperBound() - plot.getDomainAxis().getLowerBound();

                        // hide unwanted chart details
                        plot.setDomainGridlinesVisible(false);
                        chart.getPlot().setOutlineVisible(false);

                        // set background color
                        chart.getPlot().setBackgroundPaint(Color.WHITE);
                        chart.setBackgroundPaint(Color.WHITE);

                        chartPanel = new ChartPanel(chart) {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (selectionActive) {
                                    setMouseZoomable(false);
                                    super.mouseReleased(e);
                                    setMouseZoomable(true);
                                } else {
                                    super.mouseReleased(e);
                                }
                            }
                        };

                        chartPanel.setBackground(Color.WHITE);

                        // add the plot to the chart
                        plotPanel.add(chartPanel);
                        plotPanel.revalidate();
                        plotPanel.repaint();

                        // add chart mouse listener
                        chartPanel.addChartMouseListener(new ChartMouseListener() {
                            public void chartMouseClicked(ChartMouseEvent cme) {
                                mouseClickedInChart(cme);
                            }

                            public void chartMouseMoved(ChartMouseEvent cme) {
                                mouseMovedInChart(cme);
                            }
                        });

                        // add chart mouse motion listener
                        chartPanel.addMouseMotionListener(new MouseAdapter() {
                            @Override
                            public void mouseDragged(MouseEvent e) {
                                if (!dragToZoomRadioButton.isSelected()) {
                                    selectionActive = true;
                                    mouseDragged = true;
                                } else {
                                    selectionActive = false;
                                    super.mouseDragged(e);
                                }
                            }
                        });

                        // add more chart mouse listeners
                        chartPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                dragStart = e.getPoint();
                                mouseDragged = false;
                                super.mouseClicked(e);
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (mouseDragged) {
                                    dragEnd = e.getPoint();

                                    // clear the old selection
                                    selectedDataPoints = new HashMap<Integer, ArrayList<Integer>>();
                                    selectedModelRows = new ArrayList<Integer>();

                                    double dragStartX = (dragStart.getX() - chartPanel.getInsets().left) / chartPanel.getScaleX();
                                    double dragStartY = (dragStart.getY() - chartPanel.getInsets().top) / chartPanel.getScaleY();
                                    double dragEndX = (dragEnd.getX() - chartPanel.getInsets().left) / chartPanel.getScaleX();
                                    double dragEndY = (dragEnd.getY() - chartPanel.getInsets().top) / chartPanel.getScaleY();

                                    ArrayList<XYItemEntity> entitiesFound = new ArrayList<XYItemEntity>();
                                    EntityCollection entities = chartPanel.getChartRenderingInfo().getEntityCollection();
                                    Iterator<ChartEntity> iterator = entities.iterator();

                                    while (iterator.hasNext()) {
                                        ChartEntity entity = iterator.next();
                                        if (entity instanceof XYItemEntity) {
                                            if (entity.getArea().intersects(dragStartX, dragStartY, dragEndX - dragStartX, dragEndY - dragStartY)) {
                                                if (!entitiesFound.contains((XYItemEntity) entity)) {
                                                    entitiesFound.add((XYItemEntity) entity);
                                                }
                                            }
                                        }
                                    }

                                    for (XYItemEntity entity : entitiesFound) {
                                        selectEntity(entity, false);
                                    }
                                }

                                mouseDragged = false;
                                chartPanel.getChart().fireChartChanged();
                                filterTable();
                                super.mouseReleased(e);
                            }
                        });
                    }

                    if (tableModel instanceof SelfUpdatingTableModel) {
                        ((SelfUpdatingTableModel) tableModel).setSelfUpdating(selfUpdating);
                    }

                    isPlotting = false;
                    filterTable();
                    progressDialog.setRunFinished();

                    if (maxBubbleSize > xAxisRange && !sizeLogCheckBox.isSelected()) {
                        int value = JOptionPane.showConfirmDialog(dialogParent, "Seems like your bubbles are too large.\nTurn on log scale?", "Log Scale?", JOptionPane.YES_NO_OPTION);
                        if (value == JOptionPane.YES_OPTION) {
                            sizeLogCheckBox.setSelected(true);
                            updatePlot();
                        }
                    }
                }
            }.start();
        }
    }

    /**
     * Handles mouse clicks in the chart panel. Selects/de-selects data points.
     *
     * @param event
     */
    private void mouseClickedInChart(ChartMouseEvent event) {

        ArrayList<ChartEntity> entities = getEntitiesForPoint(event.getTrigger().getPoint().x, event.getTrigger().getPoint().y);

        if (entities.isEmpty()) {
            return;
        }

        boolean dataPointsSelected = false;

        // check if any data points are selected, and select/de-select them
        for (ChartEntity entity : entities) {
            // Get entity details
            if (entity instanceof XYItemEntity) {
                selectEntity((XYItemEntity) entity, true);
                dataPointsSelected = true;
            }
        }

        // if no data points were selected then clear the selection
        if (!dataPointsSelected) {
            selectedDataPoints = new HashMap<Integer, ArrayList<Integer>>();
            selectedModelRows = new ArrayList<Integer>();
        }

        filterTable();
        chartPanel.getChart().fireChartChanged();
    }

    /**
     * Selects/de-selects a given entity in the plot.
     *
     * @param entity the entity to select/de-select
     * @param removeSelected if true, already selected entities are de-selected
     */
    private void selectEntity(XYItemEntity entity, boolean removeSelected) {

        Integer seriesIndex = ((XYItemEntity) entity).getSeriesIndex();
        Integer itemIndex = ((XYItemEntity) entity).getItem();

        if (selectedDataPoints.containsKey(seriesIndex)) {
            if (selectedDataPoints.get(seriesIndex).contains(itemIndex)) {
                if (removeSelected) {
                    selectedDataPoints.get(seriesIndex).remove(itemIndex);
                    if (selectedDataPoints.get(seriesIndex).isEmpty()) {
                        selectedDataPoints.remove(seriesIndex);
                    }
                    selectedModelRows.remove(dataPointToRowNumber.get(seriesIndex + "_" + itemIndex));
                }
            } else {
                selectedDataPoints.get(seriesIndex).add(itemIndex);
                selectedModelRows.add(dataPointToRowNumber.get(seriesIndex + "_" + itemIndex));
            }
        } else {
            ArrayList<Integer> itemList = new ArrayList<Integer>();
            itemList.add(itemIndex);
            selectedDataPoints.put(seriesIndex, itemList);
            if (!selectedModelRows.contains(dataPointToRowNumber.get(seriesIndex + "_" + itemIndex))) {
                selectedModelRows.add(dataPointToRowNumber.get(seriesIndex + "_" + itemIndex));
            }
        }
    }

    /**
     * Handles mouse movements in the chart panel.
     *
     * @param event
     */
    private void mouseMovedInChart(ChartMouseEvent event) {
        // @TODO: should we do something here?
    }

    /**
     * Filters the selected values table according to the currently selected
     * values and the current data filters.
     */
    public void filterTable() {
        if (tableModel instanceof SelfUpdatingTableModel) {
            TableRowSorter sorter = new TableRowSorter(tableModel);
            sorter.setRowFilter(new SelectedValuesTableFilter());
            selectedValuesTable.setRowSorter(sorter);
        } else {
            ((TableRowSorter) selectedValuesTable.getRowSorter()).setRowFilter(new SelectedValuesTableFilter());
        }
        ((TitledBorder) selectedValuesPanel.getBorder()).setTitle("Selected Values (" + selectedValuesTable.getRowCount() + ")");
        selectedValuesPanel.repaint();
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

    public void setSelectedExportFolder(LastSelectedFolder selectedFolder) {
        lastSelectedFolder = selectedFolder;
    }

    public void setVisibleColumns(HashMap<Integer, Boolean> showColumns) {
        this.visibleColumns = showColumns;
    }

    public HashMap<Integer, Boolean> getVisibleColumns() {
        return visibleColumns;
    }

    public JTable getTable() {
        return selectedValuesTable;
    }

    public ArrayList<TableColumn> getAllTableColumns() {
        return allTableColumns;
    }

    public Image getNormalIcon() {
        return normalIcon;
    }

    public Image getWaitingIcon() {
        return waitingIcon;
    }

    /**
     * A filter that filters the table based on if the data point is selected in
     * the plot or not.
     */
    public class SelectedValuesTableFilter extends RowFilter<DefaultTableModel, Integer> {

        public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {

            // see if the row has already been filtered out
            if (!rowsAfterDataFiltering.contains(entry.getIdentifier())) {
                return false;
            }

            // see of the data point is currently selected
            if (selectedModelRows.isEmpty() || selectedModelRows.contains(entry.getIdentifier())) {
                return true;
            }

            return false;
        }
    }

    /**
     * This class makes use of "SSJ: Stochastic Simulation in Java" library from
     * iro.umontreal.ca to estimate probability density function of an array of
     * double. It first generates independent and identically distributed random
     * variables from the dataset, at which the density needs to be computed and
     * then generates the vector of density estimates at the corresponding
     * variables.
     *
     * The KernelDensityGen class from the same library is used: the class
     * implements random variate generators for distributions obtained via
     * kernel density estimation methods from a set of n individual observations
     * x1,..., xn. The basic idea is to center a copy of the same symmetric
     * density at each observation and take an equally weighted mixture of the n
     * copies as an estimator of the density from which the observations come.
     * The resulting kernel density has the general form: fn(x) =
     * (1/nh)?i=1nk((x - xi)/h). K is the kernel (here a Gaussian is chosen) and
     * h is the bandwidth (smoothing factor).
     *
     * @author Paola Masuzzo
     */
    public class NormalKernelDensityEstimator {

        // @TODO: move to a separate class?
        //N, estimation precision, is set to a default of 512, as in most KDE algorithms default values, i.e. R "density"function, OmicSoft, Matlab algorithm
        private final int n = 4096;
        private EmpiricalDist empiricalDist;
        private KernelDensityGen kernelDensityGen;
        private double datasetSize;

        /**
         * This method initiates the KDE, i.e. sort values in ascending order,
         * compute an empirical distribution out of it, makes use of a NormalGen
         * to generate random variates from the normal distribution, and then
         * use these variates to generate a kernel density generator of the
         * empirical distribution.
         *
         * @param data
         */
        private void init(double[] data) {
            datasetSize = (double) data.length;
            Arrays.sort(data);
            empiricalDist = new EmpiricalDist(data);
            // new Stream to randomly generate numbers
            // combined multiple recursive generator (CMRG)
            RandomStream stream = new MRG31k3p();
            NormalGen normalKernelDensityGen = new NormalGen(stream);
            kernelDensityGen = new KernelDensityGen(stream, empiricalDist, normalKernelDensityGen);
        }

        public ArrayList estimateDensityFunction(Double[] data) {
            // init the KDE with a normal generator
            init(excludeNullValues(data));
            return estimateDensityFunction();
        }

        public ArrayList estimateDensityFunction(double[] data) {
            // init the KDE with a normal generator
            init(data);
            return estimateDensityFunction();
        }

        private ArrayList estimateDensityFunction() {

            ArrayList densityFunction = new ArrayList();

            // array for random samples
            double[] randomSamples = new double[n];

            // compute x values
            for (int i = 0; i < n; i++) {
                double nextDouble = kernelDensityGen.nextDouble();
                randomSamples[i] = nextDouble;
            }

            Arrays.sort(randomSamples);
            densityFunction.add(randomSamples);

            // compute y values
            // use normal default kernel
            NormalDist kern = new NormalDist();

            // calculate optimal bandwidth with the (ROBUST) Silverman's "rule of thumb" (Scott Variation uses factor = 1.06)
            double bandWidth = 0.99 * Math.min(empiricalDist.getSampleStandardDeviation(), (empiricalDist.getInterQuartileRange() / 1.34)) / Math.pow(datasetSize, 0.2);

            // estimate density and store values in a vector
            double[] estimatedDensityValues = KernelDensity.computeDensity(empiricalDist, kern, bandWidth, randomSamples);
            densityFunction.add(estimatedDensityValues);

            return densityFunction;
        }

        /**
         * Exclude null values from an array of Double.
         *
         * @param data the data
         * @return another double array with no longer null values
         */
        public double[] excludeNullValues(Double[] data) {
            ArrayList<Double> list = new ArrayList<Double>();
            for (Double value : data) {
                if (value != null) {
                    list.add(value);
                }
            }
            double[] newArray = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                newArray[i] = list.get(i);
            }
            return newArray;
        }
    }

    /**
     * Get the histogram bin size.
     *
     * @param values the values to calculate the bin size for
     * @return the histogram bin size
     */
    private int getNumberOfBins(double[] values) {

        // @TODO: this seems to always return very low number of bins??
        // @TODO: when working this code should be moved out of this class!!
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;

        // Get a DescriptiveStatistics instance
        DescriptiveStatistics stats = new DescriptiveStatistics();
        // Add the data from the array
        for (int i = 0; i < values.length; i++) {
            stats.addValue(values[i]);

            if (values[i] > maxValue) {
                maxValue = values[i];
            }
            if (values[i] < minValue) {
                minValue = values[i];
            }
        }

        double q1 = stats.getPercentile(25);
        double q3 = stats.getPercentile(75);
        double range = Math.abs(maxValue - minValue);

        if (q3 - q1 == 0 || values.length == 0) {
            return 10;
        }

        int freedmanDiaconisValue = (int) Math.ceil((Math.pow(values.length, 1 / 3) * range) / (2 * (q3 - q1)));
        //int freedmanDiaconisValue = (int) Math.ceil((Math.pow(values.length, 1 / 3) * range)/(3.5*stats.getStandardDeviation())); // scott
        //int freedmanDiaconisValue = (int) Math.ceil(Math.log(2*values.length) + 1); // sturges

        if (freedmanDiaconisValue == 0 || freedmanDiaconisValue < 10) {
            return 10;
        }

        return freedmanDiaconisValue;
    }

    /**
     * Return the column names.
     *
     * @return the column names
     */
    public Vector<String> getColummnNames() {
        return colummnNames;
    }

    /**
     * Return the data filters.
     *
     * @return the data filters
     */
    public HashMap<String, String> getDataFilters() {
        return filters;
    }

    /**
     * Set the data filters.
     *
     * @param filters the filters to set
     */
    public void setDataFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }

    /**
     * Apply the data filters.
     */
    private void filterData() {

        rowsAfterDataFiltering = new ArrayList<Integer>();

        boolean filterError = false,
                selfUpdating = true;

        if (tableModel instanceof SelfUpdatingTableModel) {
            SelfUpdatingTableModel selfUpdatingTableModel = (SelfUpdatingTableModel) tableModel;
            selfUpdating = selfUpdatingTableModel.isSelfUpdating();
            selfUpdatingTableModel.setSelfUpdating(false);
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            boolean include = true;

            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                String filter = filters.get(tableModel.getColumnName(j));

                if (filter != null) {

                    // @TODO: what about AND/OR filters? or support for ranges, e.g., [500-1000]?
                    if (filter.startsWith(">")) {

                        if (tableModel.getValueAt(i, j) instanceof String) {
                            // not supported
                            filterError = true;
                        } else {

                            try {
                                double value = Double.valueOf(filter.substring(1));

                                if (tableModel.getValueAt(i, j) instanceof Integer) {
                                    if ((Integer) tableModel.getValueAt(i, j) <= value) {
                                        include = false;
                                    }
                                } else if (tableModel.getValueAt(i, j) instanceof Double) {
                                    if ((Double) tableModel.getValueAt(i, j) <= value) {
                                        include = false;
                                    }
                                } else if (tableModel.getValueAt(i, j) instanceof XYDataPoint) {
                                    if (((XYDataPoint) tableModel.getValueAt(i, j)).getX() <= value) {
                                        include = false;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                filterError = true;
                            }
                        }
                    } else if (filter.startsWith("<")) {

                        if (tableModel.getValueAt(i, j) instanceof String) {
                            // not supported
                            filterError = true;
                        } else {

                            try {
                                double value = Double.valueOf(filter.substring(1));

                                if (tableModel.getValueAt(i, j) instanceof Integer) {
                                    if ((Integer) tableModel.getValueAt(i, j) >= value) {
                                        include = false;
                                    }
                                } else if (tableModel.getValueAt(i, j) instanceof Double) {
                                    if ((Double) tableModel.getValueAt(i, j) >= value) {
                                        include = false;
                                    }
                                } else if (tableModel.getValueAt(i, j) instanceof XYDataPoint) {
                                    if (((XYDataPoint) tableModel.getValueAt(i, j)).getX() >= value) {
                                        include = false;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                filterError = true;
                            }
                        }
                    } else if (filter.startsWith("=")) {

                        if (tableModel.getValueAt(i, j) instanceof String) {

                            String pattern = filter.substring(1); // @TODO: support patterns

                            if (!((String) tableModel.getValueAt(i, j)).equalsIgnoreCase(pattern)) {
                                include = false;
                            }

                        } else {

                            try {
                                double value = Double.valueOf(filter.substring(1));

                                if (tableModel.getValueAt(i, j) instanceof Integer) {
                                    if (((Integer) tableModel.getValueAt(i, j)).intValue() != value) {
                                        include = false;
                                    }
                                } else if (tableModel.getValueAt(i, j) instanceof Double) {
                                    if (((Double) tableModel.getValueAt(i, j)).doubleValue() != value) {
                                        include = false;
                                    }
                                } else if (tableModel.getValueAt(i, j) instanceof XYDataPoint) {
                                    if (((XYDataPoint) tableModel.getValueAt(i, j)).getX() != value) {
                                        include = false;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                filterError = true;
                            }
                        }
                    }
                }
            }

            if (include) {
                rowsAfterDataFiltering.add(i);
            }
        }

        if (tableModel instanceof SelfUpdatingTableModel) {
            ((SelfUpdatingTableModel) tableModel).setSelfUpdating(selfUpdating);
        }

        if (filterError) {
            JOptionPane.showMessageDialog(this, "There was an error with one of the filters. Please check the filter settings.", "Filter Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
