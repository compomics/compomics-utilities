package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * DasFeature.
 *
 * @author Niklaas Colaert
 */
public class DasFeature {

    /**
     * The feature.
     */
    private String iFeature;
    /**
     * The feature ID.
     */
    private String featureId;
    /**
     * The feature label.
     */
    private String featureLabel;
    /**
     * The type ID.
     */
    private String typeId;
    /**
     * The type category.
     */
    private String typeCategory;
    /**
     * The type reference.
     */
    private String typeReference;
    /**
     * The type sub parts.
     */
    private String typeSubparts;
    /**
     * The type super parts. 
     */
    private String typeSuperparts;
    /**
     * The type.
     */
    private String type;
    /**
     * The method ID.
     */
    private String methodId;
    /**
     * The method.
     */
    private String method;
    /**
     * The start.
     */
    private int start;
    /**
     * The end.
     */
    private int end;
    /**
     * The score.
     */
    private double score;
    /**
     * The orientation.
     */
    private String orientation;
    /**
     * The phase.
     */
    private String phase;
    /**
     * The notes.
     */
    private String[] note;
    /**
     * The links.
     */
    private String[] link;
    /**
     * The link hrefs.
     */
    private String[] linkHref;
    /**
     * True if valid.
     */
    private boolean valid = true;

    /**
     * Constructor.
     * 
     * @param aFeature the feature
     */
    public DasFeature(String aFeature) {
        this.iFeature = aFeature;
        this.featureId = iFeature.substring(iFeature.indexOf("id=") + 4, iFeature.indexOf("label") - 2);
        this.featureLabel = iFeature.substring(iFeature.indexOf("label=") + 7, iFeature.indexOf("\">"));
        if (featureLabel.equalsIgnoreCase("Invalid segment")) {
            valid = false;
            return;
        }
        this.typeId = iFeature.substring(iFeature.indexOf("id=", iFeature.indexOf("<TYPE")) + 4, iFeature.indexOf("\"", iFeature.indexOf("\"", iFeature.indexOf("<TYPE")) + 1));
        if (iFeature.contains("category")) {
            this.typeCategory = iFeature.substring(iFeature.indexOf("category=") + 10, (iFeature.indexOf("\"", iFeature.indexOf("category=") + 10)));
        }
        if (iFeature.contains("reference")) {
            this.typeReference = iFeature.substring(iFeature.indexOf("reference=") + 11, (iFeature.indexOf("\"", iFeature.indexOf("reference=") + 11)));
        }
        if (iFeature.contains("subparts")) {
            this.typeSubparts = iFeature.substring(iFeature.indexOf("subparts=") + 10, (iFeature.indexOf("\"", iFeature.indexOf("subparts=") + 10)));
        }
        if (iFeature.contains("superparts")) {
            this.typeSuperparts = iFeature.substring(iFeature.indexOf("superparts=") + 12, (iFeature.indexOf("\"", iFeature.indexOf("superparts=") + 12)));
        }
        this.type = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<TYPE")) + 1, iFeature.indexOf("</TYPE"));
        this.methodId = iFeature.substring(iFeature.indexOf("id=", iFeature.indexOf("<METHOD")) + 4, iFeature.indexOf("\">", iFeature.indexOf("<METHOD")));
        this.method = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<METHOD")) + 1, iFeature.indexOf("</METHOD"));
        Integer strt = new Integer(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<START")) + 1, iFeature.indexOf("</START")));
        this.start = strt;
        Integer nd = new Integer(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<END")) + 1, iFeature.indexOf("</END")));
        this.end = nd;
        if (iFeature.contains("score")) {
            Double scr = new Double(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<SCORE")) + 1, iFeature.indexOf("</SCORE")));
            this.score = scr;
        }

        if (iFeature.contains("<ORIENTATION")) {
            this.orientation = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<ORIENTATION")) + 1, iFeature.indexOf("</ORIENTATION"));
        }
        if (iFeature.contains("<PHASE")) {
            this.phase = iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<PHASE")) + 1, iFeature.indexOf("</PHASE"));
        }
        if (iFeature.contains("<NOTE")) {
            int lastNoteFound = 0;
            Vector notes = new Vector();
            while (iFeature.indexOf("<NOTE", lastNoteFound) != -1) {
                notes.add(iFeature.substring(iFeature.indexOf(">", iFeature.indexOf("<NOTE", lastNoteFound)) + 1, iFeature.indexOf("</NOTE", lastNoteFound)));
                lastNoteFound = iFeature.indexOf("</NOTE", lastNoteFound) + 6;
            }
            note = new String[notes.size()];
            notes.toArray(note);
        }
        if (iFeature.contains("<LINK")) {
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

    /**
     * Returns the feature ID.
     * 
     * @return the feature ID
     */
    public String getFeatureId() {
        return this.featureId;
    }

    /**
     * Returns the feature label.
     * 
     * @return the feature label
     */
    public String getFeatureLabel() {
        return this.featureLabel;
    }
    
    /**
     * Returns the type ID.
     * 
     * @return the type ID
     */
    public String getTypeId() {
        return this.typeId;
    }

    /**
     * Returns the type category.
     * 
     * @return the type category
     */
    public String getTypeCategory() {
        return this.typeCategory;
    }

    /**
     * Returns the type reference.
     * 
     * @return the type reference
     */
    public String getTypeReference() {
        return this.typeReference;
    }

    /**
     * Returns the type subparts.
     * 
     * @return the type subparts.
     */
    public String getTypeSubparts() {
        return this.typeSubparts;
    }

    /**
     * Returns the type super parts.
     * 
     * @return the type super parts
     */
    public String getTypeSuperparts() {
        return this.typeSuperparts;
    }

    /**
     * Returns the type.
     * 
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the method ID.
     * 
     * @return the method ID
     */
    public String getMethodId() {
        return this.methodId;
    }

    /**
     * Returns the method.
     * 
     * @return method
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Returns the start.
     * 
     * @return start
     */
    public int getStart() {
        return this.start;
    }

    /**
     * Returns the end.
     * 
     * @return end
     */
    public int getEnd() {
        return this.end;
    }

    /**
     * Returns the score.
     * 
     * @return score
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Returns the orientation.
     * 
     * @return orientation
     */
    public String getOrientation() {
        return this.orientation;
    }

    /**
     * Returns the phase.
     * 
     * @return phase
     */
    public String getPhase() {
        return this.phase;
    }

    /**
     * Returns the note.
     * 
     * @return note
     */
    public String[] getNote() {
        return this.note;
    }

    /**
     * Returns the link.
     * 
     * @return link
     */
    public String[] getLink() {
        return this.link;
    }

    /**
     * Returns the linkHref.
     * 
     * @return linkHref
     */
    public String[] getLinkHref() {
        return this.linkHref;
    }

    public String toString() {
        String result;
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
            for (String note1 : note) {
                result = result + "\nNote: " + note1;
            }
        }
        if (link != null) {
            for (int i = 0; i < link.length; i++) {
                result = result + "\nLink: " + link[i] + " (" + linkHref[i] + ")";
            }
        }

        return result;
    }

    /**
     * Returns true if valid.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }
}
