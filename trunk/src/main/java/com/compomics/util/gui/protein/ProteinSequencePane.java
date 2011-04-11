package com.compomics.util.gui.protein;

import java.awt.FontMetrics;
import java.util.ArrayList;
import javax.swing.JEditorPane;

/**
 * This class contains a method that formats a given protein sequence such that 
 * both the covered parts of the sequence and the peptide selected in the peptide 
 * table is highlighted. The result is inserted into a JEditorPane.
 *
 * @author Harald Barsnes
 */
public class ProteinSequencePane {

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
        return formatProteinSequence(editorPane, cleanSequence, -1, -1, coverage);
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
        double temp = editorPane.getWidth() / (fm.stringWidth("W"));
        int numberOfAminoAcidsPerRow = (int) temp / 10;
        numberOfAminoAcidsPerRow *= 10;

        // add some additional amino acids when we have lots of room
        if (numberOfAminoAcidsPerRow > 50) {
            numberOfAminoAcidsPerRow += 10;
        }

        ArrayList<Integer> referenceMarkers = new ArrayList<Integer>();

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

            // highlight the covered and selected peptides
            if (selectedPeptide) {
                currentCellSequence += "<font color=red>" + cleanSequence.charAt(i - 1) + "</font>";
            } else if (coveredPeptide) {
                currentCellSequence += "<font color=blue>" + cleanSequence.charAt(i - 1) + "</font>";
            } else {
                currentCellSequence += "<font color=black>" + cleanSequence.charAt(i - 1) + "</font>";
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
        editorPane.updateUI();

        // make sure that the currently selected peptide is visible
        if (selectedPeptideStart.get(0) != -1) {

            boolean referenceMarkerFound = false;

            for (int i = 0; i < referenceMarkers.size() - 1 && !referenceMarkerFound; i++) {
                if (selectedPeptideStart.get(0) >= referenceMarkers.get(i) && selectedPeptideStart.get(0) < referenceMarkers.get(i + 1)) {
                    editorPane.scrollToReference(referenceMarkers.get(i).toString());
                    referenceMarkerFound = true;
                }
            }

            if (!referenceMarkerFound) {
                editorPane.scrollToReference(referenceMarkers.get(referenceMarkers.size() - 1).toString());
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
     * @return                          the calculated sequence coverage in percent (0-100)
     */
    public static double formatProteinSequence(JEditorPane editorPane, String cleanSequence, int selectedPeptideStart, int selectedPeptideEnd, int[] coverage) {

        if (cleanSequence.length() != coverage.length - 1) {
            throw new IllegalArgumentException("The lenght of the coverage map has to be equal to the lenght of the sequence + 1!");
        }

        String sequenceTable = "", currentCellSequence = "";
        boolean selectedPeptide = false, coveredPeptide = false;
        double sequenceCoverage = 0;

        // see how many amino acids we have room for
        FontMetrics fm = editorPane.getGraphics().getFontMetrics();
        double temp = editorPane.getWidth() / (fm.stringWidth("W"));
        int numberOfAminoAcidsPerRow = (int) temp / 10;
        numberOfAminoAcidsPerRow *= 10;

        // add some additional amino acids when we have lots of room
        if (numberOfAminoAcidsPerRow > 50) {
            numberOfAminoAcidsPerRow += 10;
        }

        ArrayList<Integer> referenceMarkers = new ArrayList<Integer>();

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
            if (i == selectedPeptideStart) {
                selectedPeptide = true;
            } else if (i == selectedPeptideEnd + 1) {
                selectedPeptide = false;
            }

            // highlight the covered and selected peptides
            if (selectedPeptide) {
                currentCellSequence += "<font color=red>" + cleanSequence.charAt(i - 1) + "</font>";
            } else if (coveredPeptide) {
                currentCellSequence += "<font color=blue>" + cleanSequence.charAt(i - 1) + "</font>";
            } else {
                currentCellSequence += "<font color=black>" + cleanSequence.charAt(i - 1) + "</font>";
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
        editorPane.updateUI();

        // make sure that the currently selected peptide is visible
        if (selectedPeptideStart != -1) {

            boolean referenceMarkerFound = false;

            for (int i = 0; i < referenceMarkers.size() - 1 && !referenceMarkerFound; i++) {
                if (selectedPeptideStart >= referenceMarkers.get(i) && selectedPeptideStart < referenceMarkers.get(i + 1)) {
                    editorPane.scrollToReference(referenceMarkers.get(i).toString());
                    referenceMarkerFound = true;
                }
            }

            if (!referenceMarkerFound) {
                editorPane.scrollToReference(referenceMarkers.get(referenceMarkers.size() - 1).toString());
            }

        } else {
            editorPane.setCaretPosition(0);
        }

        return (sequenceCoverage / cleanSequence.length()) * 100;
    }
}
