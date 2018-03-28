package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.IonFactory;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.impl.TagFragmentIon;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class compares the results of peptide fragment mass prediction with
 * results from protein prospector.
 *
 * @author Marc Vaudel
 */
public class FragmentFactoryTest extends TestCase {

    /**
     * The fragment factory.
     */
    private final IonFactory fragmentFactory = IonFactory.getInstance();
    /**
     * The mass tolerance.
     */
    private final double tolerance = 0.01;

    /**
     * Tests the in silico fragmentation of a peptide.
     */
    public void testPeptideFragmentation() {

        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        
        Peptide peptide = new Peptide(sequence);
        
        ModificationParameters modificationParameters = new ModificationParameters();
        SequenceProvider sequenceProvider = null;
        SequenceMatchingParameters modificationMatchingParameters = null;

        HashMap<NeutralLoss, Integer> neutralLosses = new HashMap<>();
        neutralLosses.put(NeutralLoss.H2O, 3);
        neutralLosses.put(NeutralLoss.NH3, 9);
        double protonMass = ElementaryIon.proton.getTheoreticMass();

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> ions = fragmentFactory.getFragmentIons(peptide, modificationParameters, sequenceProvider, modificationMatchingParameters);
        HashMap<Integer, ArrayList<Ion>> fragmentIons = ions.get(Ion.IonType.PEPTIDE_FRAGMENT_ION.index);

        for (Integer subType : fragmentIons.keySet()) {
            for (Ion ion : fragmentIons.get(subType)) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                if (!peptideFragmentIon.hasNeutralLosses()) {
                    if (peptideFragmentIon.getNumber() == 1) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 166.0624) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 208.0604) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 182.0812) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 2) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 147.0587) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 175.0536) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 192.0801) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 394.1397) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 368.1605) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 352.1418) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 3) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 262.0856) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 290.0805) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 307.1071) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 493.2085) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 467.2289) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 451.2102) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 4) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 391.1282) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 419.1231) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 436.1497) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 594.2558) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 568.2766) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 552.2579) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 5) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 538.1966) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 566.1915) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 583.2181) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 681.2879) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 655.3086) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 639.2899) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 6) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 595.2181) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 623.2130) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 640.2395) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 837.3890) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 811.4097) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 795.3910) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 7) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 732.2770) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 760.2719) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 777.2984) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 965.4476) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 939.4683) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 923.4496) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 8) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 845.3610) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 873.3560) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 890.3825) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1062.5003) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1036.5211) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 9) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 973.4560) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1001.4509) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1018.4775) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1176.5432) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1150.5640) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1134.5453) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 10) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1086.5401) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1114.5350) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1131.5615) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1307.5837) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1281.6045) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1265.5857) < tolerance);
                        }
                    }
                } else if (peptideFragmentIon.getNeutralLosses().length == 1
                        && peptideFragmentIon.getNeutralLosses()[0].isSameAs(NeutralLoss.H2O)) {
                    if (peptideFragmentIon.getNumber() == 3) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 272.0700) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 4) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 401.1125) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 550.2660) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 5) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 548.1810) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 637.2980) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 6) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 605.2024) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 793.3991) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 7) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 742.2613) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 921.4517) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 8) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 855.3454) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1018.5105) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 9) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 983.4404) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1132.5534) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 10) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1096.5244) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1263.5939) < tolerance);
                        }
                    }
                } else if (peptideFragmentIon.getNeutralLosses().length == 1
                        && peptideFragmentIon.getNeutralLosses()[0].isSameAs(NeutralLoss.NH3)) {
                    if (peptideFragmentIon.getNumber() == 6) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 794.3832) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 7) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 922.4417) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 8) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1019.4945) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 9) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 956.4295) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 984.4244) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1133.5374) < tolerance);
                        }
                    } else if (peptideFragmentIon.getNumber() == 10) {
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1069.5135) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1097.5084) < tolerance);
                        } else if (peptideFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(peptideFragmentIon.getTheoreticMass() + protonMass - 1264.5779) < tolerance);
                        }
                    }
                }
            }
        }
        HashMap<Integer, ArrayList<Ion>> precursorIons = ions.get(Ion.IonType.PRECURSOR_ION.index);
        for (Integer subType : precursorIons.keySet()) {
            for (Ion ion : precursorIons.get(subType)) {
                if (!ion.hasNeutralLosses()) {
                    Assert.assertTrue(
                            Math.abs(ion.getTheoreticMass() + protonMass - 2395.1322) < tolerance);

                } else if (ion.getNeutralLosses().length == 1
                        && ion.getNeutralLosses()[0].isSameAs(NeutralLoss.H2O)) {
                    Assert.assertTrue(
                            Math.abs(ion.getTheoreticMass() + protonMass - 2377.1216) < tolerance);
                } else if (ion.getNeutralLosses().length == 1
                        && ion.getNeutralLosses()[0].isSameAs(NeutralLoss.NH3)) {
                    Assert.assertTrue(
                            Math.abs(ion.getTheoreticMass() + protonMass - 2378.1056) < tolerance);
                }
            }
        }
    }

    /**
     * Tests the in silico fragmentation of a tag.
     */
    public void testTagFragmentation() {

        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        Tag tag = new Tag(0, new AminoAcidSequence(sequence), 0);
        
        ModificationParameters modificationParameters = new ModificationParameters();
        SequenceProvider sequenceProvider = null;
        SequenceMatchingParameters modificationMatchingParameters = null;

        HashMap<NeutralLoss, Integer> neutralLosses = new HashMap<>();
        neutralLosses.put(NeutralLoss.H2O, 3);
        neutralLosses.put(NeutralLoss.NH3, 9);
        double protonMass = ElementaryIon.proton.getTheoreticMass();

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> ions = fragmentFactory.getFragmentIons(tag, modificationParameters, modificationMatchingParameters);
        HashMap<Integer, ArrayList<Ion>> fragmentIons = ions.get(Ion.IonType.TAG_FRAGMENT_ION.index);

        // add the theoretical masses to the table
        for (Integer subType : fragmentIons.keySet()) {
            for (Ion ion : fragmentIons.get(subType)) {
                TagFragmentIon tagFragmentIon = (TagFragmentIon) ion;
                if (!tagFragmentIon.hasNeutralLosses()) {
                    if (tagFragmentIon.getNumber() == 1) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 166.0624) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 208.0604) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 182.0812) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 2) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 147.0587) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 175.0536) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 192.0801) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 394.1397) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 368.1605) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 352.1418) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 3) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 262.0856) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 290.0805) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 307.1071) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 493.2085) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 467.2289) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 451.2102) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 4) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 391.1282) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 419.1231) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 436.1497) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 594.2558) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 568.2766) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 552.2579) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 5) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 538.1966) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 566.1915) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 583.2181) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 681.2879) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 655.3086) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 639.2899) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 6) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 595.2181) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 623.2130) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 640.2395) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 837.3890) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 811.4097) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 795.3910) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 7) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 732.2770) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 760.2719) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 777.2984) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 965.4476) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 939.4683) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 923.4496) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 8) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 845.3610) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 873.3560) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 890.3825) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1062.5003) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1036.5211) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 9) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 973.4560) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1001.4509) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1018.4775) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1176.5432) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1150.5640) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1134.5453) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 10) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1086.5401) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1114.5350) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1131.5615) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.X_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1307.5837) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1281.6045) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Z_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1265.5857) < tolerance);
                        }
                    }

                } else if (tagFragmentIon.getNeutralLosses().length == 1
                        && tagFragmentIon.getNeutralLosses()[0].isSameAs(NeutralLoss.H2O)) {
                    if (tagFragmentIon.getNumber() == 3) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 272.0700) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 4) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 401.1125) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 550.2660) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 5) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 548.1810) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 637.2980) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 6) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 605.2024) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 793.3991) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 7) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 742.2613) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 921.4517) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 8) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 855.3454) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1018.5105) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 9) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 983.4404) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1132.5534) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 10) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1096.5244) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1263.5939) < tolerance);
                        }
                    }
                } else if (tagFragmentIon.getNeutralLosses().length == 1
                        && tagFragmentIon.getNeutralLosses()[0].isSameAs(NeutralLoss.NH3)) {
                    if (tagFragmentIon.getNumber() == 6) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 794.3832) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 7) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 922.4417) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 8) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1019.4945) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 9) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 956.4295) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 984.4244) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1133.5374) < tolerance);
                        }
                    } else if (tagFragmentIon.getNumber() == 10) {
                        if (tagFragmentIon.getSubType() == PeptideFragmentIon.A_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1069.5135) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.B_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1097.5084) < tolerance);
                        } else if (tagFragmentIon.getSubType() == PeptideFragmentIon.Y_ION) {
                            Assert.assertTrue(
                                    Math.abs(tagFragmentIon.getTheoreticMass() + protonMass - 1264.5779) < tolerance);
                        }
                    }
                }
            }
        }
        HashMap<Integer, ArrayList<Ion>> precursorIons = ions.get(Ion.IonType.PRECURSOR_ION.index);
        for (Integer subType : precursorIons.keySet()) {
            for (Ion ion : precursorIons.get(subType)) {
                if (!ion.hasNeutralLosses()) {
                    Assert.assertTrue(
                            Math.abs(ion.getTheoreticMass() + protonMass - 2395.1322) < tolerance);

                } else if (ion.getNeutralLosses().length == 1
                        && ion.getNeutralLosses()[0].isSameAs(NeutralLoss.H2O)) {
                    Assert.assertTrue(
                            Math.abs(ion.getTheoreticMass() + protonMass - 2377.1216) < tolerance);
                } else if (ion.getNeutralLosses().length == 1
                        && ion.getNeutralLosses()[0].isSameAs(NeutralLoss.NH3)) {
                    Assert.assertTrue(
                            Math.abs(ion.getTheoreticMass() + protonMass - 2378.1056) < tolerance);
                }
            }
        }
    }
}
