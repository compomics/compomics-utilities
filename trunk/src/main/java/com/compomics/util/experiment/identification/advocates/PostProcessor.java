package com.compomics.util.experiment.identification.advocates;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.utils.ExperimentObject;

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
        this.id=id;
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
