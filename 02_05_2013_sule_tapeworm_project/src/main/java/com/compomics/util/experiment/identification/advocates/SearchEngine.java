package com.compomics.util.experiment.identification.advocates;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models a search engine. For now only Mascot, OMSSA and X!Tandem
 * are implemented.
 *
 * @author Marc Vaudel
 */
public class SearchEngine extends ExperimentObject implements Advocate {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 9162799394758139976L;
    /**
     * Index of the search engine.
     */
    private int id;

    /**
     * Constructor for a search engine.
     */
    public SearchEngine() {
    }

    /**
     * Constructor for a search engine.
     *
     * @param searchEngineId the search engine index
     */
    public SearchEngine(int searchEngineId) {
        id = searchEngineId;
    }

    /**
     * Getter for the search engine name.
     *
     * @param id the id of the search engine
     * @return the search engine name
     */
    public static String getName(int id) {
        switch (id) {
            case MASCOT:
                return "Mascot";
            case OMSSA:
                return "OMSSA";
            case XTANDEM:
                return "X!Tandem";
            case PEPTIZER:
                return "Peptizer";
            case ANDROMEDA:
                return "Andromeda";
            case PEPTIDE_SHAKER:
                return "PeptideShaker";
            case PEPNOVO:
                return "PepNovo";
            default:
                return "Unknown";
        }
    }

    /**
     * Getter for the search engine name.
     *
     * @return the search engine name
     */
    public String getName() {
        return getName(id);
    }

    /**
     * Getter for the search engine index.
     *
     * @return the search engine index
     */
    public int getId() {
        return id;
    }

    /**
     * Indicates whether a search engine score is increasing when the quality of
     * the match increases. Typically false for an e-value or a PEP, true for a
     * confidence.
     *
     * @param id the index of the search engine according to the Advocate static
     * fields
     * @return true if the score is ascending, false otherwise
     */
    public static boolean isAscendingScore(int id) {
        switch (id) {
            case MASCOT:
                return false;
            case OMSSA:
                return false;
            case XTANDEM:
                return false;
            case ANDROMEDA:
                return true;
            case PEPTIDE_SHAKER:
                return true;
            case PEPNOVO:
                return true;
            default:
                return false;
        }
    }
}
