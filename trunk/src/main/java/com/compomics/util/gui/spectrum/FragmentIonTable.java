package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.FragmentFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import com.compomics.util.experiment.identification.SpectrumAnnotator.SpectrumAnnotationMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.gui.renderers.AlignedTableCellRenderer;
import com.compomics.util.gui.renderers.FragmentIonTableCellRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Creates a fragment ion table highlighting the detected b and y fragment ions.
 *
 * @author Harald Barsnes
 */
public class FragmentIonTable extends JTable {

    /**
     * Creates a fragment ion table highlighting the detected fragment ions.
     * All b and y ion types are included in the table.
     *
     * @param currentPeptide    the Peptide to show the table for
     * @param annotations       the spectrum annotations (from SpectrumAnnotator)
     */
    public FragmentIonTable(Peptide currentPeptide, SpectrumAnnotationMap annotations) {
        super();

        // set up table properties
        setUpTable();

        // get the peptide sequence
        String peptideSequence = currentPeptide.getSequence();

        // add the peptide sequence and numbers to the table
        for (int i = 0; i < peptideSequence.length(); i++) {
            ((DefaultTableModel) getModel()).addRow(new Object[]{
                        (i + 1),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        peptideSequence.charAt(i),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        peptideSequence.length() - i
                    });
        }

        // get all fragmentions for the peptide
        FragmentFactory fragmentFactory = FragmentFactory.getInstance();
        ArrayList<PeptideFragmentIon> fragmentIons = fragmentFactory.getFragmentIons(currentPeptide);

        // add the theoretical masses to the table
        for (PeptideFragmentIon fragmentIon : fragmentIons) {

            double fragmentMzChargeOne = (fragmentIon.theoreticMass + 1 * Atom.H.mass) / 1;
            double fragmentMzChargeTwo = (fragmentIon.theoreticMass + 2 * Atom.H.mass) / 2;

            int fragmentNumber = fragmentIon.getNumber();

            if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b").getModelIndex());
                setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++").getModelIndex());
            } else if (fragmentIon.getType() == PeptideFragmentIonType.BH2O_ION) {
                setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b-H20").getModelIndex());
                setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++-H20").getModelIndex());
            } else if (fragmentIon.getType() == PeptideFragmentIonType.BNH3_ION) {
                setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b-NH3").getModelIndex());
                setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++-NH3").getModelIndex());
            } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y").getModelIndex());
                setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++").getModelIndex());
            } else if (fragmentIon.getType() == PeptideFragmentIonType.YH2O_ION) {
                setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y-H20").getModelIndex());
                setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++-H20").getModelIndex());
            } else if (fragmentIon.getType() == PeptideFragmentIonType.YNH3_ION) {
                setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y-NH3").getModelIndex());
                setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++-NH3").getModelIndex());
            }
        }

        // see which ions are detected in the spectrum
        Iterator<String> ionTypeIterator = annotations.getAnnotations().keySet().iterator();

        ArrayList<Integer> bIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsDoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsH2OSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsH2ODoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsNH3SinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsNH3DoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsDoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsH2OSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsH2ODoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsNH3SinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsNH3DoublyCharged = new ArrayList<Integer>();

        // highlight the detected ions
        while (ionTypeIterator.hasNext()) {
            String ionType = ionTypeIterator.next();

            HashMap<Integer, IonMatch> chargeMap = annotations.getAnnotations().get(ionType);
            Iterator<Integer> chargeIterator = chargeMap.keySet().iterator();

            while (chargeIterator.hasNext()) {
                Integer currentCharge = chargeIterator.next();

                if (currentCharge == 1 || currentCharge == 2) {
                    IonMatch ionMatch = chargeMap.get(currentCharge);

                    PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                    int fragmentNumber = fragmentIon.getNumber();

                    if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        if (currentCharge == 1) {
                            bIonsSinglyCharged.add(fragmentNumber - 1);
                        } else {
                            bIonsDoublyCharged.add(fragmentNumber - 1);
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.BH2O_ION) {
                        if (currentCharge == 1) {
                            bIonsH2OSinglyCharged.add(fragmentNumber - 1);
                        } else {
                            bIonsH2ODoublyCharged.add(fragmentNumber - 1);
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.BNH3_ION) {
                        if (currentCharge == 1) {
                            bIonsNH3SinglyCharged.add(fragmentNumber - 1);
                        } else {
                            bIonsNH3DoublyCharged.add(fragmentNumber - 1);
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        if (currentCharge == 1) {
                            yIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                        } else {
                            yIonsDoublyCharged.add(peptideSequence.length() - fragmentNumber);
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.YH2O_ION) {
                        if (currentCharge == 1) {
                            yIonsH2OSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                        } else {
                            yIonsH2ODoublyCharged.add(peptideSequence.length() - fragmentNumber);
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.YNH3_ION) {
                        if (currentCharge == 1) {
                            yIonsNH3SinglyCharged.add(peptideSequence.length() - fragmentNumber);
                        } else {
                            yIonsNH3DoublyCharged.add(peptideSequence.length() - fragmentNumber);
                        }
                    }
                }
            }
        }

        // highlight the detected fragment ions in the table
        getColumn("b").setCellRenderer(new FragmentIonTableCellRenderer(bIonsSinglyCharged, Color.BLUE, Color.WHITE));
        getColumn("b++").setCellRenderer(new FragmentIonTableCellRenderer(bIonsDoublyCharged, Color.BLUE, Color.WHITE));
        getColumn("b-H20").setCellRenderer(new FragmentIonTableCellRenderer(bIonsH2OSinglyCharged, Color.BLUE, Color.WHITE));
        getColumn("b++-H20").setCellRenderer(new FragmentIonTableCellRenderer(bIonsH2ODoublyCharged, Color.BLUE, Color.WHITE));
        getColumn("b-NH3").setCellRenderer(new FragmentIonTableCellRenderer(bIonsNH3SinglyCharged, Color.BLUE, Color.WHITE));
        getColumn("b++-NH3").setCellRenderer(new FragmentIonTableCellRenderer(bIonsNH3DoublyCharged, Color.BLUE, Color.WHITE));
        getColumn("y").setCellRenderer(new FragmentIonTableCellRenderer(yIonsSinglyCharged, Color.RED, Color.WHITE));
        getColumn("y++").setCellRenderer(new FragmentIonTableCellRenderer(yIonsDoublyCharged, Color.RED, Color.WHITE));
        getColumn("y-H20").setCellRenderer(new FragmentIonTableCellRenderer(bIonsH2OSinglyCharged, Color.RED, Color.WHITE));
        getColumn("y++-H20").setCellRenderer(new FragmentIonTableCellRenderer(bIonsH2ODoublyCharged, Color.RED, Color.WHITE));
        getColumn("y-NH3").setCellRenderer(new FragmentIonTableCellRenderer(bIonsNH3SinglyCharged, Color.RED, Color.WHITE));
        getColumn("y++-NH3").setCellRenderer(new FragmentIonTableCellRenderer(bIonsNH3DoublyCharged, Color.RED, Color.WHITE));
    }

    /**
     * Set up the table properties.
     */
    private void setUpTable() {
        // disallow column reordering
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setReorderingAllowed(false);

        // centrally align the column headers in the fragment ions table
        TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
        JLabel label = (JLabel) renderer;
        label.setHorizontalAlignment(JLabel.CENTER);

        // set the table model
        setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    " ", "b", "b++", "b-H20", "b++-H20", "b-NH3", "b++-NH3", "AA", "y", "y++", "y-H20", "y++-H20", "y-NH3", "y++-NH3", "  "
                }) {

            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Integer.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
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
}
