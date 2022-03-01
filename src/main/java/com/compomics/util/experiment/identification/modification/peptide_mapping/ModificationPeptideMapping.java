package com.compomics.util.experiment.identification.modification.peptide_mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
/**
 * Functions for the mapping of modifications on peptides.
 *
 * @author Dafni Skiadopoulou
 * @author Marc Vaudel
 */
public class ModificationPeptideMapping {

    /**
     * Returns map of site to modification mapping.
     * 
     * @param modificationToPossibleSiteMap Map of modification mass to site to modification names.
     * @param modificationOccurrenceMap Map of modification mass to number of modifications.
     * @param modificationToSiteToScore Map of modification mass to modification site to localization score.
     * 
     * @return The site to modification mass mapping.
     */
    public static HashMap<Integer, Double> mapModifications(
            //HashMap<Double, HashMap<Integer, ArrayList<String>>> modificationToPossibleSiteMap,
            HashMap<Double, ArrayList<Integer>> modificationToPossibleSiteMap,
            HashMap<Double, Integer> modificationOccurrenceMap,
            HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore
    ) {

        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        
        Set<String> sitesVertices = new HashSet<>();
        Set<String> modificationsVertices = new HashSet<>();

        for (Map.Entry<Double, ArrayList<Integer>> modificationEntry : modificationToPossibleSiteMap.entrySet()) {
            
            double modID = modificationEntry.getKey();
            int modOccNum = modificationOccurrenceMap.get(modID);
            
            for (int i = 0; i < modOccNum; i++){
            
                String vertexName = String.join("-",String.valueOf(modID),String.valueOf(i));
                g.addVertex(vertexName);
                modificationsVertices.add(vertexName);
                
            }
            
            HashMap<Integer, Double> localizationScores = modificationToSiteToScore.get(modID);
            
            ArrayList<Integer> sites = modificationEntry.getValue();
            
            for (int site : sites) {
                
                String siteVertexName = String.valueOf(site);
                
                if (!g.vertexSet().contains(siteVertexName)){
                    
                    g.addVertex(siteVertexName);
                    sitesVertices.add(siteVertexName);
                    
                }
                
                double locScore = localizationScores.get(site);
                
                for (int i = 0; i < modOccNum; i++){
                    
                    String modVertexName = String.join("-", String.valueOf(modID), String.valueOf(i));
                    DefaultWeightedEdge e = g.addEdge(modVertexName, siteVertexName);
                    g.setEdgeWeight(e, locScore); 
                    
                }
                
            }
        }
        
        MaximumWeightBipartiteMatching​ matching = new MaximumWeightBipartiteMatching​(g, modificationsVertices, sitesVertices);
        
        Matching matchingInfo = matching.getMatching();
        
        Set<DefaultWeightedEdge> matchingEdges = matchingInfo.getEdges();
        
        HashMap<Integer, Double> matchedSiteToModification = new HashMap<>(1);
        
        for(DefaultWeightedEdge e : matchingEdges){
            
            String eInfo = e.toString();
            String[] eVertices = eInfo.split(":");
            double modVertex = Double.parseDouble(eVertices[0].substring(1,eVertices[0].length()-1).split("-")[0]);
            int siteVertex = Integer.parseInt(eVertices[1].substring(1,eVertices[1].length()-1));
            matchedSiteToModification.put(siteVertex, modVertex);
            
        }
        
        //System.out.println(matchedSiteToModification);
        //System.out.println(matching.getMatchingWeight());
                
        return matchedSiteToModification;
        
    }

}
