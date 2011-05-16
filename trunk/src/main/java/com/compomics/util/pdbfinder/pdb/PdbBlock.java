package com.compomics.util.pdbfinder.pdb;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 9-jul-2008
 * Time: 13:53:46
 */
public class PdbBlock {

    private String iBlock;
    private int iStart_protein;
    private int iEnd_protein;
    private int iStart_block;
    private int iEnd_block;
    private Integer[] iSelectedPositions;
    private boolean iSelection = false;

    public PdbBlock(String aBlock, int aStart_protein, int aEnd_protein, int aStart_block, int aEnd_block) {
        this.iBlock = aBlock;
        this.iStart_protein = aStart_protein;
        this.iEnd_protein = aEnd_protein;
        this.iStart_block = aStart_block;
        this.iEnd_block = aEnd_block;
    }

    public String getBlock() {
        return iBlock;
    }

    public void setBlock(String aBlock) {
        this.iBlock = aBlock;
    }

    public int getStart_protein() {
        return iStart_protein;
    }

    public void setStart_protein(int aStart_protein) {
        this.iStart_protein = aStart_protein;
    }

    public int getEnd_protein() {
        return iEnd_protein;
    }

    public void setEnd_protein(int aEnd_protein) {
        this.iEnd_protein = aEnd_protein;
    }

    public int getStart_block() {
        return iStart_block;
    }

    public void setStart_block(int aStart_block) {
        this.iStart_block = aStart_block;
    }

    public int getEnd_block() {
        return iEnd_block;
    }

    public void setEnd_block(int aEnd_block) {
        this.iEnd_block = aEnd_block;
    }

    public int getDifference() {
        int diff = iStart_protein - iStart_block;
        return diff;
    }

    public boolean getSelection() {
        return iSelection;
    }

    public Integer[] getSelectedPositions() {
        return iSelectedPositions;
    }

    public void setSelectedPositions(Integer[] aSelectedPositions) {
        this.iSelectedPositions = aSelectedPositions;
        iSelection = true;
    }
}
