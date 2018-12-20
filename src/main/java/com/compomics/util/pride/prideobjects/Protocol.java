package com.compomics.util.pride.prideobjects;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.pride.PrideObject;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object for storing Protocol details.
 *
 * @author Harald Barsnes
 */
public class Protocol extends DbObject implements PrideObject {

    /**
     * Empty default constructor
     */
    public Protocol() {
    }

    /**
     * The protocol name.
     */
    private String name;
    /**
     * The list of CV terms.
     */
    private ArrayList<CvTerm> cvTerms;

    /**
     * Create a new Protocol object.
     *
     * @param name the name
     * @param cvTerms the CV terms
     */
    public Protocol(String name, ArrayList<CvTerm> cvTerms) {
        this.name = name;
        this.cvTerms = cvTerms;
    }

    /**
     * Returns the protocol name.
     * 
     * @return the name
     */
    public String getName() {
        readDBMode();
        return name;
    }

    /**
     * Set the protocol name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        writeDBMode();
        this.name = name;
    }

    /**
     * Returns the CV terms.
     * 
     * @return the cvTerms
     */
    public ArrayList<CvTerm> getCvTerms() {
        readDBMode();
        return cvTerms;
    }

    /**
     * Set the CV terms.
     * 
     * @param cvTerms the cvTerms to set
     */
    public void setCvTerms(ArrayList<CvTerm> cvTerms) {
        writeDBMode();
        this.cvTerms = cvTerms;
    }

    /**
     * Returns a list of default protocols.
     *
     * @return a list of default protocols
     */
    public static ArrayList<Protocol> getDefaultProtocols() {
        ArrayList<Protocol> result = new ArrayList<>();

        result.add(new Protocol("In Gel Protein Digestion",
                new ArrayList<>(Arrays.asList(
                new CvTerm("PRIDE", "PRIDE:0000025", "Reduction", "DTT"),
                new CvTerm("PRIDE", "PRIDE:0000026", "Alkylation", "iodoacetamide"),
                new CvTerm("PRIDE", "PRIDE:0000160", "Enzyme", "Trypsin")))));

        return result;
    }

    public String getFileName() {
        readDBMode();
        return name;
    }
}
