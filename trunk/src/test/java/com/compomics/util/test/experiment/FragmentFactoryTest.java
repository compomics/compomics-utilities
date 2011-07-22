package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.FragmentFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class compares the results of peptide fragment mass prediction with results from protein prospector.
 *
 * @author Marc
 */
public class FragmentFactoryTest extends TestCase {

    private FragmentFactory fragmentFactory = FragmentFactory.getInstance();
    private double tolerance = 0.01;

    public void testFragmentation() {

        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        Peptide peptide = new Peptide(sequence, 0.0, new ArrayList<Protein>(), new ArrayList<ModificationMatch>());

        HashMap<NeutralLoss, Integer> neutralLosses = new HashMap<NeutralLoss, Integer>();
        neutralLosses.put(NeutralLoss.H2O, 3);
        neutralLosses.put(NeutralLoss.NH3, 9);

        ArrayList<PeptideFragmentIon> fragments = fragmentFactory.getFragmentIons(peptide, neutralLosses);
        
        for (PeptideFragmentIon peptideFragmentIon : fragments) {
            if (peptideFragmentIon.getNeutralLosses().isEmpty()) {
                if (peptideFragmentIon.getNumber() == 1) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 166.0624) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 208.0604) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 182.0812) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 2) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 147.0587) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 175.0536) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 192.0801) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 394.1397) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 368.1605) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 352.1418) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 3) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 262.0856) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 290.0805) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 307.1071) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 493.2085) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 467.2289) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 451.2102) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 4) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 391.1282) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 419.1231) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 436.1497) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 594.2558) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 568.2766) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 552.2579) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 5) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 538.1966) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 566.1915) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 583.2181) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 681.2879) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 655.3086) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 639.2899) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 6) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 595.2181) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 623.2130) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 640.2395) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 837.3890) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 811.4097) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 795.3910) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 7) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 732.2770) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 760.2719) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 777.2984) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 965.4476) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 939.4683) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 923.4496) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 8) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 845.3610) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 873.3560) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 890.3825) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1062.5003) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1036.5211) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 9) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 973.4560) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1001.4509) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1018.4775) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1176.5432) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1150.5640) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1134.5453) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 10) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1086.5401) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1114.5350) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.C_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1131.5615) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.X_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1307.5837) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1281.6045) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1265.5857) < tolerance);
                    }
                } else {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.MH_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 2395.1322) < tolerance);
                    }
                }
            } else if (peptideFragmentIon.getNeutralLosses().size() == 1
                    && peptideFragmentIon.getNeutralLosses().get(0).isSameAs(NeutralLoss.H2O)) {
                if (peptideFragmentIon.getNumber() == 3) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 272.0700) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 4) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 401.1125) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 550.2660) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 5) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 548.1810) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 637.2980) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 6) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 605.2024) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 793.3991) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 7) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 742.2613) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 921.4517) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 8) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 855.3454) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1018.5105) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 9) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 983.4404) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1132.5534) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 10) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1096.5244) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1263.5939) < tolerance);
                    }
                } else {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.MH_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 2377.1216) < tolerance);
                    }
                }
            } else if (peptideFragmentIon.getNeutralLosses().size() == 1
                    && peptideFragmentIon.getNeutralLosses().get(0).isSameAs(NeutralLoss.NH3)) {
                if (peptideFragmentIon.getNumber() == 6) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 794.3832) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 7) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 922.4417) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 8) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1019.4945) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 9) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 956.4295) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 984.4244) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1133.5374) < tolerance);
                    }
                } else if (peptideFragmentIon.getNumber() == 10) {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.A_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1069.5135) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1097.5084) < tolerance);
                    } else if (peptideFragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 1264.5779) < tolerance);
                    }
                } else {
                    if (peptideFragmentIon.getType() == PeptideFragmentIonType.MH_ION) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.theoreticMass + Atom.H.mass - 2378.1056) < tolerance);
                    }
                }
            }
        }
    }
}
