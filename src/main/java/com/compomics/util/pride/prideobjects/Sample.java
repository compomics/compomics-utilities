package com.compomics.util.pride.prideobjects;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.pride.PrideObject;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object for storing Sample details.
 *
 * @author Harald Barsnes
 */
public class Sample extends DbObject implements PrideObject {

    /**
     * Empty default constructor
     */
    public Sample() {
    }

    /**
     * The sample name.
     */
    private String name;
    /**
     * The list of CV terms.
     */
    private ArrayList<CvTerm> cvTerms;

    /**
     * Create a new Sample object.
     *
     * @param name the name
     * @param cvTerms the CV terms
     */
    public Sample(String name, ArrayList<CvTerm> cvTerms) {
        this.name = name;
        this.cvTerms = cvTerms;
    }

    /**
     * Returns the name of the sample.
     * 
     * @return the name
     */
    public String getName() {
        readDBMode();
        return name;
    }

    /**
     * Set the name of the sample.
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
     * Returns a list of default samples.
     *
     * @return a list of default samples
     */
    public static ArrayList<Sample> getDefaultSamples() {
        ArrayList<Sample> result = new ArrayList<>();
        result.add(new Sample("Example sample set", new ArrayList<>(Arrays.asList(
                new CvTerm("NEWT", "9606", "Homo sapiens (Human)", null),
                new CvTerm("BTO", "BTO:0000763", "lung", null),
                new CvTerm("BTO", "BTO:0000762", "lung cancer cell line", null)))));
        return result;
    }

    public String getFileName() {
        readDBMode();
        return name;
    }
}
