package com.compomics.util.preferences;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class groups the preferences for the digestion of proteins.
 *
 * @author Marc Vaudel
 */
public class DigestionPreferences {

    /**
     * List of enzyme used.
     */
    private ArrayList<Enzyme> enzymes;
    /**
     * Number of allowed missed cleavages.
     */
    private HashMap<String, Integer> nMissedCleavages;

    /**
     * Enum for the different types of enzyme specificity.
     */
    public enum Specificity {

        /**
         * Specific at both termini.
         */
        specific,
        /**
         * Specific at only one of the termini.
         */
        semiSpecific,
        /**
         * Specific at the N-terminus only.
         */
        specificNTermOnly,
        /**
         * Specific at the C-terminus only.
         */
        specificCTermOnly,
        /**
         * Not specific.
         */
        notSpecific;

        @Override
        public String toString() {
            switch (this) {
                case specific:
                    return "Specific";
                case semiSpecific:
                    return "Semi-specific";
                case specificNTermOnly:
                    return "N-term specific";
                case specificCTermOnly:
                    return "C-term specific";
                case notSpecific:
                    return "Not specific";
                default:
                    throw new UnsupportedOperationException("Specificity " + this.name() + "Not implemented.");
            }
        }
    }
    /**
     * The specificity of the enzyme.
     */
    private HashMap<String, Specificity> specificity;

    /**
     * Constructor.
     */
    public DigestionPreferences() {
        String enzymeName = "Tryspsin";
        Enzyme trypsin = EnzymeFactory.getInstance().getEnzyme(enzymeName);
        addEnzyme(trypsin);
        setnMissedCleavages(enzymeName, 2);
    }

    /**
     * Constructor based on other preferences.
     *
     * @param digestionPreferences the preferences to reuse
     */
    public DigestionPreferences(DigestionPreferences digestionPreferences) {
        for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
            addEnzyme(enzyme);
            String enzymeName = enzyme.getName();
            setSpecificity(enzymeName, digestionPreferences.getSpecificity(enzymeName));
            setnMissedCleavages(enzymeName, digestionPreferences.getnMissedCleavages(enzymeName));
        }
    }

    /**
     * Returns the enzymes used for digestion in a list.
     *
     * @return the enzymes used for digestion in a list
     */
    public ArrayList<Enzyme> getEnzymes() {
        return enzymes;
    }

    /**
     * Sets the enzymes used for digestion.
     *
     * @param enzymes the enzymes used for digestion in a list
     */
    public void setEnzymes(ArrayList<Enzyme> enzymes) {
        this.enzymes = enzymes;
    }

    /**
     * Adds an enzyme. The specificity of the enzyme is set by default to
     * specific and the number of allowed missed cleavages to 0.
     *
     * @param enzyme an enzyme used for digestion.
     */
    public void addEnzyme(Enzyme enzyme) {
        if (enzymes == null) {
            enzymes = new ArrayList<Enzyme>(1);
        }
        enzymes.add(enzyme);
        setSpecificity(enzyme.getName(), Specificity.specific);
        setnMissedCleavages(enzyme.getName(), 0);
    }

    /**
     * Clears the enzymes used.
     */
    public void clearEnzymes() {
        enzymes = null;
    }

    /**
     * Returns the number of allowed missed cleavages for the given enzyme. Null
     * if not set.
     *
     * @param enzymeName the name of the enzyme
     *
     * @return the number of allowed missed cleavages
     */
    public Integer getnMissedCleavages(String enzymeName) {
        if (nMissedCleavages == null) {
            return null;
        }
        return nMissedCleavages.get(enzymeName);
    }

    /**
     * Sets the number of allowed missed cleavages.
     *
     * @param enzymeName the name of the enzyme
     * @param enzymeMissedCleavages the number of allowed missed cleavages
     */
    public void setnMissedCleavages(String enzymeName, int enzymeMissedCleavages) {
        if (nMissedCleavages == null) {
            nMissedCleavages = new HashMap<String, Integer>(1);
        }
        nMissedCleavages.put(enzymeName, enzymeMissedCleavages);
    }

    /**
     * Returns the expected specificity of the given enzyme. Null if not set.
     *
     * @param enzymeName the name of the enzyme
     *
     * @return the specificity
     */
    public Specificity getSpecificity(String enzymeName) {
        if (specificity == null) {
            return null;
        }
        Specificity enzymeSpecificity = specificity.get(enzymeName);
        return enzymeSpecificity;
    }

    /**
     * Sets the expected specificity of the enzyme.
     *
     * @param enzymeName the name of the enzyme
     * @param enzymeSpecificity the expected specificity of the enzyme
     */
    public void setSpecificity(String enzymeName, Specificity enzymeSpecificity) {
        if (specificity == null) {
            specificity = new HashMap<String, Specificity>(1);
        }
        specificity.put(enzymeName, enzymeSpecificity);
    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        DigestionPreferences defaultPreferences = new DigestionPreferences();
        StringBuilder stringBuilder = new StringBuilder();
        if (!defaultPreferences.isSameAs(this)) {
            String newLine = System.getProperty("line.separator");
            for (int i = 0; i < enzymes.size(); i++) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(newLine);
                }
                Enzyme enzyme = enzymes.get(i);
                String enzymeName = enzyme.getName();
                stringBuilder.append(enzymeName).append(", ").append(getSpecificity(enzymeName));
                Integer nmc = getnMissedCleavages(enzymeName);
                if (nmc != null) {
                    stringBuilder.append(" ").append(nmc).append(" missed cleavages");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Returns a boolean indicating whether these digestion preferences are the
     * same as the given other preferences.
     *
     * @param otherDigestionPreferences the other digestion preferences
     *
     * @return a boolean indicating whether these digestion preferences are the
     * same as the given other preferences
     */
    public boolean isSameAs(DigestionPreferences otherDigestionPreferences) {
        ArrayList<Enzyme> otherEnzymes = otherDigestionPreferences.getEnzymes();
        if (enzymes.size() != otherEnzymes.size()) {
            return false;
        }
        ArrayList<String> enzymeNames = new ArrayList<String>(enzymes.size());
        for (Enzyme enzyme : enzymes) {
            enzymeNames.add(enzyme.getName());
        }
        ArrayList<String> otherNames = new ArrayList<String>(otherEnzymes.size());
        for (Enzyme enzyme : otherEnzymes) {
            otherNames.add(enzyme.getName());
        }
        if (!Util.sameLists(enzymeNames, otherNames)) {
            return false;
        }
        for (String enzymeName : enzymeNames) {
            if (getSpecificity(enzymeName) != otherDigestionPreferences.getSpecificity(enzymeName)
                    || !getnMissedCleavages(enzymeName).equals(otherDigestionPreferences.getnMissedCleavages(enzymeName))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the X!Tandem enzyme format.
     *
     * @return the enzyme X!Tandem format as String
     */
    public String getXTandemFormat() {

        StringBuilder result = new StringBuilder();

        for (Enzyme enzyme : enzymes) {
            if (result.length() > 0) {
                result.append(",");
            }
            Specificity specificity = getSpecificity(enzyme.getName());
            if (specificity == Specificity.notSpecific) {
                result.append("[X]|[X]");
            } else {
                if (enzyme.getAminoAcidBefore().size() > 0) {
                    result.append("[");
                    for (Character aa : enzyme.getAminoAcidBefore()) {
                        result.append(aa);
                    }
                    result.append("]");
                }

                if (enzyme.getRestrictionBefore().size() > 0) {
                    result.append("{");
                    for (Character aa : enzyme.getRestrictionBefore()) {
                        result.append(aa);
                    }
                    result.append("}");
                }

                if (enzyme.getAminoAcidBefore().isEmpty() && enzyme.getRestrictionBefore().isEmpty()) {
                    result.append("[X]");
                }

                result.append("|");

                if (enzyme.getAminoAcidAfter().size() > 0) {
                    result.append("[");
                    for (Character aa : enzyme.getAminoAcidAfter()) {
                        result.append(aa);
                    }
                    result.append("]");
                }

                if (enzyme.getRestrictionAfter().size() > 0) {
                    result.append("{");
                    for (Character aa : enzyme.getRestrictionAfter()) {
                        result.append(aa);
                    }
                    result.append("}");
                }

                if (enzyme.getAminoAcidAfter().isEmpty() && enzyme.getRestrictionAfter().isEmpty()) {
                    result.append("[X]");
                }
            }
        }

        return result.toString();
    }

    /**
     * Get the MyriMatch enzyme format. In case multiple enzymes are present all
     * possible cleavage sites will be included.
     *
     * @return the enzyme MyriMatch format as String
     */
    public String getMyriMatchFormat() {

        // example: trypsin corresponds to "[|R|K . . ]"
        // details: http://www.mc.vanderbilt.edu/root/vumc.php?site=msrc/bioinformatics&doc=27121
        String result = "[";

        for (Enzyme enzyme : enzymes) {
            if (enzyme.getAminoAcidBefore().size() > 0) {
                for (Character aa : enzyme.getAminoAcidBefore()) {
                    result += "|" + aa;
                }
                result += " ";
            } else {
                result += " ";
            }
        }

        ArrayList<Character> commonRestrictionAfter;
        if (enzymes.size() == 1) {
            commonRestrictionAfter = enzymes.get(0).getRestrictionAfter();
        } else {
            commonRestrictionAfter = new ArrayList<Character>();
            for (Character aa : enzymes.get(0).getRestrictionAfter()) {
                boolean missing = false;
                for (Enzyme enzyme : enzymes) {
                    if (!enzyme.getRestrictionAfter().contains(aa)) {
                        missing = true;
                        break;
                    }
                }
                if (!missing) {
                    commonRestrictionAfter.add(aa);
                }
            }
        }
        if (commonRestrictionAfter.size() > 0) {
            String temp = "";
            for (Character aa : AminoAcid.getUniqueAminoAcids()) {
                if (!commonRestrictionAfter.contains(aa)) {
                    if (!temp.isEmpty()) {
                        temp += "|";
                    }
                    temp += aa;
                }
            }
            result += temp + " ";
        } else {
            result += ". ";
        }

        ArrayList<Character> commonRestrictionBefore;
        if (enzymes.size() == 1) {
            commonRestrictionBefore = enzymes.get(0).getRestrictionBefore();
        } else {
            commonRestrictionBefore = new ArrayList<Character>();
            for (Character aa : enzymes.get(0).getRestrictionBefore()) {
                boolean missing = false;
                for (Enzyme enzyme : enzymes) {
                    if (!enzyme.getRestrictionBefore().contains(aa)) {
                        missing = true;
                        break;
                    }
                }
                if (!missing) {
                    commonRestrictionBefore.add(aa);
                }
            }
        }
        if (commonRestrictionBefore.size() > 0) {
            String temp = "";
            for (Character aa : AminoAcid.getUniqueAminoAcids()) {
                if (!commonRestrictionBefore.contains(aa)) {
                    if (!temp.isEmpty()) {
                        temp += "|";
                    }
                    temp += aa;
                }
            }
            result += temp + " ";
        } else {
            result += ". ";
        }

        for (Enzyme enzyme : enzymes) {
            if (enzyme.getAminoAcidAfter().size() > 0) {
                String temp = "";
                for (Character aa : enzyme.getAminoAcidAfter()) {
                    if (!temp.isEmpty()) {
                        temp += "|";
                    }
                    temp += aa;
                }
                result += temp + "|";
            }
        }

        return result + "]";
    }

}
