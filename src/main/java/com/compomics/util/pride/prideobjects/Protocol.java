package com.compomics.util.pride.prideobjects;

import com.compomics.util.pride.CvTerm;
import com.compomics.util.pride.PrideObject;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object for storing Protocol details.
 *
 * @author Harald Barsnes
 */
public class Protocol implements PrideObject, Serializable {

    /**
     * serialization number for backward compatibility
     */
    static final long serialVersionUID = -9045298216154032632L;
    /**
     * The protcol name.
     */
    private String name;
    /**
     * The list of CV terms.
     */
    private ArrayList<CvTerm> cvTerms;

    /**
     * Create a new Protocol object.
     *
     * @param name
     * @param cvTerms
     */
    public Protocol(String name, ArrayList<CvTerm> cvTerms) {
        this.name = name;
        this.cvTerms = cvTerms;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cvTerms
     */
    public ArrayList<CvTerm> getCvTerms() {
        return cvTerms;
    }

    /**
     * @param cvTerms the cvTerms to set
     */
    public void setCvTerms(ArrayList<CvTerm> cvTerms) {
        this.cvTerms = cvTerms;
    }

    /**
     * Returns a list of default protocols
     *
     * @return a list of default protocols
     */
    public static ArrayList<Protocol> getDefaultProtocols() {
        ArrayList<Protocol> result = new ArrayList<Protocol>();

        result.add(new Protocol("In Gel Protein Digestion",
                new ArrayList<CvTerm>(Arrays.asList(
                new CvTerm("PRIDE", "PRIDE:0000025", "Reduction", "DTT"),
                new CvTerm("PRIDE", "PRIDE:0000026", "Alkylation", "iodoacetamide"),
                new CvTerm("PRIDE", "PRIDE:0000160", "Enzyme", "Trypsin")))));

        return result;
    }

    public String getFileName() {
        return name;
    }
}
