package com.compomics.util.experiment.biology.modifications;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * This class models a peptide or protein modification.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Modification extends ExperimentObject {

    /**
     * The version UID for backward compatibility.
     */
    static final long serialVersionUID = -545472596243822505L;

    /**
     * The modification type.
     */
    private ModificationType modificationType;
    /**
     * Name of the modification.
     */
    private String name;
    /**
     * Short name of the modification.
     */
    private String shortName;
    /**
     * Mass difference produced by this modification. Null if not set.
     */
    private Double mass = null;
    /**
     * List of known neutral losses for this modification.
     */
    private ArrayList<NeutralLoss> neutralLosses = new ArrayList<>(0);
    /**
     * List of known reporter ions for this modification.
     */
    private ArrayList<ReporterIon> reporterIons = new ArrayList<>(0);
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
     * The CV term associated with this PTM. Null if not set.
     */
    private CvTerm cvTerm = null;
    /**
     * The ambiguity key for ambiguous modifications.
     */
    private String ambiguityKey = null;
    /**
     * The number of decimals used in the getRoundedMass method.
     */
    private static final int NUMBER_OF_ROUNDED_DECIMALS = 6;

    /**
     * Constructor for the modification.
     */
    public Modification() {
    }

    /**
     * Constructor for a reference modification.
     *
     * @param modificationType type of modification
     * @param name name of the modification
     * @param shortName short name of the modification
     * @param atomChainAdded atomic composition of the molecule added
     * @param atomChainRemoved atomic composition of the molecule removed
     * @param aminoAcidPattern residue pattern affected by this modification
     */
    public Modification(ModificationType modificationType, String name, String shortName, AtomChain atomChainAdded, AtomChain atomChainRemoved, AminoAcidPattern aminoAcidPattern) {
        this.modificationType = modificationType;
        this.name = name;
        this.shortName = shortName;
        this.atomChainAdded = atomChainAdded;
        this.atomChainRemoved = atomChainRemoved;
        this.pattern = aminoAcidPattern;
        this.cvTerm = null;
    }

    /**
     * Constructor for a reference modification.
     *
     * @param modificationType type of modification
     * @param name name of the modification
     * @param shortName short name of the modification
     * @param atomChainAdded atomic composition of the molecule added
     * @param atomChainRemoved atomic composition of the molecule removed
     * @param aminoAcidPattern residue pattern affected by this modification
     * @param cvTerm the CV term associated with this PTM, null if not set
     */
    public Modification(ModificationType modificationType, String name, String shortName, AtomChain atomChainAdded, AtomChain atomChainRemoved, AminoAcidPattern aminoAcidPattern, CvTerm cvTerm) {
        this.modificationType = modificationType;
        this.name = name;
        this.shortName = shortName;
        this.atomChainAdded = atomChainAdded;
        this.atomChainRemoved = atomChainRemoved;
        this.pattern = aminoAcidPattern;
        this.cvTerm = cvTerm;
    }

    /**
     * Simple constructor for a PTM. This constructor does not set the atomic
     * composition or the cv term.
     *
     * @param modificationType type of modification
     * @param name name of the modification
     * @param mass the mass of the modification
     * @param residues list of residues possibly targeted by this modification
     */
    public Modification(ModificationType modificationType, String name, Double mass, ArrayList<String> residues) {
        this.modificationType = modificationType;
        this.name = name;
        this.mass = mass;
        if (residues != null) {
            this.pattern = new AminoAcidPattern(residues);
        }
    }

    /**
     * Returns the modification type.
     * 
     * @return the modification type
     */
    public ModificationType getModificationType() {
        return modificationType;
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
        if (mass == null) {
            estimateMass();
        }
        return mass;
    }

    /**
     * Estimates the mass of the PTM and stores it in the mass attribute.
     */
    private synchronized void estimateMass() {
        if (mass == null) {
            Double tempMass = 0.0;
            if (atomChainAdded != null) {
                tempMass += atomChainAdded.getMass();
            }
            if (atomChainRemoved != null) {
                tempMass -= atomChainRemoved.getMass();
            }
            mass = tempMass;
        }
    }

    /**
     * Returns the ambiguity key.
     * 
     * @return the ambiguity key
     */
    public String getAmbiguityKey() {
        
        if (ambiguityKey == null) {
            setAmbiguityKey();
        }
        
        return ambiguityKey;
    }

    /**
     * Sets the ambiguity key.
     */
    private void setAmbiguityKey() {
        this.ambiguityKey = getMass() + "";
    }

    /**
     * Getter for the rounded mass difference induced by this modification.
     *
     * @param numberOfDecimals the number of decimals to round to
     * @return the rounded mass difference induced by the modification
     */
    public double getRoundedMass(int numberOfDecimals) {
        double roundedMass = getMass();
        return Util.roundDouble(roundedMass, numberOfDecimals);
    }

    /**
     * Getter for the rounded mass difference induced by this modification.
     * Rounded to the number of decimals set in NUMBER_OF_ROUNDED_DECIMALS.
     *
     * @return the rounded mass difference induced by the modification
     */
    public double getRoundedMass() {
        return getRoundedMass(NUMBER_OF_ROUNDED_DECIMALS);
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
        mass = null;
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
        mass = null;
    }

    /**
     * Returns true if the atomic composition of the PTM is the same as another
     * one.
     *
     * @param anotherPTM the PTM to compare to
     *
     * @return true if the atomic composition of the PTM is the same as the
     * other one
     */
    public boolean isSameAtomicComposition(Modification anotherPTM) {
        if (atomChainAdded != null && !atomChainAdded.isSameCompositionAs(anotherPTM.getAtomChainAdded())
                || atomChainRemoved != null && !atomChainRemoved.isSameCompositionAs(anotherPTM.getAtomChainRemoved())) {
            return false;
        }
        if (atomChainAdded == null && anotherPTM.getAtomChainAdded() != null && !anotherPTM.getAtomChainAdded().getAtomChain().isEmpty()
                || atomChainRemoved == null && anotherPTM.getAtomChainRemoved() != null && !anotherPTM.getAtomChainRemoved().getAtomChain().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the targeted pattern of the PTM is the same as another
     * one. An empty pattern is considered to be the same as a null pattern.
     *
     * @param anotherPTM the PTM to compare to
     *
     * @return true if the targeted pattern of the PTM is the same as the other
     * one
     */
    public boolean isSamePattern(Modification anotherPTM) {
        if (pattern == null && anotherPTM.getPattern() != null && anotherPTM.getPattern().length() > 0) {
            return false;
        }
        if (pattern != null && !pattern.isSameAs(anotherPTM.getPattern(), SequenceMatchingPreferences.defaultStringMatching)) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the PTM is the same as another one.
     *
     * @param anotherPTM another PTM
     *
     * @return true if the PTM is the same as the other one
     */
    public boolean isSameAs(Modification anotherPTM) {
        if (modificationType != anotherPTM.getModificationType()) {
//            System.out.println("type difference");
//            System.out.println("local: " + type);
//            System.out.println("parameters: " + anotherPTM.getType());
            return false;
        }
        if (!isSamePattern(anotherPTM)) {
//            System.out.println("pattern difference");
//            System.out.println("local: " + pattern);
//            System.out.println("parameters: " + anotherPTM.getPattern());
            return false;
        }
        if (!isSameAtomicComposition(anotherPTM)) {
//            System.out.println("composition difference");
//            System.out.println("local added: " + atomChainAdded);
//            System.out.println("local removed: " + atomChainRemoved);
//            System.out.println("parameters added: " + anotherPTM.getAtomChainAdded());
//            System.out.println("parameters removed: " + anotherPTM.getAtomChainRemoved());
            return false;
        }
        return true;
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
        return pattern == null || pattern.length() == 1;
    }

    /**
     * Returns information about the PTM as an HTML tooltip.
     *
     * @return information about the PTM as an HTML tooltip
     */
    public String getHtmlTooltip() {

        String tooltip = "<html>";

        tooltip += "Name: " + name + "<br>";
        tooltip += "Mass: " + getRoundedMass(4) + "<br>";
        tooltip += "Type: ";

        switch (modificationType) {
            case modaa:
                tooltip += "Particular amino acid(s)";
                break;
            case modn_protein:
            case modnaa_protein:
                tooltip += "Protein N terminus";
                break;
            case modc_protein:
            case modcaa_protein:
                tooltip += "Protein C terminus";
                break;
            case modn_peptide:
            case modnaa_peptide:
                tooltip += "Peptide N terminus";
                break;
            case modc_peptide:
            case modcaa_peptide:
                tooltip += "Peptide C terminus";
                break;
            default:
                break;
        }

        tooltip += "<br>";

        if (pattern != null && !pattern.getAminoAcidsAtTarget().isEmpty()) {
            tooltip += "Target: ";
            String patternAsString = pattern.toString();
            tooltip += patternAsString;
        }

        tooltip += "</html>";

        return tooltip;
    }

    /**
     * Returns the CV term associated with this PTM.
     *
     * @return the cvTerm
     */
    public CvTerm getCvTerm() {
        return cvTerm;
    }

    /**
     * Set the CV term associated with this PTM.
     *
     * @param cvTerm the cvTerm to set
     */
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    @Override
    public String toString() {

        String target = "";
        switch (modificationType) {
            case modaa:
                target = getPattern().toString();
                break;
            case modc_protein:
                target = "Protein C-terminus";
                break;
            case modcaa_protein:
                target = "Protein C-terminus ending with " + getPattern().toString();
                break;
            case modc_peptide:
                target = "Peptide C-terminus";
                break;
            case modcaa_peptide:
                target = "Peptide C-terminus ending with " + getPattern().toString();
                break;
            case modn_protein:
                target = "Protein N-terminus";
                break;
            case modnaa_protein:
                target = "Protein N-terminus starting with " + getPattern().toString();
                break;
            case modn_peptide:
                target = "Peptide N-terminus";
                break;
            case modnaa_peptide:
                target = "Peptide N-terminus starting with " + getPattern().toString();
                break;
        }

        StringBuilder description = new StringBuilder();
        description.append(name);
        if (shortName != null && !shortName.equals("")) {
            description.append("(").append(shortName).append(")");
        }
        description.append("\t");
        if (atomChainAdded != null) {
            description.append("+{").append(atomChainAdded).append("}");
        }
        if (atomChainRemoved != null) {
            description.append("-{").append(atomChainRemoved).append("}");
        }

        double ptmMass = getRoundedMass();
        String sign;
        if (ptmMass > 0) {
            sign = "+";
        } else {
            sign = "-";
        }
        description.append(" (").append(sign).append(ptmMass).append(")");

        description.append(" targeting ").append(target);

        return description.toString();
    }
}
