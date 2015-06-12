package com.compomics.util.protein_sequences_manager;

import com.compomics.util.protein_sequences_manager.enums.SequenceContentType;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A UniProt query.
 *
 * @author Kenneth Verheggen
 */
public class UniProtQuery {

    /**
     * The query type.
     */
    private final SequenceContentType queryType;
    /**
     * The taxonomy.
     */
    private final int taxonomy;
    /**
     * The query URL.
     */
    private final URL queryURL;

    /**
     * Constructor.
     *
     * @param taxonomy the taxonomy
     * @param queryType the query type
     * @throws MalformedURLException thrown if a MalformedURLException occurs
     */
    public UniProtQuery(int taxonomy, SequenceContentType queryType) throws MalformedURLException {
        this.taxonomy = taxonomy;
        this.queryType = queryType;
        this.queryURL = getUniProtQueryURL(taxonomy, queryType);
    }

    /**
     * Get the UniProt query URL.
     *
     * @param taxonomy the taxonomy
     * @param type the type
     * @return the URL
     * @throws MalformedURLException thrown if a MalformedURLException occurs
     */
    private URL getUniProtQueryURL(int taxonomy, SequenceContentType type) throws MalformedURLException {

        String queryPlaceHolder = "@INSERT_QUERY@";
        String uniprotQueryTemplate = "http://www.uniprot.org/uniprot/?query=@INSERT_QUERY@+AND+organism:" + taxonomy;

        if (type.equals(SequenceContentType.REVIEWED)) {
            uniprotQueryTemplate = uniprotQueryTemplate.replace(queryPlaceHolder, "reviewed:yes");
        } else if (type.equals(SequenceContentType.UNREVIEWED)) {
            uniprotQueryTemplate = uniprotQueryTemplate.replace(queryPlaceHolder, "reviewed:no");
        } else if (type.equals(SequenceContentType.REVIEWED_AND_ISOFORMS)) {
            uniprotQueryTemplate = uniprotQueryTemplate.replace(queryPlaceHolder, "reviewed:yes") + "&include:yes";
        } else if (type.equals(SequenceContentType.UNREVIEWED_AND_ISOFORMS)) {
            uniprotQueryTemplate = uniprotQueryTemplate.replace(queryPlaceHolder, "reviewed:no") + "&include:yes";
        } else {
            throw new MalformedURLException("Could not create valid URL for " + taxonomy + " " + type.toString().toLowerCase());
        }

        return new URL(uniprotQueryTemplate + "&format=fasta");
    }

    /**
     * Returns the query type.
     * 
     * @return the query type
     */
    public SequenceContentType getQueryType() {
        return queryType;
    }

    /**
     * Returns the taxonomy.
     * 
     * @return the taxonomy
     */
    public int getTaxonomy() {
        return taxonomy;
    }

    /**
     * Returns the query URL.
     * 
     * @return the query URL
     */
    public URL getQueryURL() {
        return queryURL;
    }
}
