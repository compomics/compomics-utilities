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
import org.jfree.chart.plot.PlotOrientation;

/**
 * Creates a fragment ion table highlighting the detected b and y fragment ions.
 *
 * @author Harald Barsnes
 */
public class FragmentIonTable extends JTable {

    /**
     * The list of currently selected fragment ion types.
     */
    private ArrayList<PeptideFragmentIonType> currentFragmentIonTypes;
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
     * Creates a fragment ion table highlighting the detected fragment ions.
     * All b and y ion types are included in the table.
     *
     * @param currentPeptide            the Peptide to show the table for
     * @param annotations               the spectrum annotations (from SpectrumAnnotator)
     * @param currentFragmentIonTypes   the list of currently selected fragment ion types
     * @param singleCharge              if true, singly charge ions are included in the table
     * @param twoCharges                if true, doubly charged ions are included in the table
     * @param showMzValues              if true the traditional ions table with the theoretical m/z 
     *                                  values is shown, false displays the novel intensity bar chart 
     *                                  version
     */
    public FragmentIonTable(
            Peptide currentPeptide, 
            SpectrumAnnotationMap annotations, 
            ArrayList<PeptideFragmentIonType> currentFragmentIonTypes,
            boolean singleCharge, boolean twoCharges, boolean showMzValues) {
        super();

        this.currentFragmentIonTypes = currentFragmentIonTypes;
        this.singleCharge = singleCharge;
        this.twoCharges = twoCharges;

        // set up table properties
        setUpTable();

        // get the peptide sequence
        String peptideSequence = currentPeptide.getSequence();

        // add the peptide sequence and indexes to the table
        for (int i = 0; i < peptideSequence.length(); i++) {
            ((DefaultTableModel) getModel()).addRow(new Object[]{(i + 1)});
        }

        for (int i = 0; i < peptideSequence.length(); i++) {
            setValueAt(peptideSequence.charAt(i), i, getColumn("AA").getModelIndex());
            setValueAt(peptideSequence.length() - i, i, getColumn("  ").getModelIndex());
        }

        // get all fragmentions for the peptide
        FragmentFactory fragmentFactory = FragmentFactory.getInstance();
        ArrayList<PeptideFragmentIon> fragmentIons = fragmentFactory.getFragmentIons(currentPeptide);

        if (showMzValues) {

            // add the theoretical masses to the table
            for (PeptideFragmentIon fragmentIon : fragmentIons) {

                double fragmentMzChargeOne = (fragmentIon.theoreticMass + 1 * Atom.H.mass) / 1;
                double fragmentMzChargeTwo = (fragmentIon.theoreticMass + 2 * Atom.H.mass) / 2;

                int fragmentNumber = fragmentIon.getNumber();

                if (currentFragmentIonTypes.contains(fragmentIon.getType())) {

                    if (fragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("a").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.AH2O_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("a-H2O").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.ANH3_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("a-NH3").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.BH2O_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b-H2O").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++-H2O").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.BNH3_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("b-NH3").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, fragmentNumber - 1, getColumn("b++-NH3").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, fragmentNumber - 1, getColumn("c").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.YH2O_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y-H2O").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++-H2O").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.YNH3_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("y-NH3").getModelIndex());
                        }
                        if (twoCharges) {
                            setValueAt(fragmentMzChargeTwo, peptideSequence.length() - fragmentNumber, getColumn("y++-NH3").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("x").getModelIndex());
                        }
                    } else if (fragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        if (singleCharge) {
                            setValueAt(fragmentMzChargeOne, peptideSequence.length() - fragmentNumber, getColumn("z").getModelIndex());
                        }
                    }
                }
            }

            // see which ions are detected in the spectrum
            Iterator<String> ionTypeIterator = annotations.getAnnotations().keySet().iterator();

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

                        if (currentFragmentIonTypes.contains(fragmentIon.getType())) {

                            if (fragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    aIonsSinglyCharged.add(fragmentNumber - 1);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    bIonsSinglyCharged.add(fragmentNumber - 1);
                                } else if (twoCharges) {
                                    bIonsDoublyCharged.add(fragmentNumber - 1);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.BH2O_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    bIonsH2OSinglyCharged.add(fragmentNumber - 1);
                                } else if (twoCharges) {
                                    bIonsH2ODoublyCharged.add(fragmentNumber - 1);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.BNH3_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    bIonsNH3SinglyCharged.add(fragmentNumber - 1);
                                } else if (twoCharges) {
                                    bIonsNH3DoublyCharged.add(fragmentNumber - 1);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    cIonsSinglyCharged.add(fragmentNumber - 1);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    yIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                } else if (twoCharges) {
                                    yIonsDoublyCharged.add(peptideSequence.length() - fragmentNumber);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.YH2O_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    yIonsH2OSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                } else if (twoCharges) {
                                    yIonsH2ODoublyCharged.add(peptideSequence.length() - fragmentNumber);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.YNH3_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    yIonsNH3SinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                } else if (twoCharges) {
                                    yIonsNH3DoublyCharged.add(peptideSequence.length() - fragmentNumber);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    xIonsSinglyCharged.add(peptideSequence.length() - fragmentNumber);
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
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

        } else {

            // see which ions are detected in the spectrum
            Iterator<String> ionTypeIterator = annotations.getAnnotations().keySet().iterator();

            double maxIntensity = 0.0;

            // add the sparklines
            while (ionTypeIterator.hasNext()) {
                String ionType = ionTypeIterator.next();

                HashMap<Integer, IonMatch> chargeMap = annotations.getAnnotations().get(ionType);
                Iterator<Integer> chargeIterator = chargeMap.keySet().iterator();

                while (chargeIterator.hasNext()) {
                    Integer currentCharge = chargeIterator.next();

                    if (currentCharge == 1 || currentCharge == 2) {
                        IonMatch ionMatch = chargeMap.get(currentCharge);

                        double peakIntensity = ionMatch.peak.intensity;

                        if (maxIntensity < peakIntensity) {
                            maxIntensity = peakIntensity;
                        }

                        PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                        int fragmentNumber = fragmentIon.getNumber();

                        if (currentFragmentIonTypes.contains(fragmentIon.getType())) {

                            if (fragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("a").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("b").getModelIndex());
                                } else if (twoCharges) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("b++").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.BH2O_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("b-H2O").getModelIndex());
                                } else if (twoCharges) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("b++-H2O").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.BNH3_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("b-NH3").getModelIndex());
                                } else if (twoCharges) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("b++-NH3").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, fragmentNumber - 1, getColumn("c").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("y").getModelIndex());
                                } else if (twoCharges) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("y++").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.YH2O_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("y-H2O").getModelIndex());
                                } else if (twoCharges) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("y++-H2O").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.YNH3_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("y-NH3").getModelIndex());
                                } else if (twoCharges) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("y++-NH3").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("x").getModelIndex());
                                }
                            } else if (fragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                                if (currentCharge == 1 && singleCharge) {
                                    setValueAt(peakIntensity, peptideSequence.length() - fragmentNumber, getColumn("z").getModelIndex());
                                }
                            }
                        }
                    }
                }
            }


            try {
                getColumn("a").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("a")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("b").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("b")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("b++").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("b++")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("b-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("b-H2O")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("b++-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("b++-H2O")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("b-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("b-NH3")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("b++-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("b++-NH3")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("c").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("c")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("y").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("y")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("y++").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("y++")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("y-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("y-H2O")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("y++-H2O").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("y++-H2O")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("y-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("y-NH3")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("y++-NH3").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("y++-NH3")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("x").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("x")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            try {
                getColumn("z").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, maxIntensity, SpectrumPanel.determineFragmentIonColor("z")));
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
    }
    
    protected JTableHeader createDefaultTableHeader() { 
        return new JTableHeader(columnModel) {        
            public String getToolTipText(MouseEvent e) { 
                String tip = null;                 
                java.awt.Point p = e.getPoint();       
                int index = columnModel.getColumnIndexAtX(p.x);                 
                int realIndex = columnModel.getColumn(index).getModelIndex();                 
                tip = (String) tooltips.get(realIndex);                 
                return tip;             
            }         
        };     
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

        Vector columnHeaders = new Vector();
        ArrayList<Class> tempColumnTypes = new ArrayList<Class>();
        tooltips = new ArrayList<String>();

        columnHeaders.add(" ");
        tempColumnTypes.add(java.lang.Integer.class);
        tooltips.add("a, b and c ion index");


        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.A_ION)) {
            columnHeaders.add("a");
            tempColumnTypes.add(java.lang.Double.class);
            tooltips.add("a-ion");
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.AH2O_ION)) {
            columnHeaders.add("a-H2O");
            tempColumnTypes.add(java.lang.Double.class);
            tooltips.add("a-ion with water loss");
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.ANH3_ION)) {
            columnHeaders.add("a-NH3");
            tempColumnTypes.add(java.lang.Double.class);
            tooltips.add("a-ion with ammonia loss");
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.B_ION)) {
            if (singleCharge) {
                columnHeaders.add("b");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("b-ion");
            }
            if (twoCharges) {
                columnHeaders.add("b++");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("b-ion doubly charged");
            }
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.BH2O_ION)) {
            if (singleCharge) {
                columnHeaders.add("b-H2O");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("b-ion with water loss");
            }
            if (twoCharges) {
                columnHeaders.add("b++-H2O");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("b-ion with water loss, doubly charged");
            }
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.BNH3_ION)) {
            if (singleCharge) {
                columnHeaders.add("b-NH3");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("b-ion with ammonia loss");
            }
            if (twoCharges) {
                columnHeaders.add("b++-NH3");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("b-ion with ammonia loss, doubly charged");
            }
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.C_ION)) {
            columnHeaders.add("c");
            tempColumnTypes.add(java.lang.Double.class);
            tooltips.add("c-ion");
        }

        columnHeaders.add("AA");
        tempColumnTypes.add(java.lang.String.class);
        tooltips.add("Amino acid sequence");

        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.Z_ION)) {
            columnHeaders.add("z");
            tempColumnTypes.add(java.lang.Double.class);
            tooltips.add("z-ion");
        }

        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.Y_ION)) {
            if (singleCharge) {
                columnHeaders.add("y");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("y-ion");
            }
            if (twoCharges) {
                columnHeaders.add("y++");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("y-ion, doubly charged");
            }
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.YH2O_ION)) {
            if (singleCharge) {
                columnHeaders.add("y-H2O");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("y-ion with water loss");
            }
            if (twoCharges) {
                columnHeaders.add("y++-H2O");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("y-ion with water loss, doubly charged");
            }
        }
        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.YNH3_ION)) {
            if (singleCharge) {
                columnHeaders.add("y-NH3");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("y-ion with ammonia loss");
            }
            if (twoCharges) {
                columnHeaders.add("y++-NH3");
                tempColumnTypes.add(java.lang.Double.class);
                tooltips.add("y-ion with ammonia loss, doubly charged");
            }
        }

        if (currentFragmentIonTypes.contains(PeptideFragmentIonType.X_ION)) {
            columnHeaders.add("x");
            tempColumnTypes.add(java.lang.Double.class);
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
}
