package com.compomics.util.experiment.io.biology.protein;

/**
 * The parameters used to parse a fasta file.
 *
 * @author Marc Vaudel
 */
public class FastaParameters {

    /**
     * The name of the database.
     */
    private String name;
    /**
     * Description of the database.
     */
    private String description;
    /**
     * The version of the database.
     */
    private String version;
    /**
     * Indicates whether the database is a concatenated target/decoy.
     */
    private boolean targetDecoy;
    /**
     * The flag for decoy proteins in the accession.
     */
    private String decoyFlag;
    /**
     * Boolean indicating whether the decoy flag is a suffix or a prefix.
     */
    private boolean decoySuffix;

    /**
     * Returns the decoy flag.
     *
     * @return the decoy flag
     */
    public String getDecoyFlag() {
        return decoyFlag;
    }

    /**
     * Sets the decoy flag.
     *
     * @param decoyFlag the decoy flag
     */
    public void setDecoyFlag(String decoyFlag) {
        this.decoyFlag = decoyFlag;
    }

    /**
     * Returns a boolean indicating whether the decoy flag is a suffix or a
     * prefix.
     *
     * @return a boolean indicating whether the decoy flag is a suffix or a
     * prefix
     */
    public boolean isDecoySuffix() {
        return decoySuffix;
    }

    /**
     * Sets whether the decoy flag is a suffix or a prefix.
     *
     * @param decoySuffix whether the decoy flag is a suffix or a prefix
     */
    public void setDecoySuffix(boolean decoySuffix) {
        this.decoySuffix = decoySuffix;
    }


    /**
     * Returns the name of the database.
     *
     * @return the name for the database
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for the database.
     *
     * @param name a new name for the database
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the database version.
     *
     * @return the database version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the database version.
     *
     * @param version the database version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the description for this database.
     * 
     * @return the description for this database
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this database.
     * 
     * @param description the description for this database
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a boolean indicating whether the database is concatenated target decoy.
     * 
     * @return a boolean indicating whether the database is concatenated target decoy
     */
    public boolean isTargetDecoy() {
        return targetDecoy;
    }

    /**
     * Sets whether the database is concatenated target decoy.
     * 
     * @param targetDecoy a boolean indicating whether the database is concatenated target decoy
     */
    public void setTargetDecoy(boolean targetDecoy) {
        this.targetDecoy = targetDecoy;
    }
    
    public boolean isSameAs(FastaParameters fastaParameters) {
        
        if (name != null && fastaParameters.getName() == null
                || name == null && fastaParameters.getName() != null
                || name != null && fastaParameters.getName() != null && !name.equals(fastaParameters.getName())) {
            
            return false;
            
        }
        if (description != null && fastaParameters.getDescription()== null
                || description == null && fastaParameters.getDescription() != null
                || description != null && fastaParameters.getDescription() != null && !description.equals(fastaParameters.getDescription())) {
            
            return false;
            
        }
        if (version != null && fastaParameters.getVersion()== null
                || version == null && fastaParameters.getVersion() != null
                || version != null && fastaParameters.getVersion() != null && !version.equals(fastaParameters.getVersion())) {
            
            return false;
            
        }
        if (targetDecoy != fastaParameters.isTargetDecoy()) {
            
            return false;
            
        }
        if (decoyFlag != null && fastaParameters.getDecoyFlag()== null
                || decoyFlag == null && fastaParameters.getDecoyFlag() != null
                || decoyFlag != null && fastaParameters.getDecoyFlag() != null && !decoyFlag.equals(fastaParameters.getDecoyFlag())) {
            
            return false;
            
        }
        if (decoySuffix != fastaParameters.isDecoySuffix()) {
            
            return false;
            
        }
        
        return true;
    } 
}
