package com.compomics.util.pride.prideobjects.webservice.protein;

import java.util.ArrayList;
import java.util.List;

/**
 * The PRIDE ProteinDetailList object
 *
 * @author Kenneth Verheggen
 */
public class ProteinDetailList {

    /**
     * A list containing the project summaries
     */
    private List<ProteinDetail> list;

    /**
     * Create a new ProteinDetailList object.
     *
     */
    public ProteinDetailList() {
    }

    /**
     * Returns a list of project summaries
     *
     * @return the list of project summaries
     */
    public List<ProteinDetail> getList() {
        if (list == null) {
            list = new ArrayList<ProteinDetail>();
        }
        return list;
    }

    /**
     * Set the list of project summaries
     *
     * @param list a list with project summaries
     */
    public void setList(List<ProteinDetail> list) {
        this.list = list;
    }
}
