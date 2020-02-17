package com.compomics.util.experiment.io.mass_spectrometry.cms;

/**
 * Convenience class for storing an array buffer and a length.
 *
 * @author Marc Vaudel
 */
public class TempByteArray {

        public final byte[] array;
        public final int length;

        public TempByteArray(
                byte[] array,
                int length
        ) {

            this.array = array;
            this.length = length;

        }

}
