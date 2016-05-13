package com.compomics.util.io.export.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Simple writer for xml files.
 *
 * @author Marc Vaudel
 */
public class SimpleXmlWriter {

    /**
     * Integer keeping track of the number of indents to include at the
     * beginning of each line.
     */
    protected int indentCounter = 0;
    /**
     * Cache for the indents.
     */
    private HashMap<Integer, String> indentMap = new HashMap<Integer, String>();
    /**
     * The indent characters to use, e.g. tab or space.
     */
    private String indentString = "\t";
    /**
     * The buffered writer to use.
     */
    private BufferedWriter bw;

    /**
     * Constructor.
     *
     * @param bw the buffered writer to use.
     */
    public SimpleXmlWriter(BufferedWriter bw) {
        this.bw = bw;
    }

    /**
     * Convenience method returning the indent in the beginning of each line
     * depending on the tabCounter.
     *
     * @return the tabs in the beginning of each line as a string
     */
    private String getCurrentIndent() {
        return getIndentAtN(indentCounter);
    }

    /**
     * Convenience method returning the indent in the beginning of each line
     * depending on the tabCounter.
     *
     * @return the tabs in the beginning of each line as a string
     */
    private String getIndentAtN(int n) {

        if (n == 0) {
            return "";
        }
        String currentTab = indentMap.get(n);
        if (currentTab == null) {
            String previousIndent = getIndentAtN(n - 1);
            StringBuilder tabBuilder = new StringBuilder(previousIndent.length() + indentString.length());
            tabBuilder.append(previousIndent);
            tabBuilder.append(indentString);
            currentTab = tabBuilder.toString();
            indentMap.put(indentCounter, currentTab);
        }
        return currentTab;
    }

    /**
     * Returns the indent string to use, e.g. tab or space.
     *
     * @return the indent string to use
     */
    public String getIndentString() {
        return indentString;
    }

    /**
     * Sets the indent string to use, e.g. tab or space.
     *
     * @param indentString the indent string to use
     */
    public void setIndentString(String indentString) {
        this.indentString = indentString;
        indentMap.clear();
    }

    /**
     * Writes the current indent.
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    public void writeIndent() throws IOException {
        bw.write(getCurrentIndent());
    }

    /**
     * Writes the content to the file.
     *
     * @param content the content as String
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    public void write(String content) throws IOException {
        bw.write(content);
    }

    /**
     * Writes the given line.
     *
     * @param line the line to write
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    public void writeLine(String line) throws IOException {
        writeLine(line, false, false);
    }

    /**
     * Writes the given line with an increased indent.
     *
     * @param line the line to write
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    public void writeLineIncreasedIndent(String line) throws IOException {
        writeLine(line, true, false);
    }

    /**
     * Writes the given line with a decreased indent.
     *
     * @param line the line to write
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    public void writeLineDecreasedIndent(String line) throws IOException {
        writeLine(line, false, true);
    }

    /**
     * Writes the given line with eventual increase or decrease in indents.
     *
     * @param line the line to write
     * @param increaseIndent boolean indicating whether the indent should be
     * increased for this line
     * @param decreaseIndent boolean indicating whether the indent should be
     * decreased for this line
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    private void writeLine(String line, boolean increaseIndent, boolean decreaseIndent) throws IOException {
        if (increaseIndent) {
            increaseIndent();
        }
        if (decreaseIndent) {
            decreaseIndent();
        }
        bw.write(getCurrentIndent());
        bw.write(line);
        newLine();
    }

    /**
     * Adds an end of line character.
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing
     */
    public void newLine() throws IOException {
        bw.newLine();
    }

    /**
     * Increases the indent counter.
     */
    public void increaseIndent() {
        indentCounter++;
    }

    /**
     * Decreases the indent counter.
     */
    public void decreaseIndent() {
        indentCounter--;
    }

    /**
     * Closes the buffered writer.
     *
     * @throws java.io.IOException Exception thrown whenever an error occurred
     * while closing the file.
     */
    public void close() throws IOException {
        bw.close();
    }
}
