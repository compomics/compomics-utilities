package com.compomics.util.pride.prideobjects.webservice.peptide;

/**
 * The PRIDE PsmDetail object
 *
 * @author Kenneth Verheggen
 */
public class SearchEngineScore {

    /**
     * the score from the search engine
     */
    String score;
    /**
     * the search engine
     */
    String searchEngine;

    /**
     * Creates a new SearchEngineScore object
     *
     */
    public SearchEngineScore() {
    }

    /**
     * Returns the score
     *
     * @return the score
     */
    public String getScore() {
        return score;
    }

    /**
     * Set the score
     *
     * @param score the score
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * Returns the used search engine
     *
     * @return the used search engine
     */
    public String getSearchEngine() {
        return searchEngine;
    }

    /**
     * Set the used search engine
     *
     * @param searchEngine the used search engine
     */
    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
    }

}
