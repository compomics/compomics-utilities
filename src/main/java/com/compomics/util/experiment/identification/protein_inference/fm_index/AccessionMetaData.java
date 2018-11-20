/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.io.biology.protein.Header;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 *
 * @author dominik.kopczynski
 */
public class AccessionMetaData extends ExperimentObject {/**
    * The header as string
    */
   String headerAsString;
   /**
    * The header
    */
   private Header header = null;
   /**
    * The index
    */
   int index;
   /**
    * the index part
    */
   int indexPart;

    /**
     * Empty default constructor
     */
    public AccessionMetaData() {}

   /**
    * Constructor.
    *
    * @param header the header as parsed from the fasta file
    */
   public AccessionMetaData(String header) {
       this.headerAsString = header;
   }

   /**
    * Constructor.
    *
    * @param header the header as parsed from the fasta file
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
    
}
