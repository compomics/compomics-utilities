package com.compomics.util.pdbfinder.pdb;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 9-jul-2008
 * Time: 13:53:14
 */
public class PdbParameter {

    private String iPdbaccession;
    private String iTitle;
    private String iExperiment_type;
    private String iResolution;
    private PdbBlock[] blocks = new PdbBlock[0];

    public PdbParameter(String aPdbaccession, String aTitle, String aExperiment_type, String aResolution) {
        this.iPdbaccession = aPdbaccession;
        this.iTitle = aTitle;
        this.iExperiment_type = aExperiment_type;
        this.iResolution = aResolution;
    }

    public PdbBlock[] getBlocks() {
        return blocks;
    }

    public void setBlocks(PdbBlock[] blocks) {
        this.blocks = blocks;
    }

    public void addBlock(PdbBlock block) {
        PdbBlock[] blocksAdded = new PdbBlock[blocks.length + 1];
        for (int b = 0; b < blocks.length; b++) {
            blocksAdded[b] = blocks[b];
        }
        blocksAdded[blocks.length] = block;
        blocks = blocksAdded;
    }

    public String getPdbaccession() {
        return iPdbaccession;
    }

    public void setPdbaccession(String aPdbaccession) {
        this.iPdbaccession = aPdbaccession;
    }

    public String getTitle() {
        return iTitle;
    }

    public void setTitle(String aTitle) {
        this.iTitle = aTitle;
    }

    public String getExperiment_type() {
        return iExperiment_type;
    }

    public void setExperiment_type(String aExperiment_type) {
        this.iExperiment_type = aExperiment_type;
    }

    public String getResolution() {
        return iResolution;
    }

    public void setResolution(String aResolution) {
        this.iResolution = aResolution;
    }
}