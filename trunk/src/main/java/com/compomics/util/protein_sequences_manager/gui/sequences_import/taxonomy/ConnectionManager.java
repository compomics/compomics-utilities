/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.protein_sequences_manager.gui.taxonomy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author Kenneth
 */
public class ConnectionManager {
    //the spoofed agent to make sure the uniprot host does not disconnect us
    private static final String USER_AGENT = "Mozilla/5.0";
    //the uniprot host name
    private static final String uniprotHost = "http://www.uniprot.org/";
   
    /**
     *
     * @return the uniprot website hostname
     */
    public static String getUniprotHost() {
        return uniprotHost;
    }

    /**
     *
     * @param query the queryterm you wish to search for
     * @param queryType the type of query (taxonomy or fasta)
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection getQueryConnection(String query, QueryType queryType) throws IOException, URISyntaxException {
        //String address = uniprotHost + queryType.getLocation() + "/?query=" + URLEncoder.encode(query, encoding);
        String address = uniprotHost + queryType.getLocation() + "/?query=" + query.replace(" ", "+");
        System.out.println(address);
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //add request header
        con.addRequestProperty("User-Agent", USER_AGENT);
        con.setConnectTimeout(10000);
        return con;
    }

}
