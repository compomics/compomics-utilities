package com.compomics.util.pride.prideobjects.webservice.peptide;

import java.util.ArrayList;
import java.util.List;

/**
 * The PRIDE PsmDetail object
 *
 * @author Kenneth Verheggen
 */
public class PsmDetailList {

    /**
     * A list containing the assay details
     */
    private List<PsmDetail> list;

    /**
     * Create a new PsmDetailList object.
     *
     */
    public PsmDetailList() {
    }

    /**
     * Returns a list of psm details
     *
     * @return the list of psm details
     */
    public List<PsmDetail> getList() {
        if (list == null) {
            list = new ArrayList<PsmDetail>();
        }
        return list;
    }

    /**
     * Set the list of psm details
     *
     * @param list a list with psm details
     */
    public void setList(List<PsmDetail> list) {
        this.list = list;
    }
}
