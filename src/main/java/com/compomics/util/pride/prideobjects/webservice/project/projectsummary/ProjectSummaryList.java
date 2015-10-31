package com.compomics.util.pride.prideobjects.webservice.project.projectsummary;

import java.util.ArrayList;
import java.util.List;

/**
 * The PRIDE ProjectSummaryList object
 *
 * @author Kenneth Verheggen
 */
public class ProjectSummaryList {

    /**
     * A list containing the project summaries
     */
    private List<ProjectSummary> list;

    /**
     * Create a new ProjectSummaryList object.
     *
     */
    public ProjectSummaryList() {
    }

    /**
     * Returns a list of project summaries
     *
     * @return the list of project summaries
     */
    public List<ProjectSummary> getList() {
        if (list == null) {
            list = new ArrayList<ProjectSummary>();
        }
        return list;
    }

    /**
     * Set the list of project summaries
     *
     * @param list a list with project summaries
     */
    public void setList(List<ProjectSummary> list) {
        this.list = list;
    }

}
