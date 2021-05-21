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
//            //this.header = iAlignment.substring(iAlignment.indexOf("property=\"header\">") + 18,iAlignment.indexOf("<",iAlignment.indexOf("property=\"header\">")));
//            this.title = iAlignment.substring(iAlignment.indexOf("property=\"title\">") + 17, iAlignment.indexOf("<", iAlignment.indexOf("property=\"title\">")));

            this.pdbAccession = iAlignment.get("pdb_id").getAsString();
            this.spAccession = iAlignment.get("chain_id").getAsString();
            this.title = this.getChainTitle(pdbAccession);
//            //System.out.println(iAlignment);
//            //this.resolution = iAlignment.substring(iAlignment.indexOf("property=\"resolution\">") + 22,iAlignment.indexOf("<",iAlignment.indexOf("property=\"resolution\">")));
//            this.experiment_type = iAlignment.substring(iAlignment.indexOf("property=\"method\">") + 18, iAlignment.indexOf("<", iAlignment.indexOf("property=\"method\">")));
//            //this.header = iAlignment.substring(iAlignment.indexOf("property=\"header\">") + 18,iAlignment.indexOf("<",iAlignment.indexOf("property=\"header\">")));
//            this.title = iAlignment.substring(iAlignment.indexOf("property=\"title\">") + 17, iAlignment.indexOf("<", iAlignment.indexOf("property=\"title\">")));
//            this.pdbAccession = iAlignment.substring(iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28, iAlignment.indexOf("\"", iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28));
//            this.spAccession = iAlignment.substring(iAlignment.indexOf("<alignObject dbAccessionId=\"", iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28) + 28, iAlignment.indexOf("\"", iAlignment.indexOf("<alignObject dbAccessionId=\"", iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28) + 28));
            int startSp = iAlignment.get("unp_start").getAsInt();
            int endSp = iAlignment.get("unp_end").getAsInt();

            int startPdb = iAlignment.get("start").getAsInt();
            int endPdb = iAlignment.get("end").getAsInt();
            AlignmentBlock align = new AlignmentBlock(startPdb, endPdb, startSp, endSp, pdbAccession, spAccession);
            Vector block = new Vector();
            block.add(align);
//            int startBlock = 0;
//            try {
//                while (iAlignment.indexOf("<block", startBlock) > -1) {
//                    String blockStr = iAlignment.substring(iAlignment.indexOf(">", iAlignment.indexOf("<block", startBlock)), iAlignment.indexOf("</block", startBlock));
//                    startBlock = iAlignment.indexOf("</block", startBlock) + 5;
//                    String segment1 = blockStr.substring(blockStr.indexOf("<segment"), blockStr.indexOf(">", blockStr.indexOf("<segment")) + 1);
//                    int segment1End = blockStr.indexOf(">", blockStr.indexOf("<segment")) + 1;
//                    String segment2 = blockStr.substring(blockStr.indexOf("<segment", segment1End), blockStr.indexOf(">", blockStr.indexOf("<segment", segment1End)) + 1);
//                    String pdbA = segment1.substring(segment1.indexOf("Id=\"") + 4, segment1.indexOf("\"", segment1.indexOf("Id=\"") + 4));
//                    String startStrPdb = segment1.substring(segment1.indexOf("rt=\"") + 4, segment1.indexOf("\"", segment1.indexOf("rt=\"") + 4));
//                    String endStrPdb = segment1.substring(segment1.indexOf("nd=\"") + 4, segment1.indexOf("\"", segment1.indexOf("nd=\"") + 4));
//                    int startPdb = Integer.valueOf(startStrPdb);
//                    int endPdb = Integer.valueOf(endStrPdb);
//                    String spA = segment2.substring(segment2.indexOf("Id=\"") + 4, segment2.indexOf("\"", segment2.indexOf("Id=\"") + 4));
//                    String startStrSp = segment2.substring(segment2.indexOf("rt=\"") + 4, segment2.indexOf("\"", segment2.indexOf("rt=\"") + 4));
//                    String endStrSp = segment2.substring(segment2.indexOf("nd=\"") + 4, segment2.indexOf("\"", segment2.indexOf("nd=\"") + 4));
//                    int startSp = Integer.valueOf(startStrSp);
//                    int endSp = Integer.valueOf(endStrSp);
//
//                    AlignmentBlock align = new AlignmentBlock(startPdb, endPdb, startSp, endSp, pdbA, spA);
//                    block.add(align);
//                }
//            } catch (NumberFormatException e) {
//                System.out.println("Number format exception for pdb alignment!");
//            }
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
            String title = obj.getAsJsonArray(aPdbAccession).get(0).getAsJsonObject().get("title").getAsString();
            return title;

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
