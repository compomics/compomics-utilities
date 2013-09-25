package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class compares the results of peptide fragment mass prediction with
 * results from protein prospector.
 *
 * @author Marc
 */
public class FragmentFactoryTest extends TestCase {

    private IonFactory fragmentFactory = IonFactory.getInstance();
    private double tolerance = 0.01;

    public void testFragmentation() {

        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        Peptide peptide = new Peptide(sequence, new ArrayList<ModificationMatch>());

        HashMap<NeutralLoss, Integer> neutralLosses = new HashMap<NeutralLoss, Integer>();
        neutralLosses.put(NeutralLoss.H2O, 3);
        neutralLosses.put(NeutralLoss.NH3, 9);
        double protonMass = ElementaryIon.proton.getTheoreticMass();

        ArrayList<Ion> fragments = fragmentFactory.getFragmentIons(peptide);

        for (Ion ion : fragments) {
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                if (peptideFragmentIon.getNeutralLosses().isEmpty()) {
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

                    } else if (peptideFragmentIon.getNeutralLosses().size() == 1
                            && peptideFragmentIon.getNeutralLosses().get(0).isSameAs(NeutralLoss.H2O)) {
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
                    } else if (peptideFragmentIon.getNeutralLosses().size() == 1
                            && peptideFragmentIon.getNeutralLosses().get(0).isSameAs(NeutralLoss.NH3)) {
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
                } else if (ion.getType() == Ion.IonType.PRECURSOR_ION) {
                    if (ion.getNeutralLosses().isEmpty()) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.getTheoreticMass() + 2 * protonMass - 2395.1322) < tolerance);

                    } else if (peptideFragmentIon.getNeutralLosses().size() == 1
                            && peptideFragmentIon.getNeutralLosses().get(0).isSameAs(NeutralLoss.H2O)) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.getTheoreticMass() + 2 * protonMass - 2377.1216) < tolerance);
                    } else if (peptideFragmentIon.getNeutralLosses().size() == 1
                            && peptideFragmentIon.getNeutralLosses().get(0).isSameAs(NeutralLoss.NH3)) {
                        Assert.assertTrue(
                                Math.abs(peptideFragmentIon.getTheoreticMass() + 2 * protonMass - 2378.1056) < tolerance);
                    }
                }
            }
        }
    }
}