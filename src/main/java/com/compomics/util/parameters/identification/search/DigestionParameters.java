package com.compomics.util.parameters.identification.search;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class groups the preferences for the digestion of proteins.
 *
 * @author Marc Vaudel
 */
public class DigestionParameters implements Serializable {

    /**
     * Enum for the different types of enzyme specificity.
     */
    public enum Specificity {

        /**
         * Specific at both termini.
         */
        specific(0, "Specific"),
        /**
         * Specific at only one of the termini.
         */
        semiSpecific(1, "Semi-Specific"),
        /**
         * Specific at the N-terminus only.
         */
        specificNTermOnly(2, "N-term Specific"),
        /**
         * Specific at the C-terminus only.
         */
        specificCTermOnly(3, "C-term Specific");

        /**
         * The index.
         */
        public final int index;
        /**
         * The name.
         */
        public final String name;

        /**
         * Constructor.
         *
         * @param index the index as integer
         * @param name the name
         */
        private Specificity(int index, String name) {
            this.index = index;
            this.name = name;
        }

        /**
         * Returns the specificity of the given index.
         *
         * @param index the index of the specificity
         *
         * @return the corresponding specificity
         */
        public static Specificity getSpecificity(int index) {
            for (Specificity specificity : values()) {
                if (specificity.index == index) {
                    return specificity;
                }
            }
            throw new IllegalArgumentException("No specificity found for index " + index + ".");
        }

        /**
         * Returns the different options as command line description.
         *
         * @return the different options as command line description
         */
        public static String getCommandLineDescription() {
            StringBuilder stringBuilder = new StringBuilder();
            for (Specificity specificity : values()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(specificity.index).append(": ").append(specificity.name);
            }
            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Enum for the different types of digestion.
     */
    public enum CleavagePreference {

        /**
         * Digestion with an enzyme.
         */
        enzyme(0, "Enzyme"),
        /**
         * Unspecific digestion.
         */
        unSpecific(1, "Unspecific"),
        /**
         * Whole protein, no digestion.
         */
        wholeProtein(2, "Whole Protein");

        /**
         * The index.
         */
        public final int index;
        /**
         * The name.
         */
        public final String name;

        /**
         * Constructor.
         *
         * @param index the index as integer
         * @param name the name
         */
        private CleavagePreference(int index, String name) {
            this.index = index;
            this.name = name;
        }

        /**
         * Returns the cleavage preference of the given index.
         *
         * @param index the index of the cleavage preference
         *
         * @return the corresponding cleavage preference
         */
        public static CleavagePreference getCleavagePreferences(int index) {
            for (CleavagePreference cleavagePreference : values()) {
                if (cleavagePreference.index == index) {
                    return cleavagePreference;
                }
            }
            throw new IllegalArgumentException("No cleavage preference found for index " + index + ".");
        }

        /**
         * Returns the different options as command line description.
         *
         * @return the different options as command line description
         */
        public static String getCommandLineDescription() {
            StringBuilder stringBuilder = new StringBuilder();
            for (CleavagePreference cleavagePreference : values()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(cleavagePreference.index).append(": ").append(cleavagePreference.name);
            }
            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return name;
        }
    }
    /**
     * Boolean indicating whether the sample was not digested.
     */
    private CleavagePreference cleavagePreference;
    /**
     * List of enzyme used.
     */
    private ArrayList<Enzyme> enzymes;
    /**
     * Number of allowed missed cleavages.
     */
    private HashMap<String, Integer> nMissedCleavages;
    /**
     * The specificity of the enzyme.
     */
    private HashMap<String, Specificity> specificity;

    /**
     * Constructor for empty preferences.
     */
    public DigestionParameters() {
    }

    /**
     * Clones the given preferences.
     *
     * @param digestionPreferences the preferences to clone
     *
     * @return a new object containing the same preferences
     */
    public static DigestionParameters clone(DigestionParameters digestionPreferences) {
        DigestionParameters clone = new DigestionParameters();
        clone.setCleavagePreference(digestionPreferences.getCleavagePreference());
        if (digestionPreferences.getCleavagePreference() == DigestionParameters.CleavagePreference.enzyme) {
            for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
                clone.addEnzyme(enzyme);
                String enzymeName = enzyme.getName();
                clone.setSpecificity(enzymeName, digestionPreferences.getSpecificity(enzymeName));
                clone.setnMissedCleavages(enzymeName, digestionPreferences.getnMissedCleavages(enzymeName));
            }
        }
        return clone;
    }

    /**
     * Returns default digestion preferences. Trypsin specific with two missed
     * cleavages.
     *
     * @return default digestion preferences
     */
    public static DigestionParameters getDefaultPreferences() {
        DigestionParameters digestionPreferences = new DigestionParameters();
        digestionPreferences.setCleavagePreference(CleavagePreference.enzyme);
        String enzymeName = "Trypsin";
        Enzyme trypsin = EnzymeFactory.getInstance().getEnzyme(enzymeName);
        digestionPreferences.addEnzyme(trypsin);
        digestionPreferences.setnMissedCleavages(enzymeName, 2);
        return digestionPreferences;
    }

    /**
     * Returns a boolean indicating whether enzyme settings were set.
     *
     * @return a boolean indicating whether enzyme settings were set
     */
    public boolean hasEnzymes() {
        return enzymes != null && !enzymes.isEmpty();
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
            enzymes = new ArrayList<>(1);
        }
        enzymes.add(enzyme);
        setSpecificity(enzyme.getName(), Specificity.specific);
        setnMissedCleavages(enzyme.getName(), 0);
    }

    /**
     * Clears the parameters.
     */
    public void clear() {
        cleavagePreference = null;
        enzymes = null;
        nMissedCleavages = null;
        specificity = null;
    }

    /**
     * Clears the enzymes set including specificity and missed cleavages.
     */
    public void clearEnzymes() {
        enzymes = null;
        nMissedCleavages = null;
        specificity = null;
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
            nMissedCleavages = new HashMap<>(1);
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
            specificity = new HashMap<>(1);
        }
        specificity.put(enzymeName, enzymeSpecificity);
    }

    /**
     * Returns the cleavage preferences.
     *
     * @return the cleavage preferences
     */
    public CleavagePreference getCleavagePreference() {
        return cleavagePreference;
    }

    /**
     * Sets the cleavage preferences.
     *
     * @param cleavagePreference the cleavage preferences
     */
    public void setCleavagePreference(CleavagePreference cleavagePreference) {
        this.cleavagePreference = cleavagePreference;
    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        DigestionParameters defaultPreferences = DigestionParameters.getDefaultPreferences();
        StringBuilder stringBuilder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        if (!defaultPreferences.isSameAs(this)) {
            stringBuilder.append("Digestion: ");
            switch (cleavagePreference) { // @TODO: can be null..?
                case wholeProtein:
                    stringBuilder.append("Whole Protein").append(newLine);
                    break;
                case unSpecific:
                    stringBuilder.append("Unspecific").append(newLine);
                    break;
                case enzyme:
                    for (Enzyme enzyme1 : enzymes) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append(newLine);
                        }
                        Enzyme enzyme = enzyme1;
                        String enzymeName = enzyme.getName();
                        stringBuilder.append(enzymeName).append(", ").append(getSpecificity(enzymeName));
                        Integer nmc = getnMissedCleavages(enzymeName);
                        if (nmc != null) {
                            stringBuilder.append(", ").append(nmc).append(" missed cleavages");
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Description not implemented for cleavage preference " + cleavagePreference + ".");
            }
            stringBuilder.append(".").append(newLine);
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
    public boolean isSameAs(DigestionParameters otherDigestionPreferences) {
        if (cleavagePreference != otherDigestionPreferences.getCleavagePreference()) {
            return false;
        }
        ArrayList<Enzyme> otherEnzymes = otherDigestionPreferences.getEnzymes();
        if ((enzymes != null && otherEnzymes == null)
                || (enzymes == null && otherEnzymes != null)) {
            return false;
        }
        if (enzymes != null && otherEnzymes != null) {
            if (enzymes.size() != otherEnzymes.size()) {
                return false;
            }
            ArrayList<String> enzymeNames = new ArrayList<>(enzymes.size());
            for (Enzyme enzyme : enzymes) {
                enzymeNames.add(enzyme.getName());
            }
            ArrayList<String> otherNames = new ArrayList<>(otherEnzymes.size());
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
        }
        return true;
    }

    /**
     * Get the X!Tandem enzyme format.
     *
     * @return the enzyme X!Tandem format as String
     */
    public String getXTandemFormat() {

        switch (cleavagePreference) {
            case wholeProtein:
                return "";
            case unSpecific:
                return "[X]|[X]";
            case enzyme:
                StringBuilder result = new StringBuilder();
                for (Enzyme enzyme : enzymes) {
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    Specificity specificity = getSpecificity(enzyme.getName());
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
                return result.toString();
            default:
                throw new UnsupportedOperationException("X!Tandem format not implemented for cleavage preference " + cleavagePreference + ".");
        }
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

        HashSet<Character> commonRestrictionAfter;
        if (enzymes.size() == 1) {
            commonRestrictionAfter = enzymes.get(0).getRestrictionAfter();
        } else {
            commonRestrictionAfter = new HashSet<>();
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

        HashSet<Character> commonRestrictionBefore;
        if (enzymes.size() == 1) {
            commonRestrictionBefore = enzymes.get(0).getRestrictionBefore();
        } else {
            commonRestrictionBefore = new HashSet<>();
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
