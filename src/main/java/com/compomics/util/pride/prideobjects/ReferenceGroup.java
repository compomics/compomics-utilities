package com.compomics.util.pride.prideobjects;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.pride.PrideObject;
import java.util.ArrayList;

/**
 * An object for storing ReferenceGroup details.
 *
 * @author Harald Barsnes
 */
public class ReferenceGroup extends DbObject implements PrideObject {

    /**
     * Empty default constructor
     */
    public ReferenceGroup() {
    }

    /**
     * The references.
     */
    private ArrayList<Reference> references;
    /**
     * The reference group name.
     */
    private String groupName;

    /**
     * Create a new ReferenceGroup object.
     *
     * @param references the list of references
     * @param groupName the reference group name
     */
    public ReferenceGroup(ArrayList<Reference> references, String groupName) {
        this.references = references;
        this.groupName = groupName;
    }

    /**
     * Returns the references.
     *
     * @return the name
     */
    public ArrayList<Reference> getReferences() {
        readDBMode();
        return references;
    }

    /**
     * Set the references.
     *
     * @param references the references
     */
    public void setReferences(ArrayList<Reference> references) {
        writeDBMode();
        this.references = references;
    }

    /**
     * Returns the references group name.
     *
     * @return the group name
     */
    public String getName() {
        readDBMode();
        return groupName;
    }

    /**
     * Set the references group name.
     *
     * @param groupName the group name to set
     */
    public void setName(String groupName) {
        writeDBMode();
        this.groupName = groupName;
    }
    
    /**
     * Returns the default references.
     *
     * @return the default references
     */
    public static ArrayList<ReferenceGroup> getDefaultReferences() {
        ArrayList<ReferenceGroup> result = new ArrayList<>();
        return result;
    }

    public String getFileName() {
        readDBMode();
        return groupName;
    }
}
