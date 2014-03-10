package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.*;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.gui.renderers.AlignedTableCellRenderer;
import com.compomics.util.gui.renderers.FragmentIonTableCellRenderer;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesErrorBarChartTableCellRenderer;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

/**
 * Creates a fragment ion table with the detected fragment ions. Either shows a
 * traditional ion table with the theoretical fragment ions, or a novel version
 * showing the intensities as bar charts for each ion type.
 *
 * @author Harald Barsnes
 */
public class FragmentIonTable extends JTable {

    /**
     * The list of currently selected fragment ion types.
     */
    private ArrayList<Integer> currentFragmentIonTypes;
    /**
     * The list of the currently selected neutral loss types.
     */
    private NeutralLossesMap neutralLosses;
    /**
     * If true, singly charge ions are included in the table.
     */
    private boolean singleCharge;
    /**
     * If true, doubly charged ions are included in the table.
     */
    private boolean twoCharges;
    /**
     * The table tooltips.
     */
    private ArrayList<String> tooltips = new ArrayList<String>();
    /**
     * The current peptide.
     */
    private Peptide currentPeptide;
    /**
     * The current peptide sequence.
     */
    private String peptideSequence;
    /**
     * The spectrum annotations map.
     */
    private ArrayList<ArrayList<IonMatch>> allAnnotations;
    /**
     * The list of spectra. Needed for intensity normalization.
     */
    private ArrayList<MSnSpectrum> allSpectra;

    /**
     * Creates a traditional fragment ion table with the theoretical mz values
     * and the detected fragment ions highlighted.
     *
     * @param currentPeptide the peptide to show the table for
     * @param allAnnotations the spectrum annotations (from SpectrumAnnotator)
     * @param currentFragmentIonTypes the list of currently selected fragment
     * ion types
     * @param neutralLosses the list of the currently selected neutral loss
     * types
     * @param singleCharge if true, singly charge ions are included in the table
     * @param twoCharges if true, doubly charged ions are included in the table
     */
    public FragmentIonTable(
            Peptide currentPeptide,
            ArrayList<ArrayList<IonMatch>> allAnnotations,
            ArrayList<Integer> currentFragmentIonTypes,
            NeutralLossesMap neutralLosses,
            boolean singleCharge, boolean twoCharges) {
        super();

        this.currentPeptide = currentPeptide;
        this.currentFragmentIonTypes = currentFragmentIonTypes;
        this.neutralLosses = neutralLosses;
        this.singleCharge = singleCharge;
        this.twoCharges = twoCharges;
        this.allAnnotations = allAnnotations;

        peptideSequence = currentPeptide.getSequence();

        // set up table properties
        setUpTable(java.lang.Double.class);

        // add the peptide sequence and indexes to the table
        addPeptideSequenceAndIndexes();

        // add the values to the table
        insertMzValues();
    }

    /**
     * Creates a novel fragment ion table displaying bar charts with the
     * intensity of each fragment ion type. If more than one spectrum annotation
     * set is provided the bars show the average intensities.
     *
     * @param currentPeptide the peptide to show the table for
     * @param allAnnotations the spectrum annotations (from SpectrumAnnotator)
     * @param allSpectra the list of spectra
     * @param currentFragmentIonTypes the list of currently selected fragment
     * ion types
     * @param neutralLosses the list of the currently selected neutral loss
     * types
     * @param singleCharge if true, singly charge ions are included in the table
     * @param twoCharges if true, doubly charged ions are included in the table
     */
    public FragmentIonTable(
            Peptide currentPeptide,
            ArrayList<ArrayList<IonMatch>> allAnnotations,
            ArrayList<MSnSpectrum> allSpectra,
            ArrayList<Integer> currentFragmentIonTypes,
            NeutralLossesMap neutralLosses,
            boolean singleCharge, boolean twoCharges) {
        super();

        this.currentPeptide = currentPeptide;
        this.currentFragmentIonTypes = currentFragmentIonTypes;
        this.neutralLosses = neutralLosses;
        this.singleCharge = singleCharge;
        this.twoCharges = twoCharges;
        this.allAnnotations = allAnnotations;
        this.allSpectra = allSpectra;

        peptideSequence = currentPeptide.getSequence();

        // set up table properties
        if (allAnnotations.size() == 1) {
            setUpTable(java.lang.Double.class);
        } else {
            setUpTable(DefaultStatisticalCategoryDataset.class);
        }

        // add the peptide sequence and indexes to the table
        addPeptideSequenceAndIndexes();

        // add the values to the table
        insertBarCharts();
    }

    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return (String) tooltips.get(realIndex);
            }
        };
    }

    /**
     * Set up the table properties.
     */
    private void setUpTable(Class valueClass) {

        // disallow column reordering
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setReorderingAllowed(false);

        // control the cell selection
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(false);
        setCellSelectionEnabled(true);
        setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // centrally align the column headers in the fragment ions table
        TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
        JLabel label = (JLabel) renderer;
        label.setHorizontalAlignment(JLabel.CENTER);

        // set up the column headers, types and tooltips
        Vector columnHeaders = new Vector();
        ArrayList<Class> tempColumnTypes = new ArrayList<Class>();
        tooltips = new ArrayList<String>();

        columnHeaders.add(" ");
        tempColumnTypes.add(java.lang.Integer.class);
        tooltips.add("a, b and c ion index");

        // @TODO: add H20 and NH3 losses for a,c,x and z ions
        if (currentFragmentIonTypes.contains(PeptideFragmentIon.A_ION)) {
            columnHeaders.add("a");
            tempColumnTypes.add(valueClass);
            tooltips.add("a-ion");
        }

        if (currentFragmentIonTypes.contains(PeptideFragmentIon.B_ION)) {
            if (singleCharge) {
                columnHeaders.add("b");
                tempColumnTypes.add(valueClass);
                tooltips.add("b-ion");
            }
            if (twoCharges) {
                columnHeaders.add("b++");
                tempColumnTypes.add(valueClass);
                tooltips.add("b-ion doubly charged");
            }

            if (neutralLosses.containsLoss(NeutralLoss.H2O)) {
                if (singleCharge) {
                    columnHeaders.add("b-H2O");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("b-ion with water loss");
                }
                if (twoCharges) {
                    columnHeaders.add("b++-H2O");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("b-ion with water loss, doubly charged");
                }
            }
            if (neutralLosses.containsLoss(NeutralLoss.NH3)) {
                if (singleCharge) {
                    columnHeaders.add("b-NH3");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("b-ion with ammonia loss");
                }
                if (twoCharges) {
                    columnHeaders.add("b++-NH3");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("b-ion with ammonia loss, doubly charged");
                }
            }
        }

        if (currentFragmentIonTypes.contains(PeptideFragmentIon.C_ION)) {
            columnHeaders.add("c");
            tempColumnTypes.add(valueClass);
            tooltips.add("c-ion");
        }

        columnHeaders.add("AA");
        tempColumnTypes.add(java.lang.String.class);
        tooltips.add("amino acid sequence");

        if (currentFragmentIonTypes.contains(PeptideFragmentIon.Z_ION)) {
            columnHeaders.add("z");
            tempColumnTypes.add(valueClass);
            tooltips.add("z-ion");
        }

        if (currentFragmentIonTypes.contains(PeptideFragmentIon.Y_ION)) {
            if (singleCharge) {
                columnHeaders.add("y");
                tempColumnTypes.add(valueClass);
                tooltips.add("y-ion");
            }
            if (twoCharges) {
                columnHeaders.add("y++");
                tempColumnTypes.add(valueClass);
                tooltips.add("y-ion, doubly charged");
            }

            if (neutralLosses.containsLoss(NeutralLoss.H2O)) {
                if (singleCharge) {
                    columnHeaders.add("y-H2O");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("y-ion with water loss");
                }
                if (twoCharges) {
                    columnHeaders.add("y++-H2O");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("y-ion with water loss, doubly charged");
                }
            }
            if (neutralLosses.containsLoss(NeutralLoss.NH3)) {
                if (singleCharge) {
                    columnHeaders.add("y-NH3");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("y-ion with ammonia loss");
                }
                if (twoCharges) {
                    columnHeaders.add("y++-NH3");
                    tempColumnTypes.add(valueClass);
                    tooltips.add("y-ion with ammonia loss, doubly charged");
                }
            }
        }

        if (currentFragmentIonTypes.contains(PeptideFragmentIon.X_ION)) {
            columnHeaders.add("x");
            tempColumnTypes.add(valueClass);
            tooltips.add("x-ion");
        }

        columnHeaders.add("  ");
        tempColumnTypes.add(java.lang.Integer.class);
        tooltips.add("x, y and z ion index");

        final ArrayList<Class> columnTypes = tempColumnTypes;

        // set the table model
        setModel(new javax.swing.table.DefaultTableModel(
                new Vector(),
                columnHeaders) {
                    public Class getColumnClass(int columnIndex) {
                        return columnTypes.get(columnIndex);
                    }

                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return false;
                    }
                });

        int tempWidth = 30; // @TODO: maybe this should not be hardcoded?

        // set the max column widths
        getColumn(" ").setMaxWidth(tempWidth);
        getColumn(" ").setMinWidth(tempWidth);
        getColumn("  ").setMaxWidth(tempWidth);
        getColumn("  ").setMinWidth(tempWidth);
        getColumn("AA").setMaxWidth(tempWidth);
        getColumn("AA").setMinWidth(tempWidth);

        // centrally align the columns in the fragment ions table
        getColumn(" ").setCellRenderer(new AlignedTableCellRenderer(SwingConstants.CENTER, Color.LIGHT_GRAY));
        getColumn("  ").setCellRenderer(new AlignedTableCellRenderer(SwingConstants.CENTER, Color.LIGHT_GRAY));
        getColumn("AA").setCellRenderer(new AlignedTableCellRenderer(SwingConstants.CENTER, Color.LIGHT_GRAY));
    }

    /**
     * Add the peptide and sequence indexes to the table.
     */
    private void addPeptideSequenceAndIndexes() {

        // add the peptide sequence and indexes to the table
        for (int i = 0; i < peptideSequence.length(); i++) {
            ((DefaultTableModel) getModel()).addRow(new Object[]{(i + 1)});
        }

        for (int i = 0; i < peptideSequence.length(); i++) {
            setValueAt(peptideSequence.charAt(i), i, getColumn("AA").getModelIndex());
            setValueAt(peptideSequence.length() - i, i, getColumn("  ").getModelIndex());
        }
    }

    /**
     * Add the theoretical mz values to the table.
     */
    private void insertMzValues() {

        // get all fragmentions for the peptide
        IonFactory fragmentFactory = IonFactory.getInstance();
        ArrayList<Ion> fragmentIons = fragmentFactory.getFragmentIons(currentPeptide);

        // add the theoretical masses to the table
        for (Ion ion : fragmentIons) {
            // @TODO: implement neutral losses
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION && ion.getNeutralLosses().isEmpty()) {
                PeptideFragmentIon fragmention = (PeptideFragmentIon) ion;
                double fragmentMzChargeOne = (ion.getTheoreticMass() + 1 * ElementaryIon.proton.getTheoreticMass()) / 1;
                double fragmentMzChargeTwo = (ion.getTheoreticMass() + 2 * ElementaryIon.proton.getTheoreticMass()) / 2;

                int fragmentNumber = fragmention.getNumber();

                if (currentFragmentIonTypes.contains(fragmention.getSubType())) {

                    if (fragmention.getSubType() == PeptideFragmentIon.A_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("a").getModelIndex());
                        }
                    } else if (fragmention.getSubType() == PeptideFragmentIon.B_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++").getModelIndex());
                        }

                        if (neutralLosses.containsLoss(NeutralLoss.H2O)) {
                            if (singleCharge) {
                                setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b-H2O").getModelIndex());
                            }
                            if (twoCharges) {
                                setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++-H2O").getModelIndex());
                            }
                        }

                        if (neutralLosses.containsLoss(NeutralLoss.NH3)) {
                            if (singleCharge) {
                                setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b-NH3").getModelIndex());
                            }
                            if (twoCharges) {
                                setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++-NH3").getModelIndex());
                            }
                        }
                    } else if (fragmention.getSubType() == PeptideFragmentIon.C_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("c").getModelIndex());
                        }
                    } else if (fragmention.getSubType() == PeptideFragmentIon.Y_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++").getModelIndex());
                        }

                        if (neutralLosses.containsLoss(NeutralLoss.H2O)) {
                            if (singleCharge) {
                                setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y-H2O").getModelIndex());
                            }
                            if (twoCharges) {
                                setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++-H2O").getModelIndex());
                            }
                        }

                        if (neutralLosses.containsLoss(NeutralLoss.NH3)) {
                            if (singleCharge) {
                                setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y-NH3").getModelIndex());
                            }
                            if (twoCharges) {
                                setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++-NH3").getModelIndex());
                            }
                        }
                    } else if (fragmention.getSubType() == PeptideFragmentIon.X_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("x").getModelIndex());
                        }
                    } else if (fragmention.getSubType() == PeptideFragmentIon.Z_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("z").getModelIndex());
                        }
                    }
                }
            }
        }

        // @TODO: implement a better way to handle charge
        ArrayList<Integer> aIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsDoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsH2OSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsH2ODoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsNH3SinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsNH3DoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> cIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsDoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsH2OSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsH2ODoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsNH3SinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsNH3DoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> xIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> zIonsSinglyCharged = new ArrayList<Integer>();

        // highlight the detected ions
        for (int i = 0; i < allAnnotations.size(); i++) {

            ArrayList<IonMatch> currentAnnotations = allAnnotations.get(i);

            for (IonMatch ionMatch : currentAnnotations) {
                if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {

                    int currentCharge = ionMatch.charge.value;
                    PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                    int fragmentNumber = fragmentIon.getNumber();

                    if (currentFragmentIonTypes.contains(fragmentIon.getSubType())) {

                        if (fragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            if (currentCharge == 1 && singleCharge) {
                                aIonsSinglyCharged.add(fragmentNumber - 1);
                            }
                        } else if (fragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            if (fragmentIon.getNeutralLosses().isEmpty()) {
                                if (currentCharge == 1 && singleCharge) {
                                    bIonsSinglyCharged.add(fragmentNumber - 1);
                                } else if (twoCharges) {
                                    bIonsDoublyCharged.add(fragmentNumber - 1);
                                }
                            } else {
                                if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-H20") && neutralLosses.containsLoss(NeutralLoss.H2O)) {
                                    if (currentCharge == 1 && singleCharge) {
                                        bIonsH2OSinglyCharged.add(fragmentNumber - 1);
                                    } else if (twoCharges) {
                                        bIonsH2ODoublyCharged.add(fragmentNumber - 1);
                                    }
                                } else if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-NH3") && neutralLosses.containsLoss(NeutralLoss.NH3)) {
                                    if (currentCharge == 1 && singleCharge) {
                                        bIonsNH3SinglyCharged.add(fragmentNumber - 1);
                                    } else if (twoCharges) {
                                        bIonsNH3DoublyCharged.add(fragmentNumber - 1);
                                    }
                                }
                            }
                        } else if (fragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            if (currentCharge == 1 && singleCharge) {
                                cIonsSinglyCharged.add(fragmentNumber - 1);
                            }
                        } else if (fragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            if (fragmentIon.getNeutralLosses().isEmpty()) {
                                if (currentCharge == 1 && singleCharge) {
                                    yIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                } else if (twoCharges) {
                                    yIonsDoublyCharged.add(peptideSequence.length() - fragmentNumber);
                                }
                            } else {
                                if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-H20") && neutralLosses.containsLoss(NeutralLoss.H2O)) {
                                    if (currentCharge == 1 && singleCharge) {
                                        yIonsH2OSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                    } else if (twoCharges) {
                                        yIonsH2ODoublyCharged.add(peptideSequence.length() - fragmentNumber);
                                    }
                                } else if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-NH3") && neutralLosses.containsLoss(NeutralLoss.NH3)) {
                                    if (currentCharge == 1 && singleCharge) {
                                        yIonsNH3SinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                    } else if (twoCharges) {
                                        yIonsNH3DoublyCharged.add(peptideSequence.length() - fragmentNumber);
                                    }
                                }
                            }
                        } else if (fragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            if (currentCharge == 1 && singleCharge) {
                                xIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                            }
                        } else if (fragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            if (currentCharge == 1 && singleCharge) {
                                zIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                            }
                        }
                    }
                }
            }
        }

        // highlight the detected fragment ions in the table
        try {
            getColumn("a").setCellRenderer(new FragmentIonTableCellRenderer(aIonsSinglyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("b").setCellRenderer(new FragmentIonTableCellRenderer(bIonsSinglyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("b++").setCellRenderer(new FragmentIonTableCellRenderer(bIonsDoublyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("b-H2O").setCellRenderer(new FragmentIonTableCellRenderer(bIonsH2OSinglyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("b++-H2O").setCellRenderer(new FragmentIonTableCellRenderer(bIonsH2ODoublyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("b-NH3").setCellRenderer(new FragmentIonTableCellRenderer(bIonsNH3SinglyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("b++-NH3").setCellRenderer(new FragmentIonTableCellRenderer(bIonsNH3DoublyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("c").setCellRenderer(new FragmentIonTableCellRenderer(cIonsSinglyCharged, Color.BLUE, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y").setCellRenderer(new FragmentIonTableCellRenderer(yIonsSinglyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y++").setCellRenderer(new FragmentIonTableCellRenderer(yIonsDoublyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y-H2O").setCellRenderer(new FragmentIonTableCellRenderer(yIonsH2OSinglyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y++-H2O").setCellRenderer(new FragmentIonTableCellRenderer(yIonsH2ODoublyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y-NH3").setCellRenderer(new FragmentIonTableCellRenderer(yIonsNH3SinglyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y++-NH3").setCellRenderer(new FragmentIonTableCellRenderer(yIonsNH3DoublyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("x").setCellRenderer(new FragmentIonTableCellRenderer(xIonsSinglyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            getColumn("y").setCellRenderer(new FragmentIonTableCellRenderer(yIonsSinglyCharged, Color.RED, Color.WHITE));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    /**
     * Insert bar charts into the table.
     */
    private void insertBarCharts() {

        HashMap<String, ArrayList<Double>> values = new HashMap<String, ArrayList<Double>>();

        double maxIntensity = 0.0;

        for (int i = 0; i < allAnnotations.size(); i++) {

            ArrayList<IonMatch> currentAnnotations = allAnnotations.get(i);

            if (i < allSpectra.size()) { // escape possible null pointer

                double totalIntensity = allSpectra.get(i).getTotalIntensity();

                for (IonMatch ionMatch : currentAnnotations) {

                    if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {

                        int currentCharge = ionMatch.charge.value;
                        double peakIntensity = ionMatch.peak.intensity;

                        if (allAnnotations.size() > 1) {
                            peakIntensity /= totalIntensity;
                        }

                        if (maxIntensity < peakIntensity) {
                            maxIntensity = peakIntensity;
                        }

                        PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                        int fragmentNumber = fragmentIon.getNumber();

                        if (currentFragmentIonTypes.contains(fragmentIon.getSubType())) {

                            if (fragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    String key = "a" + "_" + (fragmentNumber - 1);
                                    addValue(values, key, peakIntensity);
                                }
                            } else if (fragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                                if (fragmentIon.getNeutralLosses().isEmpty()) {
                                    if (currentCharge == 1 && singleCharge) {
                                        String key = "b" + "_" + (fragmentNumber - 1);
                                        addValue(values, key, peakIntensity);
                                    } else if (twoCharges) {
                                        String key = "b++" + "_" + (fragmentNumber - 1);
                                        addValue(values, key, peakIntensity);
                                    }
                                } else {
                                    if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-H20") && neutralLosses.containsLoss(NeutralLoss.H2O)) {
                                        if (currentCharge == 1 && singleCharge) {
                                            String key = "b-H2O" + "_" + (fragmentNumber - 1);
                                            addValue(values, key, peakIntensity);
                                        } else if (twoCharges) {
                                            String key = "b++-H2O" + "_" + (fragmentNumber - 1);
                                            addValue(values, key, peakIntensity);
                                        }
                                    } else if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-NH3") && neutralLosses.containsLoss(NeutralLoss.NH3)) {
                                        if (currentCharge == 1 && singleCharge) {
                                            String key = "b-NH3" + "_" + (fragmentNumber - 1);
                                            addValue(values, key, peakIntensity);
                                        } else if (twoCharges) {
                                            String key = "b++-NH3" + "_" + (fragmentNumber - 1);
                                            addValue(values, key, peakIntensity);
                                        }
                                    }
                                }
                            } else if (fragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    String key = "c" + "_" + (fragmentNumber - 1);
                                    addValue(values, key, peakIntensity);
                                }
                            } else if (fragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                                if (fragmentIon.getNeutralLosses().isEmpty()) {
                                    if (currentCharge == 1 && singleCharge) {
                                        String key = "y" + "_" + (peptideSequence.length() - fragmentNumber);
                                        addValue(values, key, peakIntensity);
                                    } else if (twoCharges) {
                                        String key = "y++" + "_" + (peptideSequence.length() - fragmentNumber);
                                        addValue(values, key, peakIntensity);
                                    }
                                } else {
                                    if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-H20") && neutralLosses.containsLoss(NeutralLoss.H2O)) {
                                        if (currentCharge == 1 && singleCharge) {
                                            String key = "y-H2O" + "_" + (peptideSequence.length() - fragmentNumber);
                                            addValue(values, key, peakIntensity);
                                        } else if (twoCharges) {
                                            String key = "y++-H2O" + "_" + (peptideSequence.length() - fragmentNumber);
                                            addValue(values, key, peakIntensity);
                                        }
                                    } else if (fragmentIon.getNeutralLossesAsString().equalsIgnoreCase("-NH3") && neutralLosses.containsLoss(NeutralLoss.NH3)) {
                                        if (currentCharge == 1 && singleCharge) {
                                            String key = "y-NH3" + "_" + (peptideSequence.length() - fragmentNumber);
                                            addValue(values, key, peakIntensity);
                                        } else if (twoCharges) {
                                            String key = "y++-NH3" + "_" + (peptideSequence.length() - fragmentNumber);
                                            addValue(values, key, peakIntensity);
                                        }
                                    }
                                }
                            } else if (fragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    String key = "x" + "_" + (peptideSequence.length() - fragmentNumber);
                                    addValue(values, key, peakIntensity);
                                }
                            } else if (fragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    String key = "z" + "_" + (peptideSequence.length() - fragmentNumber);
                                    addValue(values, key, peakIntensity);
                                }
                            }
                        }
                    }
                }
            }
        }

        Iterator<String> valuesIterator = values.keySet().iterator();

        while (valuesIterator.hasNext()) {

            String ionType = valuesIterator.next();

            String[] ionTypeSplit = ionType.split("_");

            String ion = ionTypeSplit[0];
            Integer ionNumber = new Integer(ionTypeSplit[1]);

            ArrayList<Double> allCurrentIonValues = values.get(ionType);
            SummaryStatistics stats = new SummaryStatistics();

            for (int i = 0; i < allCurrentIonValues.size(); i++) {
                stats.addValue(allCurrentIonValues.get(i));
            }

            while (stats.getN() < allAnnotations.size()) {
                stats.addValue(0);
            }

            double meanValue = stats.getMean();
            double standardDeviation = stats.getStandardDeviation();

            if (allAnnotations.size() > 1 && meanValue + standardDeviation > maxIntensity) {
                maxIntensity = meanValue + standardDeviation;
            }

            DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
            dataset.add(meanValue, standardDeviation, ion, ionNumber);

            if (allAnnotations.size() > 1) {
                setValueAt(dataset, ionNumber, getColumn(ion).getModelIndex());
            } else {
                setValueAt(meanValue, ionNumber, getColumn(ion).getModelIndex());
            }
        }

        // set the column renderers
        try {
            if (allAnnotations.size() > 1) {
                getColumn("a").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.A_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("a").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("a").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.A_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("a").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("b").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.B_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("b").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("b").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.B_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("b").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("b++").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.B_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("b++").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("b++").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.B_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("b++").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.H2O);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.B_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("b-H2O").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("b-H2O").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("b-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("b-H2O").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.H2O);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.B_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("b++-H2O").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("b++-H2O").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("b++-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("b++-H2O").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.NH3);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.B_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("b-NH3").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("b-NH3").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("b-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("b-NH3").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.NH3);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.B_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("b++-NH3").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("b++-NH3").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("b++-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("b++-NH3").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("c").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.C_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("c").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("c").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.C_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("c").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("y").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.Y_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("y").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("y").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.Y_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("y").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("y++").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.Y_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("y++").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("y++").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.Y_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("y++").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.H2O);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.Y_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("y-H2O").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("y-H2O").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("y-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("y-H2O").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.H2O);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.Y_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("y++-H2O").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("y++-H2O").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("y++-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("y++-H2O").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.NH3);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.Y_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("y-NH3").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("y-NH3").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("y-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("y-NH3").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            ArrayList<NeutralLoss> tempNeutralLosses = new ArrayList<NeutralLoss>();
            tempNeutralLosses.add(NeutralLoss.NH3);
            PeptideFragmentIon tempPeptideFragmentIon = new PeptideFragmentIon(PeptideFragmentIon.Y_ION, tempNeutralLosses);
            if (allAnnotations.size() > 1) {
                getColumn("y++-NH3").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("y++-NH3").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("y++-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(tempPeptideFragmentIon, false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("y++-NH3").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("x").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.X_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("x").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("x").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.X_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("x").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        try {
            if (allAnnotations.size() > 1) {
                getColumn("z").setCellRenderer(new JSparklinesErrorBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.Z_ION), false)));
                ((JSparklinesErrorBarChartTableCellRenderer) getColumn("z").getCellRenderer()).setMinimumChartValue(0);
            } else {
                getColumn("z").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity,
                        SpectrumPanel.determineFragmentIonColor(new PeptideFragmentIon(PeptideFragmentIon.Z_ION), false)));
                ((JSparklinesBarChartTableCellRenderer) getColumn("z").getCellRenderer()).setMinimumChartValue(0);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    /**
     * Helper method adding a fragment ion annotation to the total list.
     *
     * @param values the map to add the annotation to
     * @param key the key for the current annotation
     * @param peakIntensity the peak intensity
     */
    private void addValue(HashMap<String, ArrayList<Double>> values, String key, Double peakIntensity) {
        if (values.containsKey(key)) {
            values.get(key).add(peakIntensity);
        } else {
            ArrayList<Double> tempArray = new ArrayList<Double>();
            tempArray.add(peakIntensity);
            values.put(key, tempArray);
        }
    }
}
