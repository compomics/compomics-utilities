package com.compomics.util.io.export;

import com.compomics.util.io.export.writers.ExcelWriter;
import com.compomics.util.io.export.writers.TextWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The export writer will write the output in the desired export format.
 *
 * @author Marc Vaudel
 */
public abstract class ExportWriter {

    /**
     * Key to store the last export folder.
     */
    public static final String lastFolderKey = "export";
    /**
     * The format of the export.
     */
    protected ExportFormat exportFormat;

    /**
     * Returns the export of the format.
     *
     * @return the export of the format
     */
    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    /**
     * Writes text to the export.
     *
     * @param text the text to write
     *
     * @throws IOException if an IOException occurs
     */
    public void write(String text) throws IOException {
        write(text, null);
    }

    /**
     * Writes text to the export.
     *
     * @param text the text to write
     * @param textStyle the style to use, overwrites any previous/default
     *
     * @throws IOException if an IOException occurs
     */
    public abstract void write(String text, WorkbookStyle textStyle) throws IOException;

    /**
     * Writes the main title.
     *
     * @param text the text to write
     *
     * @throws IOException if an IOException occurs
     */
    public void writeMainTitle(String text) throws IOException {
        writeMainTitle(text, null);
    }

    /**
     * Writes the main title.
     *
     * @param text the text to write
     * @param textStyle the style to use, overwrites any previous/default
     *
     * @throws IOException if an IOException occurs
     */
    public abstract void writeMainTitle(String text, WorkbookStyle textStyle) throws IOException;

    /**
     * Starts a new section.
     *
     * @throws IOException if an IOException occurs
     */
    public void startNewSection() throws IOException {
        startNewSection(null, null);
    }

    /**
     * Starts a new section.
     *
     * @param sectionTitle the text to write
     *
     * @throws IOException if an IOException occurs
     */
    public void startNewSection(String sectionTitle) throws IOException {
        startNewSection(sectionTitle, null);
    }

    /**
     * Starts a new section.
     *
     * @param sectionTitle the text to write
     * @param textStyle the style to use, overwrites any previous/default
     *
     * @throws IOException if an IOException occurs
     */
    public abstract void startNewSection(String sectionTitle, WorkbookStyle textStyle) throws IOException;

    /**
     * Writes header text to the export.
     *
     * @param text the text to write
     *
     * @throws IOException if an IOException occurs
     */
    public void writeHeaderText(String text) throws IOException {
        writeHeaderText(text, null);
    }

    /**
     * Writes header text to the export.
     *
     * @param text the text to write
     * @param textStyle the style to use, overwrites any previous/default
     *
     * @throws IOException if an IOException occurs
     */
    public abstract void writeHeaderText(String text, WorkbookStyle textStyle) throws IOException;

    /**
     * Adds a separator.
     *
     * @throws IOException if an IOException occurs
     */
    public void addSeparator() throws IOException {
        addSeparator(null);
    }

    /**
     * Adds a separator.
     * 
     * @param textStyle the style to use, overwrites any previous/default
     *
     * @throws IOException if an IOException occurs
     */
    public abstract void addSeparator(WorkbookStyle textStyle) throws IOException;

    /**
     * Adds a separator.
     *
     * @throws IOException if an IOException occurs
     */
    public abstract void newLine() throws IOException;

    /**
     * Writes the content in cache and closes the connection to the file.
     *
     * @throws IOException if an IOException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public abstract void close() throws IOException, FileNotFoundException;

    /**
     * Notifies the writer that data of a higher hierarchical depth will be
     * written, e.g. going from protein to peptide.
     */
    public abstract void increaseDepth();

    /**
     * Notifies the writer that data of a lower hierarchical depth will be
     * written, e.g. going from peptide to protein.
     */
    public abstract void decreseDepth();

    /**
     * Returns an export writer for the desired format.
     *
     * @param exportFormat the export format
     * @param destinationFile the file where to write the export
     * @param separator the separator for a text export
     * @param nSeparationLines the number of separation lines between two
     * sections for a text export
     *
     * @return an export writer for the desired format
     *
     * @throws IOException if an IOException occurs
     */
    public static ExportWriter getExportWriter(ExportFormat exportFormat, File destinationFile, String separator, int nSeparationLines) throws IOException {
        switch (exportFormat) {
            case excel:
                return new ExcelWriter(destinationFile);
            case text:
                return new TextWriter(destinationFile, separator, nSeparationLines);
            default:
                throw new IllegalArgumentException("No exporter implemented for format " + exportFormat.name + ".");
        }
    }
}
