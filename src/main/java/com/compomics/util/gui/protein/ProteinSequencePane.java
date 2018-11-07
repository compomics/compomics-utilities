package com.compomics.util.gui.protein;

import com.compomics.util.Util;
import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * This class contains a method that formats a given protein sequence such that 
 * both the covered parts of the sequence and the peptide selected in the peptide 
 * table is highlighted. The result is inserted into a JEditorPane.
 *
 * @author Harald Barsnes
 */
public class ProteinSequencePane {

    /**
     * Empty default constructor
     */
    public ProteinSequencePane() {
    }

    /**
     * The map of PEFF key value pairs.
     */
    private static TreeMap<String, String> peffKeyValuePairs;

    /**
     * Formats the protein sequence such that the covered parts of the sequence
     * is highlighted. The result is inserted into the provided JEditorPane.
     *
     * @param editorPane                the editor pane to add the formatted sequence to
     * @param cleanSequence             the clean protein sequence, i.e., just the amino acid sequence
     * @param coverage                  the sequence coverage map with numbers indicating the number
     *                                  of times a given residue is used, zero means no coverage,
     *                                  (note: only uses index 1-n)
     * @param keyValuePairs             the key value pairs used for PEFF formating
     * @param tagColors                 the colors to use for the different tags, key is the tag
     * @param showModifications         if the modifications are to be highlighted or not
     * @param showVariants              if the variants are to be highlighted or not
     * @param showCoverage              if the coverage is to be highlighted or not
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int[] coverage,
            TreeMap<String, String> keyValuePairs, HashMap<String, Color> tagColors, boolean showModifications, boolean showVariants, boolean showCoverage) {
        return formatProteinSequence(editorPane, cleanSequence, -1, -1, coverage, keyValuePairs, tagColors);
    }

    /**
     * Formats the protein sequence such that the covered parts of the sequence
     * is highlighted. The result is inserted into the provided JEditorPane.
     *
     * @param editorPane                the editor pane to add the formatted sequence to
     * @param cleanSequence             the clean protein sequence, i.e., just the amino acid sequence
     * @param coverage                  the sequence coverage map with numbers indicating the number
     *                                  of times a given residue is used, zero means no coverage,
     *                                  (note: only uses index 1-n)
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int[] coverage) {
        return formatProteinSequence(editorPane, cleanSequence, -1, -1, coverage, new TreeMap<>(), new HashMap<>());
    }

    /**
     * Formats the protein sequence such that the covered parts of the sequence
     * is highlighted. The result is inserted into the provided JEditorPane.
     *
     * @param editorPane                the editor pane to add the formatted sequence to
     * @param cleanSequence             the clean protein sequence, i.e., just the amino acid sequence
     * @param selectedPeptideStart      the starting index of the selected peptide
     * @param selectedPeptideEnd        the ending index of the selected peptide
     * @param coverage                  the sequence coverage map with numbers indicating the number
     *                                  of times a given residue is used, zero means no coverage,
     *                                  (note: only uses index 1-n)
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int selectedPeptideStart, int selectedPeptideEnd, int[] coverage) {
        return formatProteinSequence(editorPane, cleanSequence, selectedPeptideStart, selectedPeptideEnd, coverage, new TreeMap<>(), new HashMap<>());
    }

    /**
     * Formats the protein sequence such that both the covered parts of the sequence
     * and the peptide selected in the peptide table is highlighted. The result is
     * inserted into the provided JEditorPane. This method accounts for redundancies of the selected peptide in the protein sequence
     *
     * @param editorPane                the editor pane to add the formatted sequence to
     * @param cleanSequence             the clean protein sequence, i.e., just the amino acid sequence
     * @param selectedPeptideStart      the start indexes of the currently selected peptide
     * @param selectedPeptideEnd        the end indexes if the currently selected peptide
     * @param coverage                  the sequence coverage map with numbers indicating the number
     *                                  of times a given residue is used, zero means no coverage,
     *                                  (note: only uses index 1-n)
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, ArrayList<Integer> selectedPeptideStart, ArrayList<Integer> selectedPeptideEnd, int[] coverage) {

        if (cleanSequence.length() != coverage.length - 1) {
            throw new IllegalArgumentException("The lenght of the coverage map has to be equal to the lenght of the sequence + 1!");
        }

        String sequenceTable = "", currentCellSequence = "";
        boolean selectedPeptide = false, coveredPeptide = false;
        double sequenceCoverage = 0;

        // see how many amino acids we have room for
        FontMetrics fm = editorPane.getGraphics().getFontMetrics();
        int fontWidth = fm.stringWidth("X");
        
        // hardcoding needed to due issues with other look and feels
        if (!UIManager.getLookAndFeel().getName().equalsIgnoreCase("Nimbus")) {
            fontWidth = 8; // 8 is to represent the default average font width in html
            
            // @TODO: find a way of removing this hardcoding...
        }

        int indexWidth = 200;

        if (cleanSequence.length() > 999) {
            indexWidth += 50;
        }

        double temp = (editorPane.getParent().getWidth() - indexWidth) / fontWidth;
        int numberOfAminoAcidsPerRow = (int) temp / 10;
        numberOfAminoAcidsPerRow *= 10;
        
        ArrayList<Integer> referenceMarkers = new ArrayList<>();

        boolean previousAminoAcidWasCovered = false;
        boolean previousAminoAcidWasSelected = false;

        // iterate the coverage table and create the formatted sequence string
        for (int i = 1; i < coverage.length; i++) {

            // add residue number and line break
            if (i % numberOfAminoAcidsPerRow == 1 || i == 1) {
                sequenceTable += "</tr><tr><td><font color=black><a name=\"" + i + "\">" + i + "</a></font></td>";
                referenceMarkers.add(i);
            }

            // check if the current residues is covered
            if (coverage[i] > 0) {
                sequenceCoverage++;
                coveredPeptide = true;
            } else {
                coveredPeptide = false;
            }

            // check if the current residue is contained in the selected peptide
            for (int possibleStart : selectedPeptideStart) {
                if (i == possibleStart) {
                    selectedPeptide = true;
                }
            }
            for (int possibleEnd : selectedPeptideEnd) {
                if (i == possibleEnd) {
                    selectedPeptide = false;
                }
            }

            if (previousAminoAcidWasSelected && !selectedPeptide) {
                currentCellSequence += "</span>";
            }

            // highlight the covered and selected peptides
            if (selectedPeptide) {
                if (i % 10 == 1) {
                    currentCellSequence += "<span style=\"background:#CEE3F6\">" + cleanSequence.charAt(i - 1);
                } else {
                    if (previousAminoAcidWasSelected) {
                        currentCellSequence += cleanSequence.charAt(i - 1);
                    } else {
                        currentCellSequence += "</span><span style=\"background:#CEE3F6\">" + cleanSequence.charAt(i - 1);
                    }
                }

                previousAminoAcidWasSelected = true;

            } else {

                previousAminoAcidWasSelected = false;

                if (coveredPeptide) {
                    if (i % 10 == 1) {
                        currentCellSequence += cleanSequence.charAt(i - 1);
                    } else {
                        if (previousAminoAcidWasCovered) {
                            currentCellSequence += cleanSequence.charAt(i - 1);
                        } else {
                            currentCellSequence += "</span>" + cleanSequence.charAt(i - 1);
                        }
                    }
                } else {
                    if (i % 10 == 1) {
                        currentCellSequence += "<span style=\"color:#BDBDBD\">" + cleanSequence.charAt(i - 1);
                    } else {
                        if (previousAminoAcidWasCovered) {
                            currentCellSequence += "<span style=\"color:#BDBDBD\">" + cleanSequence.charAt(i - 1);
                        } else {
                            currentCellSequence += cleanSequence.charAt(i - 1);
                        }
                    }
                }
            }

            // add the sequence to the formatted sequence
            if (i % 10 == 0) {
                if (previousAminoAcidWasCovered && !previousAminoAcidWasSelected) {
                    sequenceTable += "<td><tt>" + currentCellSequence + "</tt></td>";
                } else {
                    sequenceTable += "<td><tt>" + currentCellSequence + "</span></tt></td>";
                }

                currentCellSequence = "";
            }

            previousAminoAcidWasCovered = coveredPeptide;
        }

        // add remaining tags and complete the formatted sequence
        sequenceTable += "<td><tt>" + currentCellSequence + "</tt></td></table>";
        String formattedSequence = "<html><body><table cellspacing='2'>" + sequenceTable + "</html></body>";

        // display the formatted sequence
        editorPane.setText(formattedSequence);

        // make sure that the currently selected peptide is visible
        if (selectedPeptideStart.size() > 0 && selectedPeptideStart.get(0) != -1) {

            boolean referenceMarkerFound = false;

            for (int i = 0; i < referenceMarkers.size() - 1 && !referenceMarkerFound; i++) {
                if (selectedPeptideStart.get(0) >= referenceMarkers.get(i) && selectedPeptideStart.get(0) < referenceMarkers.get(i + 1)) {

                    final JEditorPane tempEditorPane = editorPane;
                    final String referenceMarker = referenceMarkers.get(i).toString();

                    // invoke later to give time for components to update
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            tempEditorPane.scrollToReference(referenceMarker);
                        }
                    });

                    referenceMarkerFound = true;
                }
            }

            if (!referenceMarkerFound) {

                final JEditorPane tempEditorPane = editorPane;
                final String referenceMarker = referenceMarkers.get(referenceMarkers.size() - 1).toString();

                // invoke later to give time for components to update
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        tempEditorPane.scrollToReference(referenceMarker);
                    }
                });
            }

        } else {
            editorPane.setCaretPosition(0);
        }

        return (sequenceCoverage / cleanSequence.length()) * 100;
    }

    /**
     * Formats the protein sequence such that both the covered parts of the sequence
     * and the peptide selected in the peptide table is highlighted. The result is
     * inserted into the provided JEditorPane.
     *
     * @param editorPane                the editor pane to add the formatted sequence to
     * @param cleanSequence             the clean protein sequence, i.e., just the amino acid sequence
     * @param selectedPeptideStart      the start index of the currently selected peptide
     * @param selectedPeptideEnd        the end index if the currently selected peptide
     * @param coverage                  the sequence coverage map with numbers indicating the number
     *                                  of times a given residue is used, zero means no coverage,
     *                                  (note: only uses index 1-n)
     * @param aKeyValuePairs            the key value pairs used for PEFF formating
     * @param selectedAnnotationType    the colors to use for the different tags, key is the tag
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int selectedPeptideStart, int selectedPeptideEnd, int[] coverage,
            TreeMap<String, String> aKeyValuePairs, HashMap<String, Color> selectedAnnotationType) {

        // @TODO: the html code ought to be optimized similar to the method above!!

        if (cleanSequence.length() != coverage.length - 1) {
            throw new IllegalArgumentException("The lenght of the coverage map has to be equal to the lenght of the sequence + 1!");
        }

        peffKeyValuePairs = aKeyValuePairs;

        String sequenceTable = "", currentCellSequence = "";
        boolean selectedPeptide = false, coveredPeptide = false;
        double sequenceCoverage = 0;

        // see how many amino acids we have room for
        FontMetrics fm = editorPane.getGraphics().getFontMetrics();
        int fontWidth = fm.stringWidth("X");
        
        // hardcoding needed to due issues with other look and feels
        if (!UIManager.getLookAndFeel().getName().equalsIgnoreCase("Nimbus")) {
            fontWidth = 8; // 8 is to represent the default average font width in html
            
            // @TODO: find a way of removing this hardcoding...
        }

        int indexWidth = 200;

        if (cleanSequence.length() > 999) {
            indexWidth += 50;
        }

        double temp = (editorPane.getParent().getWidth() - indexWidth) / fontWidth; 
        int numberOfAminoAcidsPerRow = (int) temp / 10;
        numberOfAminoAcidsPerRow *= 10;

        ArrayList<Integer> referenceMarkers = new ArrayList<>();

        String[] modificationMap = new String[coverage.length];
        String[] variantMap = new String[coverage.length];
        String[] signalMap = new String[coverage.length];
        String[] siteMap = new String[coverage.length];
        

        if (peffKeyValuePairs.containsKey("ModRes") && selectedAnnotationType.containsKey("ModRes_Foreground")) {
            
            // @TODO: support ModResPsi!!

            String modifications = peffKeyValuePairs.get("ModRes");
            modifications = modifications.substring(1, modifications.length() - 1);

            String[] mods = modifications.split("\\)\\(");

            for (int j = 0; j < mods.length; j++) {

                String[] tempMod = mods[j].split("\\|");

                int index = new Integer(tempMod[0]);

                String psiMod = tempMod[1];

                if (modificationMap[index] == null) {
                    modificationMap[index] = "Modification(s):<br>" + index + ": " + psiMod;
                } else {
                    modificationMap[index] += "<br>" + index + ": " + psiMod;
                }
            }

            for (int i = 0; i < modificationMap.length; i++) {
                if (modificationMap[i] != null) {
                    modificationMap[i] = "<html>" + modificationMap[i] + "<html>";
                }
            }
        }

        if (peffKeyValuePairs.containsKey("Variant") && selectedAnnotationType.containsKey("Variant_Foreground")) {
            fillTwoValueMap("Variant(s)", peffKeyValuePairs.get("Variant"), variantMap);
        }

        if (peffKeyValuePairs.containsKey("Signal") && selectedAnnotationType.containsKey("Signal_Foreground")) {
            fillTwoValueMap("Signal(s)", peffKeyValuePairs.get("Signal"), signalMap); 
        }
        
        if (peffKeyValuePairs.containsKey("Site") && selectedAnnotationType.containsKey("Site_Foreground")) {
            fillTwoValueMap("Site(s)", peffKeyValuePairs.get("Site"), siteMap); 
        }

        String uncoveredForegroundColor = Util.color2Hex(Color.lightGray);
        String uncoveredBackgroundColor = Util.color2Hex(Color.WHITE);

        String modsForegroundColor = null;
        String modsBackgroundColor = null;
        String variantsForegroundColor = null;
        String variantsBackgroundColor = null;
        String signalForegroundColor = null;
        String signalBackgroundColor = null;
        String siteForegroundColor = null;
        String siteBackgroundColor = null;
        String multipleForegroundColor = null;
        String multipleBackgroundColor = null;

        if (selectedAnnotationType.get("ModRes_Foreground") != null) {
            modsForegroundColor = Util.color2Hex(selectedAnnotationType.get("ModRes_Foreground"));
            modsBackgroundColor = Util.color2Hex(selectedAnnotationType.get("ModRes_Background"));
        }
        if (selectedAnnotationType.get("Variant_Foreground") != null) {
            variantsForegroundColor = Util.color2Hex(selectedAnnotationType.get("Variant_Foreground"));
            variantsBackgroundColor = Util.color2Hex(selectedAnnotationType.get("Variant_Background"));
        }
        if (selectedAnnotationType.get("Signal_Foreground") != null) {
            signalForegroundColor = Util.color2Hex(selectedAnnotationType.get("Signal_Foreground"));
            signalBackgroundColor = Util.color2Hex(selectedAnnotationType.get("Signal_Background"));
        }
        if (selectedAnnotationType.get("Site_Foreground") != null) {
            siteForegroundColor = Util.color2Hex(selectedAnnotationType.get("Site_Foreground"));
            siteBackgroundColor = Util.color2Hex(selectedAnnotationType.get("Site_Background"));
        }
        if (selectedAnnotationType.get("Multiple_Foreground") != null) {
            multipleForegroundColor = Util.color2Hex(selectedAnnotationType.get("Multiple_Foreground"));
            multipleBackgroundColor = Util.color2Hex(selectedAnnotationType.get("Multiple_Background"));
        }

        // iterate the coverage table and create the formatted sequence string
        for (int i = 1; i < coverage.length; i++) {

            // add residue number and line break
            if (i % numberOfAminoAcidsPerRow == 1 || i == 1) {
                sequenceTable += "</tr><tr><td><font color=black><a name=\"" + i + "\">" + i + "</a></font></td>";
                referenceMarkers.add(i);
            }

            // check if the current residues is covered
            if (coverage[i] > 0) {
                sequenceCoverage++;
                coveredPeptide = true;
            } else {
                coveredPeptide = false;
            }

            // @TODO: make it possible to turn of the coverage highlighting again...
//            if (!showCoverage) {
//                coveredPeptide = true;
//            }

            // check if the current residue is contained in the selected peptide
            if (i == selectedPeptideStart) {
                selectedPeptide = true;
            } else if (i == selectedPeptideEnd + 1) {
                selectedPeptide = false;
            }


            int index = 0;

            if (modificationMap[i] != null) {
                index++;
            }
            if (variantMap[i] != null) {
                index++;
            }
            if (signalMap[i] != null) {
                index++;
            }
            if (siteMap[i] != null) {
                index++;
            }

            if (index == 0) {

                // no annotations

                if (coveredPeptide) {

                    if (selectedPeptide) {
                        currentCellSequence += "<font color=black>" + "<u>" + cleanSequence.charAt(i - 1) + "</u>" + "</font>";
                    } else {
                        currentCellSequence += "<font color=black>" + cleanSequence.charAt(i - 1) + "</font>";
                    }

                } else {
                    currentCellSequence += "<span style=\"color:#" + uncoveredForegroundColor + "; background:#" + uncoveredBackgroundColor + "\">" + cleanSequence.charAt(i - 1) + "</span>";
                }

            } else if (index == 1) {

                // single annotation

                String currentForegroundColor = "";
                String currentBackgroundColor = "";
                String currentValue = null;

                if (modificationMap[i] != null) {

                    currentForegroundColor = modsForegroundColor;
                    currentBackgroundColor = modsBackgroundColor;
                    currentValue = modificationMap[i];

                } else if (variantMap[i] != null) {

                    currentForegroundColor = variantsForegroundColor;
                    currentBackgroundColor = variantsBackgroundColor;
                    currentValue = variantMap[i];

                } else if (signalMap[i] != null) {

                    currentForegroundColor = signalForegroundColor;
                    currentBackgroundColor = signalBackgroundColor;
                    currentValue = signalMap[i];
                    
                } else if (siteMap[i] != null) {

                    currentForegroundColor = siteForegroundColor;
                    currentBackgroundColor = siteBackgroundColor;
                    currentValue = siteMap[i];
                }

                if (selectedPeptide) {
                    currentCellSequence += "<span style=\"color:#" + currentForegroundColor + ";background:#" + currentBackgroundColor + "\">"
                            + "<A HREF=\"" + currentValue + "\" TITLE=\"" + currentValue + "\">"
                            + cleanSequence.charAt(i - 1) + "</A></span>";
                } else {
                    if (coveredPeptide) {
                        currentCellSequence += "<span style=\"color:#" + currentForegroundColor + ";background:#" + currentBackgroundColor + ";text-decoration:none\">"
                                + "<A HREF=\"" + currentValue + "\" TITLE=\"" + currentValue + "\">"
                                + cleanSequence.charAt(i - 1) + "</A></span>";
                    } else {
                        currentCellSequence += "<span style=\"color:#" + uncoveredForegroundColor + ";background:#" + currentBackgroundColor + ";text-decoration:none\">"
                                + "<A HREF=\"" + currentValue + "\" TITLE=\"" + currentValue + "\">"
                                + cleanSequence.charAt(i - 1) + "</A></span>";
                    }
                }

            } else {

                // multiple annotations

                String tempAnnotation = "";

                if (modificationMap[i] != null) {
                    tempAnnotation += modificationMap[i];
                } 
                if (variantMap[i] != null) {
                    if (tempAnnotation.length() > 0) {
                        tempAnnotation += "<br><br>" + variantMap[i];
                    } else {
                        tempAnnotation += variantMap[i];
                    }
                } 
                if (signalMap[i] != null) {
                    if (tempAnnotation.length() > 0) {
                        tempAnnotation += "<br><br>" + signalMap[i];
                    } else {
                        tempAnnotation += signalMap[i];
                    }
                } 
                if (siteMap[i] != null) {
                    if (tempAnnotation.length() > 0) {
                        tempAnnotation += "<br><br>" + siteMap[i];
                    } else {
                        tempAnnotation += siteMap[i];
                    }
                }

                if (selectedPeptide) {
                    currentCellSequence += "<span style=\"color:#" + multipleForegroundColor + ";background:#" + multipleBackgroundColor + "\"><b>"
                            + "<A HREF=\"" + tempAnnotation + "\" TITLE=\"" + tempAnnotation + "\">"
                            + cleanSequence.charAt(i - 1) + "</A></b></span>";
                } else {
                    if (coveredPeptide) {
                        currentCellSequence += "<span style=\"color:#" + multipleForegroundColor + ";background:#" + multipleBackgroundColor + ";text-decoration:none\"><b>"
                                + "<A HREF=\"" + tempAnnotation + "\" TITLE=\"" + tempAnnotation + "\">"
                                + cleanSequence.charAt(i - 1) + "</A></b></span>";
                    } else {
                        currentCellSequence += "<span style=\"color:#" + uncoveredForegroundColor + ";background:#" + multipleBackgroundColor + ";text-decoration:none\"><b>"
                                + "<A HREF=\"" + tempAnnotation + "\" TITLE=\"" + tempAnnotation + "\">"
                                + cleanSequence.charAt(i - 1) + "</A></b></span>";
                    }
                }
            }

            // add the sequence to the formatted sequence
            if (i % 10 == 0) {
                sequenceTable += "<td><tt>" + currentCellSequence + "</tt></td>";
                currentCellSequence = "";
            }
        }

        // add remaining tags and complete the formatted sequence
        sequenceTable += "<td><tt>" + currentCellSequence + "</tt></td></table><font color=black>";
        String formattedSequence = "<html><body><table cellspacing='2'>" + sequenceTable + "</html></body>";

        // display the formatted sequence
        editorPane.setText(formattedSequence);

        // make sure that the currently selected peptide is visible
        if (selectedPeptideStart != -1) {

            boolean referenceMarkerFound = false;

            for (int i = 0; i < referenceMarkers.size() - 1 && !referenceMarkerFound; i++) {
                if (selectedPeptideStart >= referenceMarkers.get(i) && selectedPeptideStart < referenceMarkers.get(i + 1)) {

                    final JEditorPane tempEditorPane = editorPane;
                    final String referenceMarker = referenceMarkers.get(i).toString();

                    // invoke later to give time for components to update
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            tempEditorPane.scrollToReference(referenceMarker);
                        }
                    });

                    referenceMarkerFound = true;
                }
            }

            if (!referenceMarkerFound) {
                final JEditorPane tempEditorPane = editorPane;
                final String referenceMarker = referenceMarkers.get(referenceMarkers.size() - 1).toString();

                // invoke later to give time for components to update
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        tempEditorPane.scrollToReference(referenceMarker);
                    }
                });
            }

        } else {
            editorPane.setCaretPosition(0);
        }

        return (sequenceCoverage / cleanSequence.length()) * 100;
    }

    private static void fillTwoValueMap(String type, String values, String[] map) {

        values = values.substring(1, values.length() - 1);

        String[] value = values.split("\\)\\(");

        for (int j = 0; j < value.length; j++) {

            String[] tempValue = value[j].split("\\|");

            int start = new Integer(tempValue[0]);
            int end = new Integer(tempValue[1]);

            String sequence = "";
            
            if (tempValue.length > 2) {
                sequence = tempValue[2];
            }

            if (start != end) {
                for (int k = start; k <= end; k++) {

                    if (map[k] == null) {
                        map[k] = type + ":<br>" + start + "-" + end + ": " + sequence;
                    } else {
                        map[k] += "<br>" + start + "-" + end + ": " + sequence;
                    }
                }
            } else {

                if (map[start] == null) {
                    map[start] = type + ":<br>" + start + ": " + sequence;
                } else {
                    map[start] += "<br>" + start + ": " + sequence;
                }
            }
        }

        for (int i = 0; i < map.length; i++) {
            if (map[i] != null) {
                map[i] = "<html>" + map[i] + "<html>";
            }
        }
    }
}
