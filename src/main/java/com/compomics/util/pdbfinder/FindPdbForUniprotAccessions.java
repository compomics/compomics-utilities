package com.compomics.util.pdbfinder;

import com.compomics.util.pdbfinder.das.readers.AlignmentBlock;
import com.compomics.util.pdbfinder.das.readers.DasAlignment;
import com.compomics.util.pdbfinder.das.readers.DasAnnotationServerAlingmentReader;
import com.compomics.util.pdbfinder.pdb.PdbBlock;
import com.compomics.util.pdbfinder.pdb.PdbParameter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * Maps uniprot protein accession numbers to PDB file ids.
 * 
 * @author Niklaas Colaert
 */
public class FindPdbForUniprotAccessions {
    
    /**
     * The protein accession number.
     */
    private String iProteinAccession;
    /**
     * @TODO: JavaDoc missing...
     */
    private DasAlignment[] iAlignments;
    /**
     * @TODO: JavaDoc missing...
     */
    private Vector<PdbParameter> iPdbs = new Vector<PdbParameter>();
    /**
     * @TODO: JavaDoc missing...
     */
    private DasAnnotationServerAlingmentReader iDasReader = new DasAnnotationServerAlingmentReader("empty");
    /**
     * @TODO: JavaDoc missing...
     */
    private String iUrl;
    /**
     * @TODO: JavaDoc missing...
     */
    private boolean isFirstTry = true;
    /**
     * Set to true of the PDB URL could be read, false otherwise.
     */
    private boolean urlRead = false;

    /**
     * @TODO: JavaDoc missing...
     * 
     * @param aProteinAccession 
     */
    public FindPdbForUniprotAccessions(String aProteinAccession) {

        this.iProteinAccession = aProteinAccession;

        //find features
        String urlMake = "http://www.rcsb.org/pdb/rest/das/pdb_uniprot_mapping/alignment?query=" + iProteinAccession;
        readUrl(urlMake);
        iAlignments = iDasReader.getAllAlignments();

        try {
            for (int a = 0; a < iAlignments.length; a++) {
                DasAlignment align = iAlignments[a];
                String pdb = align.getPdbAccession().substring(0, 4);
                pdb = pdb.toUpperCase();
                boolean newPdb = true;
                PdbParameter pdbParamToAddBlock = null;
                for (int v = 0; v < iPdbs.size(); v++) {
                    PdbParameter pdbParam = iPdbs.get(v);
                    if (pdb.equalsIgnoreCase(pdbParam.getPdbaccession())) {
                        newPdb = false;
                        v = iPdbs.size();
                        pdbParamToAddBlock = pdbParam;
                    }
                }

                if (newPdb) {
                    pdbParamToAddBlock = new PdbParameter(pdb, align.getTitle(), align.getExperiment_type(), align.getResolution());
                    for (int i = 0; i < align.getAlignmentBlocks().length; i++) {
                        AlignmentBlock alignBlock = align.getAlignmentBlocks()[i];
                        PdbBlock block = new PdbBlock(alignBlock.getPdbAccession().substring(5), alignBlock.getSpStart(), alignBlock.getSpEnd(), alignBlock.getPdbStart(), alignBlock.getPdbEnd());
                        pdbParamToAddBlock.addBlock(block);
                    }
                    iPdbs.add(pdbParamToAddBlock);
                } else {
                    for (int i = 0; i < align.getAlignmentBlocks().length; i++) {
                        AlignmentBlock alignBlock = align.getAlignmentBlocks()[i];
                        PdbBlock block = new PdbBlock(alignBlock.getPdbAccession().substring(5), alignBlock.getSpStart(), alignBlock.getSpEnd(), alignBlock.getPdbStart(), alignBlock.getPdbEnd());
                        pdbParamToAddBlock.addBlock(block);
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Error in reading das pdb alignment");
            e.printStackTrace();
        }
    }
    
    /**
     * Returns true if the PDB URL was read, false otherwise.
     * 
     * @return true if the PDB URL was read, false otherwise
     */
    public boolean urlWasRead() {
        return urlRead;
    }

    /**
     * Returns a vector of the PDB files mapped to the given protein 
     * accession number.
     * 
     * @return a vector of the PDB files
     */
    public Vector<PdbParameter> getPdbs() {
        return iPdbs;
    }

    /**
     * Tries to read the PDB URL.
     * 
     * @param aUrl the PDB URL to read
     */
    private void readUrl(String aUrl) {
        
        urlRead = false;
        
        this.iUrl = aUrl;
        
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

            iDasReader = new DasAnnotationServerAlingmentReader(input.toString());
            urlRead = true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println("Connect exception for url " + iUrl);
            if (isFirstTry) {
                this.readUrl(iUrl);
            }
            isFirstTry = false;
        } catch (IOException e) {
            System.out.println("I/O exception for url " + iUrl);
        }
    }

    /**
     * Main method. Used for testing purposes.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        String lAccession = "O75369";
        FindPdbForUniprotAccessions lF = new FindPdbForUniprotAccessions(lAccession);

        System.out.println("Found " + lF.getPdbs().size() + " pdf file(s) for " + lAccession);
        
        for (int i = 0; i < lF.getPdbs().size(); i++) {
            PdbParameter lParam = lF.getPdbs().get(i);
            System.out.println((i + 1) + ". " + lParam.getPdbaccession() + " : " + lParam.getTitle() + " (" + lParam.getExperiment_type() + ") " + lParam.getResolution());
            System.out.println("\t\tDownload from : " + "http://www.rcsb.org/pdb/files/" + lParam.getPdbaccession() + ".pdb");
            System.out.println(lParam.getBlocks().length + " block(s) found in this pdf file");
            PdbBlock[] lBlocks = lParam.getBlocks();
            
            for (int j = 0; j < lBlocks.length; j++) {
                System.out.println("\tBlock : " + lBlocks[j].getBlock());
                System.out.println("\tAlignment between uniprot protein sequence and sequences in this block");
                System.out.println("\t\tStart block " + lBlocks[j].getStart_block() + " <=> Start protein " + lBlocks[j].getStart_protein());
                System.out.println("\t\tEnd block " + lBlocks[j].getEnd_block() + " <=> End protein " + lBlocks[j].getEnd_protein());
            }
            
            System.out.println("\n");
        }
    }
}
