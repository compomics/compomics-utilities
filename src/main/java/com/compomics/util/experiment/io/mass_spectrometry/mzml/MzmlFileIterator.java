package com.compomics.util.experiment.io.mass_spectrometry.mzml;

import java.io.File;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.PrecursorParameter;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.waiting.WaitingHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.codec.binary.Base64;

/**
 * An iterator of the spectra in an mzml file. Based on code from jmzML.
 *
 * @author Harald Barsnes
 */
public class MzmlFileIterator implements MsFileIterator {

    /**
     * The supported precision types for the binary data as defined in the mzML
     * specifications and the PSI-MS ontology.
     */
    public enum Precision {
        /**
         * Corresponds to the PSI-MS ontology term "MS:1000521" / "32-bit float"
         * and binary data will be represented in the Java primitive: float
         */
        FLOAT32BIT,
        /**
         * Corresponds to the PSI-MS ontology term "MS:1000523" / "64-bit float"
         * and binary data will be represented in the Java primitive: double
         */
        FLOAT64BIT,
        /**
         * Corresponds to the PSI-MS ontology term "MS:1000519" / "32-bit
         * integer" and binary data will be represented in the Java primitive:
         * int
         */
        INT32BIT,
        /**
         * Corresponds to the PSI-MS ontology term "MS:1000522" / "64-bit
         * integer" and binary data will be represented in the Java primitive:
         * long
         */
        INT64BIT,
        /**
         * Corresponds to the PSI-MS ontology term "MS:1001479" /
         * "null-terminated ASCII string" and binary data will be represented in
         * the Java type: String
         */
        NTSTRING
    }

    /**
     * The reader going through the file.
     */
    private final SimpleFileReader reader;
    /**
     * The waiting handler used to provide progress feedback and cancel the
     * process.
     */
    private final WaitingHandler waitingHandler;
    /**
     * The spectrum read in the last call of the next method.
     */
    private Spectrum spectrum = null;
    /**
     * The XML parser.
     */
    private XMLStreamReader parser;

    /**
     * Constructor.
     *
     * @param mzmlFile the mzml file to go through
     * @param waitingHandler the waiting handler
     */
    public MzmlFileIterator(File mzmlFile, WaitingHandler waitingHandler) {

        reader = SimpleFileReader.getFileReader(mzmlFile);
        XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            parser = factory.createXMLStreamReader(reader.getReader());

            boolean spectrumListFound = false;

            // move to the spectrum list
            while (parser.hasNext() && !spectrumListFound) {

                double progress = reader.getProgressInPercent();
                waitingHandler.setSecondaryProgressCounter((int) progress);

                parser.next();

                switch (parser.getEventType()) {

                    case XMLStreamConstants.START_ELEMENT:

                        String element = parser.getLocalName();

                        if (element.equalsIgnoreCase("spectrumList")) {
                            spectrumListFound = true;
                        }

                        break;

                    default:
                        break;
                }

            }

            if (!spectrumListFound) {

                throw new IllegalArgumentException(
                        "Spectrum list not found when parsing mzML file!"
                );

            }

        } catch (XMLStreamException ex) {

            ex.printStackTrace();
            throw new IllegalArgumentException(
                    "An exception was thrown when trying to create the mzML parser."
            );

        }

        this.waitingHandler = waitingHandler;

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(100);

    }

    @Override
    public String next() {

        spectrum = null;

        try {

            while (parser.hasNext()) {

                double progress = reader.getProgressInPercent();
                waitingHandler.setSecondaryProgressCounter((int) progress);

                parser.next();

                switch (parser.getEventType()) {

                    case XMLStreamConstants.START_ELEMENT:

                        String element = parser.getLocalName();

                        if (element.equalsIgnoreCase("spectrum")) {

                            String id = parser.getAttributeValue("", "id");
                            parseSpectrum();
                            return id;

                        }

                        break;

                    default:
                        break;
                }

            }

        } catch (XMLStreamException ex) {

            ex.printStackTrace();
            throw new IllegalArgumentException(
                    "An exception was thrown when trying to parse the mzML file."
            );

        }

        return null;
    }

    /**
     * Parse a spectrum.
     *
     * @throws XMLStreamException thrown if an XMLStreamException occurs
     */
    private void parseSpectrum() throws XMLStreamException {

        int spectrumLevel = -1;
        double retentionTimeInSeconds = -1.0;
        ArrayList<Integer> possibleChargesAsArray = new ArrayList<>();
        double precursorMz = 0.0;
        double precursorIntensity = 0.0;
        ArrayList<String> precursorIdentifiers = new ArrayList<>();
        double[] mzArray = new double[0];
        double[] intensityArray = new double[0];
        boolean mzArrayValues = true;
        Precision precision = Precision.FLOAT64BIT;
        String compression = "MS:1000574";

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.END_ELEMENT:

                    if ("spectrum".equalsIgnoreCase(parser.getLocalName())) {

                        Precursor precursor = null; // no precusors for ms1 spectra

                        if (spectrumLevel > 1) {

                            precursor = new Precursor(
                                    retentionTimeInSeconds,
                                    precursorMz,
                                    precursorIntensity,
                                    possibleChargesAsArray.stream().mapToInt(i -> i).toArray()
                            );

                        }

                        spectrum = new Spectrum(
                                precursor,
                                mzArray,
                                intensityArray,
                                spectrumLevel
                        );

                        if (!precursorIdentifiers.isEmpty()) {
                            spectrum.addUrParam(new PrecursorParameter(precursorIdentifiers));
                        }

                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    switch (parser.getLocalName().toLowerCase()) {

                        case "precursor":

                            if (parser.getAttributeValue("", "spectrumRef") != null) {

                                String tempPrecursorId = parser.getAttributeValue("", "spectrumRef");

                                if (!precursorIdentifiers.contains(tempPrecursorId)) {
                                    precursorIdentifiers.add(tempPrecursorId);
                                }

                            }

                            break;

                        case "cvparam":

                            if (parser.getAttributeValue("", "accession") != null) {

                                String accession = parser.getAttributeValue("", "accession");
                                String value = parser.getAttributeValue("", "value");

                                if (accession.equalsIgnoreCase("MS:1000511")) {

                                    spectrumLevel = Integer.parseInt(value);

                                } else if (accession.equalsIgnoreCase("MS:1000016")) {

                                    if (parser.getAttributeValue("", "unitName").equalsIgnoreCase("minute")) {
                                        retentionTimeInSeconds = Double.parseDouble(value) * 60;
                                    } else if (parser.getAttributeValue("", "unitName").equalsIgnoreCase("second")) {
                                        retentionTimeInSeconds = Double.parseDouble(value);
                                    }

                                } else if (accession.equalsIgnoreCase("MS:1000041")) {

                                    possibleChargesAsArray.add(Integer.valueOf(value));

                                } else if (accession.equalsIgnoreCase("MS:1000744")) {

                                    precursorMz = Double.parseDouble(value);

                                } else if (accession.equalsIgnoreCase("MS:1000042")) {

                                    precursorIntensity = Double.parseDouble(value);

                                } else if (accession.equalsIgnoreCase("MS:1000514")) {

                                    mzArrayValues = true;

                                } else if (accession.equalsIgnoreCase("MS:1000515")) {

                                    mzArrayValues = false;

                                } else if (accession.equalsIgnoreCase("MS:1000521")) {

                                    precision = Precision.FLOAT32BIT;

                                } else if (accession.equalsIgnoreCase("MS:1000523")) {

                                    precision = Precision.FLOAT64BIT;

                                } else if (accession.equalsIgnoreCase("MS:1000519")) {

                                    precision = Precision.INT32BIT;

                                } else if (accession.equalsIgnoreCase("MS:1000522")) {

                                    precision = Precision.INT64BIT;

                                } else if (accession.equalsIgnoreCase("MS:1001479")) {

                                    precision = Precision.NTSTRING;

                                } else if (accession.equalsIgnoreCase("MS:1000574")
                                        || accession.equalsIgnoreCase("MS:1000576")
                                        || accession.equalsIgnoreCase("MS:1002312")
                                        || accession.equalsIgnoreCase("MS:1002314")
                                        || accession.equalsIgnoreCase("MS:1002313")) {

                                    compression = accession;

                                }

                            }

                            break;

                        case "binary":

                            String binaryAsText = parser.getElementText();
                            byte[] binaryDataArray = Base64.decodeBase64(binaryAsText.getBytes());

                            if (mzArrayValues) {

                                Number[] tempNumbers = getBinaryDataAsNumberArray(compression, precision, binaryDataArray);
                                mzArray = new double[tempNumbers.length];

                                for (int i = 0; i < tempNumbers.length; i++) {
                                    mzArray[i] = tempNumbers[i].doubleValue();
                                }

                            } else {

                                Number[] tempNumbers = getBinaryDataAsNumberArray(compression, precision, binaryDataArray);
                                intensityArray = new double[tempNumbers.length];

                                for (int i = 0; i < tempNumbers.length; i++) {
                                    intensityArray[i] = tempNumbers[i].doubleValue();
                                }

                            }

                            break;

                        default:

                            break;
                    }

                    break;

                default:
                    break;
            }
        }

    }

    @Override
    public Spectrum getSpectrum() {

        return spectrum;

    }

    @Override
    public void close() {

        try {

            parser.close();

        } catch (XMLStreamException ex) {

            ex.printStackTrace();
            throw new IllegalArgumentException(
                    "An exception was thrown when closing the mzML parser."
            );

        }

        reader.close();
    }

    /**
     * Reads true if the binary data is compressed.
     *
     * @param compression the compression level
     *
     * @return true if the data is compressed
     */
    public boolean needsUncompressing(String compression) {

        return compression.equalsIgnoreCase("MS:1000574");

    }

    /**
     * Retrieve the binary data as an array of numeric values.
     *
     * @param compression the compression accession number
     * @param precision the precision type
     * @param binary the binary data, base64 encoded
     *
     * @return a Number array representation of the binary data
     */
    public Number[] getBinaryDataAsNumberArray(
            String compression,
            Precision precision,
            byte[] binary
    ) {

        // decompression of the data
        byte[] data;

        if (needsUncompressing(compression)) {
            data = decompress(binary);
        } else {
            data = binary;
        }

        Number[] dataArray;

        // if data has been numpress compressed, do the decompression
        if (compression.equalsIgnoreCase("MS:1002312")
                || compression.equalsIgnoreCase("MS:1002314")
                || compression.equalsIgnoreCase("MS:1002313")) {

            dataArray = MSNumpress.decode(compression, data);
            return dataArray;

        }

        // if not, apply the specified precision when converting into numeric values        
        switch (precision) {

            case FLOAT64BIT:
                dataArray = convertData(data, Precision.FLOAT64BIT);
                break;

            case FLOAT32BIT:
                dataArray = convertData(data, Precision.FLOAT32BIT);
                break;

            case INT64BIT:
                dataArray = convertData(data, Precision.INT64BIT);
                break;

            case INT32BIT:
                dataArray = convertData(data, Precision.INT32BIT);
                break;

            case NTSTRING:
                throw new IllegalArgumentException(
                        "Precision " + Precision.NTSTRING + " is not supported in this method!"
                );

            default:
                throw new IllegalStateException(
                        "Not supported Precision in BinaryDataArray: " + precision
                );
        }

        return dataArray;
    }

    /**
     * Convert the binary data.
     *
     * @param data the binary data to convert
     * @param precision the precision
     * @return the converted data
     */
    private Number[] convertData(
            byte[] data,
            Precision precision
    ) {

        int step;

        switch (precision) {

            case FLOAT64BIT:
            case INT64BIT:
                step = 8;
                break;

            case FLOAT32BIT:
            case INT32BIT:
                step = 4;
                break;

            default:
                step = -1;

        }

        // create a Number array of sufficient size
        Number[] resultArray = new Number[data.length / step];

        // create a buffer around the data array for easier retrieval
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN); // the order is always LITTLE_ENDIAN

        // progress in steps of 4/8 bytes according to the set step
        for (int indexOut = 0; indexOut < data.length; indexOut += step) {

            // Note that the 'getFloat(index)' and getInt(index) methods read the next 4 bytes
            // and the 'getDouble(index)' and getLong(index) methods read the next 8 bytes.
            Number num;

            switch (precision) {

                case FLOAT64BIT:
                    num = bb.getDouble(indexOut);
                    break;

                case INT64BIT:
                    num = bb.getLong(indexOut);
                    break;

                case FLOAT32BIT:
                    num = bb.getFloat(indexOut);
                    break;

                case INT32BIT:
                    num = bb.getInt(indexOut);
                    break;

                default:
                    num = null;

            }

            resultArray[indexOut / step] = num;

        }

        return resultArray;
    }

    /**
     * Decompress the data.
     *
     * @param compressedData the compressed data
     * @return the decompressed data
     */
    private byte[] decompress(byte[] compressedData) {

        byte[] decompressedData;

        // using a ByteArrayOutputStream to not having to define the result array size beforehand
        Inflater decompressor = new Inflater();

        decompressor.setInput(compressedData);

        // create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
        byte[] buf = new byte[1024];

        while (!decompressor.finished()) {

            try {

                int count = decompressor.inflate(buf);

                if (count == 0 && decompressor.needsInput()) {
                    break;
                }

                bos.write(buf, 0, count);

            } catch (DataFormatException e) {
                throw new IllegalStateException(
                        "Encountered wrong data format "
                        + "while trying to decompress binary data!",
                        e
                );
            }

        }

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the decompressed data
        decompressedData = bos.toByteArray();

        if (decompressedData == null) {

            throw new IllegalStateException(
                    "Decompression of binary data produced no result (null)!"
            );

        }

        return decompressedData;
    }
}