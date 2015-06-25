package com.compomics.util.protein_sequences_manager.gui.sequences_import.taxonomy;

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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * UniProt taxonomy provider.
 *
 * @author Kenneth Verheggen
 */
public class UniprotTaxonomyProvider {

    /**
     * The lowest layer of taxonomy.
     */
    private static DefaultMutableTreeNode rootNode;
    /**
     * The lineages for a certain taxonomy.
     */
    private final HashSet<String> lineages = new HashSet<String>();
    /**
     * The default JTreeModel.
     */
    private final DefaultTreeModel model;
    /**
     * Cache of taxonomies to increase speed and reduce server load.
     */
    private final HashMap<String, String> cachedTaxonomies = new HashMap<String, String>();

    /**
     * GUI constructor.
     *
     * @param model the tree model you want to update
     */
    public UniprotTaxonomyProvider(DefaultTreeModel model) {
        rootNode = (DefaultMutableTreeNode) model.getRoot();
        this.model = model;
    }

    /**
     * Normal constructor, GUI-less mode.
     */
    public UniprotTaxonomyProvider() {
        rootNode = new DefaultMutableTreeNode("root");
        model = new DefaultTreeModel(rootNode);
    }

    /**
     * Returns the child taxonomies.
     *
     * @param taxonomyName the taxonomy name
     * @return all the children names for a given taxonomy name (or identifier)
     * @throws MalformedURLException if a MalformedURLException occurs
     * @throws IOException if an IOException occurs
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
            in.readLine();
            while ((inputLine = in.readLine()) != null) {
                String[] split = inputLine.split("\t");
                String taxName = split[2];
                String taxID = split[0];
                String lineage = split[8] + ";" + taxName;
                lineages.add(lineage);
                cachedTaxonomies.put(taxName, taxID);
                childrenTaxonomies.add(taxName);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //this was an endpoint
            // @TODO: better error handling
        } catch (IOException e) {
            // @TODO: better error handling
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return childrenTaxonomies;
    }

    /**
     * Returns the possible lineages for a given taxonomy.
     *
     * @param taxonomyName the taxonomy name
     * @return the possible lineages for a given taxonomy
     * @throws MalformedURLException if a MalformedURLException occurs
     * @throws IOException if an IOException occurs
     * @throws URISyntaxException if a URISyntaxException occurs
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

            String inputLine;
            in.readLine(); // read the header
            while ((inputLine = in.readLine()) != null) {
                String[] split = inputLine.split("\t");
                String taxName = split[2];
                String taxID = split[0];
                String lineage = split[8] + ";" + taxName;
                tempLineages.add(lineage);
                tempTaxIdMap.put(taxName, taxID);
                if (tempLineages.size() > maxPerQuery) {
                    throw new IllegalArgumentException("There were over " + maxPerQuery + " lineages found for this query. Try a more specific search or browse manually");
                }
            }

            lineages.addAll(tempLineages);
            cachedTaxonomies.putAll(tempTaxIdMap);
        } catch (ArrayIndexOutOfBoundsException e) {
            //this was an endpoint
            // @TODO: better error handling
        } catch (IOException e) {
            // @TODO: better error handling
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lineages;
    }

    /**
     * Returns the possible lineages for a given taxonomy
     *
     * @param taxonomyFile taxonomy file from the UniProt web page
     * @return the possible lineages for a given taxonomy
     * @throws MalformedURLException if a MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public Set<String> getLineagesFromFile(File taxonomyFile) throws MalformedURLException, IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(taxonomyFile)));
            String inputLine;
            in.readLine(); // read the header
            while ((inputLine = in.readLine()) != null) {
                if (lineages.size() % 10000 == 0) {
                    System.out.println(lineages.size() + " processed lineages"); // @TODO: never have prints in a method
                }
                String[] split = inputLine.split("\t");
                lineages.add(split[8] + ";" + split[2]);
                cachedTaxonomies.put(split[2], split[0]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //this was an endpoint
            // @TODO: better error handling
        } catch (IOException e) {
            // @TODO: better error handling
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lineages;
    }

    /**
     * Returns a model after searching for an unknown taxonomy name (for example
     * after a search).
     *
     * @param taxonomyTabFile taxonomy file from the UniProt web page
     * @return a model after searching for an unknown taxonomy name (for example
     * after a search)
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs
     */
    public DefaultTreeModel getModelFromFile(File taxonomyTabFile) throws IOException, InterruptedException {

        System.out.println("Building Tree"); // @TODO: never have prints in a method
        getLineagesFromFile(taxonomyTabFile);
        int processed = 0;

        for (String lineage : lineages) {
            if (processed % 10000 == 0) {
                System.out.println(processed + " added in tree"); // @TODO: never have prints in a method
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
     * Returns a model after searching for an unknown taxonomy name (for example
     * after a search).
     *
     * @param taxonomyName the taxonomy name
     * @return a model after searching for an unknown taxonomy name (for example
     * after a search)
     * @throws MalformedURLException if a MalformedURLException occurs
     * @throws IOException if an IOException occurs
     * @throws URISyntaxException if a URISyntaxException occurs
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
     * Returns a model for a known taxonomy name (for example after node click).
     *
     * @param taxonomyName the taxonomy name
     * @return a model for a known taxonomy name (for example after node click)
     * @throws IOException if an IOException occurs
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

    /**
     * Returns the given node.
     *
     * @param nodeStr the node string.
     * @return the given node
     */
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
     * Returns the taxonomyID that was encountered for a taxonomyName. This is
     * required to speed up the tree considerably.
     *
     * @param taxonomyName the taxonomy name
     * @return the taxonomyID
     */
    public String getCachedTaxonomyID(String taxonomyName) {
        return cachedTaxonomies.get(taxonomyName);
    }

    /**
     * Returns the query taxonomy.
     *
     * @param queryTerm the term you wish to search for (example "human" or
     * 9606)
     * @param returnID boolean indicating whether you want the taxonomyID or the
     * taxonomy name
     * @return the query taxonomy
     * @throws MalformedURLException if a MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public String queryTaxonomy(String queryTerm, boolean returnID) throws MalformedURLException, IOException {

        String url = ConnectionManager.getUniprotHost() + "taxonomy/?query=" + queryTerm + "&sort=score&format=tab";
        URL website = new URL(url);
        String taxName = "";
        String taxID = "";
        URLConnection connection = website.openConnection();
        HashMap<String, String> tempTaxIdMap = new HashMap<String, String>();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            in.readLine(); // read the header
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
            ex.printStackTrace(); // @TODO: better error handling
        }

        if (returnID) {
            return taxID;
        } else {
            return taxName;
        }
    }
}
