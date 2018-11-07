package com.compomics.util.pdbfinder.pdb;

/**
 * PdbParameter.
 *
 * @author Niklaas Colaert
 */
public class PdbParameter {

    /**
     * Empty default constructor
     */
    public PdbParameter() {
    }

    /**
     * The PDB accession.
     */
    private String iPdbaccession;
    /**
     * The title.
     */
    private String iTitle;
    /**
     * The experiment type.
     */
    private String iExperiment_type;
    /**
     * The resolution.
     */
    private String iResolution;
    /**
     * The blocks.
     */
    private PdbBlock[] blocks = new PdbBlock[0];

    /**
     * Constructor.
     *
     * @param aPdbaccession the PDB accession
     * @param aTitle the title
     * @param aExperiment_type the experiment type
     * @param aResolution the resolution
     */
    public PdbParameter(String aPdbaccession, String aTitle, String aExperiment_type, String aResolution) {
        this.iPdbaccession = aPdbaccession;
        this.iTitle = aTitle;
        this.iExperiment_type = aExperiment_type;
        this.iResolution = aResolution;
    }

    /**
     * Returns the blocks.
     *
     * @return the blocks
     */
    public PdbBlock[] getBlocks() {
        return blocks;
    }

    /**
     * Set the blocks.
     *
     * @param blocks the blocks
     */
    public void setBlocks(PdbBlock[] blocks) {
        this.blocks = blocks;
    }

    /**
     * Add a block.
     *
     * @param block the block
     */
    public void addBlock(PdbBlock block) {
        PdbBlock[] blocksAdded = new PdbBlock[blocks.length + 1];
        System.arraycopy(blocks, 0, blocksAdded, 0, blocks.length);
        blocksAdded[blocks.length] = block;
        blocks = blocksAdded;
    }

    /**
     * Returns the PDB accessions.
     *
     * @return the PDB accessions
     */
    public String getPdbaccession() {
        return iPdbaccession;
    }

    /**
     * Set the PDB accession.
     *
     * @param aPdbaccession the PDB accession
     */
    public void setPdbaccession(String aPdbaccession) {
        this.iPdbaccession = aPdbaccession;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return iTitle;
    }

    /**
     * Set the title.
     *
     * @param aTitle the title
     */
    public void setTitle(String aTitle) {
        this.iTitle = aTitle;
    }

    /**
     * Returns the experiment type.
     *
     * @return the experiment type
     */
    public String getExperiment_type() {
        return iExperiment_type;
    }

    /**
     * Set the experiment type.
     *
     * @param aExperiment_type the experiment type
     */
    public void setExperiment_type(String aExperiment_type) {
        this.iExperiment_type = aExperiment_type;
    }

    /**
     * Returns the resolution.
     *
     * @return the resolution
     */
    public String getResolution() {
        return iResolution;
    }

    /**
     * Set the resolution.
     *
     * @param aResolution the resolution
     */
    public void setResolution(String aResolution) {
        this.iResolution = aResolution;
    }
}
