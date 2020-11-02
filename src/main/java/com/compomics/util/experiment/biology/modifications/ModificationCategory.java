package com.compomics.util.experiment.biology.modifications;

/**
 * Enum for the different modification categories supported.
 *
 * @author Harald Barsnes
 */
public enum ModificationCategory {

    /**
     * Common fixed and variable modifications.
     */
    Common,
    /**
     * Common biological modifications.
     */
    Common_Biological,
    /**
     * Common chemical artifacts.
     */
    Common_Artifact,
    /**
     * Modifications including metals.
     */
    Metal,
    /**
     * Glycosylation.
     */
    Glyco,
    /**
     * Less common modifications.
     */
    Less_Common,
    /**
     * Modifications as part of labeling.
     */
    Labeling,
    /**
     * Nucleotide substitutions that can be done by changing one nucleotide.
     */
    Nucleotide_Substitution_One,
    /**
     * Nucleotide substitutions that require changing two or more nucleotide.
     */
    Nucleotide_Substitution_TwoPlus,
    /**
     * Modifications not fitting in any of the other categories.
     */
    Other;

    @Override
    public String toString() {

        switch (this) {
            case Common:
                return "Common Fixed and Variable";
            case Common_Biological:
                return "Common Biological";
            case Common_Artifact:
                return "Common Artifact";
            case Metal:
                return "Metal";
            case Glyco:
                return "Glycosylation";
            case Less_Common:
                return "Less Common";
            case Labeling:
                return "Labeling";
            case Nucleotide_Substitution_One:
                return "Substitution (1 Nucleotide)";
            case Nucleotide_Substitution_TwoPlus:
                return "Substitution (2+ Nucleotides)";
            case Other:
                return "Other";
            default:
                throw new UnsupportedOperationException(
                        "Modification category " + this.name() + " not implemented.");
        }

    }

    /**
     * Returns all of the modification category options as a string.
     *
     * @return the modification category options
     */
    public static String getCategoriesAsString() {

        StringBuilder modCategoriesAsString = new StringBuilder();

        for (ModificationCategory modCategory : values()) {
            if (modCategoriesAsString.length() > 0) {
                modCategoriesAsString.append(", ");
            }
            modCategoriesAsString.append(modCategory);
        }

        modCategoriesAsString.append(".");

        return modCategoriesAsString.toString();

    }

}
