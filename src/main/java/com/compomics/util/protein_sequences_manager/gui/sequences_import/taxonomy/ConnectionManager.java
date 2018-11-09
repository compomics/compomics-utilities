package com.compomics.util.protein_sequences_manager.gui.sequences_import.taxonomy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Connection manager.
 * 
 * @author Kenneth Verheggen
 */
public class ConnectionManager {

    /**
     * The spoofed agent to make sure the uniprot host does not disconnect us.
     */
    private static final String USER_AGENT = "Mozilla/5.0";
    /**
     * The UniProt host name.
     */
    private static final String uniprotHost = "https://www.uniprot.org/";

    /**
     * Returns the UniProt web site host name.
     * 
     * @return the UniProt web site host name
     */
    public static String getUniprotHost() {
        return uniprotHost;
    }

    /**
     * Returns the query connection.
     * 
     * @param query the query term you wish to search for
     * @param queryType the type of query (taxonomy or FASTA)
     * @return the query connection
     * @throws IOException if an IOException is thrown
     * @throws URISyntaxException if an URISyntaxException is thrown
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
