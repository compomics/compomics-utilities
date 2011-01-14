package com.compomics.util.experiment.identification.advocates;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models a search engine. For now only Mascot, OMSSA and X!Tandem are implemented.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 1:56:59 PM
 */
public class SearchEngine extends ExperimentObject implements Advocate {

    /**
     * index of the search engine
     */
    private int id;

    /**
     * constructor for a search engine
      */
    public SearchEngine() {

    }

    /**
     * constructor for a search engine
     *
     * @param searchEngineId    the search engine index
     */
    public SearchEngine(int searchEngineId) {
        id = searchEngineId;
    }

    /**
     * getter for the search engine name
     *
     * @return the search engine name
     */
    public String getName() {
        switch (id) {
            case MASCOT:
                return "Mascot";
            case OMSSA:
                return "OMSSA";
            case XTANDEM:
                return "X!Tandem";
            default:
                return "Unknown";
        }
    }

    /**
     * getter for the search engine index
     *
     * @return the search engine index
     */
    public int getId() {
        return id;
    }
}
