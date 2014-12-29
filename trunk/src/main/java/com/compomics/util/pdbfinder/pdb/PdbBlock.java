package com.compomics.util.pdbfinder.pdb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * PdbBlock.
 *
 * @author Niklaas Colaert
 */
public class PdbBlock {

    /**
     * The block.
     */
    private String iBlock;
    /**
     * The protein start.
     */
    private int iStartProtein;
    /**
     * The protein end.
     */
    private int iEndProtein;
    /**
     * The block start.
     */
    private int iStartBlock;
    /**
     * The block end.
     */
    private int iEndBlock;
    /**
     * The selected positions.
     */
    private Integer[] iSelectedPositions;
    /**
     * True if there is a selection.
     */
    private boolean iSelection = false;
    /**
     * The URL.
     */
    private String iUrl;

    /**
     * Constructor.
     * 
     * @param aBlock the block
     * @param aStart_protein the protein start position
     * @param aEnd_protein the protein end position
     * @param aStart_block the block start position
     * @param aEnd_block the block end position
     */
    public PdbBlock(String aBlock, int aStart_protein, int aEnd_protein, int aStart_block, int aEnd_block) {
        this.iBlock = aBlock;
        this.iStartProtein = aStart_protein;
        this.iEndProtein = aEnd_protein;
        this.iStartBlock = aStart_block;
        this.iEndBlock = aEnd_block;
    }

    /**
     * Returns the block.
     * 
     * @return block
     */
    public String getBlock() {
        return iBlock;
    }

    /**
     * Sets the block.
     * 
     * @param aBlock the block
     */
    public void setBlock(String aBlock) {
        this.iBlock = aBlock;
    }

    /**
     * Returns the protein start.
     * 
     * @return the protein start
     */
    public int getStartProtein() {
        return iStartProtein;
    }

    /**
     * Set the protein start.
     * 
     * @param aStartProtein the protein start
     */
    public void setStartProtein(int aStartProtein) {
        this.iStartProtein = aStartProtein;
    }

    /**
     * Returns the protein end.
     * 
     * @return the protein end
     */
    public int getEndProtein() {
        return iEndProtein;
    }

    /**
     * Set the protein end.
     * 
     * @param aEndProtein the protein end
     */
    public void setEndProtein(int aEndProtein) {
        this.iEndProtein = aEndProtein;
    }

    /**
     * Returns the block start.
     * 
     * @return the block start
     */
    public int getStartBlock() {
        return iStartBlock;
    }

    /**
     * Set the block start.
     * 
     * @param aStartBlock the start block
     */
    public void setStartBlock(int aStartBlock) {
        this.iStartBlock = aStartBlock;
    }

    /**
     * Returns the block end.
     * 
     * @return the block end
     */
    public int getEndBlock() {
        return iEndBlock;
    }

    /**
     * Set the block end.
     * 
     * @param aEndBlock the end block
     */
    public void setEndBlock(int aEndBlock) {
        this.iEndBlock = aEndBlock;
    }

    /**
     * Returns the difference.
     * 
     * @return the difference
     */
    public int getDifference() {
        int diff = iStartProtein - iStartBlock;
        return diff;
    }

    /**
     * Returns true if there is a selection.
     * 
     * @return true if there is a selection.
     */
    public boolean getSelection() {
        return iSelection;
    }

    /**
     * Returns the selected positions.
     * 
     * @return the selected positions
     */
    public Integer[] getSelectedPositions() {
        return iSelectedPositions;
    }

    /**
     * Set the selected positions.
     * 
     * @param aSelectedPositions the selected positions
     */
    public void setSelectedPositions(Integer[] aSelectedPositions) {
        this.iSelectedPositions = aSelectedPositions;
        iSelection = true;
    }

    /**
     * Get the blocked sequence.
     * 
     * @param aPdbAccession the PDB accession
     * @return the blocked sequence
     */
    public String getBlockSequence(String aPdbAccession) {
        String lUrl = "http://www.rcsb.org/pdb/files/fasta.txt?structureIdList=" + aPdbAccession;
        return readUrl(lUrl, aPdbAccession);
    }

    /**
     * Read a URL.
     * 
     * @param aUrl the URL
     * @param aPdbAccession the PDB accession
     * @return the sequence
     */
    public String readUrl(String aUrl, String aPdbAccession) {

        this.iUrl = aUrl;
        String lSequence = null;

        try {
            URL myURL = new URL(aUrl);
            StringBuilder input = new StringBuilder();
            HttpURLConnection c = (HttpURLConnection) myURL.openConnection();
            BufferedInputStream in = new BufferedInputStream(c.getInputStream());
            Reader r = new InputStreamReader(in);

            int i;

            while ((i = r.read()) != -1) {
                input.append((char) i);
            }

            r.close();
            in.close();

            lSequence = readFasta(input.toString(), aPdbAccession);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println("Connect exception for url " + iUrl);
        } catch (IOException e) {
            System.out.println("I/O exception for url " + iUrl);
        }

        return lSequence;
    }

    /**
     * Read a FASTA.
     * 
     * @param lFasta the FASTA entry
     * @param aPdbAccession the PDB accession
     * @return the sequence
     */
    public String readFasta(String lFasta, String aPdbAccession) {

        String[] lLines = lFasta.split("\n");
        boolean lSequenceNeeded = false;
        StringBuilder lSequence = new StringBuilder();
        for (String lLine : lLines) {
            if (lLine.startsWith(">")) {
                //check if we need to read this
                if (lLine.contains(aPdbAccession + ":" + iBlock + "|")) {
                    //we need this
                    lSequenceNeeded = true;
                } else {
                    lSequenceNeeded = false;
                }
            } else {
                if (lSequenceNeeded) {
                    lSequence.append(lLine);
                }
            }
        }

        return lSequence.toString();
    }
}
