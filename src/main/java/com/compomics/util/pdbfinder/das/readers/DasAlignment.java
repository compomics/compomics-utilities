package com.compomics.util.pdbfinder.das.readers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import javax.net.ssl.HttpsURLConnection;

/**
 * DasAlignment.
 *
 * @author Niklaas Colaert
 * @author Yehia Farag
 */
public class DasAlignment {

    /**
     * Empty default constructor
     */
    public DasAlignment() {
    }

    /**
     * The alignment.
     */
    private JsonObject iAlignment;
    /**
     * The resolution.
     */
    private String resolution = "";
    /**
     * The experiment type.
     */
    private String experiment_type = "";
    /**
     * The header.
     */
    private String header = "";
    /**
     * The title.
     */
    private String title = "";
    /**
     * The PDB accession.
     */
    private String pdbAccession = "";
    /**
     * The SP accession.
     */
    private String spAccession = "";
    /**
     * The alignment blocks.
     */
    private AlignmentBlock[] alignmentBlocks;
    /**
     * The PDB group.
     */
    private String pdbGroup = "";

    /**
     * Constructor.
     *
     * @param aDasAlignment the DAS alignment
     */
    public DasAlignment(JsonObject aDasAlignment) {
        try {
            this.iAlignment = aDasAlignment;
            this.experiment_type = iAlignment.get("experimental_method").getAsString();

            this.pdbAccession = iAlignment.get("pdb_id").getAsString();
            this.spAccession = iAlignment.get("chain_id").getAsString();
            this.title = this.getChainTitle(pdbAccession);
            int startSp = iAlignment.get("unp_start").getAsInt();
            int endSp = iAlignment.get("unp_end").getAsInt();

            int startPdb = iAlignment.get("start").getAsInt();
            int endPdb = iAlignment.get("end").getAsInt();
            AlignmentBlock align = new AlignmentBlock(startPdb, endPdb, startSp, endSp, pdbAccession, spAccession);
            Vector block = new Vector();
            block.add(align);

            alignmentBlocks = new AlignmentBlock[block.size()];
            block.toArray(alignmentBlocks);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Error in reading das - pdb alignment");
        }
    }

    /**
     * Returns the resolution.
     *
     * @return the resolution
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Returns the experiment type.
     *
     * @return the experiment type
     */
    public String getExperimentType() {
        return experiment_type;
    }

    /**
     * Returns the header.
     *
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the PDB accession.
     *
     * @return the PDB accession
     */
    public String getPdbAccession() {
        return pdbAccession;
    }

    /**
     * Returns the SP accession.
     *
     * @return the SP accession
     */
    public String getSpAccession() {
        return spAccession;
    }

    /**
     * Returns the alignment blocks.
     *
     * @return the alignment blocks
     */
    public AlignmentBlock[] getAlignmentBlocks() {
        return alignmentBlocks;
    }

    /**
     * Returns the PDB groups.
     *
     * @return the PDB groups
     */
    public String getPdbGroup() {
        pdbGroup = pdbAccession.substring(pdbAccession.indexOf(".") + 1);
        pdbGroup = pdbGroup.toLowerCase();
        return pdbGroup;
    }

    /**
     * Get the chain title.
     *
     * @param aPdbAccession the PDB accession
     * @return the chain title
     */
    private String getChainTitle(String aPdbAccession) {
        String lUrl = "https://www.ebi.ac.uk/pdbe/api/pdb/entry/summary/" + aPdbAccession + "";
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

        try {
            URL myURL = new URL(aUrl);
            StringBuilder input = new StringBuilder();
            HttpsURLConnection c = (HttpsURLConnection) myURL.openConnection();
            BufferedInputStream in = new BufferedInputStream(c.getInputStream());
            Reader r = new InputStreamReader(in);

            int i;

            while ((i = r.read()) != -1) {
                input.append((char) i);
            }

            r.close();
            in.close();
            JsonObject obj = new Gson().fromJson(input.toString(), JsonObject.class);
            String tempTitle = obj.getAsJsonArray(aPdbAccession).get(0).getAsJsonObject().get("title").getAsString();
            return tempTitle;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println("Connect exception for url " + aUrl);
        } catch (IOException e) {
            System.out.println("I/O exception for url " + aUrl);
        }

        return "";
    }
}
