package com.compomics.util.io.export.writers;

import com.compomics.util.io.export.ExportFormat;
import com.compomics.util.io.export.ExportWriter;
import com.compomics.util.io.export.WorkbookStyle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

/**
 * ExportWriter for the export to text files.
 *
 * @author Marc Vaudel
 */
public class TextWriter extends ExportWriter {

    /**
     * Writer.
     */
    private final BufferedWriter writer;
    /**
     * Encoding for the file, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    /**
     * The separator.
     */
    private final String separator;
    /**
     * The number of lines to include between sections.
     */
    private final int nSeparationLines;
    /**
     * The number of sections written.
     */
    private int nSections = 0;

    /**
     * Constructor.
     *
     * @param destinationFile the file where to write the report
     * @param separator separator between two values
     * @param nSeparationLines the number of lines to include between two
     * sections
     * @param gzip if true export as gzipped file
     *
     * @throws IOException if an IOException occurs
     */
    public TextWriter(File destinationFile, String separator, int nSeparationLines, boolean gzip) throws IOException {
        
        this.separator = separator;
        this.exportFormat = ExportFormat.text;
        this.nSeparationLines = nSeparationLines;

        if (gzip) {

            // Setup the writer
            FileOutputStream fileStream = new FileOutputStream(destinationFile);
            GZIPOutputStream gzipStream = new GZIPOutputStream(fileStream);
            OutputStreamWriter encoder = new OutputStreamWriter(gzipStream, encoding);
            this.writer = new BufferedWriter(encoder);

        } else {

            this.writer = new BufferedWriter(new FileWriter(destinationFile));

        }
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
            writer.newLine();
            writer.write(sectionTitle);
        }
        for (int i = 1; i <= nSeparationLines; i++) {
            writer.newLine();
        }
        nSections++;
    }

    @Override
    public void writeHeaderText(String text, WorkbookStyle textStyle) throws IOException {
        writer.write(text);
    }

    @Override
    public void addSeparator(WorkbookStyle textStyle) throws IOException {
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
