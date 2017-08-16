package com.compomics.util.experiment.identification.ptm;

import java.util.ArrayList;

/**
 * An enum of the PTM scores.
 *
 * @author Marc Vaudel
 */
public enum PtmScore {

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
    private int id;
    /**
     * Score name.
     */
    private String name;

    /**
     * Constructor.
     *
     * @param id the id number
     * @param name the name
     */
    private PtmScore(int id, String name) {
        this.id = id;
        this.name = name;
    }

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
     * Returns a list of the implemented scores.
     *
     * @return a list of the implemented scores
     */
    public static ArrayList<PtmScore> getImplementedPtmScores() {
        ArrayList<PtmScore> result = new ArrayList<>(2);
        result.add(PhosphoRS);
        result.add(None);
        return result;
    }

    /**
     * Returns the PTM score indexed by the given id.
     *
     * @param id the id number of the PTM score
     * @return the desired PTM score
     */
    public static PtmScore getScore(int id) {
        for (PtmScore ptmScore : getImplementedPtmScores()) {
            if (ptmScore.getId() == id) {
                return ptmScore;
            }
        }
        throw new IllegalArgumentException("PTM score of id " + id + " not recognized.");
    }

    /**
     * Returns the PTM score of the given name.
     *
     * @param name the name of the score
     * @return the desired PTM score
     */
    public static PtmScore getScore(String name) {
        for (PtmScore ptmScore : getImplementedPtmScores()) {
            if (ptmScore.getName().equals(name)) {
                return ptmScore;
            }
        }
        throw new IllegalArgumentException("PTM score of name " + name + " not recognized.");
    }

    /**
     * Returns the different implemented scores as list of command line option.
     *
     * @return the different implemented scores as list of command line option
     */
    public static String getCommandLineOptions() {
        String result = "";
        for (PtmScore ptmScore : getImplementedPtmScores()) {
            if (!result.equals("")) {
                result += ", ";
            }
            result += ptmScore.getId() + ": " + ptmScore.getName();
        }
        return result;
    }

    /**
     * Returns a list containing the names of the implemented scores.
     *
     * @return a list containing the names of the implemented scores
     */
    public static PtmScore[] getScoresAsList() {
        ArrayList<PtmScore> tempScores = getImplementedPtmScores();
        PtmScore[] scores = new PtmScore[tempScores.size()];
        for (int i = 0; i < tempScores.size(); i++) {
            scores[i] = tempScores.get(i);
        }
        return scores;
    }
}
