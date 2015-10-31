package com.compomics.util.pride.prideobjects.webservice.project;

/**
 * The PRIDE Reference object
 *
 * @author Kenneth Verheggen
 */
public class Reference {

    /**
     *
     * Free text description of the reference (usually the publication ref line)
     */
    String desc;
    /**
     *
     * The (prefixed) IDs identifying the reference (prefixes: PMID, PMCID, DOI)
     */
    String[] ids;

    /**
     * Creates a new ProjectDetail object
     *
     */
    public Reference() {
    }

    /**
     * Returns the description
     *
     * @return the description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the description
     *
     * @param desc the description
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Returns the reference ids
     *
     * @return the reference ids
     */
    public String[] getIds() {
        return ids;
    }

    /**
     * Sets the reference ids
     *
     * @param ids the reference ids
     */
    public void setIds(String[] ids) {
        this.ids = ids;
    }

}
