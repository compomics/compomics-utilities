package com.compomics.util.gui.protein;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

/**
 * This class contains a method that formats a given protein sequence such that 
 * both the covered parts of the sequence and the peptide selected in the peptide 
 * table is highlighted. The result is inserted into a JEditorPane.
 *
 * @author Harald Barsnes
 */
public class ProteinSequencePane {

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
     * @param showModifications         if the modifications are to be highlighted or not
     * @param showVariants              if the variants are to be highlighted or not
     * @param showCoverage              if the coverage is to be highlighted or not
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int[] coverage,
            TreeMap<String, String> keyValuePairs, boolean showModifications, boolean showVariants, boolean showCoverage) {
        return formatProteinSequence(editorPane, cleanSequence, -1, -1, coverage, keyValuePairs, showModifications, showVariants, showCoverage);
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
        return formatProteinSequence(editorPane, cleanSequence, -1, -1, coverage, new TreeMap<String, String>(), false, false, true);
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
        return formatProteinSequence(editorPane, cleanSequence, selectedPeptideStart, selectedPeptideEnd, coverage, new TreeMap<String, String>(), false, false, true);
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
        
        int indexWidth = 200;
        
        if (cleanSequence.length() > 999) {
            indexWidth = 250;
        }
        
        double temp = (editorPane.getWidth() - indexWidth) / (fm.stringWidth("X"));
        int numberOfAminoAcidsPerRow = (int) temp / 10;
        numberOfAminoAcidsPerRow *= 10;

        ArrayList<Integer> referenceMarkers = new ArrayList<Integer>();

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
     * @param aKeyValuePairs             the key value pairs used for PEFF formating
     * @param showModifications         if the modifications are to be highlighted or not
     * @param showVariants              if the variants are to be highlighted or not
     * @param showCoverage              if the coverage is to be highlighted or not
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int selectedPeptideStart, int selectedPeptideEnd, int[] coverage,
            TreeMap<String, String> aKeyValuePairs, boolean showModifications, boolean showVariants, boolean showCoverage) {

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
        
        int indexWidth = 200;
        
        if (cleanSequence.length() > 999) {
            indexWidth = 250;
        }
        
        double temp = (editorPane.getWidth() - indexWidth) / (fm.stringWidth("X"));
        int numberOfAminoAcidsPerRow = (int) temp / 10;
        numberOfAminoAcidsPerRow *= 10;

        ArrayList<Integer> referenceMarkers = new ArrayList<Integer>();

        String[] modificationMap = new String[coverage.length];
        String[] variantMap = new String[coverage.length];

        if (peffKeyValuePairs.containsKey("ModRes") && showModifications) {

            String modifications = peffKeyValuePairs.get("ModRes");
            modifications = modifications.substring(1, modifications.length() - 1);

            String[] mods = modifications.split("\\)\\(");

            for (int j = 0; j < mods.length; j++) {

                String[] tempMod = mods[j].split("\\|");

                int index = new Integer(tempMod[0]);

                String psiMod = tempMod[1];

                modificationMap[index] = "<html>Modification(s):<br>" + index + ": " + psiMod + "</html>"; // @TODO: what about more than one mod on the same residue!?
            }
        }

        if (peffKeyValuePairs.containsKey("Variant") && showVariants) {

            String variants = peffKeyValuePairs.get("Variant");
            variants = variants.substring(1, variants.length() - 1);

            String[] variant = variants.split("\\)\\(");

            for (int j = 0; j < variant.length; j++) {

                String[] tempVariant = variant[j].split("\\|");

                int start = new Integer(tempVariant[0]);
                int end = new Integer(tempVariant[1]);

                String sequence = tempVariant[2];

                if (start != end) {
                    for (int k = start; k <= end; k++) {
                        variantMap[k] = "<html>Variants:<br>" + start + "-" + end + ": " + sequence + "</html>"; // @TODO: what about more than one variant on the same residue!?
                    }
                } else {
                    variantMap[start] = "<html>Variant:<br>" + start + ": " + sequence + "</html>"; // @TODO: what about more than one variant on the same residue!?
                }

            }
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

            if (!showCoverage) {
                coveredPeptide = true;
            }

            // check if the current residue is contained in the selected peptide
            if (i == selectedPeptideStart) {
                selectedPeptide = true;
            } else if (i == selectedPeptideEnd + 1) {
                selectedPeptide = false;
            }

            String underlineStart = "";
            String underlineEnd = "";

            if (selectedPeptide) {
                underlineStart = "<u>";
                underlineEnd = "</u>";
            }

            if (modificationMap[i] != null && variantMap[i] == null) {

                if (coveredPeptide) {
                    currentCellSequence += "<span style=\"color:#FFFFFF; background:#A9D0F5; text-decoration:none\">" + underlineStart
                            + "<A HREF=\"" + modificationMap[i] + "\" TITLE=\"" + modificationMap[i] + "\">"
                            + cleanSequence.charAt(i - 1) + underlineEnd + "</A></span>";
                } else {
                    currentCellSequence += "<span style=\"color:#BDBDBD; background:#A9D0F5; text-decoration:none\">"
                            + "<A HREF=\"" + modificationMap[i] + "\" TITLE=\"" + modificationMap[i] + "\">"
                            + cleanSequence.charAt(i - 1) + "</A></span>";
                }

            } else if (modificationMap[i] == null && variantMap[i] != null) {

                if (coveredPeptide) {
                    currentCellSequence += "<span style=\"color:#FFFFFF; background:#F78181;text-decoration:none\">" + underlineStart
                            + "<A HREF=\"" + variantMap[i] + "\" TITLE=\"" + variantMap[i] + "\">"
                            + cleanSequence.charAt(i - 1) + underlineEnd + "</A></span>";
                } else {
                    currentCellSequence += "<span style=\"color:#BDBDBD; background:#F78181;text-decoration:none\">"
                            + "<A HREF=\"" + variantMap[i] + "\" TITLE=\"" + variantMap[i] + "\">"
                            + cleanSequence.charAt(i - 1) + "</A></span>";
                }

            } else if (modificationMap[i] != null && variantMap[i] != null) {
                currentCellSequence += "<span style=\"color:#FFFFFF; background:#01DF01;text-decoration:none\"><b>"
                        + "<A HREF=\"" + modificationMap[i] + "<br>" + variantMap[i] + "\" TITLE=\"" + modificationMap[i] + "<br>" + variantMap[i] + "\">"
                        + cleanSequence.charAt(i - 1) + "</A></b></span>";
            } else {

                if (coveredPeptide) {
                    currentCellSequence += "<font color=black>" + underlineStart + cleanSequence.charAt(i - 1) + underlineEnd + "</font>";
                } else {
                    currentCellSequence += "<span style=\"color:#BDBDBD\">" + cleanSequence.charAt(i - 1) + "</span>";
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
}
