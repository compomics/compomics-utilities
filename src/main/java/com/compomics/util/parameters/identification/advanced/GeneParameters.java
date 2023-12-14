package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.io.biology.protein.FastaSummary;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Contains methods for downloading gene and GO mappings.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class GeneParameters extends ExperimentObject {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = -5077297373352021745L;
    /**
     * If true, the gene mappings will auto update.
     */
    private Boolean autoUpdate;
    /**
     * If true the gene mappings will be used.
     */
    private Boolean useGeneMapping;
    /**
     * The Latin name of the species selected as background for the GO analysis.
     */
    private String backgroundSpecies;

    /**
     * Create a new GenePreferences object.
     */
    public GeneParameters() {

    }

    /**
     * Creates new gene preferences based on a GenePreferences object.
     *
     * @param genePreferences the gene preferences
     */
    public GeneParameters(GeneParameters genePreferences) {

    }

    /**
     * Returns a boolean indicating whether gene mappings should be used.
     *
     * @return a boolean indicating whether gene mappings should be used
     */
    public Boolean getUseGeneMapping() {

        if (useGeneMapping == null) {
            useGeneMapping = true;
        }

        return useGeneMapping;

    }

    /**
     * Sets whether gene mappings should be used.
     *
     * @param useGeneMapping a boolean indicating whether gene mappings should
     * be used
     */
    public void setUseGeneMapping(Boolean useGeneMapping) {

        this.useGeneMapping = useGeneMapping;

    }

    /**
     * Indicates whether the gene mappings should be automatically updated.
     *
     * @return a boolean indicating whether the gene mappings should be
     * automatically updated
     */
    public Boolean getAutoUpdate() {

        if (autoUpdate == null) {
            autoUpdate = true;
        }

        return autoUpdate;

    }

    /**
     * Sets whether the gene mappings should be automatically updated.
     *
     * @param autoUpdate a boolean indicating whether the gene mappings should
     * be automatically updated
     */
    public void setAutoUpdate(Boolean autoUpdate) {

        this.autoUpdate = autoUpdate;

    }

    /**
     * Compares these preferences to other preferences.
     *
     * @param genePreferences other preferences to compare to.
     *
     * @return a boolean indicating whether the other preferences are the same
     * as these ones.
     */
    public boolean equals(GeneParameters genePreferences) {

        return getAutoUpdate().equals(genePreferences.getAutoUpdate());

    }

    /**
     * Returns the Latin name of the species selected as background species.
     *
     * @return the Latin name of the species selected as background species
     */
    public String getBackgroundSpecies() {

        return backgroundSpecies;

    }

    /**
     * Sets the taxon of the species selected as background species.
     *
     * @param selectedBackgroundSpecies the Latin name of the species selected
     * as background species
     */
    public void setBackgroundSpecies(String selectedBackgroundSpecies) {

        this.backgroundSpecies = selectedBackgroundSpecies;

    }

    /**
     * Set the background species to the most common species in the FASTA file.
     *
     * @param fastaSummary the FASTA summary
     */
    public void setBackgroundSpeciesFromFastaSummary(FastaSummary fastaSummary) {

        try {

            TreeMap<String, Integer> speciesOccurrence = fastaSummary.speciesOccurrence;
            Integer occurrenceMax = null;

            // select the background species based on occurrence in the factory
            for (Entry<String, Integer> entry : speciesOccurrence.entrySet()) {

                String latinName = entry.getKey();

                if (!latinName.equals(SpeciesFactory.UNKNOWN) && getUseGeneMapping()) {

                    Integer occurrence = entry.getValue();

                    if (occurrenceMax == null || occurrence > occurrenceMax) {

                        occurrenceMax = occurrence;

                        try {
                            
                            String organismNameLowerCase = latinName.toLowerCase().replaceAll(" ", "_");
                            setBackgroundSpecies(organismNameLowerCase);

                        } catch (Exception e) {

                            // taxon not available, ignore
                            e.printStackTrace();

                        }

                    }

                }

            }

        } catch (Exception e) {
            // Not able to read the species, ignore
            e.printStackTrace();
        }

    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("Use Gene Mappings: ").append(getUseGeneMapping()).append(".").append(newLine);
        output.append("Update Gene Mappings: ").append(getAutoUpdate()).append(".").append(newLine);

        if (backgroundSpecies != null) {

            output.append("Species: ").append(backgroundSpecies).append(".").append(newLine);

        } else {

            output.append("Species: ").append("(not selected)").append(newLine);

        }

        return output.toString();
    }

}
