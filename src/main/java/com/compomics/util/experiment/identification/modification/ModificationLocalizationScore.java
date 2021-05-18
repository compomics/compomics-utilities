package com.compomics.util.experiment.identification.modification;

import com.compomics.util.math.BasicMathFunctions;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An enum of the modification localization scores.
 *
 * @author Marc Vaudel
 */
public enum ModificationLocalizationScore {

    /**
     * The PhosphoRS score.
     */
    PhosphoRS(1, "PhosphoRS"),
    /**
     * No probabilistic score.
     */
    None(2, "None");
    /**
     * Score id number.
     */
    private final int id;
    /**
     * Score name.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param id the id number
     * @param name the name
     */
    private ModificationLocalizationScore(
            int id,
            String name
    ) {

        this.id = id;
        this.name = name;

    }

    @Override
    public String toString() {

        return name;

    }

    /**
     * Returns the id number of the score.
     *
     * @return the id number of the score
     */
    public int getId() {

        return id;

    }

    /**
     * Returns the name of the score.
     *
     * @return the name of the score
     */
    public String getName() {

        return name;

    }

    /**
     * Returns the PTM score indexed by the given id.
     *
     * @param id the id number of the PTM score
     * @return the desired PTM score
     */
    public static ModificationLocalizationScore getScore(
            int id
    ) {

        for (ModificationLocalizationScore ptmScore : values()) {

            if (ptmScore.getId() == id) {

                return ptmScore;

            }
        }

        throw new IllegalArgumentException("Modification localization score id " + id + " not recognized.");

    }

    /**
     * Returns the PTM score of the given name.
     *
     * @param name the name of the score
     * @return the desired PTM score
     */
    public static ModificationLocalizationScore getScore(
            String name
    ) {

        for (ModificationLocalizationScore ptmScore : values()) {

            if (ptmScore.getName().equals(name)) {

                return ptmScore;

            }
        }

        throw new IllegalArgumentException("Modification localization score name " + name + " not recognized.");

    }

    /**
     * Returns the different implemented scores as list of command line option.
     *
     * @return the different implemented scores as list of command line option
     */
    public static String getCommandLineOptions() {

        return Arrays.stream(values())
                .map(score -> score.getId() + ": " + score.getName())
                .collect(Collectors.joining(","));

    }

    /**
     * Returns the threshold (inclusive) to use to consider an assignment
     * random. E.g. PhosphoRS with one modifications in two sites, a score of
     * 50% is considered random. Returns Double.NaN if the number of
     * modifications equals zero or the number of sites.
     *
     * @param nModifications The number of modifications.
     * @param nSites The number of sites.
     *
     * @return the threshold to use to consider an assignment random
     */
    public double getRandomThreshold(
            int nModifications,
            int nSites
    ) {

        if (nModifications == 0 || nModifications == nSites) {

            return Double.NaN;

        }

        switch (this) {

            case PhosphoRS:
                return BasicMathFunctions.getOneOverCombinationDouble(nModifications, nSites);

            default:
                throw new UnsupportedOperationException("Threshold not implemented for score " + this + ".");

        }

    }
}
