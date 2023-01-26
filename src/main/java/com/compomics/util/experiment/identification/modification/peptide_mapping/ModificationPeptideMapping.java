package com.compomics.util.experiment.identification.modification.peptide_mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching​;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching​;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;

/**
 * Functions for the mapping of modifications on peptides.
 *
 * @author Dafni Skiadopoulou
 * @author Marc Vaudel
 */
public class ModificationPeptideMapping {

    /**
     * Separator for the keys of vertices.
     */
    public static final String SEPARATOR = "_";

    /**
     * Returns map of site to modification mapping.
     *
     * @param modificationToPossibleSiteMap Map of modification mass to site to
     * modification names.
     * @param modificationOccurrenceMap Map of modification mass to number of
     * modifications.
     * @param modificationToSiteToScore Map of modification mass to modification
     * site to localization score.
     *
     * @deprecated does not handle corner cases
     * @return The list of best sites per modification.
     */
    public static HashMap<Double, TreeSet<Integer>> mapModificationsDeprecated(
            HashMap<Double, int[]> modificationToPossibleSiteMap,
            HashMap<Double, Integer> modificationOccurrenceMap,
            HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore
    ) {

        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        Set<String> sitesVertices = new HashSet<>();
        Set<String> modificationsVertices = new HashSet<>();

        for (Map.Entry<Double, int[]> modificationEntry : modificationToPossibleSiteMap.entrySet()) {

            double modID = modificationEntry.getKey();
            int modOccNum = modificationOccurrenceMap.get(modID);

            for (int i = 0; i < modOccNum; i++) {

                String vertexName = String.join(SEPARATOR, String.valueOf(modID), String.valueOf(i));
                g.addVertex(vertexName);
                modificationsVertices.add(vertexName);

            }

            HashMap<Integer, Double> localizationScores = modificationToSiteToScore.get(modID);

            int[] sites = modificationEntry.getValue();

            for (int site : sites) {

                String siteVertexName = String.valueOf(site);

                if (!g.vertexSet().contains(siteVertexName)) {

                    g.addVertex(siteVertexName);
                    sitesVertices.add(siteVertexName);

                }

                double locScore = localizationScores.get(site);

                for (int i = 0; i < modOccNum; i++) {

                    String modVertexName = String.join(SEPARATOR, String.valueOf(modID), String.valueOf(i));
                    DefaultWeightedEdge e = g.addEdge(modVertexName, siteVertexName);
                    
                    if (e == null) {
                        
                        System.out.println("vertex " + modVertexName);
                        
                    }
                    
                    g.setEdgeWeight(e, locScore);
                    
                }
            }
        }

        MaximumWeightBipartiteMatching​ matching = new MaximumWeightBipartiteMatching​(g, modificationsVertices, sitesVertices);

        Matching matchingInfo = matching.getMatching();

        Set<DefaultWeightedEdge> matchingEdges = matchingInfo.getEdges();

        HashMap<Double, TreeSet<Integer>> matchedSiteToModification = new HashMap<>(1);

        for (DefaultWeightedEdge e : matchingEdges) {

            String eInfo = e.toString();
            String[] eVertices = eInfo.split(":");
            double modVertex = Double.parseDouble(eVertices[0].substring(1, eVertices[0].length() - 1).split(SEPARATOR)[0]);
            int siteVertex = Integer.parseInt(eVertices[1].substring(1, eVertices[1].length() - 1));

            TreeSet<Integer> modificationSites = matchedSiteToModification.get(modVertex);

            if (modificationSites == null) {

                modificationSites = new TreeSet<>();
                matchedSiteToModification.put(modVertex, modificationSites);

            }

            modificationSites.add(siteVertex);

        }

        //System.out.println(matchedSiteToModification);
        //System.out.println(matching.getMatchingWeight());
        return matchedSiteToModification;

    }
    
    /**
     * Returns map of site to modification mapping.
     *
     * @param modificationToPossibleSiteMap Map of modification mass to site to
     * modification names.
     * @param modificationOccurrenceMap Map of modification mass to number of
     * modifications.
     * @param modificationToSiteToScore Map of modification mass to modification
     * site to localization score.
     *
     * @return The list of best sites per modification.
     */
    public static HashMap<Double, TreeSet<Integer>> mapModifications(
            HashMap<Double, int[]> modificationToPossibleSiteMap,
            HashMap<Double, Integer> modificationOccurrenceMap,
            HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore
    ) {

        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        Set<String> sitesVertices = new HashSet<>();
        Set<String> modificationsVertices = new HashSet<>();
        
        //Use maxScore to reverse localization scores to implement minimum weight matching algorithm
        double maxScore = 0.0;
        
        for (Map.Entry<Double, int[]> modificationEntry : modificationToPossibleSiteMap.entrySet()) {

            double modID = modificationEntry.getKey();
            
            HashMap<Integer, Double> localizationScores = modificationToSiteToScore.get(modID);

            for (Double score : localizationScores.values()) {
                if (score > maxScore){
                    maxScore = score;
                }
            }
            
        }

        for (Map.Entry<Double, int[]> modificationEntry : modificationToPossibleSiteMap.entrySet()) {

            double modID = modificationEntry.getKey();
            int modOccNum = modificationOccurrenceMap.get(modID);

            for (int i = 0; i < modOccNum; i++) {

                String vertexName = String.join(SEPARATOR, String.valueOf(modID), String.valueOf(i));
                g.addVertex(vertexName);
                modificationsVertices.add(vertexName);

            }

            HashMap<Integer, Double> localizationScores = modificationToSiteToScore.get(modID);

            int[] sites = modificationEntry.getValue();

            for (int site : sites) {

                String siteVertexName = String.valueOf(site);

                if (!g.vertexSet().contains(siteVertexName)) {

                    g.addVertex(siteVertexName);
                    sitesVertices.add(siteVertexName);

                }

                //Add 0.1 to ensure weights are not zero
                double locScore = maxScore - localizationScores.get(site) + 0.1;

                for (int i = 0; i < modOccNum; i++) {

                    String modVertexName = String.join(SEPARATOR, String.valueOf(modID), String.valueOf(i));
                    DefaultWeightedEdge e = g.addEdge(modVertexName, siteVertexName);
                    
                    if (e == null) {
                        
                        System.out.println("vertex " + modVertexName);
                        
                    }
                    
                    //System.out.println(modVertexName + " " + siteVertexName + " " + String.valueOf(locScore));
                    
                    g.setEdgeWeight(e, locScore);
                    
                }
            }
        }
        
        //Add dummy vertices and necessary edges to make the graph complete
        
        Set<String> dummyVertices = new HashSet<>();
        if (sitesVertices.size() > modificationsVertices.size()){
            for (int i=1; i < sitesVertices.size() - modificationsVertices.size() + 1; i++){
                String vertexName = String.join(SEPARATOR, "dummy_vertex", String.valueOf(i));
                g.addVertex(vertexName);
                dummyVertices.add(vertexName);
            }
        }
        
        //Larger weight for the dummy edges (add 50.0 such that even if max score = 0.0 the dummy edges have larger weights)
        double dummyEdgeWeight = (maxScore + 50.0) * 10.0;
        
        ArrayList<String> sitesVerticesList = new ArrayList<>(sitesVertices);
        ArrayList<String> modsVerticesList = new ArrayList<>(modificationsVertices);
        modsVerticesList.addAll(dummyVertices);
        for (int i=0; i < sitesVerticesList.size(); i++){
            for (int j=0; j < modsVerticesList.size(); j++){
                DefaultWeightedEdge e = g.getEdge(sitesVerticesList.get(i), modsVerticesList.get(j));
                if (e == null){
                    e = g.addEdge(sitesVerticesList.get(i), modsVerticesList.get(j));
                    g.setEdgeWeight(e, dummyEdgeWeight);
                    
                    //System.out.println(modsVerticesList.get(j) + " " + sitesVerticesList.get(i) + " " + String.valueOf(dummyEdgeWeight));
                }
            }
        }
        
        Set<String> modsPlusDummyVertices = new HashSet<>();
        modsPlusDummyVertices.addAll(modificationsVertices);
        modsPlusDummyVertices.addAll(dummyVertices);

        KuhnMunkresMinimalWeightBipartitePerfectMatching​ matching = new KuhnMunkresMinimalWeightBipartitePerfectMatching​(g, modsPlusDummyVertices, sitesVertices);

        Matching matchingInfo = matching.getMatching();

        Set<DefaultWeightedEdge> matchingEdges = matchingInfo.getEdges();

        HashMap<Double, TreeSet<Integer>> matchedSiteToModification = new HashMap<>(1);

        for (DefaultWeightedEdge e : matchingEdges) {

            String eInfo = e.toString();
            String[] eVertices = eInfo.split(":");
            
            //Check if edge is adjacent to dummy vertices
            int isDummy1 = eVertices[0].indexOf("dummy");
            int isDummy2 = eVertices[1].indexOf("dummy");
            if ((isDummy1 != -1) || (isDummy2 != -1)){
                continue;
            }
            
            double modVertex;
            int siteVertex;
            if (eVertices[0].indexOf(SEPARATOR) != -1){
                modVertex = Double.parseDouble(eVertices[0].substring(1, eVertices[0].length() - 1).split(SEPARATOR)[0]);
                siteVertex = Integer.parseInt(eVertices[1].substring(1, eVertices[1].length() - 1));
            }
            else{
                modVertex = Double.parseDouble(eVertices[1].substring(1, eVertices[1].length() - 1).split(SEPARATOR)[0]);
                siteVertex = Integer.parseInt(eVertices[0].substring(1, eVertices[0].length() - 1));
            }

            TreeSet<Integer> modificationSites = matchedSiteToModification.get(modVertex);

            if (modificationSites == null) {

                modificationSites = new TreeSet<>();
                matchedSiteToModification.put(modVertex, modificationSites);

            }

            modificationSites.add(siteVertex);

        }
        
        return matchedSiteToModification;

    }

}
