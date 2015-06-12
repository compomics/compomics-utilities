/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.protein_sequences_manager;

import com.compomics.util.protein_sequences_manager.enums.SequenceContentType;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Kenneth
 */
public class UniProtQuery {

    private final SequenceContentType queryType;
    private final int taxonomy;
    private final URL queryURL;

    public UniProtQuery(int taxonomy, SequenceContentType queryType) throws MalformedURLException {
        this.taxonomy = taxonomy;
        this.queryType = queryType;
        this.queryURL = getUniProtQueryURL(taxonomy, queryType);
    }

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

    public SequenceContentType getQueryType() {
        return queryType;
    }

    public int getTaxonomy() {
        return taxonomy;
    }

    public URL getQueryURL() {
        return queryURL;
    }

}
