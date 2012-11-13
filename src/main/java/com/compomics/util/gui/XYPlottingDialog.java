package com.compomics.util.gui;

import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import no.uib.jsparklines.data.XYDataPoint;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import no.uib.jsparklines.renderers.util.GradientColorCoding;
import org.jfree.chart.*;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.*;

/**
 * A dialog that makes it straightforward to inspect compare the values of two
 * columns in a table in a XY plot.
 *
 * @author Harald Barsnes
 */
public class XYPlottingDialog extends javax.swing.JDialog {

    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The table model.
     */
    private TableModel tabelModel;
    /**
     * The chart panel.
     */
    private ChartPanel chartPanel;
    /**
     * The bubble size.
     */
    private double bubbleSize = 1.0;
    /**
     * The number if bins for the histograms.
     */
    private int numberOfBins = 100;
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
     * The datapoint to model row number map.
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
     * The minmum column widths for the table.
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
     * Creates a new XYPlottingDialog.
     *
     * @param dialogParent the dialog parent
     * @param table the table to display the xy plot for
     * @param tableToolTips the table tooltips
     * @param modal
     */
    public XYPlottingDialog(java.awt.Frame dialogParent, JTable table, ArrayList<String> tableToolTips, boolean modal) {
        super(dialogParent, modal);
        initComponents();
        this.dialogParent = dialogParent;
        this.tabelModel = table.getModel();

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
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {

        Vector<String> colummnNames = new Vector<String>();

        int columnCount = tabelModel.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            if (tabelModel.getColumnName(i).trim().length() == 0) {
                colummnNames.add("(column " + (i + 1) + ")");
            } else {
                colummnNames.add(tabelModel.getColumnName(i));
            }
        }

        xAxisComboBox.setModel(new DefaultComboBoxModel(colummnNames));
        yAxisComboBox.setModel(new DefaultComboBoxModel(colummnNames));
        colorsComboBox.setModel(new DefaultComboBoxModel(colummnNames));

        selectedValuesTable.setModel(tabelModel);

        selectedValuesScrollPane.getViewport().setOpaque(false);
        selectedValuesTable.getTableHeader().setReorderingAllowed(false);
        selectedValuesTable.setAutoCreateRowSorter(true);

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
        backgroundPanel = new javax.swing.JPanel();
        xyPlotPanel = new javax.swing.JPanel();
        plotPanel = new javax.swing.JPanel();
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
        plotTypePanel = new javax.swing.JPanel();
        xyPlotRadioButton = new javax.swing.JRadioButton();
        histogramRadioButton = new javax.swing.JRadioButton();
        xAxisPanel = new javax.swing.JPanel();
        xAxisComboBox = new javax.swing.JComboBox();
        xAxisLabel = new javax.swing.JLabel();
        yAxisComboBox = new javax.swing.JComboBox();
        yAxisLabel = new javax.swing.JLabel();
        colorLabel = new javax.swing.JLabel();
        colorsComboBox = new javax.swing.JComboBox();
        plotSettingsPanel = new javax.swing.JPanel();
        bubbleOrBinSizeSpinner = new javax.swing.JSpinner();
        bubbleOrBinSizeLabel = new javax.swing.JLabel();
        dragSettingsPanel = new javax.swing.JPanel();
        dragToSelectRadioButton = new javax.swing.JRadioButton();
        dragToZoomRadioButton = new javax.swing.JRadioButton();
        logAcisPanel = new javax.swing.JPanel();
        xAxisLogCheckBox = new javax.swing.JCheckBox();
        yAxisLogCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Statistics");

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

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
                .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        xyPlotPanelLayout.setVerticalGroup(
            xyPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xyPlotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
                .addComponent(selectedValuesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1037, Short.MAX_VALUE)
                .addContainerGap())
        );
        selectedValuesPanelLayout.setVerticalGroup(
            selectedValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedValuesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedValuesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        plotTypePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plot Type"));
        plotTypePanel.setOpaque(false);

        plotTypeButtonGroup.add(xyPlotRadioButton);
        xyPlotRadioButton.setSelected(true);
        xyPlotRadioButton.setText("XY Plot");
        xyPlotRadioButton.setIconTextGap(15);
        xyPlotRadioButton.setOpaque(false);
        xyPlotRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyPlotRadioButtonActionPerformed(evt);
            }
        });

        plotTypeButtonGroup.add(histogramRadioButton);
        histogramRadioButton.setText("Histogram");
        histogramRadioButton.setIconTextGap(15);
        histogramRadioButton.setOpaque(false);
        histogramRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histogramRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout plotTypePanelLayout = new javax.swing.GroupLayout(plotTypePanel);
        plotTypePanel.setLayout(plotTypePanelLayout);
        plotTypePanelLayout.setHorizontalGroup(
            plotTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plotTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xyPlotRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(histogramRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        plotTypePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {histogramRadioButton, xyPlotRadioButton});

        plotTypePanelLayout.setVerticalGroup(
            plotTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plotTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plotTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xyPlotRadioButton)
                    .addComponent(histogramRadioButton))
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

        colorLabel.setText("Color");

        colorsComboBox.setMaximumRowCount(30);
        colorsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorsComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout xAxisPanelLayout = new javax.swing.GroupLayout(xAxisPanel);
        xAxisPanel.setLayout(xAxisPanelLayout);
        xAxisPanelLayout.setHorizontalGroup(
            xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xAxisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yAxisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorLabel))
                .addGap(20, 20, 20)
                .addGroup(xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yAxisComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xAxisComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(colorLabel)
                    .addComponent(colorsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        plotSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plot Settings"));
        plotSettingsPanel.setOpaque(false);

        bubbleOrBinSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));
        bubbleOrBinSizeSpinner.setToolTipText("The size of the bubbles relative to the x-axis");
        bubbleOrBinSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bubbleOrBinSizeSpinnerStateChanged(evt);
            }
        });

        bubbleOrBinSizeLabel.setText("Bubble");
        bubbleOrBinSizeLabel.setToolTipText("The size of the bubbles relative to the x-axis");

        javax.swing.GroupLayout plotSettingsPanelLayout = new javax.swing.GroupLayout(plotSettingsPanel);
        plotSettingsPanel.setLayout(plotSettingsPanelLayout);
        plotSettingsPanelLayout.setHorizontalGroup(
            plotSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, plotSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bubbleOrBinSizeLabel)
                .addGap(18, 18, 18)
                .addComponent(bubbleOrBinSizeSpinner)
                .addContainerGap())
        );
        plotSettingsPanelLayout.setVerticalGroup(
            plotSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plotSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plotSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bubbleOrBinSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bubbleOrBinSizeLabel))
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

        logAcisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logarithmic Axis"));
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

        javax.swing.GroupLayout logAcisPanelLayout = new javax.swing.GroupLayout(logAcisPanel);
        logAcisPanel.setLayout(logAcisPanelLayout);
        logAcisPanelLayout.setHorizontalGroup(
            logAcisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logAcisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xAxisLogCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(yAxisLogCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        logAcisPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {xAxisLogCheckBox, yAxisLogCheckBox});

        logAcisPanelLayout.setVerticalGroup(
            logAcisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logAcisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logAcisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xAxisLogCheckBox)
                    .addComponent(yAxisLogCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addComponent(xyPlotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(xAxisPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(plotSettingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dragSettingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(logAcisPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(plotTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(selectedValuesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(plotSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logAcisPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dragSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(xyPlotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedValuesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private void bubbleOrBinSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bubbleOrBinSizeSpinnerStateChanged
        if (xyPlotRadioButton.isSelected()) {
            bubbleSize = (Double) bubbleOrBinSizeSpinner.getValue();
        } else { // histogram selected
            numberOfBins = (Integer) bubbleOrBinSizeSpinner.getValue();
        }
        updatePlot();
    }//GEN-LAST:event_bubbleOrBinSizeSpinnerStateChanged

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
    private void xyPlotRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyPlotRadioButtonActionPerformed
        histogramRadioButtonActionPerformed(null);
    }//GEN-LAST:event_xyPlotRadioButtonActionPerformed

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
        xAxisLogCheckBox.setEnabled(xyPlotRadioButton.isSelected());
        yAxisLogCheckBox.setEnabled(xyPlotRadioButton.isSelected());
        yAxisLogCheckBox.setEnabled(xyPlotRadioButton.isSelected());
        dragToSelectRadioButton.setEnabled(xyPlotRadioButton.isSelected());
        dragToZoomRadioButton.setEnabled(xyPlotRadioButton.isSelected());

        if (xyPlotRadioButton.isSelected()) {
            bubbleOrBinSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(bubbleSize, Double.valueOf(0.0d), null, Double.valueOf(1.0d)));
            bubbleOrBinSizeLabel.setText("Bubble");
            bubbleOrBinSizeLabel.setToolTipText("The size of the bubbles relative to the x-axis");
            bubbleOrBinSizeSpinner.setToolTipText("The size of the bubbles relative to the x-axis");
        } else {
            bubbleOrBinSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(numberOfBins, 1, null, 1));
            bubbleOrBinSizeLabel.setText("Bins  ");
            bubbleOrBinSizeLabel.setToolTipText("The number of bins for the histogram");
            bubbleOrBinSizeSpinner.setToolTipText("The number of bins for the histogram");
        }

        updatePlot();
    }//GEN-LAST:event_histogramRadioButtonActionPerformed

    /**
     * Turn the cursor into a hand cursor.
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
     * Turn the cursor into a hand cursor.
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel bubbleOrBinSizeLabel;
    private javax.swing.JSpinner bubbleOrBinSizeSpinner;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JComboBox colorsComboBox;
    private javax.swing.ButtonGroup dragButtonGroup;
    private javax.swing.JPanel dragSettingsPanel;
    private javax.swing.JRadioButton dragToSelectRadioButton;
    private javax.swing.JRadioButton dragToZoomRadioButton;
    private javax.swing.JRadioButton histogramRadioButton;
    private javax.swing.JPanel logAcisPanel;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JPanel plotSettingsPanel;
    private javax.swing.ButtonGroup plotTypeButtonGroup;
    private javax.swing.JPanel plotTypePanel;
    private javax.swing.JPanel selectedValuesPanel;
    private javax.swing.JScrollPane selectedValuesScrollPane;
    private javax.swing.JTable selectedValuesTable;
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
    private void updatePlot() {

        if (!isPlotting) {
            selectedDataPoints = new HashMap<Integer, ArrayList<Integer>>();
            dataPointToRowNumber = new HashMap<String, Integer>();
            selectedModelRows = new ArrayList<Integer>();

            isPlotting = true;

            progressDialog = new ProgressDialogX(this, dialogParent,
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker-orange.gif")),
                    true);
            progressDialog.setIndeterminate(true);
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

                    if (histogramRadioButton.isSelected()) {

                        ((TitledBorder) xyPlotPanel.getBorder()).setTitle(xAxisName);
                        xyPlotPanel.revalidate();
                        xyPlotPanel.repaint();

                        progressDialog.setIndeterminate(false);
                        progressDialog.setMaxProgressValue(tabelModel.getRowCount());
                        progressDialog.setValue(0);

                        int xAxisIndex = xAxisComboBox.getSelectedIndex();
                        double[] values = new double[tabelModel.getRowCount()];

                        // @TODO: possible to use batch selection here??

                        for (int index = 0; index < tabelModel.getRowCount(); index++) {
                            progressDialog.increaseProgressValue();

                            // @TODO: support more data types!!

                            if (tabelModel.getValueAt(index, xAxisIndex) instanceof XYDataPoint) {
                                values[index] = ((XYDataPoint) tabelModel.getValueAt(index, xAxisIndex)).getX();
                            } else if (tabelModel.getValueAt(index, xAxisIndex) instanceof Integer) {
                                values[index] = ((Integer) tabelModel.getValueAt(index, xAxisIndex)).doubleValue();
                            } else if (tabelModel.getValueAt(index, xAxisIndex) instanceof Double) {
                                values[index] = ((Double) tabelModel.getValueAt(index, xAxisIndex)).doubleValue();
                            }

                            // @TODO: what about null values?
                        }

                        HistogramDataset dataset = new HistogramDataset();
                        dataset.setType(HistogramType.FREQUENCY);
                        dataset.addSeries(xAxisName, values, numberOfBins);

                        JFreeChart chart = ChartFactory.createHistogram(null, xAxisName, "Frequency", dataset, PlotOrientation.VERTICAL, false, true, false);

                        chartPanel = new ChartPanel(chart);
                        chartPanel.setBorder(null);
                        chart.setBorderVisible(false);

                        XYPlot plot = chart.getXYPlot();

                        // set up the chart renderer
                        XYBarRenderer renderer = new XYBarRenderer();
                        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
                        renderer.setShadowVisible(false);
                        renderer.setSeriesPaint(0, histogramColor);
                        plot.setRenderer(renderer);

                        //plot.getRangeAxis().setRange(0, plot.getRangeAxis().getUpperBound());

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

                        DefaultXYDataset xyDataset = new DefaultXYDataset();
                        DefaultXYZDataset xyzDataset = new DefaultXYZDataset();

                        ArrayList<String> datasetNames = new ArrayList<String>();
                        HashMap<String, ArrayList<Integer>> datasets = new HashMap<String, ArrayList<Integer>>();

                        int colorIndex = colorsComboBox.getSelectedIndex();

                        progressDialog.setIndeterminate(false);
                        progressDialog.setMaxProgressValue(tabelModel.getRowCount() * 2);
                        progressDialog.setValue(0);

                        // @TODO: possible to use batch selection here??

                        for (int i = 0; i < tabelModel.getRowCount(); i++) {

                            progressDialog.increaseProgressValue();

                            ArrayList<Integer> tempArray;
                            if (!datasets.containsKey("" + tabelModel.getValueAt(i, colorIndex))) {
                                tempArray = new ArrayList<Integer>();
                                datasetNames.add("" + tabelModel.getValueAt(i, colorIndex));
                            } else {
                                tempArray = datasets.get("" + tabelModel.getValueAt(i, colorIndex));
                            }
                            tempArray.add(i);
                            datasets.put("" + tabelModel.getValueAt(i, colorIndex), tempArray);
                        }

                        int xAxisIndex = xAxisComboBox.getSelectedIndex();
                        int yAxisIndex = yAxisComboBox.getSelectedIndex();

                        progressDialog.setIndeterminate(false);
                        progressDialog.setMaxProgressValue(tabelModel.getRowCount());
                        progressDialog.setValue(0);

                        int datasetCounter = 0;

                        // @TODO: the below does not yet work
//                    HashMap<Integer, Color> datasetColors = new HashMap<Integer, Color>();
//                    double minValue = 0, maxValue = 1;
//
//                    if (cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex())) instanceof JSparklinesBarChartTableCellRenderer) {
//                        JSparklinesBarChartTableCellRenderer colorRenderer =
//                                (JSparklinesBarChartTableCellRenderer) cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex()));
//                        minValue = colorRenderer.getMinValue();
//                        maxValue = colorRenderer.getMaxValue();
//                    } else if (cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex())) instanceof JSparklinesTwoValueBarChartTableCellRenderer) {
//                        JSparklinesTwoValueBarChartTableCellRenderer colorRenderer =
//                                (JSparklinesTwoValueBarChartTableCellRenderer) cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex()));
//                        minValue = 0;
//                        maxValue = colorRenderer.getMaxValue();
//                    }


                        // @TODO: add the option of filtering the data based on the values in one or more columns?
                        //        for example remove all non-validated proteins or show only coverage > 50%?


                        // split the data into the datasets
                        for (String dataset : datasetNames) {

                            double[][] tempDataXY = new double[2][datasets.get(dataset).size()];
                            double[][] tempDataXYZ = new double[3][datasets.get(dataset).size()];

                            int counter = 0;

                            for (Integer index : datasets.get(dataset)) {

                                progressDialog.increaseProgressValue();

                                // @TODO: support more data types!!

                                if (tabelModel.getValueAt(index, xAxisIndex) instanceof XYDataPoint) {
                                    tempDataXY[0][counter] = ((XYDataPoint) tabelModel.getValueAt(index, xAxisIndex)).getX();
                                    tempDataXYZ[0][counter] = ((XYDataPoint) tabelModel.getValueAt(index, xAxisIndex)).getX();
                                } else if (tabelModel.getValueAt(index, xAxisIndex) instanceof Integer) {
                                    tempDataXY[0][counter] = (Integer) tabelModel.getValueAt(index, xAxisIndex);
                                    tempDataXYZ[0][counter] = (Integer) tabelModel.getValueAt(index, xAxisIndex);
                                } else if (tabelModel.getValueAt(index, xAxisIndex) instanceof Double) {
                                    tempDataXY[0][counter] = (Double) tabelModel.getValueAt(index, xAxisIndex);
                                    tempDataXYZ[0][counter] = (Double) tabelModel.getValueAt(index, xAxisIndex);
                                }

                                if (tabelModel.getValueAt(index, yAxisIndex) instanceof XYDataPoint) {
                                    tempDataXY[1][counter] = ((XYDataPoint) tabelModel.getValueAt(index, yAxisIndex)).getX();
                                    tempDataXYZ[1][counter] = ((XYDataPoint) tabelModel.getValueAt(index, yAxisIndex)).getX();
                                } else if (tabelModel.getValueAt(index, yAxisIndex) instanceof Integer) {
                                    tempDataXY[1][counter] = (Integer) tabelModel.getValueAt(index, yAxisIndex);
                                    tempDataXYZ[1][counter] = (Integer) tabelModel.getValueAt(index, yAxisIndex);
                                } else if (tabelModel.getValueAt(index, yAxisIndex) instanceof Double) {
                                    tempDataXY[1][counter] = (Double) tabelModel.getValueAt(index, yAxisIndex);
                                    tempDataXYZ[1][counter] = (Double) tabelModel.getValueAt(index, yAxisIndex);
                                }

                                tempDataXYZ[2][counter] = bubbleSize;

                                // @TODO: the below does not yet work
//                            // get the color to use if using gradient color coding
//                            if (tabelModel.getValueAt(index, colorIndex) instanceof Integer) {
//                                datasetColors.put(datasetCounter,
//                                        GradientColorCoding.findGradientColor(((Integer) tabelModel.getValueAt(index, colorIndex)).doubleValue(),
//                                        minValue, maxValue, colorGradient));
//                            } else if (tabelModel.getValueAt(index, colorIndex) instanceof Double) {
//                                datasetColors.put(datasetCounter,
//                                        GradientColorCoding.findGradientColor((Double) tabelModel.getValueAt(index, colorIndex),
//                                        minValue, maxValue, colorGradient));
//                            } else if (tabelModel.getValueAt(index, colorIndex) instanceof XYDataPoint) {
//                                datasetColors.put(datasetCounter,
//                                        GradientColorCoding.findGradientColor(((XYDataPoint) tabelModel.getValueAt(index, colorIndex)).getX(),
//                                        minValue, maxValue, colorGradient));
//                            } else {
//                                datasetColors.put(datasetCounter,
//                                        GradientColorCoding.findGradientColor(minValue,
//                                        minValue, maxValue, colorGradient));
//                            }

                                dataPointToRowNumber.put(datasetCounter + "_" + counter++, index);
                            }

                            xyDataset.addSeries(dataset, tempDataXY);
                            xyzDataset.addSeries(dataset, tempDataXYZ);
                            datasetCounter++;
                        }

                        // create the plot
                        JFreeChart chart = ChartFactory.createBubbleChart(null, xAxisName, yAxisName, xyzDataset, PlotOrientation.VERTICAL, false, true, false);
                        XYPlot plot = chart.getXYPlot();

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
                        if (cellRenderers.containsKey(new Integer(colorsComboBox.getSelectedIndex()))) {
                            if (cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex())) instanceof JSparklinesIntegerColorTableCellRenderer) {
                                JSparklinesIntegerColorTableCellRenderer integerColorRenderer =
                                        (JSparklinesIntegerColorTableCellRenderer) cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex()));
                                HashMap<Integer, Color> colors = integerColorRenderer.getColors();

                                for (int i = 0; i < datasetNames.size(); i++) {
                                    Integer datasetInteger = new Integer(datasetNames.get(i));
                                    renderer.setSeriesPaint(i, colors.get(new Integer(datasetInteger)));
                                }
                            }

                            // @TODO: the below does not yet work
//                        else if (cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex())) instanceof JSparklinesBarChartTableCellRenderer) {
//                            for (int i = 0; i < datasetNames.size(); i++) {
//                                renderer.setSeriesPaint(i, datasetColors.get(i));
//                            }
//                        } else if (cellRenderers.get(new Integer(colorsComboBox.getSelectedIndex())) instanceof JSparklinesTwoValueBarChartTableCellRenderer) {
//                            for (int i = 0; i < datasetNames.size(); i++) {
//                                renderer.setSeriesPaint(i, datasetColors.get(i));
//                            }
//                        }
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

                    isPlotting = false;
                    filterTable();
                    progressDialog.setRunFinished();
                }
            }.start();
        }
    }

    /**
     * Handles mouse clicks in the chart panel. Selects/de-selects datapoints.
     *
     * @param event
     */
    public void mouseClickedInChart(ChartMouseEvent event) {

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

        // if no data points was selected clear the selection
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
    public void mouseMovedInChart(ChartMouseEvent event) {
        // @TODO: should we do something here?
    }

    /**
     * Filters the selected values table according to the currently selected
     * values.
     */
    public void filterTable() {

        if (selectedDataPoints.isEmpty()) {
            // no filtering, show all values
            ((TableRowSorter) selectedValuesTable.getRowSorter()).setRowFilter(null);
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

    /**
     * A filter that filters the table based on if the datapoint is selected in
     * the plot or not.
     */
    public class SelectedValuesTableFilter extends RowFilter<DefaultTableModel, Integer> {

        public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
            if (selectedModelRows.isEmpty() || selectedModelRows.contains(entry.getIdentifier())) {
                return true;
            }
            return false;
        }
    }
}
