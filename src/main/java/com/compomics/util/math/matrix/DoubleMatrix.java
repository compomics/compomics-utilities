/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.math.matrix;

import java.util.ArrayList;

/**
 * Implementation of a matrix for double objects. Warning: all indexes start
 * from 0.
 *
 * @author Marc
 */
public class DoubleMatrix {

    private int nLines = 0;

    private ArrayList<ArrayList<Double>> content;

    public DoubleMatrix() {
        content = new ArrayList<ArrayList<Double>>();
    }

    public DoubleMatrix(int nColumns) {
        content = new ArrayList<ArrayList<Double>>(nColumns);
    }

    public DoubleMatrix(DoubleMatrix matrix) {
        nLines = matrix.getNLines();
        for (ArrayList<Double> column : matrix.getColumns()) {
            ArrayList<Double> newColumn = new ArrayList<Double>(column);
            content.add(newColumn);
        }
    }

    public void addColumn(ArrayList<Double> column) {
        if (column == null) {
            throw new IllegalArgumentException("Attempting to add null column to matrix.");
        }
        int columnsize = column.size();
        if (columnsize == 0) {
            throw new IllegalArgumentException("Attempting to add empty column to matrix");
        }
        if (nLines == 0) {
            nLines = columnsize;
        } else if (columnsize != nLines) {
            throw new IllegalArgumentException("Impossible to add column of length " + column.size() + " in matrix of length " + nLines + ".");
        }
        content.add(column);
    }

    public void addLine(ArrayList<Double> line) {
        if (line == null) {
            throw new IllegalArgumentException("Attempting to add null line to matrix.");
        }
        int lineSize = line.size();
        if (lineSize != getNColumns()) {
            throw new IllegalArgumentException("Impossible to add line of length " + lineSize + " in matrix of width " + getNColumns() + ".");
        }
        for (int i = 0; i < getNColumns(); i++) {
            ArrayList<Double> column = content.get(i);
            column.add(line.get(i));
        }
        nLines++;
    }
    
    public void setLine(int lineIndex, ArrayList<Double> line) {
        if (line == null) {
            throw new IllegalArgumentException("Attempting to add null line to matrix.");
        }
        int lineSize = line.size();
        if (lineSize != getNColumns()) {
            throw new IllegalArgumentException("Impossible to add line of length " + lineSize + " in matrix of width " + getNColumns() + ".");
        }
        if (lineIndex >= nLines) {
            throw new IllegalArgumentException("Impossible to add line at index " + lineIndex + " in matrix of length " + nLines + ".");
        }
        for (int i = 0; i < getNColumns(); i++) {
            ArrayList<Double> column = content.get(i);
            column.set(lineIndex, line.get(i));
        }
    }
    
    public void setColumn(int columnIndex, ArrayList<Double> column) {
        if (column == null) {
            throw new IllegalArgumentException("Attempting to add null column to matrix.");
        }
        int columnSize = column.size();
        if (columnSize != getNLines()) {
            throw new IllegalArgumentException("Impossible to add line of length " + columnSize + " in matrix of width " + getNLines()+ ".");
        }
        if (columnIndex >= getNColumns()) {
            throw new IllegalArgumentException("Impossible to add line at index " + columnIndex + " in matrix of length " + getNColumns() + ".");
        }
        content.set(columnIndex, column);
    }

    public int getNColumns() {
        return content.size();
    }

    public int getNLines() {
        return nLines;
    }

    public boolean isSquare() {
        return getNLines() == getNColumns();
    }

    public ArrayList<Double> getColumn(int columnIndex) {
        return content.get(columnIndex);
    }

    public ArrayList<ArrayList<Double>> getColumns() {
        return new ArrayList<ArrayList<Double>>(content);
    }

    public ArrayList<ArrayList<Double>> getLines() {
        ArrayList<ArrayList<Double>> lines = new ArrayList<ArrayList<Double>>(nLines);
        for (int i = 0; i < nLines; i++) {
            lines.add(getLine(i));
        }
        return lines;
    }

    public ArrayList<Double> getLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= nLines) {
            throw new IllegalArgumentException("Invalid index " + lineIndex + " for matrix of size " + nLines + ".");
        }
        ArrayList<Double> result = new ArrayList<Double>(nLines);
        for (ArrayList<Double> column : content) {
            result.add(column.get(lineIndex));
        }
        return result;
    }

    /**
     * Returns the value of the matrix at given indexes. 0 is the first index.
     *
     * @param lineIndex
     * @param columnIndex
     * @return
     */
    public Double getValueAt(int lineIndex, int columnIndex) {
        return content.get(columnIndex).get(lineIndex);
    }
    
    public void setValueAt(int lineIndex, int columnIndex, Double value) {
        if (columnIndex >= content.size()) {
            throw new IllegalArgumentException("Column index " + columnIndex + " larger than matrix capacity " + content.size() + ".");
        }
        if (lineIndex >= nLines) {
            throw new IllegalArgumentException("Line index " + columnIndex + " larger than matrix capacity " + nLines + ".");
        }
        content.get(columnIndex).set(lineIndex, value);
    }

    public static DoubleMatrix transpose(DoubleMatrix matrix) {
        int nLines = matrix.getNLines();
        DoubleMatrix transposedMatrix = new DoubleMatrix(nLines);
        for (int i = 0; i < nLines; i++) {
            transposedMatrix.addColumn(matrix.getLine(i));
        }
        return transposedMatrix;
    }

    public boolean equals(DoubleMatrix anotherMatrix) {
        if (anotherMatrix.getNColumns() != getNColumns() || anotherMatrix.getNLines() != getNLines()) {
            return false;
        }
        for (int i = 0; i < getNLines(); i++) {
            for (int j = 0; j < getNColumns(); j++) {
                if (!getValueAt(i, j).equals(anotherMatrix.getValueAt(i, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static DoubleMatrix getIdentityMatrix(int n) {
        DoubleMatrix identityMatrix = new DoubleMatrix(n);
        for (int i = 0; i < n; i++) {
            ArrayList<Double> column = new ArrayList<Double>(n);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    column.add(1.0);
                } else {
                    column.add(0.0);
                }
            }
            identityMatrix.addColumn(column);
        }
        return identityMatrix;
    }

    public double getTrace() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Attempting to estimate the trace on a non-square matrix (" + getNLines() + " lines, " + getNColumns() + " columns).");
        }
        double result = 0;
        for (int i = 0; i < nLines; i++) {
            result += getValueAt(i, i);
        }
        return result;
    }

    public DoubleMatrix getSubMatrix(int lineStart, int lineStop, int columnStart, int columnStop) {
        if (lineStop < lineStart) {
            throw new IllegalArgumentException("End line index smaller than start index.");
        }
        if (columnStop < columnStart) {
            throw new IllegalArgumentException("End column index smaller than start index.");
        }
        if (columnStop + 1 == columnStart) {
            return new DoubleMatrix();
        }
        DoubleMatrix subMatrix = new DoubleMatrix(columnStop + 1 - columnStart);
        for (int i = columnStart; i <= columnStop; i++) {
            ArrayList<Double> column = getColumn(i);
            ArrayList<Double> subColumn = new ArrayList<Double>(column.subList(lineStart, lineStop + 1));
            subMatrix.addColumn(subColumn);
        }
        return subMatrix;
    }

    public void appendColumns(DoubleMatrix columns) {
        if (columns.getNLines() != getNLines()) {
            throw new IllegalArgumentException("Attempting to appennd columns with different number of lines.");
        }
        for (ArrayList<Double> column : columns.getColumns()) {
            ArrayList<Double> newColumn = new ArrayList<Double>(column);
            addColumn(newColumn);
        }
    }

    public void appendLines(DoubleMatrix lines) {
        if (lines.getNColumns() != getNColumns()) {
            throw new IllegalArgumentException("Attempting to appennd lines with different number of columns.");
        }
        for (ArrayList<Double> line : lines.getLines()) {
            ArrayList<Double> newLine = new ArrayList<Double>(line);
            addLine(newLine);
        }
    }

    public double getDeterminant() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Attempting to estimate the determinant on a non-square matrix (" + getNLines() + " lines, " + getNColumns() + " columns).");
        }
        if (nLines == 0) {
            throw new IllegalArgumentException("Attempting to estimate the determinant on an empty matrix.");
        }
        if (nLines == 1) {
            return getValueAt(0, 0);
        } else if (nLines == 2) {
            return getValueAt(0, 0) * getValueAt(1, 1) - getValueAt(0, 1) * getValueAt(1, 0);
        } else {
            double determinant = 0;
            for (int i = 0; i < getNLines(); i++) {
                int line = i + 1;
                System.out.println(line + " in " + nLines);
                DoubleMatrix subMatrix;
                if (i == nLines - 1) {
                    subMatrix = getSubMatrix(0, nLines - 2, 1, nLines - 1);
                } else {
                    subMatrix = getSubMatrix(i + 1, nLines - 1, 1, nLines - 1);
                    if (i > 0) {
                        DoubleMatrix topMatrix = getSubMatrix(0, i - 1, 1, nLines - 1);
                        subMatrix.appendLines(topMatrix);
                    }
                }
                determinant += Math.pow(-1, i) * getValueAt(i, 0) * subMatrix.getDeterminant();
            }
            return determinant;
        }
    }

    /**
     * Returns a score based on the non-diagonal values. Score is 0 for
     * identity, 1 for the (1) matrix.
     *
     * @return
     */
    public double getNonDiagonalScore() {
        if (!isSquare()) {
            throw new IllegalArgumentException("The non diagonal score can only be computed on square matrices (" + getNLines() + " lines, " + getNColumns() + " columns).");
        }
        if (nLines < 2) {
            return 0;
        }
        double score = 0;
        for (int i = 0; i < nLines; i++) {
            for (int j = 0; j < nLines; j++) {
                if (i != j) {
                    int distance = Math.abs(i - j);
                    int nValues = nLines - distance;
                    score += getValueAt(i, j) / nValues;
                }
            }
        }
        return score/nLines;
    }
    
    public void linePermutation(int line1, int line2) {
        ArrayList<Double> tempLine = getLine(line2);
        setLine(line2, getLine(line1));
        setLine(line1, tempLine);
    }
    
    public void columnPermutation(int column1, int column2) {
        ArrayList<Double> tempColumn = getColumn(column2);
        setColumn(column2, getColumn(column1));
        setColumn(column1, tempColumn);
    }

}
