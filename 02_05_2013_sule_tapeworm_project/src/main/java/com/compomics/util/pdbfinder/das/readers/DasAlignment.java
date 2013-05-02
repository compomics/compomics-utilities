package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 25-jan-2008
 * Time: 9:09:14
 */
public class DasAlignment {
    
    // @TODO: add JavaDoc...

    private String iAlignment;
    //feature elements
    private String resolution = "";
    private String experiment_type = "";
    private String header = "";
    private String title = "";
    private String pdbAccession = "";
    private String spAccession = "";
    private AlignmentBlock[] alignmentBlocks;
    private String pdbGroup = "";

    public DasAlignment(String aDasAlignment) {
        try {
            this.iAlignment = aDasAlignment;
            //System.out.println(iAlignment);
            //this.resolution = iAlignment.substring(iAlignment.indexOf("property=\"resolution\">") + 22,iAlignment.indexOf("<",iAlignment.indexOf("property=\"resolution\">")));
            this.experiment_type = iAlignment.substring(iAlignment.indexOf("property=\"method\">") + 18, iAlignment.indexOf("<", iAlignment.indexOf("property=\"method\">")));
            //this.header = iAlignment.substring(iAlignment.indexOf("property=\"header\">") + 18,iAlignment.indexOf("<",iAlignment.indexOf("property=\"header\">")));
            this.title = iAlignment.substring(iAlignment.indexOf("property=\"title\">") + 17, iAlignment.indexOf("<", iAlignment.indexOf("property=\"title\">")));
            this.pdbAccession = iAlignment.substring(iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28, iAlignment.indexOf("\"", iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28));
            this.spAccession = iAlignment.substring(iAlignment.indexOf("<alignObject dbAccessionId=\"", iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28) + 28, iAlignment.indexOf("\"", iAlignment.indexOf("<alignObject dbAccessionId=\"", iAlignment.indexOf("<alignObject dbAccessionId=\"") + 28) + 28));

            Vector block = new Vector();
            int startBlock = 0;
            try {
                while (iAlignment.indexOf("<block", startBlock) > -1) {
                    String blockStr = iAlignment.substring(iAlignment.indexOf(">", iAlignment.indexOf("<block", startBlock)), iAlignment.indexOf("</block", startBlock));
                    startBlock = iAlignment.indexOf("</block", startBlock) + 5;
                    String segment1 = blockStr.substring(blockStr.indexOf("<segment"), blockStr.indexOf(">", blockStr.indexOf("<segment")) + 1);
                    int segment1End = blockStr.indexOf(">", blockStr.indexOf("<segment")) + 1;
                    String segment2 = blockStr.substring(blockStr.indexOf("<segment", segment1End), blockStr.indexOf(">", blockStr.indexOf("<segment", segment1End)) + 1);
                    String pdbA = segment1.substring(segment1.indexOf("Id=\"") + 4, segment1.indexOf("\"", segment1.indexOf("Id=\"") + 4));
                    String startStrPdb = segment1.substring(segment1.indexOf("rt=\"") + 4, segment1.indexOf("\"", segment1.indexOf("rt=\"") + 4));
                    String endStrPdb = segment1.substring(segment1.indexOf("nd=\"") + 4, segment1.indexOf("\"", segment1.indexOf("nd=\"") + 4));
                    int startPdb = Integer.valueOf(startStrPdb);
                    int endPdb = Integer.valueOf(endStrPdb);
                    String spA = segment2.substring(segment2.indexOf("Id=\"") + 4, segment2.indexOf("\"", segment2.indexOf("Id=\"") + 4));
                    String startStrSp = segment2.substring(segment2.indexOf("rt=\"") + 4, segment2.indexOf("\"", segment2.indexOf("rt=\"") + 4));
                    String endStrSp = segment2.substring(segment2.indexOf("nd=\"") + 4, segment2.indexOf("\"", segment2.indexOf("nd=\"") + 4));
                    int startSp = Integer.valueOf(startStrSp);
                    int endSp = Integer.valueOf(endStrSp);

                    AlignmentBlock align = new AlignmentBlock(startPdb, endPdb, startSp, endSp, pdbA, spA);
                    block.add(align);
                }
            } catch (NumberFormatException e) {
                System.out.println("Number format exception for pdb alignment!");
            }
            alignmentBlocks = new AlignmentBlock[block.size()];
            block.toArray(alignmentBlocks);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Error in reading das - pdb alignment");
        }
    }
    //getters

    public String getResolution() {
        return resolution;
    }

    public String getExperiment_type() {
        return experiment_type;
    }

    public String getHeader() {
        return header;
    }

    public String getTitle() {
        return title;
    }

    public String getPdbAccession() {
        return pdbAccession;
    }

    public String getSpAccession() {
        return spAccession;
    }

    public AlignmentBlock[] getAlignmentBlocks() {
        return alignmentBlocks;
    }

    public String getPdbGroup() {
        pdbGroup = pdbAccession.substring(pdbAccession.indexOf(".") + 1);
        pdbGroup = pdbGroup.toLowerCase();
        return pdbGroup;
    }
}
