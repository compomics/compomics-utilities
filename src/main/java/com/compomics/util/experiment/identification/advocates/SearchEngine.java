package com.compomics.util.experiment.identification.advocates;

import com.compomics.util.experiment.identification.Advocate;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 1:56:59 PM
 * This class modelizes a search engine. For now only Mascot, OMSSA and X!Tandem are implemented.
 */
public class SearchEngine implements Advocate, Serializable {


    // Attributes

    private int id;


    // constructor

    public SearchEngine() {

    }

    public SearchEngine(int searchEngineId) {
        id = searchEngineId;
    }


    // methods

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

    public int getId() {
        return id;
    }

}
