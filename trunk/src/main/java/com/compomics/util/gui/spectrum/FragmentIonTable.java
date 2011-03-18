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
 * Creates a fragment ion table highlighting the detected fragment ions.
 *
 * @author Harald Barsnes
 */
public class FragmentIonTable extends JTable {

    /**
     * Creates a fragment ion table highlighting the detected fragment ions.
     * Currently only singly and doubly charged ions are included in the table.
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
                        peptideSequence.charAt(i),
                        null,
                        null,
                        peptideSequence.length() - i
                    });
        }

        // get all singly and doubly charged  b and y fragmentions for the peptide
        FragmentFactory fragmentFactory = FragmentFactory.getInstance();
        ArrayList<PeptideFragmentIon> fragmentIons = fragmentFactory.getFragmentIons(currentPeptide);

        // add the theoretical masses to the table
        for (PeptideFragmentIon fragmentIon : fragmentIons) {

            // @TODO: also include charge 2 and neutral loss versions??

            if (fragmentIon.getType() == PeptideFragmentIonType.B_ION || fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                double fragmentMzChargeOne = (fragmentIon.theoreticMass + 1 * Atom.H.mass) / 1;
                double fragmentMzChargeTwo = (fragmentIon.theoreticMass + 2 * Atom.H.mass) / 2;

                int fragmentNumber = fragmentIon.getNumber();

                if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                    setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b+").getModelIndex());
                    setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++").getModelIndex());
                } else {
                    setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y+").getModelIndex());
                    setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++").getModelIndex());
                }
            }
        }

        // see which ions are detected in the spectrum
        Iterator<String> ionTypeIterator = annotations.getAnnotations().keySet().iterator();

        ArrayList<Integer> bIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> bIonsDoublyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsSinglyCharged = new ArrayList<Integer>();
        ArrayList<Integer> yIonsDoublyCharged = new ArrayList<Integer>();

        while (ionTypeIterator.hasNext()) {
            String ionType = ionTypeIterator.next();

            HashMap<Integer, IonMatch> chargeMap = annotations.getAnnotations().get(ionType);
            Iterator<Integer> chargeIterator = chargeMap.keySet().iterator();

            while (chargeIterator.hasNext()) {
                Integer currentCharge = chargeIterator.next();

                if (currentCharge == 1 || currentCharge == 2) {
                    IonMatch ionMatch = chargeMap.get(currentCharge);

                    PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                    if (fragmentIon.getType() == PeptideFragmentIonType.B_ION || fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        int fragmentNumber = fragmentIon.getNumber();

                        if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                            if (currentCharge == 1) {
                                bIonsSinglyCharged.add(fragmentNumber - 1);
                            } else {
                                bIonsDoublyCharged.add(fragmentNumber - 1);
                            }
                        } else {
                            if (currentCharge == 1) {
                                yIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                            } else {
                                yIonsDoublyCharged.add(peptideSequence.length() - fragmentNumber);
                            }
                        }
                    }
                }
            }
        }

        // highlight the detected fragment ions in the table
        getColumn("b+").setCellRenderer(new FragmentIonTableCellRenderer(bIonsSinglyCharged, Color.BLUE, Color.WHITE));
        getColumn("b++").setCellRenderer(new FragmentIonTableCellRenderer(bIonsDoublyCharged, Color.BLUE, Color.WHITE));
        getColumn("y+").setCellRenderer(new FragmentIonTableCellRenderer(yIonsSinglyCharged, Color.RED, Color.WHITE));
        getColumn("y++").setCellRenderer(new FragmentIonTableCellRenderer(yIonsDoublyCharged, Color.RED, Color.WHITE));
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
                    " ", "b+", "b++", "AA", "y++", "y+", "  "
                }) {

            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Integer.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        // set the max column widths
        getColumn(" ").setMaxWidth(40);
        getColumn("  ").setMaxWidth(40);
        getColumn("AA").setMaxWidth(40);

        // centrally align the columns in the fragment ions table
        getColumn(" ").setCellRenderer(new AlignedTableCellRenderer(SwingConstants.CENTER, Color.LIGHT_GRAY));
        getColumn("  ").setCellRenderer(new AlignedTableCellRenderer(SwingConstants.CENTER, Color.LIGHT_GRAY));
        getColumn("AA").setCellRenderer(new AlignedTableCellRenderer(SwingConstants.CENTER, Color.LIGHT_GRAY));
    }
}
