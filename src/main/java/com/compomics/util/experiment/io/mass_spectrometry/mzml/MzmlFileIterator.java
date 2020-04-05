package com.compomics.util.experiment.io.mass_spectrometry.mzml;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.File;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.jmzml.MzMLElement;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

/**
 * An iterator of the spectra in an mzml file.
 *
 * @author Harald Barsnes
 */
public class MzmlFileIterator implements MsFileIterator {

    /**
     * The mzML unmarshaler.
     */
    private MzMLUnmarshaller mzmlUnmarshaler;
    /**
     * The spectrum iterator.
     */
    private Iterator<uk.ac.ebi.jmzml.model.mzml.Spectrum> iterator;
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
     * Empty default constructor.
     */
    public MzmlFileIterator() {
        mzmlUnmarshaler = null;
        iterator = null;
        waitingHandler = null;
    }

    /**
     * Constructor.
     *
     * @param mzmlFile the mzml file to go through
     * @param waitingHandler the waiting handler
     */
    public MzmlFileIterator(File mzmlFile, WaitingHandler waitingHandler) {

        this.waitingHandler = waitingHandler;

        // turn off all the logs to speed up the parsing
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("psidev.psi.tools.xxindex.FastXmlElementExtractor");
        logger.setLevel(Level.toLevel("ERROR"));

        logger = loggerContext.getLogger("psidev.psi.tools.xxindex.index");
        logger.setLevel(Level.toLevel("ERROR"));

        logger = loggerContext.getLogger("uk.ac.ebi.jmzml");
        logger.setLevel(Level.toLevel("ERROR"));

        mzmlUnmarshaler = new MzMLUnmarshaller(mzmlFile);

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(mzmlUnmarshaler.getSpectrumIDs().size());
        }

        iterator = mzmlUnmarshaler.unmarshalCollectionFromXpath(
                MzMLElement.Spectrum.getXpath(),
                uk.ac.ebi.jmzml.model.mzml.Spectrum.class
        );
    }

    @Override
    public String next() {

        spectrum = null;

        if (iterator.hasNext()) {

            uk.ac.ebi.jmzml.model.mzml.Spectrum mzmlSpectrum = null;
            boolean ms2Spectrum = false;

            while (!ms2Spectrum && iterator.hasNext()) {

                mzmlSpectrum = iterator.next();

                // check the ms level
                List<CVParam> spectrumCvParams = mzmlSpectrum.getCvParam();
                Iterator<CVParam> spectrumCvParamsIterator = spectrumCvParams.iterator();

                while (spectrumCvParamsIterator.hasNext()) {

                    CVParam tempCvParam = spectrumCvParamsIterator.next();

                    if (tempCvParam.getAccession().equalsIgnoreCase("MS:1000511")
                            && Integer.valueOf(tempCvParam.getValue()) == 2) {
                        ms2Spectrum = true;
                        break;
                    }
                }

                if (waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressCounter();
                }
            }

            if (ms2Spectrum && mzmlSpectrum != null) {

                double retentionTimeInSeconds = -1.0;

                // get the retention time
                if (mzmlSpectrum.getScanList().getCount() > 0) {
                    Iterator<CVParam> scanCvParamIterator
                            = mzmlSpectrum.getScanList().getScan().get(0).getCvParam().iterator();

                    while (scanCvParamIterator.hasNext()) {

                        CVParam tempCvParam = scanCvParamIterator.next();

                        if (tempCvParam.getAccession().equalsIgnoreCase("MS:1000016")) {
                            if (tempCvParam.getUnitName().equalsIgnoreCase("minute")) {
                                retentionTimeInSeconds = Double.valueOf(tempCvParam.getValue()) * 60;
                            } else if (tempCvParam.getUnitName().equalsIgnoreCase("second")) {
                                retentionTimeInSeconds = Double.valueOf(tempCvParam.getValue());
                            }
                        }
                    }
                }

                // get the precursor m/z, charge and intensity
                ArrayList<Integer> possibleChargesAsArray = new ArrayList<>(); // @TODO: can there be more than one..?
                double precursorMz = -1.0;
                double precursorIntensity = -1.0; // @TODO: how to handle missing values?

                if (mzmlSpectrum.getPrecursorList().getCount() > 0) {
                    uk.ac.ebi.jmzml.model.mzml.Precursor mzMlPrecursor
                            = mzmlSpectrum.getPrecursorList().getPrecursor().get(0);

                    if (mzMlPrecursor.getSelectedIonList().getCount() > 0) {
                        Iterator<CVParam> selectedIonCvTerms = mzmlSpectrum.getPrecursorList().getPrecursor().get(0).getSelectedIonList().getSelectedIon().get(0).getCvParam().iterator();

                        while (selectedIonCvTerms.hasNext()) {
                            CVParam tempCvParam = selectedIonCvTerms.next();

                            if (tempCvParam.getAccession().equalsIgnoreCase("MS:1000041")) {
                                possibleChargesAsArray.add(Integer.valueOf(tempCvParam.getValue()));
                            } else if (tempCvParam.getAccession().equalsIgnoreCase("MS:1000744")) {
                                precursorMz = Double.valueOf(tempCvParam.getValue());
                            } else if (tempCvParam.getAccession().equalsIgnoreCase("MS:1000042")) {
                                precursorIntensity = Double.valueOf(tempCvParam.getValue()); // @TODO: why is this different from the mgf intensity?
                            }
                        }
                    }
                }

                Precursor precursor = new Precursor(retentionTimeInSeconds, precursorMz, precursorIntensity, possibleChargesAsArray.stream().mapToInt(i -> i).toArray());

                double[] mzArray = new double[0];
                double[] intensityArray = new double[0];

                for (BinaryDataArray binaryDataArray : mzmlSpectrum.getBinaryDataArrayList().getBinaryDataArray()) {

                    BinaryDataArray.DataType type = binaryDataArray.getDataType();

                    if (type.equals(BinaryDataArray.DataType.MZ_VALUES)) {
                        mzArray = new double[binaryDataArray.getArrayLength()];
                        Number[] tempNumbers = binaryDataArray.getBinaryDataAsNumberArray();

                        for (int i = 0; i < binaryDataArray.getArrayLength(); i++) {
                            mzArray[i] = tempNumbers[i].doubleValue();
                        }
                    }

                    if (type.equals(BinaryDataArray.DataType.INTENSITY)) {
                        intensityArray = new double[binaryDataArray.getArrayLength()];
                        Number[] tempNumbers = binaryDataArray.getBinaryDataAsNumberArray();

                        for (int i = 0; i < binaryDataArray.getArrayLength(); i++) {
                            intensityArray[i] = tempNumbers[i].doubleValue();
                        }
                    }

                }

                spectrum = new Spectrum(precursor, mzArray, intensityArray);

                return mzmlSpectrum.getId();
            }
        }

        return null;
    }

    @Override
    public Spectrum getSpectrum() {
        return spectrum;
    }

    @Override
    public void close() {
        // @TODO: there does not seem to be any close for the mzml unmarshaler..?
    }
}
