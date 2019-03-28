package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.io.biology.protein.Header;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Accession meta data.
 *
 * @author Dominik Kopczynski
 */
public class AccessionMetaData {

    /**
     * The header as string.
     */
    private String headerAsString;
    /**
     * The header
     */
    private Header header = null;
    /**
     * The index.
     */
    int index;
    /**
     * The index part.
     */
    int indexPart;

    /**
     * Empty default constructor.
     */
    public AccessionMetaData() {
    }
    
    public AccessionMetaData(DataInputStream is) throws IOException {
        headerAsString = is.readUTF();
        index = is.readInt();
        indexPart = is.readInt();
    }
    
    /**
     * Copy constructor
     * 
     * @param accessionMetaData the item to copy
     */
    public AccessionMetaData(AccessionMetaData accessionMetaData){
        headerAsString = new String(accessionMetaData.getHeaderAsString());
        index = accessionMetaData.index;
        indexPart = accessionMetaData.indexPart;
        header = null;
    }

    /**
     * Constructor.
     *
     * @param header the header as parsed from the FASTA file
     */
    public AccessionMetaData(String header) {
        this.headerAsString = header;
    }

    /**
     * Constructor.
     *
     * @param header the header as parsed from the FASTA file
     * @param index the index
     * @param indexPart the index part
     */
    public AccessionMetaData(String header, int index, int indexPart) {
        this.headerAsString = header;
        this.index = index;
        this.indexPart = indexPart;
    }

    /**
     * Returns the parsed header.
     *
     * @return the parsed header
     */
    public Header getHeader() {

        if (header == null) {
            header = Header.parseFromFASTA(headerAsString);

        }

        return header;
    }

    /**
     * Returns the header string representation.
     *
     * @return the parsed header
     */
    public String getHeaderAsString() {
        return headerAsString;
    }
    
    public void write(DataOutputStream os) throws IOException {
        os.writeUTF(headerAsString);
        os.writeInt(index);
        os.writeInt(indexPart);
    }
}
