package com.compomics.util.pride;

import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.preferences.ModificationProfile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Map linking user modification names to Unimod CV terms.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PtmToPrideMap implements Serializable {

    /**
     * Serialization number for backward compatibility.
     */
        static final long serialVersionUID = 6140067846035857282L;
    /**
     * The name of the file to save.
     */
    public final static String fileName = "modMapUniMod.cus";
    /**
     * Map of the CV terms indexed by modification user name.
     */
    private HashMap<String, CvTerm> modToCvMap = new HashMap<String, CvTerm>();

    /**
     * Constructor.
     */
    public PtmToPrideMap() {
    }

    /**
     * Returns the CV term corresponding to the given PTM name. Null if not
     * found.
     *
     * @param ptmName the PTM name
     * @return the corresponding CV term
     */
    public CvTerm getCVTerm(String ptmName) {
        return modToCvMap.get(ptmName);
    }

    /**
     * Puts a new mapping in the map. If the modification name is already loaded
     * it will be silently overwritten.
     *
     * @param modName the modification name
     * @param cvTerm the corresponding cvTerm
     */
    public void putCVTerm(String modName, CvTerm cvTerm) {
        modToCvMap.put(modName, cvTerm);
    }

    /**
     * Returns the default cvTerm of a modification when it exists.
     *
     * @param ptmName the PTM name according to the XML file, null if no mapping
     * is found
     * @return a default CV term
     */
    public static CvTerm getDefaultCVTerm(String ptmName) {

        if (ptmName.equalsIgnoreCase("methylation of K")
                || ptmName.equalsIgnoreCase("methylation of protein n-term")
                || ptmName.equalsIgnoreCase("methylation of Q")
                || ptmName.equalsIgnoreCase("methylation of D")
                || ptmName.equalsIgnoreCase("methylation of E")
                || ptmName.equalsIgnoreCase("methylation of peptide c-term")
                || ptmName.equalsIgnoreCase("methyl ester of peptide c-term (duplicate of 18)")
                || ptmName.equalsIgnoreCase("methyl ester of D")
                || ptmName.equalsIgnoreCase("methyl ester of E (duplicate of 17)")
                || ptmName.equalsIgnoreCase("methyl ester of S")
                || ptmName.equalsIgnoreCase("methyl ester of Y")
                || ptmName.equalsIgnoreCase("methyl C")
                || ptmName.equalsIgnoreCase("methyl H")
                || ptmName.equalsIgnoreCase("methyl N")
                || ptmName.equalsIgnoreCase("methylation of peptide n-term")
                || ptmName.equalsIgnoreCase("methyl R")) {
            return new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", "14.015650");
        } else if (ptmName.equalsIgnoreCase("oxidation of M")
                || ptmName.equalsIgnoreCase("hydroxylation of D")
                || ptmName.equalsIgnoreCase("hydroxylation of K")
                || ptmName.equalsIgnoreCase("hydroxylation of N")
                || ptmName.equalsIgnoreCase("hydroxylation of P")
                || ptmName.equalsIgnoreCase("hydroxylation of F")
                || ptmName.equalsIgnoreCase("hydroxylation of Y")
                || ptmName.equalsIgnoreCase("oxidation of H")
                || ptmName.equalsIgnoreCase("oxidation of W")
                || ptmName.equalsIgnoreCase("oxidation of C")
                || ptmName.equalsIgnoreCase("oxidation of Y (duplicate of 64)")) {
            return new CvTerm("UNIMOD", "UNIMOD:35", "Oxidation", "15.994915");
        } else if (ptmName.equalsIgnoreCase("carboxymethyl C")) {
            return new CvTerm("UNIMOD", "UNIMOD:6", "Carboxymethyl", "58.005479");
        } else if (ptmName.equalsIgnoreCase("carbamidomethyl C")
                || ptmName.equalsIgnoreCase("carboxyamidomethylation of K")
                || ptmName.equalsIgnoreCase("carboxyamidomethylation of H")
                || ptmName.equalsIgnoreCase("carboxyamidomethylation of D")
                || ptmName.equalsIgnoreCase("carboxyamidomethylation of E")) {
            return new CvTerm("UNIMOD", "UNIMOD:4", "Carbamidomethyl", "57.021464");
        } else if (ptmName.equalsIgnoreCase("deamidation of N and Q")
                || ptmName.equalsIgnoreCase("citrullination of R")
                || ptmName.equalsIgnoreCase("deamidation of N")) {
            return new CvTerm("UNIMOD", "UNIMOD:7", "Deamidated", "0.984016");
        } else if (ptmName.equalsIgnoreCase("propionamide C")) {
            return new CvTerm("UNIMOD", "UNIMOD:24", "Propionamide", "71.037114");
        } else if (ptmName.equalsIgnoreCase("phosphorylation of S")
                || ptmName.equalsIgnoreCase("phosphorylation of T")
                || ptmName.equalsIgnoreCase("phosphorylation of Y")
                || ptmName.equalsIgnoreCase("phosphorylation with neutral loss on C")
                || ptmName.equalsIgnoreCase("phosphorylation with neutral loss on D")
                || ptmName.equalsIgnoreCase("phosphorylation with neutral loss on H")
                || ptmName.equalsIgnoreCase("phosphorylation with neutral loss on S")
                || ptmName.equalsIgnoreCase("phosphorylation with neutral loss on T")
                || ptmName.equalsIgnoreCase("phosphorylation of S with ETD loss")
                || ptmName.equalsIgnoreCase("phosphorylation of T with ETD loss")
                || ptmName.equalsIgnoreCase("phosphorylation of H")
                || ptmName.equalsIgnoreCase("phosphorylation of C")
                || ptmName.equalsIgnoreCase("phosphorylation of D")
                || ptmName.equalsIgnoreCase("phosphorylation of K")
                || ptmName.equalsIgnoreCase("phosphorylation of Q")
                || ptmName.equalsIgnoreCase("phosphorylation of R")) {
            return new CvTerm("UNIMOD", "UNIMOD:21", "Phospho", "79.966331");
        } else if (ptmName.equalsIgnoreCase("M cleavage from protein n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:765", "Met-loss", "-131.040485");
        } else if (ptmName.equalsIgnoreCase("acetylation of protein n-term")
                || ptmName.equalsIgnoreCase("acetylation of K")) {
            return new CvTerm("UNIMOD", "UNIMOD:1", "Acetyl", "42.010565");
        } else if (ptmName.equalsIgnoreCase("tri-methylation of protein n-term")
                || ptmName.equalsIgnoreCase("tri-methylation of K")
                || ptmName.equalsIgnoreCase("tri-methylation of R")) {
            return new CvTerm("UNIMOD", "UNIMOD:37", "Trimethyl", "42.046950");
        } else if (ptmName.equalsIgnoreCase("beta methythiolation of D")) {
            return new CvTerm("UNIMOD", "UNIMOD:39", "Methylthio", "45.987721");
        } else if (ptmName.equalsIgnoreCase("tri-deuteromethylation of D")
                || ptmName.equalsIgnoreCase("tri-deuteromethylation of E")
                || ptmName.equalsIgnoreCase("tri-deuteromethylation of peptide c-term")
                || ptmName.equalsIgnoreCase("beta-methylthiolation of D (duplicate of 13)")) {
            return new CvTerm("UNIMOD", "UNIMOD:298", "Methyl:2H(3)", "17.034480");
        } else if (ptmName.equalsIgnoreCase("n-formyl met addition")) {
            return new CvTerm("UNIMOD", "UNIMOD:107", "FormylMet", "159.035399");
        } else if (ptmName.equalsIgnoreCase("2-amino-3-oxo-butanoic acid T")) {
            return new CvTerm("UNIMOD", "UNIMOD:401", "Didehydro", "-2.015650");
        } else if (ptmName.equalsIgnoreCase("amidation of peptide c-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:2", "Amidated", "-0.984016");
        } else if (ptmName.equalsIgnoreCase("carbamylation of K")
                || ptmName.equalsIgnoreCase("carbamylation of n-term peptide")) {
            return new CvTerm("UNIMOD", "UNIMOD:5", "Carbamyl", "43.005814");
        } else if (ptmName.equalsIgnoreCase("oxidation of C to cysteic acid")) {
            return new CvTerm("UNIMOD", "UNIMOD:345", "Trioxidation", "47.984744");
        } else if (ptmName.equalsIgnoreCase("di-iodination of Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:130", "Diiodo", "251.793296");
        } else if (ptmName.equalsIgnoreCase("di-methylation of K")
                || ptmName.equalsIgnoreCase("di-methylation of R")
                || ptmName.equalsIgnoreCase("di-methylation of peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:36", "Dimethyl", "28.031300");
        } else if (ptmName.equalsIgnoreCase("dimethyl 2d k")
                || ptmName.equalsIgnoreCase("dimethyl 2d n-terminus")) {
            return new CvTerm("UNIMOD", "UNIMOD:199", "Dimethyl:2H(4)", "32.056407");
        } else if (ptmName.equalsIgnoreCase("gtp desthiobiotinc12")) {
            return new CvTerm("UNIMOD", "UNIMOD:1031", "Biotin:Thermo-88310", "196.121178");
        } else if (ptmName.equalsIgnoreCase("oxidation of F to dihydroxyphenylalanine")
                || ptmName.equalsIgnoreCase("oxidation of W to formylkynurenin")
                || ptmName.equalsIgnoreCase("sulphone of M")
                || ptmName.equalsIgnoreCase("oxidation of C to sulfinic acid")) {
            return new CvTerm("UNIMOD", "UNIMOD:425", "Dioxidation", "31.989829");
        } else if (ptmName.equalsIgnoreCase("gammathiopropionylation of K")
                || ptmName.equalsIgnoreCase("gammathiopropionylation of peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:126", "Thioacyl", "87.998285");
        } else if (ptmName.equalsIgnoreCase("farnesylation of C")) {
            return new CvTerm("UNIMOD", "UNIMOD:44", "Farnesyl", "204.187801");
        } else if (ptmName.equalsIgnoreCase("formylation of K")
                || ptmName.equalsIgnoreCase("formylation of peptide n-term")
                || ptmName.equalsIgnoreCase("formylation of protein n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:122", "Formyl", "27.994915");
        } else if (ptmName.equalsIgnoreCase("fluorophenylalanine")) {
            return new CvTerm("UNIMOD", "UNIMOD:127", "Fluoro", "17.990578");
        } else if (ptmName.equalsIgnoreCase("beta-carboxylation of D")
                || ptmName.equalsIgnoreCase("gamma-carboxylation of E")) {
            return new CvTerm("UNIMOD", "UNIMOD:299", "Carboxy", "43.989829");
        } else if (ptmName.equalsIgnoreCase("geranyl-geranyl")) {
            return new CvTerm("UNIMOD", "UNIMOD:48", "GeranylGeranyl", "272.250401");
        } else if (ptmName.equalsIgnoreCase("glucuronylation of protein n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:54", "Glucuronyl", "176.032088");
        } else if (ptmName.equalsIgnoreCase("glutathione disulfide")) {
            return new CvTerm("UNIMOD", "UNIMOD:55", "Glutathione", "305.068156");
        } else if (ptmName.equalsIgnoreCase("ubiquitinylation residue")) {
            return new CvTerm("UNIMOD", "UNIMOD:121", "GlyGly", "114.042927");
        } else if (ptmName.equalsIgnoreCase("guanidination of K")) {
            return new CvTerm("UNIMOD", "UNIMOD:52", "Guanidinyl", "42.021798");
        } else if (ptmName.equalsIgnoreCase("oxidation of H to N")) {
            return new CvTerm("UNIMOD", "UNIMOD:348", "His->Asn", "-23.015984");
        } else if (ptmName.equalsIgnoreCase("oxidation of H to D")) {
            return new CvTerm("UNIMOD", "UNIMOD:349", "His->Asp", "-22.031969");
        } else if (ptmName.equalsIgnoreCase("homoserine")) {
            return new CvTerm("UNIMOD", "UNIMOD:10", "Met->Hse", "-29.992806");
        } else if (ptmName.equalsIgnoreCase("homoserine lactone")) {
            return new CvTerm("UNIMOD", "UNIMOD:11", "Met->Hsl", "-48.003371");
        } else if (ptmName.equalsIgnoreCase("oxidation of W to hydroxykynurenin")) {
            return new CvTerm("UNIMOD", "UNIMOD:350", "Trp>Hydroxykynurenin", "19.989829");
        } else if (ptmName.equalsIgnoreCase("iodination of Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:129", "Iodo", "125.896648");
        } else if (ptmName.equalsIgnoreCase("oxidation of W to kynurenin")) {
            return new CvTerm("UNIMOD", "UNIMOD:351", "Trp->Kynurenin", "3.994915");
        } else if (ptmName.equalsIgnoreCase("lipoyl K")) {
            return new CvTerm("UNIMOD", "UNIMOD:42", "Lipoyl", "188.032956");
        } else if (ptmName.equalsIgnoreCase("myristoleylation of G")) {
            return new CvTerm("UNIMOD", "UNIMOD:134", "Myristoleyl", "208.182715");
        } else if (ptmName.equalsIgnoreCase("myristoyl-4H of G")) {
            return new CvTerm("UNIMOD", "UNIMOD:135", "Myristoyl+Delta:H(-4)", "206.167065");
        } else if (ptmName.equalsIgnoreCase("myristoylation of peptide n-term G")
                || ptmName.equalsIgnoreCase("myristoylation of K")) {
            return new CvTerm("UNIMOD", "UNIMOD:45", "Myristoyl", "210.198366");
        } else if (ptmName.equalsIgnoreCase("NEM C")) {
            return new CvTerm("UNIMOD", "UNIMOD:108", "Nethylmaleimide", "125.047679");
        } else if (ptmName.equalsIgnoreCase("NIPCAM")) {
            return new CvTerm("UNIMOD", "UNIMOD:17", "NIPCAM", "99.068414");
        } else if (ptmName.equalsIgnoreCase("oxidation of W to nitro")
                || ptmName.equalsIgnoreCase("oxidation of Y to nitro")) {
            return new CvTerm("UNIMOD", "UNIMOD:354", "Nitro", "44.985078");
        } else if (ptmName.equalsIgnoreCase("O18 on peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:258", "Label:18O(1)", "2.004246");
        } else if (ptmName.equalsIgnoreCase("di-O18 on peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:193", "Label:18O(2)", "4.00849");
        } else if (ptmName.equalsIgnoreCase("phosphopantetheine S")) {
            return new CvTerm("UNIMOD", "UNIMOD:49", "Phosphopantetheine", "340.085794");
        } else if (ptmName.equalsIgnoreCase("palmitoylation of C")
                || ptmName.equalsIgnoreCase("palmitoylation of K")
                || ptmName.equalsIgnoreCase("palmitoylation of S")
                || ptmName.equalsIgnoreCase("palmitoylation of T")) {
            return new CvTerm("UNIMOD", "UNIMOD:47", "Palmitoyl", "238.229666");
        } else if (ptmName.equalsIgnoreCase("phosphorylation of S with prompt loss")
                || ptmName.equalsIgnoreCase("phosphorylation of T with prompt loss")
                || ptmName.equalsIgnoreCase("phosphorylation with prompt loss on Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:23", "Dehydrated", "-18.010565");
        } else if (ptmName.equalsIgnoreCase("propionyl light K")
                || ptmName.equalsIgnoreCase("propionyl light on peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:58", "Propionyl", "56.026215");
        } else if (ptmName.equalsIgnoreCase("propionyl heavy K")
                || ptmName.equalsIgnoreCase("propionyl heavy peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:59", "Propionyl:13C(3)", "59.036279");
        } else if (ptmName.equalsIgnoreCase("pyridyl K")
                || ptmName.equalsIgnoreCase("pyridyl peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:25", "Pyridylacetyl", "119.037114");
        } else if (ptmName.equalsIgnoreCase("pyro-cmC")
                || ptmName.equalsIgnoreCase("pyro-glu from n-term Q")) {
            return new CvTerm("UNIMOD", "UNIMOD:28", "Gln->pyro-Glu", "-17.026549");
        } else if (ptmName.equalsIgnoreCase("pyro-glu from n-term E")) {
            return new CvTerm("UNIMOD", "UNIMOD:27", "Glu->pyro-Glu", "-18.010565");
        } else if (ptmName.equalsIgnoreCase("oxidation of P to pyroglutamic acid")) {
            return new CvTerm("UNIMOD", "UNIMOD:359", "Pro->pyro-Glu", "13.979265");
        } else if (ptmName.equalsIgnoreCase("s-pyridylethylation of C")) {
            return new CvTerm("UNIMOD", "UNIMOD:31", "Pyridylethyl", "105.057849");
        } else if (ptmName.equalsIgnoreCase("SeMet")) {
            return new CvTerm("UNIMOD", "UNIMOD:162", "Delta:S(-1)Se(1)", "47.944449");
        } else if (ptmName.equalsIgnoreCase("sulfation of Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:40", "Sulfo", "79.956815");
        } else if (ptmName.equalsIgnoreCase("tri-iodination of Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:131", "Triiodo", "377.689944");
        } else if (ptmName.equalsIgnoreCase("n-acyl diglyceride cysteine")) {
            return new CvTerm("UNIMOD", "UNIMOD:51", "Tripalmitate", "788.725777");
        } else if (ptmName.equalsIgnoreCase("ICAT light")) {
            return new CvTerm("UNIMOD", "UNIMOD:105", "ICAT-C", "227.126991");
        } else if (ptmName.equalsIgnoreCase("ICAT heavy")) {
            return new CvTerm("UNIMOD", "UNIMOD:106", "ICAT-C:13C(9)", "236.157185");
        } else if (ptmName.equalsIgnoreCase("CAMthiopropanoyl K")) {
            return new CvTerm("UNIMOD", "UNIMOD:293", "CAMthiopropanoyl", "145.019749");
        } else if (ptmName.equalsIgnoreCase("heavy arginine-13C6")
                || ptmName.equalsIgnoreCase("heavy lysine-13C6")) {
            return new CvTerm("UNIMOD", "UNIMOD:188", "Label:13C(6)", "6.020129");
        } else if (ptmName.equalsIgnoreCase("heavy arginine-13C6-15N4")) {
            return new CvTerm("UNIMOD", "UNIMOD:267", "Label:13C(6)15N(4)", "10.008269");
        } else if (ptmName.equalsIgnoreCase("PNGasF in O18 water")) {
            return new CvTerm("UNIMOD", "UNIMOD:366", "Deamidated:18O(1)", "2.988261");
        } else if (ptmName.equalsIgnoreCase("beta elimination of S")
                || ptmName.equalsIgnoreCase("beta elimination of T")
                || ptmName.equalsIgnoreCase("dehydro of S and T")) {
            return new CvTerm("UNIMOD", "UNIMOD:23", "Dehydrated", "-18.010565");
        } else if (ptmName.equalsIgnoreCase("arginine to ornithine")) {
            return new CvTerm("UNIMOD", "UNIMOD:372", "Arg->Orn", "-42.021798");
        } else if (ptmName.equalsIgnoreCase("carboxykynurenin of W")) {
            return null; // @TODO: no mapping found!!
        } else if (ptmName.equalsIgnoreCase("sumoylation of K")) {
            return new CvTerm("UNIMOD", "UNIMOD:846", "EQIGG", "484.228162");
        } else if (ptmName.equalsIgnoreCase("iTRAQ114 on nterm")
                || ptmName.equalsIgnoreCase("iTRAQ114 on K")
                || ptmName.equalsIgnoreCase("iTRAQ114 on Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:532", "iTRAQ4plex114", "144.105918");
        } else if (ptmName.equalsIgnoreCase("iTRAQ115 on nterm")
                || ptmName.equalsIgnoreCase("iTRAQ115 on K")
                || ptmName.equalsIgnoreCase("iTRAQ115 on Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:533", "iTRAQ4plex115", "144.099599");
        } else if (ptmName.equalsIgnoreCase("iTRAQ116 on nterm")
                || ptmName.equalsIgnoreCase("iTRAQ116 on K")
                || ptmName.equalsIgnoreCase("iTRAQ116 on Y")
                || ptmName.equalsIgnoreCase("iTRAQ117 on nterm")
                || ptmName.equalsIgnoreCase("iTRAQ117 on K")
                || ptmName.equalsIgnoreCase("iTRAQ117 on Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:214", "iTRAQ4plex", "144.102063");
        } else if (ptmName.equalsIgnoreCase("MMTS on C")) {
            return new CvTerm("UNIMOD", "UNIMOD:39", "Methylthio", "45.987721");
        } else if (ptmName.equalsIgnoreCase("heavy lysine - 2H4")) {
            return new CvTerm("UNIMOD", "UNIMOD:481", "Label:2H(4)", "4.025107");
        } else if (ptmName.equalsIgnoreCase("heavy lysine - 13C6 15N2")) {
            return new CvTerm("UNIMOD", "UNIMOD:259", "Label:13C(6)15N(2)", "8.014199");
        } else if (ptmName.equalsIgnoreCase("Asparagine HexNAc")
                || ptmName.equalsIgnoreCase("Serine HexNAc")
                || ptmName.equalsIgnoreCase("Threonine HexNAc")) {
            return new CvTerm("UNIMOD", "UNIMOD:43", "HexNAc", "203.079373");
        } else if (ptmName.equalsIgnoreCase("Asparagine dHexHexNAc")) {
            return new CvTerm("UNIMOD", "UNIMOD:143", "HexNAc(1)dHex(1)", "349.137281");
        } else if (ptmName.equalsIgnoreCase("palmitoleyl of S")
                || ptmName.equalsIgnoreCase("palmitoleyl of C")
                || ptmName.equalsIgnoreCase("palmitoleyl of T")) {
            return new CvTerm("UNIMOD", "UNIMOD:431", "Palmitoleyl", "236.214016");
        } else if (ptmName.equalsIgnoreCase("CHD2-di-methylation of K")
                || ptmName.equalsIgnoreCase("CHD2-di-methylation of peptide n-term")) {
            return new CvTerm("UNIMOD", "UNIMOD:199", "Dimethyl:2H(4)", "32.056407");
        } else if (ptmName.equalsIgnoreCase("Maleimide-PEO2-Biotin of C")) {
            return new CvTerm("UNIMOD", "UNIMOD:522", "Maleimide-PEO2-Biotin", "525.225719");
        } else if (ptmName.equalsIgnoreCase("Uniblue A on K")) {
            return null; //new CvTerm("MOD", "MOD:01659", "Uniblue A", "484.039891"); // note: not found in unimod...
        } else if (ptmName.equalsIgnoreCase("trideuteration of L (SILAC)")) {
            return new CvTerm("UNIMOD", "UNIMOD:262", "Label:2H(3)", "3.018830");
        } else if (ptmName.equalsIgnoreCase("TMT duplex on K")
                || ptmName.equalsIgnoreCase("TMT duplex on K (old)")
                || ptmName.equalsIgnoreCase("TMT duplex on n-term peptide")
                || ptmName.equalsIgnoreCase("TMT duplex on n-term peptide (old)")) {
            return new CvTerm("UNIMOD", "UNIMOD:738", "TMT2plex", "225.155833");
        } else if (ptmName.equalsIgnoreCase("TMT 6-plex on K")
                || ptmName.equalsIgnoreCase("TMT 6-plex on K (old)")
                || ptmName.equalsIgnoreCase("TMT 6-plex on n-term peptide")
                || ptmName.equalsIgnoreCase("TMT 6-plex on n-term peptide (old)")) {
            return new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", "229.162932");
        } 
//        else if (ptmName.equalsIgnoreCase("TMT 10-plex on K")
//                || ptmName.equalsIgnoreCase("TMT 10-plex n-term")
//                || ptmName.equalsIgnoreCase("TMT 10-plex n-term peptide")) {
//            return new CvTerm("UNIMOD", "UNIMOD:???", "TMT10plex", "229.162932"); // @TODO: 10-plex TMT is missing!!
//        }  
        else if (ptmName.equalsIgnoreCase("iTRAQ8plex:13C(7)15N(1) on nterm")
                || ptmName.equalsIgnoreCase("iTRAQ8plex:13C(7)15N(1) on K")
                || ptmName.equalsIgnoreCase("iTRAQ8plex:13C(7)15N(1) on Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:730", "iTRAQ8plex", "304.205360");
        } else if (ptmName.equalsIgnoreCase("iTRAQ8plex:13C(6)15N(2) on nterm")
                || ptmName.equalsIgnoreCase("iTRAQ8plex:13C(6)15N(2) on K")
                || ptmName.equalsIgnoreCase("iTRAQ8plex:13C(6)15N(2) on Y")) {
            return new CvTerm("UNIMOD", "UNIMOD:731", "iTRAQ8plex:13C(6)15N(2)", "304.199040");
        } else if (ptmName.equalsIgnoreCase("selenocysteine")) {
            return new CvTerm("UNIMOD", "UNIMOD:162", "Delta:S(-1)Se(1)", "47.944449");
        } else if (ptmName.equalsIgnoreCase("carboxymethylated selenocysteine")) {
            return null; // @TODO: no mapping found!!
        }

        return null;
    }

    /**
     * Loads the PRIDE to PTM map from the user folder or creates a new one if
     * the file is not present. Loads a default mapping if a PTM is not mapped.
     *
     * @param searchParameters the search parameters
     * @return the PRIDE to PTM map
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public static PtmToPrideMap loadPtmToPrideMap(SearchParameters searchParameters) throws FileNotFoundException, IOException, ClassNotFoundException {
        PrideObjectsFactory prideObjectsFactory = PrideObjectsFactory.getInstance();
        PtmToPrideMap ptmToPrideMap = prideObjectsFactory.getPtmToPrideMap();
        boolean changes = false;
        ModificationProfile modificationProfile = searchParameters.getModificationProfile();
        for (String psPtm : modificationProfile.getAllModifications()) {
            if (ptmToPrideMap.getCVTerm(psPtm) == null) {
                CvTerm defaultCVTerm = PtmToPrideMap.getDefaultCVTerm(psPtm);
                if (defaultCVTerm != null) {
                    ptmToPrideMap.putCVTerm(psPtm, defaultCVTerm);
                    changes = true;
                }
            }
        }
        if (changes) {
            prideObjectsFactory.setPtmToPrideMap(ptmToPrideMap);
        }
        return ptmToPrideMap;
    }
}
