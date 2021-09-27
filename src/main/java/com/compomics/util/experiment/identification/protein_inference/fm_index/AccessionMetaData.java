package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.TempByteArray;
import com.compomics.util.io.compression.ZstdUtils;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Accession meta data.
 *
 * @author Dominik Kopczynski
 */
public class AccessionMetaData implements Serializable {

    private static final long serialVersionUID = 8320902054239025L;
    /**
     * The header as compressed string.
     */
    private byte[] headerAsCompressedString;
    /**
     * The uncompressed length of the header.
     */
    private int uncompressedLength;
    /**
     * The index.
     */
    int index;
    /**
     * The index part.
     */
    int indexPart;
    /**
     * Beginning of protein sequence in the proteome.
     */
    int trueBeginning;

    /**
     * Empty default constructor.
     */
    public AccessionMetaData() {
    }

    /**
     * Constructor.
     *
     * @param header the header as parsed from the FASTA file
     */
    public AccessionMetaData(String header) {
        setHeaderAsString(header);
    }

    /**
     * Returns the header string representation.
     *
     * @return the parsed header
     */
    public String getHeaderAsString() {

        byte[] decompressedHeader = ZstdUtils.zstdDecompress(headerAsCompressedString, uncompressedLength);

        return new String(decompressedHeader);

    }

    /**
     * Set the header as string.
     * 
     * @param header the header
     */
    public void setHeaderAsString(
            String header
    ) {

        byte[] headerBytes = header.getBytes();
        this.uncompressedLength = headerBytes.length;

        TempByteArray tempByteArray = ZstdUtils.zstdCompress(headerBytes);
        headerAsCompressedString = Arrays.copyOf(tempByteArray.array, tempByteArray.length);

    }
}
