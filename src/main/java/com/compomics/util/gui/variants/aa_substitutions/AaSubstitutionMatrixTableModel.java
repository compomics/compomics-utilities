package com.compomics.util.gui.variants.aa_substitutions;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import java.util.HashSet;
import javax.swing.table.DefaultTableModel;

/**
 * Table model to display the content of an amino acid substitution matrix.
 *
 * @author Marc Vaudel
 */
public class AaSubstitutionMatrixTableModel extends DefaultTableModel {

    /**
     * The possible amino acids represented by their single character code.
     */
    private char[] aminoAcids = AminoAcid.getUniqueAminoAcids();
    /**
     * The substitution matrix to display.
     */
    private AaSubstitutionMatrix aaSubstitutionMatrix;
    /**
     * Boolean indicating whether the table can be edited.
     */
    private boolean editable = false;

    /**
     * Constructor.
     *
     * @param aaSubstitutionMatrix the substitution matrix to display
     * @param editable a boolean indicating whether the table can be edited
     */
    public AaSubstitutionMatrixTableModel(AaSubstitutionMatrix aaSubstitutionMatrix, boolean editable) {
        this.aaSubstitutionMatrix = aaSubstitutionMatrix;
        this.editable = editable;
    }

    @Override
    public int getRowCount() {
        if (aminoAcids != null) {
            return aminoAcids.length;
        }
        return 0;
    }

    @Override
    public int getColumnCount() {
        if (aminoAcids != null) {
            return aminoAcids.length + 1;
        }
        return 0;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "  ";
        }
        return aminoAcids[column-1] + "";
    }

    @Override
    public Object getValueAt(int row, int column) {
        char originalAa = aminoAcids[row];
        switch (column) {
            case 0:
                return originalAa;
            default:
                if (aaSubstitutionMatrix != null) {
                    HashSet<Character> possibleVariants = aaSubstitutionMatrix.getSubstitutionAminoAcids(originalAa);
                    if (possibleVariants != null) {
                        Character destinationAminoAcid = aminoAcids[column - 1];
                        return possibleVariants.contains(destinationAminoAcid);
                    }
                }
                return false;
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, columnIndex) != null) {
                return getValueAt(i, columnIndex).getClass();
            }
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable && columnIndex > 1;
    }

    /**
     * Sets the substitution matrix to display.
     * 
     * @param aaSubstitutionMatrix the substitution matrix to display
     */
    public void setAaSubstitutionMatrix(AaSubstitutionMatrix aaSubstitutionMatrix) {
        this.aaSubstitutionMatrix = aaSubstitutionMatrix;
    }
}
