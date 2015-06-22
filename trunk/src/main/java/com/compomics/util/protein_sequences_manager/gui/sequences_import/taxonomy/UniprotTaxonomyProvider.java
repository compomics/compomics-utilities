/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.protein_sequences_manager.gui.taxonomy;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Kenneth
 */
public class UniprotTaxonomyProvider {
    //the lowest layer of taxonomy 
    private static DefaultMutableTreeNode rootNode;
    //the lineages for a certain taxonomy
    private final HashSet<String> lineages = new HashSet<String>();
    //the default JTreeModel
    private final DefaultTreeModel model;
    //a cache of taxonomies to increase speed and reduce server load
    private final HashMap<String, String> cachedTaxonomies = new HashMap<String, String>();

    /**
     * Gui Constructor
     *
     * @param model the treemodel you wish to have updated
     */
    public UniprotTaxonomyProvider(DefaultTreeModel model) {
        rootNode = (DefaultMutableTreeNode) model.getRoot();
        this.model = model;
    }

    /**
     * Normal constructor, guiless mode
     */
    public UniprotTaxonomyProvider() {
        rootNode = new DefaultMutableTreeNode("root");
        model = new DefaultTreeModel(rootNode);
    }

    /**
     *
     * @param taxonomyName
     * @return all the children names for a given taxonomy name (or identifier)
     * @throws MalformedURLException
     * @throws IOException
     */
    public List<String> getChildTaxonomies(String taxonomyName) throws MalformedURLException, IOException {
        List<String> childrenTaxonomies = new ArrayList<String>();
        String url = ConnectionManager.getUniprotHost() + "taxonomy/?query=\"" + taxonomyName + "\"&format=tab";
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String lineage;
            String taxName;
            String taxID;
            String headers = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                String[] split = inputLine.split("\t");
                taxName = split[2];
                taxID = split[0];
                lineage = split[8] + ";" + taxName;
                lineages.add(lineage);
                cachedTaxonomies.put(taxName, taxID);
                childrenTaxonomies.add(taxName);
            }
        } catch (ArrayIndexOutOfBoundsException e) {

//this was an endpoint
        } catch (IOException e) {

        } finally {
            if (in != null) {
                in.close();
            }
        }
        return childrenTaxonomies;
    }

    /**
     *
     * @param taxonomyName
     * @return the possible lineages for a given taxonomy
     * @throws MalformedURLException
     * @throws IOException
     * @throws java.net.URISyntaxException
     */
    public Set<String> getLineagesForTaxonomyID(String taxonomyName) throws MalformedURLException, IOException, IllegalArgumentException, URISyntaxException {
        int maxPerQuery = 20000;
        String query = "\"" + taxonomyName + "\"" + "&sort=score&format=tab";
        URLConnection connection = ConnectionManager.getQueryConnection(query, QueryType.TAXONOMY);
        HashMap<String, String> tempTaxIdMap = new HashMap<String, String>();
        List<String> tempLineages = new ArrayList<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            String lineage;
            String taxName;
            String taxID;
            String headers = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                String[] split = inputLine.split("\t");
                taxName = split[2];
                taxID = split[0];
                lineage = split[8] + ";" + taxName;
                tempLineages.add(lineage);
                tempTaxIdMap.put(taxName, taxID);
                if (tempLineages.size() > maxPerQuery) {
                    //throw exception
                    throw new IllegalArgumentException("There were over " + maxPerQuery + " lineages found for this query. Try a more specific search or browse manually");
                }
            }
            lineages.addAll(tempLineages);
            cachedTaxonomies.putAll(tempTaxIdMap);

        } catch (ArrayIndexOutOfBoundsException e) {
//this was an endpoint
//            e.printStackTrace();
        } catch (IOException e) {

        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lineages;
    }

    /**
     *
     * @param taxonomyFile taxonomy file from the uniprot webpage
     * @return the possible lineages for a given taxonomy
     * @throws MalformedURLException
     * @throws IOException
     */
    public Set<String> getLineagesFromFile(File taxonomyFile) throws MalformedURLException, IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(taxonomyFile)));
            String inputLine;
            String lineage;
            String taxName;
            String taxID;
            String headers = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                if (lineages.size() % 10000 == 0) {
                    System.out.println(lineages.size() + " processed lineages");
                }
                String[] split = inputLine.split("\t");
                lineages.add(split[8] + ";" + split[2]);
                cachedTaxonomies.put(split[2], split[0]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//this was an endpoint
        } catch (IOException e) {

        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lineages;
    }

    /**
     *
     * @param taxonomyTabFile
     * @return a model after searching for an unknown taxonomy name (example
     * after a search)
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public DefaultTreeModel getModelFromFile(File taxonomyTabFile) throws IOException, InterruptedException {
        System.out.println("Building Tree");
        getLineagesFromFile(taxonomyTabFile);
        int processed = 0;
        for (String lineage : lineages) {
            if (processed % 10000 == 0) {
                System.out.println(processed + " added in tree");
            }
            String[] taxonomies = lineage.split(";");
            DefaultMutableTreeNode parent = new DefaultMutableTreeNode(new String[]{"root", taxonomies[0]});
            for (String taxonomy : taxonomies) {
                taxonomy = taxonomy.trim();
                DefaultMutableTreeNode node = searchNode(taxonomy);
                if (node == null) {
                    node = new DefaultMutableTreeNode(taxonomy);
                    model.insertNodeInto(node, parent, parent.getChildCount());
                }
                parent = node;
            }
            processed++;
        }
        return model;
    }

    /**
     *
     * @param taxonomyName
     * @return a model after searching for an unknown taxonomy name (example
     * after a search)
     * @throws IOException
     * @throws java.net.URISyntaxException
     * @throws java.net.MalformedURLException
     */
    public DefaultTreeModel getModelAfterSearch(String taxonomyName) throws IllegalArgumentException, IOException, MalformedURLException, URISyntaxException {
        getLineagesForTaxonomyID(taxonomyName);
        for (String lineage : lineages) {
            String[] taxonomies = lineage.split(";");
            DefaultMutableTreeNode parent = new DefaultMutableTreeNode(new String[]{"root", taxonomies[0]});
            for (String taxonomy : taxonomies) {
                taxonomy = taxonomy.trim();
                DefaultMutableTreeNode node = searchNode(taxonomy);
                if (node == null) {
                    node = new DefaultMutableTreeNode(taxonomy);
                    model.insertNodeInto(node, parent, parent.getChildCount());
                }
                parent = node;
            }
        }
        return model;
    }

    /**
     *
     * @param taxonomyName
     * @return a model for a known taxonomy name (example after node click)
     * @throws IOException
     */
    public DefaultTreeModel getModelAfterClick(String taxonomyName) throws IOException {
        List<String> childTaxonomies = getChildTaxonomies(taxonomyName);
        for (String lineage : childTaxonomies) {
            String[] taxonomies = lineage.split(";");
            DefaultMutableTreeNode parent = searchNode(taxonomyName);
            DefaultMutableTreeNode node;
            for (String taxonomy : taxonomies) {
                taxonomy = taxonomy.trim();
                node = new DefaultMutableTreeNode(taxonomy);
                try {

                    model.insertNodeInto(node, parent, parent.getChildCount());
                } catch (NullPointerException e) {
                    //no more children here
                }
            }
        }
        return model;
    }

    private DefaultMutableTreeNode searchNode(String nodeStr) {
        DefaultMutableTreeNode node;
        Enumeration e = rootNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            node = (DefaultMutableTreeNode) e.nextElement();
            if (nodeStr.equals(node.getUserObject().toString())) {
                return node;
            }
        }
        return null;
    }

    /**
     *
     * @param taxonomyName
     * @return the taxonomyID that was encountered for a taxonomyName. This is
     * required to speed up the tree considerably
     */
    public String getCachedTaxonomyID(String taxonomyName) {
        return cachedTaxonomies.get(taxonomyName);
    }

    /**
     *
     * @param queryTerm the term you wish to search for (example "human" or
     * 9606)
     * @param returnID boolean indicating whether you want the taxonomyID or the
     * taxonomy name
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public String queryTaxonomy(String queryTerm, boolean returnID) throws MalformedURLException, IOException {
        String url = ConnectionManager.getUniprotHost() + "taxonomy/?query=" + queryTerm + "&sort=score&format=tab";
        URL website = new URL(url);
        String taxName = "";
        String taxID = "";
        URLConnection connection = website.openConnection();
        HashMap<String, String> tempTaxIdMap = new HashMap<String, String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String headers = in.readLine();
            if ((inputLine = in.readLine()) != null) {
                String[] split = inputLine.split("\t");
                taxName = split[2];
                taxID = split[0];
                tempTaxIdMap.put(taxName, taxID);
            }
            if (!taxName.isEmpty() && !taxID.isEmpty()) {
                cachedTaxonomies.put(taxName, taxID);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            //this was an endpoint?
        } catch (IOException ex) {
            Logger.getLogger(UniprotTaxonomyProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (returnID) {
            return taxID;
        } else {
            return taxName;
        }
    }
}
