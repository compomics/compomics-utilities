package com.compomics.util.pride.prideobjects.webservice.assay;

import java.util.ArrayList;
import java.util.List;

/**
 * The PRIDE AssayDetailList object
 *
 * @author Kenneth Verheggen
 */
public class AssayDetailList {

    /**
     * A list containing the assay details
     */
    private List<AssayDetail> list;

    /**
     * Create a new AssayDetailList object.
     *
     */
    public AssayDetailList() {
    }

    /**
     * Returns a list of assay details
     *
     * @return the list of assay details
     */
    public List<AssayDetail> getList() {
        if (list == null) {
            list = new ArrayList<AssayDetail>();
        }
        return list;
    }
    

    /**
     * Set the list of assay details
     *
     * @param list a list with assay details
     */
    public void setList(List<AssayDetail> list) {
        this.list = list;
    }
}
