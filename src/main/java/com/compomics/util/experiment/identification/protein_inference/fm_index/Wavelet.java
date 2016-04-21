/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.identification.protein_inference.fm_index.Rank;

/**
 *
 * @author dominik.kopczynski
 */
public class Wavelet {
    Rank rkg;
    long[] alphabet;
    int len_alphabet;
    int half;
    Wavelet left_child;
    Wavelet right_child;
    private final int shift = 6;
    private final int mask = 63;
        
    Wavelet(byte[] text, long[] _alphabet){
        long[] alphabet_left = new long[2];
        long[] alphabet_right = new long[2];
        alphabet = new long[2];

        alphabet_right[0] = alphabet[0] = _alphabet[0];
        alphabet_right[1] = alphabet[1] = _alphabet[1];
        alphabet_left[0] = alphabet_left[1] = 0;
        rkg = new Rank(text, alphabet);
        left_child = null;
        right_child = null;

        len_alphabet = rkg.popcount(alphabet[0]) + rkg.popcount(alphabet[1]);
        half = (len_alphabet - 1) >> 1;
        int cnt = 0;
        for (int i = 0; i < 128 && cnt <= half; ++i){
            int cell = i >> 6;
            int pos = i & 63;
            long bit = (int)((alphabet[cell] >> pos) & 1L);
            cnt += bit;
            alphabet_right[cell] &= ~(1L << pos);
            alphabet_left[cell] |= bit << pos;
        }



        int len_alphabet_left = rkg.popcount(alphabet_left[0]) + rkg.popcount(alphabet_left[1]);
        int len_alphabet_right = rkg.popcount(alphabet_right[0]) + rkg.popcount(alphabet_right[1]);

        if (len_alphabet_left > 1){
            int len_text_left = 0;
            for (int i = 0; i < text.length; ++i){
                int cell = text[i] >> 6L;
                int pos = text[i] & 63;
                len_text_left += (int)((alphabet_left[cell] >> pos) & 1L);
            }
            byte[] text_left = new byte[len_text_left + 1];
            text_left[len_text_left] = 0;
            int j = 0;
            for (int i = 0; i < text.length; ++i){
                int cell = text[i] >> 6L;
                int pos = text[i] & 63;
                long bit = (alphabet_left[cell] >> pos) & 1L;
                if (bit > 0){
                    text_left[j] = text[i];
                    ++j;
                }
            }
            left_child = new Wavelet(text_left, alphabet_left);
        }

        if (len_alphabet_right > 1){
            int len_text_right = 0;
            for (int i = 0; i < text.length; ++i){
                int cell = text[i] >> 6;
                int pos = text[i] & 63;
                len_text_right += (int)((alphabet_right[cell] >> pos) & 1L);
            }
            byte[] text_right = new byte[len_text_right + 1];
            text_right[len_text_right] = 0;
            int j = 0;
            for (int i = 0; i < text.length; ++i){
                int cell = text[i] >> 6;
                int pos = text[i] & 63;
                long bit = (alphabet_right[cell] >> pos) & 1L;
                if (bit > 0){
                    text_right[j] = text[i];
                    ++j;
                }
            }
            right_child = new Wavelet(text_right, alphabet_right);
        }
    }
    
    public int[] createLessTable(){
        int[] less = new int[128];
        int cumulative = 0;
        for (int i = 0; i < 128; ++i){
            less[i] = cumulative;
            if (((alphabet[i >> 6] >> (i & 63)) & 1) != 0){
                cumulative += getRank(rkg.length - 1, i);
            }
        }
        return less;
    }
    
    public int getRank(int i, int c){
        int cell = c >> shift;
        int pos = c & mask;
        int masked = mask - pos;
        long active_ones = alphabet[cell] << masked;
        int p = rkg.popcount(active_ones);
        p += cell * rkg.popcount(alphabet[0]);
        p -= 1;
        boolean left = (p <= half);
        int result = rkg.getRank(i, left);
        if (result == 0) return result;


        if (left && left_child != null){
            return left_child.getRank(result - 1, c);
        }
        else if (!left && right_child != null){
            return right_child.getRank(result - 1, c);
        }
        return result;
    }
}
