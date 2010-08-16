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

    private int id;

    public PostProcessor() {

    }

    public PostProcessor(int id) {
        this.id=id;
    }

    public String getName() {
        switch (id) {
            case Advocate.PEPTIZER:
                return "Peptizer";
            default:
                return "unknown";
        }
    }

    public int getId() {
        return id;
    }
}
