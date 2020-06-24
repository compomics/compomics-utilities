package com.compomics.util.experiment.io.mass_spectrometry.mzml;

/**
 * Decodes ints from the half bytes in bytes. Lossless reverse of encodeInt.
 *
 * @author fgonzalez
 * @author jteleman
 */
public class IntDecoder {

    /**
     * The position.
     */
    int pos = 0;
    /**
     * Half.
     */
    boolean half = false;
    /**
     * The bytes.
     */
    byte[] bytes;

    /**
     * Constructor.
     * 
     * @param bytes the bytes
     * @param pos the position
     */
    public IntDecoder(byte[] bytes, int pos) {
        this.bytes = bytes;
        this.pos = pos;
    }

    /**
     * Returns the next value.
     * 
     * @return the next value
     */
    public long next() {
        
        int head;
        int i, n;
        long res = 0;
        long mask, m;
        int hb;

        if (!half) {
            head = (0xff & bytes[pos]) >> 4;
        } else {
            head = 0xf & bytes[pos++];
        }

        half = !half;

        if (head <= 8) {
            n = head;
        } else {
            // leading ones, fill in res
            n = head - 8;
            mask = 0xf0000000;
            for (i = 0; i < n; i++) {
                m = mask >> (4 * i);
                res = res | m;
            }
        }

        if (n == 8) {
            return 0;
        }

        for (i = n; i < 8; i++) {
            if (!half) {
                hb = (0xff & bytes[pos]) >> 4;
            } else {
                hb = 0xf & bytes[pos++];
            }

            res = res | (hb << ((i - n) * 4));
            half = !half;
        }

        return res;
    }
}
