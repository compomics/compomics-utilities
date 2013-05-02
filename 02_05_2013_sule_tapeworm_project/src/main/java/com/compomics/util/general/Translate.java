/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-jan-03
 * Time: 18:42:27
 */
package com.compomics.util.general;
import org.apache.log4j.Logger;

import com.compomics.util.nucleotide.NucleotideSequenceImpl;
import com.compomics.util.protein.AASequenceImpl;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class allows the user to translate a DNA sequence into 6 reading frames.
 *
 * @author Lennart Martens
 */
public class Translate {

    // Class specific log4j logger for Translate instances.
    static Logger logger = Logger.getLogger(Translate.class);

    /**
     * Translate a DNA sequence into 6 reading frames.
     *
     * @param args the DNA sequence to translate
     */
    public static void main(String[] args) {
        if(args == null || args.length == 0) {
            logger.error("\n\nUsage:\n\tTranslate <DNA_sequence>");
            System.exit(1);
        }
        // Create a NucleotideSequenceImpl.
        NucleotideSequenceImpl nsi = new NucleotideSequenceImpl(args[0]);
        AASequenceImpl[] seqs = nsi.translate();
        for(int i = 0; i < seqs.length; i++) {
            AASequenceImpl lSeq = seqs[i];
            logger.info(lSeq.getSequence());
        }
    }
}
