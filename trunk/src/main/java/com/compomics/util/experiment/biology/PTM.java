package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;

/**
 * This class models a post-translational modification.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PTM extends ExperimentObject {

    /**
     * The version UID for backward compatibility.
     */
    static final long serialVersionUID = -545472596243822505L;
    /**
     * Modification at particular amino acids.
     */
    public static final int MODAA = 0;
    /**
     * Modification at the N terminus of a protein.
     */
    public static final int MODN = 1;
    /**
     * Modification at the N terminus of a protein at particular amino acids.
     */
    public static final int MODNAA = 2;
    /**
     * Modification at the C terminus of a protein.
     */
    public static final int MODC = 3;
    /**
     * Modification at the C terminus of a protein at particular amino acids.
     */
    public static final int MODCAA = 4;
    /**
     * Modification at the N terminus of a peptide.
     */
    public static final int MODNP = 5;
    /**
     * Modification at the N terminus of a peptide at particular amino acids.
     */
    public static final int MODNPAA = 6;
    /**
     * Modification at the C terminus of a peptide.
     */
    public static final int MODCP = 7;
    /**
     * Modification at the C terminus of a peptide at particular amino acids.
     */
    public static final int MODCPAA = 8;
    /**
     * The max number of modification types.
     */
    public static final int MODMAX = 9;
    /**
     * The modification type according to static field.
     */
    private int type;
    /**
     * Name of the modification.
     */
    private String name;
    /**
     * Short name of the modification.
     */
    private String shortName;
    /**
     * Mass difference produced by this modification.
     */
    private double mass;
    /**
     * List of known neutral losses for this modification.
     */
    private ArrayList<NeutralLoss> neutralLosses = new ArrayList<NeutralLoss>(0);
    /**
     * List of known reporter ions for this modification.
     */
    private ArrayList<ReporterIon> reporterIons = new ArrayList<ReporterIon>(0);
    /**
     * The amino acid pattern targeted by this modification (can be set using
     * the AminoAcidPatternDialog).
     */
    private AminoAcidPattern pattern = new AminoAcidPattern();
    /**
     * The composition of the molecule added.
     */
    private AtomChain atomChainAdded = new AtomChain();
    /**
     * The composition of the molecule removed.
     */
    private AtomChain atomChainRemoved = new AtomChain();

    /**
     * Constructor for the modification.
     */
    public PTM() {
    }

    /**
     * Constructor for a reference modification.
     *
     * @param type Type of modification according to static attributes
     * @param name Name of the modification
     * @param shortName Short name of the modification
     * @param atomChainAdded Atomic composition of the molecule added
     * @param atomChainRemoved Atomic composition of the molecule removed
     * @param aminoAcidPattern Residue pattern affected by this modification
     */
    public PTM(int type, String name, String shortName, AtomChain atomChainAdded, AtomChain atomChainRemoved, AminoAcidPattern aminoAcidPattern) {
        this.type = type;
        this.name = name;
        this.shortName = shortName;
        this.atomChainAdded = atomChainAdded;
        this.atomChainRemoved = atomChainRemoved;
        this.pattern = aminoAcidPattern;
    }

    /**
     * Getter for the modification type.
     *
     * @return the modification type
     */
    public int getType() {
        return type;
    }

    /**
     * Getter for the modification name.
     *
     * @return the modification name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the PTM name.
     *
     * @param name the PTM name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the short modification name.
     *
     * @return the short modification name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the short PTM name.
     *
     * @param shortName the PTM name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Getter for the mass difference induced by this modification.
     *
     * @return the mass difference induced by the modification
     */
    public double getMass() {
        if (atomChainAdded == null && atomChainRemoved == null) { // Backward compatibility
            return mass;
        }
        Double result = 0.0;
        if (atomChainAdded != null) {
            result += atomChainAdded.getMass();
        }
        if (atomChainRemoved != null) {
            result -= atomChainRemoved.getMass();
        }
        return result;
    }

    /**
     * Returns the atom chain added.
     *
     * @return the atom chain added
     */
    public AtomChain getAtomChainAdded() {
        return atomChainAdded;
    }

    /**
     * Sets the atom chain added.
     *
     * @param atomChainAdded the atom chain added
     */
    public void setAtomChainAdded(AtomChain atomChainAdded) {
        this.atomChainAdded = atomChainAdded;
    }

    /**
     * Returns the atom chain removed.
     *
     * @return the atom chain removed
     */
    public AtomChain getAtomChainRemoved() {
        return atomChainRemoved;
    }

    /**
     * Sets the atom chain removed.
     *
     * @param atomChainRemoved the atom chain removed
     */
    public void setAtomChainRemoved(AtomChain atomChainRemoved) {
        this.atomChainRemoved = atomChainRemoved;
    }

    /**
     * Compares two PTMs.
     *
     * @param anotherPTM another PTM
     * @return true if the given PTM is the same as the current PTM
     */
    public boolean isSameAs(PTM anotherPTM) {
        double massDiff = Math.abs(getMass() - anotherPTM.getMass());
        return type == anotherPTM.getType()
                && (massDiff == 0 || massDiff <= 0.0000000000001)
                && anotherPTM.getPattern().isSameAs(pattern, SequenceMatchingPreferences.defaultStringMatching);
    }

    /**
     * Returns the neutral losses possibly encountered with this modification.
     *
     * @return the neutral losses possibly encountered with this modification
     */
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return neutralLosses;
    }

    /**
     * Sets the neutral losses possibly encountered with this modification.
     *
     * @param neutralLosses the neutral losses possibly encountered with this
     * modification
     */
    public void setNeutralLosses(ArrayList<NeutralLoss> neutralLosses) {
        this.neutralLosses = neutralLosses;
    }

    /**
     * Adds a neutral loss.
     *
     * @param neutralLoss the new neutral loss
     */
    public void addNeutralLoss(NeutralLoss neutralLoss) {
        neutralLosses.add(neutralLoss);
    }

    /**
     * Returns the reporter ions possibly encountered with this modification.
     *
     * @return the reporter ions possibly encountered with this modification
     */
    public ArrayList<ReporterIon> getReporterIons() {
        return reporterIons;
    }

    /**
     * Sets the reporter ions possibly encountered with this modification.
     *
     * @param reporterIons the reporter ions possibly encountered with this
     * modification
     */
    public void setReporterIons(ArrayList<ReporterIon> reporterIons) {
        this.reporterIons = reporterIons;
    }

    /**
     * Adds a reporter ion.
     *
     * @param reporterIon the reporter ion to add
     */
    public void addReporterIon(ReporterIon reporterIon) {
        reporterIons.add(reporterIon);
    }

    /**
     * Returns the amino acid pattern targeted by this modification.
     *
     * @return the amino acid pattern targeted by this modification
     */
    public AminoAcidPattern getPattern() {
        return pattern;
    }

    /**
     * Sets the amino acid pattern targeted by this modification.
     *
     * @param pattern the amino acid pattern targeted by this modification
     */
    public void setPattern(AminoAcidPattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Indicates whether a modification can be searched with standard search
     * engines, i.e., true if it targets a single amino acid position, false if
     * it targets a complex pattern.
     *
     * @return a boolean indicating whether a modification can be searched with
     * standard search engines
     */
    public boolean isStandardSearch() {
        return pattern.length() == 1;
    }

    /**
     * Returns true if the PTM is an n-term PTM.
     *
     * @return true if the PTM is an n-term PTM
     */
    public boolean isNTerm() {
        return type == PTM.MODN
                || type == PTM.MODNAA
                || type == PTM.MODNP
                || type == PTM.MODNPAA;
    }

    /**
     * Returns true if the PTM is a c-term PTM.
     *
     * @return true if the PTM is a c-term PTM
     */
    public boolean isCTerm() {
        return type == PTM.MODC
                || type == PTM.MODCAA
                || type == PTM.MODCP
                || type == PTM.MODCPAA;
    }

    /**
     * Returns information about the PTM as an HTML tooltip.
     *
     * @return information about the PTM as an HTML tooltip
     */
    public String getHtmlTooltip() {

        String tooltip = "<html>";

        tooltip += "Name: " + name + "<br>";
        tooltip += "Mass: " + Util.roundDouble(mass, 4) + "<br>";
        tooltip += "Type: ";

        if (type == MODAA) {
            tooltip += "Particular amino acid(s)";
        } else if (type == MODN || type == MODNAA) {
            tooltip += "Protein N terminus";
        } else if (type == MODC || type == MODCAA) {
            tooltip += "Protein C terminus";
        } else if (type == MODNP || type == MODNPAA) {
            tooltip += "Peptide N terminus";
        } else if (type == MODCP || type == MODCPAA) {
            tooltip += "Peptide C terminus";
        }

        tooltip += "<br>";

        tooltip += "Target: ";
        if (pattern != null && !pattern.getAminoAcidsAtTarget().isEmpty()) {
            String patternAsString = pattern.toString();
            tooltip += patternAsString;
        } else {
            tooltip += "All";
        }

        tooltip += "</html>";

        return tooltip;
    }
}
