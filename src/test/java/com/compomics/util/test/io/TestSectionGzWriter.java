package com.compomics.util.test.io;

import com.compomics.util.io.IoUtil;
import com.compomics.util.io.compression.SectionGzWriter.SectionGzWriter;
import com.compomics.util.io.flat.SimpleFileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.zip.CRC32;
import junit.framework.TestCase;

/**
 * Test for the SectionGzWriter.
 *
 * @author Marc Vaudel
 */
public class TestSectionGzWriter extends TestCase {

    public void testSingleSection()
            throws FileNotFoundException, IOException {

        File destinationFile = new File("src/test/resources/tempSectionGzWriter.gz");

        String sectionName = "1";
        String content = "TEST_TEST_TEST";
        StringBuilder sb = new StringBuilder();

        SectionGzWriter writer = new SectionGzWriter(destinationFile, destinationFile.getParentFile());

        writer.registerSection(sectionName);

        for (int i = 0; i < 1000; i++) {

            String toWrite = content + "_" + i + "|";

            writer.write(sectionName, toWrite);

            sb.append(toWrite);
        }

        writer.sectionCompleted(sectionName);

        writer.close();

        String expectedLine = sb.toString();

        SimpleFileReader reader = SimpleFileReader.getFileReader(destinationFile);

        String line = reader.readLine();

        assertTrue(line.equals(expectedLine));

        assertNull(reader.readLine());

        reader.close();

        destinationFile.delete();

    }

    public void testMultipleSections()
            throws FileNotFoundException, IOException {
        
        // Single thread

        int nSections = 10;

        File destinationFile = new File("src/test/resources/tempSectionGzWriter.gz");

        String content = "TEST_TEST_TEST";
        StringBuilder sb = new StringBuilder();

        SectionGzWriter writer1 = new SectionGzWriter(destinationFile, destinationFile.getParentFile());

        for (int section = 0; section < nSections; section++) {

            String sectionName = "SECTION_" + section;

            writer1.registerSection(sectionName);

            for (int i = 0; i < 1000; i++) {

                String toWrite = content + "_" + sectionName + "_" + i + "|";

                writer1.write(sectionName, toWrite);

                sb.append(toWrite);

            }

            writer1.sectionCompleted(sectionName);

        }

        writer1.close();

        String expectedLine = sb.toString();

        SimpleFileReader reader = SimpleFileReader.getFileReader(destinationFile);

        String line = reader.readLine();

        assertTrue(line.equals(expectedLine));

        assertNull(reader.readLine());

        reader.close();

        destinationFile.delete();
        
        
        // Parallel
        
        SectionGzWriter writer2 = new SectionGzWriter(destinationFile, destinationFile.getParentFile());

        for (int section = 0; section < nSections; section++) {

            String sectionName = "SECTION_" + section;

            writer2.registerSection(sectionName);

        }

        IntStream.range(0, nSections)
                .parallel()
                .forEach(section -> {

                            String sectionName = "SECTION_" + section;

                            for (int i = 0; i < 1000; i++) {

                                String toWrite = content + "_" + sectionName + "_" + i + "|";

                                writer2.write(sectionName, toWrite);

                            }

                        }
                );

        for (int section = 0; section < nSections; section++) {

            String sectionName = "SECTION_" + section;

            writer2.sectionCompleted(sectionName);

        }

        writer2.close();

        reader = SimpleFileReader.getFileReader(destinationFile);

        line = reader.readLine();

        assertTrue(line.equals(expectedLine));

        assertNull(reader.readLine());

        reader.close();

        destinationFile.delete();

    }

}
