
package com.compomics.util.pride;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * A simple CvTerm object.
 * 
 * @author Harald Barsnes
 */
public class CvTerm extends ExperimentObject {
    
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
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return ontology;
    }

    /**
     * Sets the ontology.
     * 
     * @param ontology the ontology to set
     */
    public void setOntology(String ontology) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.ontology = ontology;
    }

    /**
     * Returns the accession.
     * 
     * @return the accession
     */
    public String getAccession() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return accession;
    }

    /**
     * Sets the accession.
     * 
     * @param accession the accession to set
     */
    public void setAccession(String accession) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.accession = accession;
    }

    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getName() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.name = name;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public String getValue() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value the value to set
     */
    public void setValue(String value) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.value = value;
    }
}
