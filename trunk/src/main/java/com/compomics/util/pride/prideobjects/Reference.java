package com.compomics.util.pride.prideobjects;

import com.compomics.util.pride.PrideObject;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple publication reference object.
 *
 * @author Harald Barsnes
 */
public class Reference implements PrideObject, Serializable {

    /**
     * serialization number for backward compatibility
     */
    static final long serialVersionUID = -5449836209751629549L;
    /**
     * The reference text.
     */
    private String reference;
    /**
     * The PubMed ID.
     */
    private String pmid;
    /**
     * The Digital Object Identifier.
     */
    private String doi;

    /**
     * Create a new Reference object.
     *
     * @param reference
     * @param pmid
     * @param doi
     */
    public Reference(String reference, String pmid, String doi) {
        this.reference = reference;
        this.pmid = pmid;
        this.doi = doi;
    }

    /**
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return the pmid
     */
    public String getPmid() {
        return pmid;
    }

    /**
     * @param pmid the pmid to set
     */
    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    /**
     * @return the doi
     */
    public String getDoi() {
        return doi;
    }

    /**
     * @param doi the doi to set
     */
    public void setDoi(String doi) {
        this.doi = doi;
    }

    /**
     * Returns the default references
     *
     * @return the default references
     */
    public static ArrayList<Reference> getDefaultReferences() {
        ArrayList<Reference> result = new ArrayList<Reference>();
        //result.add(new Reference("PeptideShaker", "not available", "not available"));
        return result;
    }

    public String getFileName() {
        return reference;
    }
}
