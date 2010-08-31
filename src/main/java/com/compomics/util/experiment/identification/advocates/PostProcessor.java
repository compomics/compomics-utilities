package com.compomics.util.experiment.identification.advocates;

import com.compomics.util.experiment.identification.Advocate;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 25, 2010
 * Time: 7:58:55 PM
 * This object modelizes a tool which post-processed identifications.
 */
public class PostProcessor implements Advocate {

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
     * @param id    index of the post-processor
     */
    public PostProcessor(int id) {
        this.id=id;
    }

    /**
     * getter for the name of the post-processor
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
     * @return
     */
    public int getId() {
        return id;
    }
}
