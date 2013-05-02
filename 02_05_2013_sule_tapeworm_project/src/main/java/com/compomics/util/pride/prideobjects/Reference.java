package com.compomics.util.pride.prideobjects;

import com.compomics.util.pride.PrideObject;
import java.io.Serializable;

/**
 * A simple publication reference object.
 *
 * @author Harald Barsnes
 */
public class Reference implements PrideObject, Serializable {

    /**
     * Serialization number for backward compatibility.
     */
    static final long serialVersionUID = -5449836209751629549L; // @TODO: has to be updated?
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
     * Returns the reference as a string.
     * 
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Set the reference.
     * 
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Returns the PubMed ID of the reference.
     * 
     * @return the pmid
     */
    public String getPmid() {
        return pmid;
    }

    /**
     * Set the PubMed ID of the reference.
     * 
     * @param pmid the pmid to set
     */
    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    /**
     * Returns the Digital Object Identifier (DOI) of the reference.
     * 
     * @return the doi
     */
    public String getDoi() {
        return doi;
    }

    /**
     * Set the Digital Object Identifier (DOI) of the reference.
     * 
     * @param doi the doi to set
     */
    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getFileName() {
        return reference;
    }
}
