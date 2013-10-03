package com.compomics.util.experiment.identification.advocates;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This object models a tool which post-processed identifications.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 25, 2010
 * Time: 7:58:55 PM
 */
public class PostProcessor extends ExperimentObject implements Advocate {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 1892975432623296983L;
    /**
     * index of the post-processor
     */
    private int id;

    /**
     * contructor for a post-processor
     */
    public PostProcessor() {
    }

    /**
     * constructor for a post-processor
     *
     * @param id    index of the post-processor
     */
    public PostProcessor(int id) {
        this.id = id;
    }

    /**
     * getter for the name of the post-processor
     *
     * @return the name of the post-processor
     */
    public String getName() {
        switch (id) {
            case Advocate.PEPTIZER:
                return "Peptizer";
            case Advocate.PEPTIDE_SHAKER:
                return "PeptideShaker";
            default:
                return "unknown";
        }
    }

    /**
     * getter for the post-processor's index
     * 
     * @return the post-processor's index
     */
    public int getId() {
        return id;
    }
}