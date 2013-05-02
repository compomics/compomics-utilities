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
 * Created by IntelliJ IDEA. User: Niklaas Colaert Date: 9-jul-2008 Time:
 * 13:53:46
 */
public class PdbBlock {

    // @TODO: add JavaDoc...
    private String iBlock;
    private int iStart_protein;
    private int iEnd_protein;
    private int iStart_block;
    private int iEnd_block;
    private Integer[] iSelectedPositions;
    private boolean iSelection = false;
    private String iUrl;

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

    public String getBlockSequence(String aPdbAccession) {
        String lUrl = "http://www.rcsb.org/pdb/files/fasta.txt?structureIdList=" + aPdbAccession;
        return readUrl(lUrl, aPdbAccession);
    }

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

    public String readFasta(String lFasta, String aPdbAccession) {

        String[] lLines = lFasta.split("\n");
        boolean lSequenceNeeded = false;
        StringBuilder lSequence = new StringBuilder();

        for (int i = 0; i < lLines.length; i++) {
            if (lLines[i].startsWith(">")) {
                //check if we need to read this
                if (lLines[i].indexOf(aPdbAccession + ":" + iBlock + "|") >= 0) {
                    //we need this
                    lSequenceNeeded = true;
                } else {
                    lSequenceNeeded = false;
                }
            } else {
                if (lSequenceNeeded) {
                    lSequence.append(lLines[i]);
                }
            }
        }

        return lSequence.toString();
    }
}
