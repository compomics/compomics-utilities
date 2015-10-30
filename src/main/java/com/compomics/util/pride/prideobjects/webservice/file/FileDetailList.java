package com.compomics.util.pride.prideobjects.webservice.file;

import java.util.ArrayList;
import java.util.List;

/**
 * The PRIDE FileDetailList object
 *
 * @author Kenneth Verheggen
 */
public class FileDetailList {

    /**
     * A list containing the file details
     */
    private List<FileDetail> list;

    /**
     * Create a new FileDetailList object.
     *
     */
    public FileDetailList() {
    }
    /* Returns a list of file details
     *
     * @return the list of file details
     */

    public List<FileDetail> getList() {
        if (list == null) {
            list = new ArrayList<FileDetail>();
        }
        return list;
    }

    /**
     * Set the list of file details
     *
     * @param list a list with file details
     */
    public void setList(List<FileDetail> list) {
        this.list = list;
    }
}
