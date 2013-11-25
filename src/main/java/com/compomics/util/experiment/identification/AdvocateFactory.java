package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.identification.advocates.SpectrumIdentificationAlgorithm;
import com.compomics.util.experiment.identification.advocates.PostProcessor;

import java.util.ArrayList;

/**
 * This factory will provide adapted advocates when required. Could be simpler, sorry that's old stuff
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 25, 2010
 * Time: 5:39:12 PM
 */
public class AdvocateFactory {

    /**
     * The instance of the factory
     */
    private static AdvocateFactory instance = null;

    /**
     * The constructor of the factory
     */
    private AdvocateFactory() {

    }

    /**
     * A static method to retrieve the factory instance
     *
     * @return the factory instance
     */
    public static AdvocateFactory getInstance() {
        if (instance == null) {
            instance = new AdvocateFactory();
        }
        return instance;
    }

    /**
     * Returns an advocate of the specified index
     *
     * @param index an advocate index
     * @return an advocate of the specified index
     */
    public Advocate getAdvocate(int index) {
        switch (index) {
            case Advocate.MASCOT:
                return new SpectrumIdentificationAlgorithm(Advocate.MASCOT);
            case Advocate.OMSSA:
                return new SpectrumIdentificationAlgorithm(Advocate.OMSSA);
            case Advocate.XTANDEM:
                return new SpectrumIdentificationAlgorithm(Advocate.XTANDEM);
            case Advocate.ANDROMEDA:
                return new SpectrumIdentificationAlgorithm(Advocate.ANDROMEDA);
            case Advocate.PEPTIZER:
                return new PostProcessor(Advocate.PEPTIZER);
            case Advocate.PEPTIDE_SHAKER:
                return new PostProcessor(Advocate.PEPTIDE_SHAKER);
            default:
                return null;
        }
    }

    /**
     * returns all implemented advocates
     *
     * @return all implemented advocates
     */
    public ArrayList<Advocate> getPossibilities() {
        ArrayList<Advocate> possibilities = new ArrayList<Advocate>();
        possibilities.add(new SpectrumIdentificationAlgorithm(Advocate.MASCOT));
        possibilities.add(new SpectrumIdentificationAlgorithm(Advocate.OMSSA));
        possibilities.add(new SpectrumIdentificationAlgorithm(Advocate.XTANDEM));
        possibilities.add(new SpectrumIdentificationAlgorithm(Advocate.ANDROMEDA));
        possibilities.add(new PostProcessor(Advocate.PEPTIZER));
        possibilities.add(new PostProcessor(Advocate.PEPTIDE_SHAKER));
        return possibilities;
    }

    /**
     * returns the index of an advocate base on its name
     * 
     * @param aName the name of an advocate
     * @return the corresponding index
     */
    public int getAdvocate(String aName) {
        ArrayList<Advocate> possibilities = getPossibilities();
        for (Advocate advocate : possibilities) {
            if (advocate.getName().compareTo(aName)==0) {
                return advocate.getId();
            }
        }
        return -1;
    }
}
