package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 22-jan-2008
 * Time: 14:07:47
 */
public class DasFeature {
    
    // @TODO: add JavaDoc...

    private String iFeature;
    
    //feature elements
    private String featureId;
    private String featureLabel;
    private String typeId;
    private String typeCategory;
    private String typeReference;
    private String typeSubparts;
    private String typeSuperparts;
    private String type;
    private String methodId;
    private String method;
    private int start;
    private int end;
    private double score;
    private String orientation;
    private String phase;
    private String[] note;
    private String[] link;
    private String[] linkHref;
    private boolean valid = true;

    public DasFeature(String aFeature) {
        this.iFeature = aFeature;
        this.featureId = iFeature.substring(iFeature.indexOf("id=") + 4, iFeature.indexOf("label") - 2);
        this.featureLabel = iFeature.substring(iFeature.indexOf("label=") + 7, iFeature.indexOf("\">"));
        if (featureLabel.equalsIgnoreCase("Invalid segment")) {
            valid = false;
            return;
        }
        this.typeId = iFeature.substring(iFeature.indexOf("id=", iFeature.indexOf("<TYPE")) + 4, iFeature.indexOf("\"", iFeature.indexOf("\"", iFeature.indexOf("<TYPE")) + 1));
        if (iFeature.indexOf("category") != -1) {
            this.typeCategory = iFeature.substring(iFeature.indexOf("category=") + 10, (iFeature.indexOf("\"", iFeature.indexOf("category=") + 10)));
        }
        if (iFeature.indexOf("reference") != -1) {
            this.typeReference = iFeature.substring(iFeature.indexOf("reference=") + 11, (iFeature.indexOf("\"", iFeature.indexOf("reference=") + 11)));
        }
        if (iFeature.indexOf("subparts") != -1) {
            this.typeSubparts = iFeature.substring(iFeature.indexOf("subparts=") + 10, (iFeature.indexOf("\"", iFeature.indexOf("subparts=") + 10)));
        }
        if (iFeature.indexOf("superparts") != -1) {
            this.typeSuperparts = iFeature.substring(iFeature.indexOf("superparts=") + 12, (iFeature.indexOf("\"", iFeature.indexOf("superparts=") + 12)));
        }
        this.type = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<TYPE")) + 1, iFeature.indexOf("</TYPE"));
        this.methodId = iFeature.substring(iFeature.indexOf("id=", iFeature.indexOf("<METHOD")) + 4, iFeature.indexOf("\">", iFeature.indexOf("<METHOD")));
        this.method = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<METHOD")) + 1, iFeature.indexOf("</METHOD"));
        Integer strt = new Integer(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<START")) + 1, iFeature.indexOf("</START")));
        this.start = strt;
        Integer nd = new Integer(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<END")) + 1, iFeature.indexOf("</END")));
        this.end = nd;
        if (iFeature.indexOf("score") != -1) {
            Double scr = new Double(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<SCORE")) + 1, iFeature.indexOf("</SCORE")));
            this.score = scr;
        }

        if (iFeature.indexOf("<ORIENTATION") != -1) {
            this.orientation = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<ORIENTATION")) + 1, iFeature.indexOf("</ORIENTATION"));
        }
        if (iFeature.indexOf("<PHASE") != -1) {
            this.phase = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<PHASE")) + 1, iFeature.indexOf("</PHASE"));
        }
        if (iFeature.indexOf("<NOTE") != -1) {
            int lastNoteFound = 0;
            Vector notes = new Vector();
            while (iFeature.indexOf("<NOTE", lastNoteFound) != -1) {
                notes.add(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<NOTE", lastNoteFound)) + 1, iFeature.indexOf("</NOTE", lastNoteFound)));
                lastNoteFound = iFeature.indexOf("</NOTE", lastNoteFound) + 6;
            }
            note = new String[notes.size()];
            notes.toArray(note);
        }
        if (iFeature.indexOf("<LINK") != -1) {
            int lastLinkFound = 0;
            Vector links = new Vector();
            Vector linksRef = new Vector();
            while (iFeature.indexOf("<LINK", lastLinkFound) != -1) {
                links.add(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<LINK", lastLinkFound)) + 1, iFeature.indexOf("</LINK", lastLinkFound)));
                linksRef.add(iFeature.substring(iFeature.indexOf("href", iFeature.indexOf("<LINK", lastLinkFound)) + 6, iFeature.indexOf(">", iFeature.indexOf("<LINK", lastLinkFound)) - 1));
                lastLinkFound = iFeature.indexOf("</LINK", lastLinkFound) + 6;
            }
            link = new String[links.size()];
            links.toArray(link);
            linkHref = new String[linksRef.size()];
            linksRef.toArray(linkHref);
        }
    }

    //getters
    public String getFeatureId() {
        return this.featureId;
    }

    public String getFeatureLabel() {
        return this.featureLabel;
    }

    public String getTypeId() {
        return this.typeId;
    }

    public String getTypeCategory() {
        return this.typeCategory;
    }

    public String getTypeReference() {
        return this.typeReference;
    }

    public String getTypeSubparts() {
        return this.typeSubparts;
    }

    public String getTypeSuperparts() {
        return this.typeSuperparts;
    }

    public String getType() {
        return this.type;
    }

    public String getMethodId() {
        return this.methodId;
    }

    public String getMethod() {
        return this.method;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public double getScore() {
        return this.score;
    }

    public String getOrientation() {
        return this.orientation;
    }

    public String getPhase() {
        return this.phase;
    }

    public String[] getNote() {
        return this.note;
    }

    public String[] getLink() {
        return this.link;
    }

    public String[] getLinkHref() {
        return this.linkHref;
    }

    //toString method
    public String toString() {
        String result = "";
        result = "Feature id: " + featureId + " feature label: " + featureLabel;
        result = result + "\nTypeid: " + typeId + " type catagory: " + typeCategory + " type reference: " + typeReference 
                + " type subparts: " + typeSubparts + " type superparts: " + typeSuperparts + " type: " + type;
        result = result + "\nMethodid: " + methodId + " method: " + method;
        result = result + "\nStart: " + start;
        result = result + "\nEnd: " + end;
        result = result + "\nScore: " + score;
        result = result + "\nOrientation: " + orientation;
        result = result + "\nPhase: " + phase;
        if (note != null) {
            for (int i = 0; i < note.length; i++) {
                result = result + "\nNote: " + note[i];
            }
        }
        if (link != null) {
            for (int i = 0; i < link.length; i++) {
                result = result + "\nLink: " + link[i] + " (" + linkHref[i] + ")";
            }
        }

        return result;
    }

    public boolean isValid() {
        return valid;
    }
}
