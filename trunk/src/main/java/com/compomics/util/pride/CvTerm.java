
package com.compomics.util.pride;

import java.io.Serializable;

/**
 * A simple CvTerm object.
 * 
 * @author Harald Barsnes
 */
public class CvTerm implements Serializable {
    
    /**
     * serialization number for backward compatibility
     */
    static final long serialVersionUID = -2890434198335005181L;
    /**
     * The ontology.
     */
    private String ontology;
    /**
     * The accession number.
     */
    private String accession;
    /**
     * The name/term.
     */
    private String name;
    /**
     * The value for the given term.
     */
    private String value;
    
    /**
     * Create a new CV term.
     * 
     * @param ontology the ontology
     * @param accession the accession
     * @param name the name
     * @param value the value
     */
    public CvTerm(String ontology, String accession, String name, String value) {
        this.ontology = ontology;
        this.accession = accession;
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the ontology.
     * 
     * @return the ontology
     */
    public String getOntology() {
        return ontology;
    }

    /**
     * Sets the ontology.
     * 
     * @param ontology the ontology to set
     */
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    /**
     * Returns the accession.
     * 
     * @return the accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Sets the accession.
     * 
     * @param accession the accession to set
     */
    public void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
