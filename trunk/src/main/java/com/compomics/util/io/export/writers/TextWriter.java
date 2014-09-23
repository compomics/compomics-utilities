package com.compomics.util.io.export.writers;

import com.compomics.util.io.export.ExportFormat;
import com.compomics.util.io.export.ExportWriter;
import com.compomics.util.io.export.WorkbookStyle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ExportWriter for the export to text files
 *
 * @author Marc
 */
public class TextWriter extends ExportWriter {

    /**
     * Writer
     */
    private final BufferedWriter writer;
    /**
     * The separator
     */
    private final String separator;
    /**
     * The number of lines to include between sections
     */
    private final int nSeparationLines;
    /**
     * The number of sections written
     */
    private int nSections = 0;

    /**
     * Constructor
     *
     * @param destinationFile the file where to write the report
     * @param separator separator between two values
     * @param nSeparationLines the number of lines to include between two
     * sections
     *
     * @throws IOException
     */
    public TextWriter(File destinationFile, String separator, int nSeparationLines) throws IOException {
        this.separator = separator;
        writer = new BufferedWriter(new FileWriter(destinationFile));
        exportFormat = ExportFormat.text;
        this.nSeparationLines = nSeparationLines;
    }

    @Override
    public void write(String text, WorkbookStyle textStyle) throws IOException {
        writer.write(text);
    }

    @Override
    public void writeMainTitle(String text, WorkbookStyle textStyle) throws IOException {
        if (text != null) {
            writer.write(text);
            for (int i = 1; i <= nSeparationLines; i++) {
                writer.newLine();
            }
        }
    }

    @Override
    public void startNewSection(String sectionTitle, WorkbookStyle textStyle) throws IOException {
        if (sectionTitle != null) {
            writer.write(sectionTitle);
        }
        if (nSections > 0) {
            for (int i = 1; i <= nSeparationLines; i++) {
                writer.newLine();
            }
        }
        nSections++;
    }

    @Override
    public void writeHeaderText(String text, WorkbookStyle textStyle) throws IOException {
        writer.write(text);
    }

    @Override
    public void addSeparator() throws IOException {
        writer.write(separator);
    }

    @Override
    public void newLine() throws IOException {
        writer.newLine();
    }

    @Override
    public void close() throws IOException, FileNotFoundException {
        writer.close();
    }

    @Override
    public void increaseDepth() {
        // Nothing to do here
    }

    @Override
    public void decreseDepth() {
        // Nothing to do here
    }

}
