package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.identification.advocates.SearchEngine;
import com.compomics.util.experiment.identification.advocates.PostProcessor;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 25, 2010
 * Time: 5:39:12 PM
 * This factory will provide adapted advocates when requested.
 */
public class AdvocateFactory {

    private static AdvocateFactory instance = null;

    private AdvocateFactory() {

    }

    public static AdvocateFactory getInstance() {
        if (instance == null) {
            instance = new AdvocateFactory();
        }
        return instance;
    }

    public com.compomics.util.experiment.identification.Advocate getAdvocate(int index) {
        switch (index) {
            case com.compomics.util.experiment.identification.Advocate.MASCOT:
                return new SearchEngine(com.compomics.util.experiment.identification.Advocate.MASCOT);
            case com.compomics.util.experiment.identification.Advocate.OMSSA:
                return new SearchEngine(com.compomics.util.experiment.identification.Advocate.OMSSA);
            case com.compomics.util.experiment.identification.Advocate.XTANDEM:
                return new SearchEngine(com.compomics.util.experiment.identification.Advocate.XTANDEM);
            case com.compomics.util.experiment.identification.Advocate.PEPTIZER:
                return new PostProcessor(com.compomics.util.experiment.identification.Advocate.PEPTIZER);
            default:
                return null;
        }
    }

    public ArrayList<com.compomics.util.experiment.identification.Advocate> getPossibilities() {
        ArrayList<com.compomics.util.experiment.identification.Advocate> possibilities = new ArrayList<com.compomics.util.experiment.identification.Advocate>();
        possibilities.add(new SearchEngine(com.compomics.util.experiment.identification.Advocate.MASCOT));
        possibilities.add(new SearchEngine(com.compomics.util.experiment.identification.Advocate.OMSSA));
        possibilities.add(new SearchEngine(com.compomics.util.experiment.identification.Advocate.XTANDEM));
        possibilities.add(new PostProcessor(com.compomics.util.experiment.identification.Advocate.PEPTIZER));
        return possibilities;
    }

    public int getAdvocate(String aName) {
        ArrayList<com.compomics.util.experiment.identification.Advocate> possibilities = getPossibilities();
        for (com.compomics.util.experiment.identification.Advocate advocate : possibilities) {
            if (advocate.getName().compareTo(aName)==0) {
                return advocate.getId();
            }
        }
        return -1;
    }
}
