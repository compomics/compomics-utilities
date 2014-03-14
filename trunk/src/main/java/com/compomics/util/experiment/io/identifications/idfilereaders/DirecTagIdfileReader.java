package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.TagAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * An identification file reader for Direct tag results.
 *
 * @author Marc Vaudel
 */
public class DirecTagIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The name of the tags generator used to create the file.
     */
    private String tagsGenerator;
    /**
     * The version of the tags generator.
     */
    private String tagsGeneratorVersion;
    /**
     * The copyright.
     */
    private String copyRight;
    /**
     * The license.
     */
    private String license;
    /**
     * The time of sequencing start.
     */
    private String timeStart;
    /**
     * The time of sequencing end.
     */
    private String timeEnd;
    /**
     * The tagging time.
     */
    private Double taggingTimeSeconds;
    /**
     * The number of processing nodes.
     */
    private Integer nProcessingNode;
    /**
     * The file used as input.
     */
    private String inputFile;
    /**
     * The tags parameters in a map.
     */
    private HashMap<String, String> tagsParameters = new HashMap<String, String>();
    /**
     * Returns the content of the columns for a spectrum line. Name -> index in
     * the column.
     */
    private HashMap<String, Integer> spectrumLineContent = new HashMap<String, Integer>();
    /**
     * Returns the content of the columns for a tag line. Name -> index in the
     * column.
     */
    private HashMap<String, Integer> tagLineContent = new HashMap<String, Integer>();
    /**
     * The indexes at which are the spectra. Spectrum ID -> index.
     */
    private HashMap<Integer, Long> spectrumIndexes = new HashMap<Integer, Long>();
    /**
     * The indexes at which are the tags. Spectrum ID -> indexes.
     */
    private HashMap<Integer, ArrayList<Long>> tagIndexes = new HashMap<Integer, ArrayList<Long>>();
    /**
     * The random access file used.
     */
    private BufferedRandomAccessFile bufferedRandomAccessFile;
    /**
     * The file inspected.
     */
    private File tagFile;
    /**
     * The spectrum factory used to retrieve spectrum titles.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public DirecTagIdfileReader() {

    }

    /**
     * Constructors, parses a file but does not index the results.
     *
     * @param tagFile the file to parse
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public DirecTagIdfileReader(File tagFile) throws FileNotFoundException, IOException {
        this(tagFile, false);
    }

    /**
     * Constructors, parses a file.
     *
     * @param tagFile the file to parse
     * @param indexResults if true the results section will be indexed
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public DirecTagIdfileReader(File tagFile, boolean indexResults) throws FileNotFoundException, IOException {
        this.tagFile = tagFile;
        bufferedRandomAccessFile = new BufferedRandomAccessFile(tagFile, "r", 1024 * 100);
        parseFile(indexResults);
    }

    /**
     * Returns the name of the different parameters names found.
     *
     * @return the name of the different parameters names found
     */
    public Set<String> getTagsParametersNames() {
        return tagsParameters.keySet();
    }

    /**
     * Returns the tagging parameter corresponding to a given parameter name.
     *
     * @param tagParameterName the name of the parameter of interest
     *
     * @return the parameter of interest
     */
    public String getTagParameter(String tagParameterName) {
        return tagsParameters.get(tagParameterName);
    }

    /**
     * Parses a result file.
     *
     * @param indexResults if true the results section will be indexed
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void parseFile(boolean indexResults) throws FileNotFoundException, IOException {

        try {
            boolean endOfFile = parseParameters();
            if (!endOfFile) {
                endOfFile = parseTagParameters();
            }
            if (!endOfFile) {
                endOfFile = parseHeaders();
            }
            if (!endOfFile && indexResults) {
                parseResults();
            }
        } finally {
            bufferedRandomAccessFile.close();
        }
    }

    /**
     * Parses the parameters section.
     *
     * @return true if the end of the file was reached
     *
     * @throws IOException
     */
    private boolean parseParameters() throws IOException {
        String line;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line == null || line.startsWith("H	TagsParameters")) {
                break;
            } else if (line == null) {
                throw new IOException("Unexpected end of file while parsing the parameters.");
            } else if (line.startsWith("H(S)") || line.startsWith("H(T)") || line.startsWith("S") || line.startsWith("T")) {
                throw new IOException("Unexpected end of parameters section.");
            } else {
                line = line.substring(1).trim();
                if (line.startsWith("TagsGeneratorVersion")) {
                    tagsGeneratorVersion = line.substring(line.indexOf("\t")).trim();
                } else if (line.startsWith("TagsGenerator")) {
                    tagsGenerator = line.substring(line.indexOf("\t")).trim();
                } else if (line.contains("(c)")) {
                    copyRight = line;
                } else if (line.contains("License")) {
                    license = line;
                } else if (line.startsWith("Tagging started at")) {
                    tagsGeneratorVersion = line.substring(line.indexOf("Tagging started at")).trim();
                } else if (line.startsWith("Tagging started at")) {
                    timeStart = line.substring(line.indexOf("Tagging started at")).trim();
                } else if (line.startsWith("Tagging finished at")) {
                    timeEnd = line.substring(line.indexOf("Tagging finished at")).trim();
                } else if (line.startsWith("Total tagging time:")) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    line = line.substring(0, line.indexOf(" ")).trim();
                    try {
                        taggingTimeSeconds = new Double(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (line.contains("node")) {
                    line = line.substring(line.indexOf(" ")).trim();
                    line = line.substring(0, line.indexOf(" ")).trim();
                    try {
                        nProcessingNode = new Integer(line);
                    } catch (Exception e) {
                        // ignore
                    }
                } else if (line.startsWith("InputFile")) {
                    inputFile = line.substring(line.indexOf("\t")).trim();
                }
            }
        }
        return line == null;
    }

    /**
     * Parses the tag parameters.
     *
     * @return true if the end of the file was reached
     *
     * @throws IOException
     */
    private boolean parseTagParameters() throws IOException {
        String line;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line.trim().isEmpty()) {
                break;
            } else if (line == null) {
                throw new IOException("Unexpected end of file while parsing the tag parameters.");
            } else if (line.startsWith("H(S)") || line.startsWith("H(T)") || line.startsWith("S") || line.startsWith("T")) {
                throw new IOException("Unexpected end of tag parameters section.");
            } else {
                line = line.substring(1).trim();
                String[] components = line.split(", ");
                for (String component : components) {
                    int index = component.indexOf(": ");
                    String key = component.substring(0, index).trim();
                    String value = component.substring(index + 1).trim();
                    tagsParameters.put(key, value);
                }
            }
        }
        return line == null;
    }

    /**
     * Parses the tables headers.
     *
     * @return true if the end of the file was reached
     *
     * @throws IOException
     */
    private boolean parseHeaders() throws IOException {
        String line = bufferedRandomAccessFile.readLine();
        if (line != null) {
            parseHeaderLine(line);
        }
        line = bufferedRandomAccessFile.readLine();
        if (line != null) {
            parseHeaderLine(line);
        }
        return line == null;
    }

    /**
     * Parses a line corresponding to a header.
     *
     * @param linea line corresponding to a header
     *
     * @throws IOException
     */
    private void parseHeaderLine(String line) throws IOException {
        if (line.startsWith("S") || line.startsWith("T")) {
            throw new IOException("No Header found.");
        }
        if (line.startsWith("H(S)")) {
            line = line.substring(4).trim();
            String[] components = line.split("\t");
            for (int i = 0; i < components.length; i++) {
                spectrumLineContent.put(components[i], i);
            }
        } else if (line.startsWith("H(T)")) {
            line = line.substring(4).trim();
            String[] components = line.split("\t");
            for (int i = 0; i < components.length; i++) {
                tagLineContent.put(components[i], i);
            }
        }
    }

    /**
     * Parses the results section.
     *
     * @throws IOException
     */
    private void parseResults() throws IOException {
        String line;
        Integer sIdIndex = spectrumLineContent.get("Index");
        int scpt = 0;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            long lineIndex = bufferedRandomAccessFile.getFilePointer();
            Integer id = ++scpt;
            if (line.startsWith("S")) {
                line = line.substring(1).trim();
                if (sIdIndex != null) {
                    String[] components = line.split("\t");
                    id = new Integer(components[sIdIndex]);
                }
                spectrumIndexes.put(id, lineIndex);
            } else if (line.startsWith("T")) {
                ArrayList<Long> indexes = tagIndexes.get(id);
                if (indexes == null) {
                    indexes = new ArrayList<Long>();
                    tagIndexes.put(id, indexes);
                }
                indexes.add(lineIndex);
            }
        }
    }

    /**
     * Returns a component in a spectrum line.
     *
     * @param spectrumId the id of the spectrum of interest
     * @param componentName the name of the component of interest according to
     * the header
     *
     * @return the component
     *
     * @throws IOException
     */
    public String getSpectrumComponent(int spectrumId, String componentName) throws IOException {
        long index = spectrumIndexes.get(spectrumId);
        bufferedRandomAccessFile.seek(index);
        String line = bufferedRandomAccessFile.readLine();
        line = line.substring(1).trim();
        String[] components = line.split("\t");
        Integer columnIndex = spectrumLineContent.get(componentName);
        if (columnIndex != null && columnIndex < components.length) {
            return components[columnIndex];
        }
        return null;
    }

    /**
     * Returns all the spectrum IDs found.
     *
     * @return the spectrum IDs found in a set
     */
    public Set<Integer> getSpectrumIds() {
        return spectrumIndexes.keySet();
    }

    /**
     * Returns all the spectrum components names found in the header.
     *
     * @return all the spectrum components names found in the header
     */
    public Set<String> getSpectrumComponentNames() {
        return spectrumLineContent.keySet();
    }

    /**
     * Returns the tag components associated to a spectrum in a map: component
     * name -> value.
     *
     * @param spectrumId the id of the spectrum
     *
     * @return the tag components associated to a spectrum in a map
     *
     * @throws IOException
     */
    private ArrayList<HashMap<String, String>> getTags(int spectrumId) throws IOException {
        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        ArrayList<Long> indexes = tagIndexes.get(spectrumId);
        if (indexes != null) {
            for (Long index : indexes) {
                bufferedRandomAccessFile.seek(index);
                String line = bufferedRandomAccessFile.readLine();
                line = line.substring(1).trim();
                String[] components = line.split("\t");
                HashMap<String, String> lineMap = new HashMap<String, String>();
                for (String componentName : tagLineContent.keySet()) {
                    int columnIndex = tagLineContent.get(componentName);
                    String value = components[columnIndex];
                    lineMap.put(componentName, value);
                }
                result.add(lineMap);
            }
        }
        return result;
    }

    @Override
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {
        String fileName = getInputFile().getName();
        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressCounter(SpectrumFactory.getInstance().getNSpectra(fileName));
            waitingHandler.setSecondaryProgressCounter(0);
        }
        HashSet<SpectrumMatch> result = new HashSet<SpectrumMatch>();
        int sCpt = 0;
        Integer sIdColumnIndex = spectrumLineContent.get("ID"),
                chargeColumnIndex = spectrumLineContent.get("Charge");
        BufferedReader reader = new BufferedReader(new FileReader(tagFile));
        try {
            String line;
            Integer sId = null, lastId = null, lastCharge = null;
            int rank = 0;
            SpectrumMatch currentMatch = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("S")) {
                    sId = ++sCpt;
                    rank = 0;
                    if (sIdColumnIndex != null) {
                        line = line.substring(1).trim();
                        String[] components = line.split("\t");
                        String id = components[sIdColumnIndex];
                        sId = new Integer(id.substring(id.indexOf("=") + 1));
                        String chargeString = components[chargeColumnIndex];
                        lastCharge = new Integer(chargeString);
                    }
                    if (!sId.equals(lastId)) {
                        if (currentMatch != null && currentMatch.hasAssumption()) {
                            result.add(currentMatch);
                        }
                        String spectrumTitle = spectrumFactory.getSpectrumTitle(fileName, sId);
                        currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(fileName, spectrumTitle));
                        lastId = sId;
                    }
                } else if (line.startsWith("T")) {
                    ++rank;
                    TagAssumption tagAssumption = getAssumptionFromLine(line, rank);
                    //@TODO: check with the developers if this is correct
                    tagAssumption.setIdentificationCharge(new Charge(Charge.PLUS, lastCharge));
                    currentMatch.addHit(Advocate.DirecTag.getIndex(), tagAssumption, true);
                }
            }
            if (currentMatch != null && currentMatch.hasAssumption()) {
                result.add(currentMatch);
            }
        } finally {
            reader.close();
        }
        return result;
    }

    /**
     * Returns the assumption associated to a tag line. If a modification index
     * is found, an "X" is put in the tag sequence and a modification match
     * named after the given index is added.
     *
     * @param line the line
     * @param rank the rank of the assumption
     *
     * @return the assumption associated to a tag line
     */
    private TagAssumption getAssumptionFromLine(String line, int rank) {
        line = line.substring(1).trim();
        String[] components = line.split("\t");
        Integer cGapIndex = tagLineContent.get("cTerminusMass");
        if (cGapIndex == null) {
            throw new IllegalArgumentException("Column cTerminusMass not found.");
        }
        Double cGap = new Double(components[cGapIndex]);
        Integer nGapIndex = tagLineContent.get("nTerminusMass");
        if (nGapIndex == null) {
            throw new IllegalArgumentException("Column nTerminusMass not found.");
        }
        Double nGap = new Double(components[nGapIndex]);
        Integer tagIndex = tagLineContent.get("Tag");
        if (tagIndex == null) {
            throw new IllegalArgumentException("Column Tag not found.");
        }
        String tagSequence = components[tagIndex];
        StringBuilder residues = new StringBuilder(tagSequence.length());
        HashMap<Integer, ModificationMatch> modificationMatches = new HashMap<Integer, ModificationMatch>();
        for (int i = 0; i < tagSequence.length(); i++) {
            char charAtI = tagSequence.charAt(i);
            try {
                AminoAcid aa = AminoAcid.getAminoAcid(charAtI);
                residues.append(aa.singleLetterCode);
            } catch (IllegalArgumentException e) {
                try {
                    String modIndexString = charAtI + "";
                    new Integer(modIndexString);
                    residues.append("X");
                    modificationMatches.put(i + 1, new ModificationMatch(modIndexString, true, i + 1));
                } catch (Exception e1) {
                    throw new IllegalArgumentException("No amino acid or modification could be mapped to tag component \"" + charAtI + "\" in tag \"" + tagSequence + "\".");
                }
            }
        }
        AminoAcidPattern tagPattern = new AminoAcidPattern(residues.toString());
        for (int i : modificationMatches.keySet()) {
            tagPattern.addModificationMatch(i, modificationMatches.get(i));
        }
        Tag tag = new Tag(nGap, tagPattern, cGap);
        Integer chargeIndex = tagLineContent.get("TagChargeState");
        if (chargeIndex == null) {
            throw new IllegalArgumentException("Column TagChargeState not found.");
        }
        int charge = new Integer(components[chargeIndex]);
        Integer eValueIndex = tagLineContent.get("Total");
        if (eValueIndex == null) {
            throw new IllegalArgumentException("Column Total not found.");
        }
        double eValue = new Double(components[eValueIndex]);
        return new TagAssumption(Advocate.DirecTag.getIndex(), rank, tag, new Charge(Charge.PLUS, charge), eValue);
    }

    /**
     * Returns the tags generator used to create the file.
     *
     * @return the tags generator used to create the file
     */
    public String getTagsGenerator() {
        return tagsGenerator;
    }

    /**
     * Returns the version of the tags generator used to create the file.
     *
     * @return the version of the tags generator used to create the file
     */
    public String getTagsGeneratorVersion() {
        return tagsGeneratorVersion;
    }

    /**
     * Returns the copyright.
     *
     * @return the copyright
     */
    public String getCopyRight() {
        return copyRight;
    }

    /**
     * Returns the license information of this file.
     *
     * @return the license information of this file
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns the starting time of the tagging as given in the file.
     *
     * @return the starting time of the tagging
     */
    public String getTimeStart() {
        return timeStart;
    }

    /**
     * Returns the ending time of the tagging as given in the file.
     *
     * @return the ending time of the tagging
     */
    public String getTimeEnd() {
        return timeEnd;
    }

    /**
     * Returns the tagging time in seconds as listed in the file.
     *
     * @return the tagging time in seconds as listed in the file
     */
    public Double getTaggingTimeSeconds() {
        return taggingTimeSeconds;
    }

    /**
     * Returns the number of processing nodes used.
     *
     * @return the number of processing nodes used
     */
    public Integer getnProcessingNode() {
        return nProcessingNode;
    }

    /**
     * Returns the spectrum file name as found in the parameters section.
     *
     * @return the spectrum file name
     */
    public File getInputFile() {
        return new File(inputFile);
    }

    @Override
    public String getExtension() {
        return ".tags";
    }

    @Override
    public void close() throws IOException {
        bufferedRandomAccessFile.close();
    }

    @Override
    public String getSoftwareVersion() {
        return tagsGeneratorVersion;
    }
}
