package com.compomics.util.experiment.identification.protein_inference.fm_index;

/**
 * Suffix array sorter.
 * 
 * @author Dominik Kopczynski
 */
public class SuffixArraySorter {

    /**
     *
     */
    private final static class StackElement {

        final int a, b, c, e;
        int d;

        StackElement(int a, int b, int c, int d, int e) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
        }

        StackElement(int a, int b, int c, int d) {
            this(a, b, c, d, 0);
        }
    }

    /*
     * 
     */
    private final static class TRBudget {

        int chance;
        int remain;
        int incval;
        int count;

        private TRBudget(int chance, int incval) {
            this.chance = chance;
            this.remain = incval;
            this.incval = incval;
        }

        /**
         * Making a check.
         *
         * @param size
         * @return
         */
        private int check(int size) {
            if (size <= this.remain) {
                this.remain -= size;
                return 1;
            }
            if (this.chance == 0) {
                this.count += size;
                return 0;
            }
            this.remain += this.incval - size;
            this.chance -= 1;
            return 1;
        }
    }

    /**
     *
     */
    private static final class TRPartitionResult {

        final int a;
        final int b;

        public TRPartitionResult(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    /* constants */
    private final static int DEFAULT_alphabetSize = 256;
    private final static int SS_INSERTIONSORT_THRESHOLD = 8;
    private final static int SS_BLOCKSIZE = 1024;
    private final static int SS_MISORT_STACKSIZE = 16;
    private final static int SS_SMERGE_STACKSIZE = 32;
    private final static int TR_STACKSIZE = 64;
    private final static int TR_INSERTIONSORT_THRESHOLD = 8;

    private final static int[] sqq_table
            = {
                0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57, 59, 61, 64, 65, 67, 69,
                71, 73, 75, 76, 78, 80, 81, 83, 84, 86, 87, 89, 90, 91, 93, 94, 96, 97, 98, 99,
                101, 102, 103, 104, 106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118,
                119, 120, 121, 122, 123, 124, 125, 126, 128, 128, 129, 130, 131, 132, 133, 134,
                135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 144, 145, 146, 147, 148, 149,
                150, 150, 151, 152, 153, 154, 155, 155, 156, 157, 158, 159, 160, 160, 161, 162,
                163, 163, 164, 165, 166, 167, 167, 168, 169, 170, 170, 171, 172, 173, 173, 174,
                175, 176, 176, 177, 178, 178, 179, 180, 181, 181, 182, 183, 183, 184, 185, 185,
                186, 187, 187, 188, 189, 189, 190, 191, 192, 192, 193, 193, 194, 195, 195, 196,
                197, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 204, 205, 206, 206,
                207, 208, 208, 209, 209, 210, 211, 211, 212, 212, 213, 214, 214, 215, 215, 216,
                217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223, 224, 224, 225, 225,
                226, 226, 227, 227, 228, 229, 229, 230, 230, 231, 231, 232, 232, 233, 234, 234,
                235, 235, 236, 236, 237, 237, 238, 238, 239, 240, 240, 241, 241, 242, 242, 243,
                243, 244, 244, 245, 245, 246, 246, 247, 247, 248, 248, 249, 249, 250, 250, 251,
                251, 252, 252, 253, 253, 254, 254, 255
            };

    /**
     * 2-logarithmic table
     */
    private final static int[] lg_table
            = {
                -1, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
                6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
                6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
            };

    /**
     * Building the suffix array.
     *
     * @param T the byte array
     * @param alphabetSize alphabet size
     * @return the suffix array
     */
    public static int[] buildSuffixArray(byte[] T, int alphabetSize) {

        int BUCKET_A_SIZE = alphabetSize;
        int BUCKET_B_SIZE = alphabetSize * alphabetSize;
        int[] SA = new int[T.length];
        int[] bucket_A = new int[BUCKET_A_SIZE];
        int[] bucket_B = new int[BUCKET_B_SIZE];
        /* Suffixsort. */
        int m = sortTypeBstar(bucket_A, bucket_B, T.length, T, SA, alphabetSize);
        constructSuffixArray(bucket_A, bucket_B, T.length, m, T, SA, alphabetSize);
        return SA;
    }

    /**
     * Constructs the suffix array by using the sorted order of type B*
     * suffixes.
     *
     * @param bucket_A
     * @param bucket_B
     * @param n
     * @param m
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void constructSuffixArray(int[] bucket_A, int[] bucket_B, int n, int m, byte[] T, int[] SA, int alphabetSize) {
        int i, j, k; // ptr
        int s, c0, c1, c2;
        if (0 < m) {
            /*
             * Construct the sorted order of type B suffixes by using the sorted order of
             * type B suffixes.
             */
            for (c1 = alphabetSize - 2; 0 <= c1; --c1) {
                /* Scan the suffix array from right to left. */
                for (i = bucket_B[(c1) * alphabetSize + (c1 + 1)], j = bucket_A[c1 + 1] - 1, k = 0, c2 = -1; i <= j; --j) {
                    if (0 < (s = SA[j])) {
                        SA[j] = ~s;
                        c0 = T[--s];
                        if ((0 < s) && (T[s - 1] > c0)) {
                            s = ~s;
                        }
                        if (c0 != c2) {
                            if (0 <= c2) {
                                bucket_B[(c1) * alphabetSize + (c2)] = k;
                            }
                            k = bucket_B[(c1) * alphabetSize + (c2 = c0)];
                        }
                        SA[k--] = s;
                    } else {
                        SA[j] = ~s;
                    }
                }
            }
        }

        /*
         * Construct the suffix array by using the sorted order of type B suffixes.
         */
        k = bucket_A[c2 = T[n - 1]];
        SA[k++] = (T[n - 2] < c2) ? ~(n - 1) : (n - 1);
        /* Scan the suffix array from left to right. */
        for (i = 0, j = n; i < j; ++i) {
            if (0 < (s = SA[i])) {
                // Tools.assertAlways(T[s - 1] >= T[s], "");
                c0 = T[--s];
                if ((s == 0) || (T[s - 1] < c0)) {
                    s = ~s;
                }
                if (c0 != c2) {
                    bucket_A[c2] = k;
                    k = bucket_A[c2 = c0];
                }
                // Tools.assertAlways(i < k, "");
                SA[k++] = s;
            } else {
                // Tools.assertAlways(s < 0, "");
                SA[i] = ~s;
            }
        }
    }

    /**
     * Sort all Bstar type characters.
     *
     * @param bucket_A
     * @param bucket_B
     * @param n
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int sortTypeBstar(int[] bucket_A, int[] bucket_B, int n, byte[] T, int[] SA, int alphabetSize) {
        int PAb, ISAb, buf;

        int i, j, k, t, m, bufsize;
        int c0, c1 = 0;

        /*
         * Count the number of occurrences of the first one or two characters of each type
         * A, B and B suffix. Moreover, store the beginning position of all type B
         * suffixes into the array SA.
         */
        for (i = n - 1, m = n, c0 = T[n - 1]; 0 <= i;) {
            /* type A suffix. */
            do {
                ++bucket_A[c1 = c0];
            } while ((0 <= --i) && ((c0 = T[i]) >= c1));
            if (0 <= i) {
                /* type B suffix. */
                ++bucket_B[(c0) * alphabetSize + (c1)];
                SA[--m] = i;
                /* type B suffix. */
                for (--i, c1 = c0; (0 <= i) && ((c0 = T[i]) <= c1); --i, c1 = c0) {
                    ++bucket_B[(c1) * alphabetSize + (c0)];
                }
            }
        }
        m = n - m;

        // note:
        // A type B* suffix is lexicographically smaller than a type B suffix
        // that
        // begins with the same first two characters.
        // Calculate the index of start/end point of each bucket.
        for (c0 = 0, i = 0, j = 0; c0 < alphabetSize; ++c0) {
            t = i + bucket_A[c0];
            bucket_A[c0] = i + j;
            /* start point */
            i = t + bucket_B[(c0) * alphabetSize + (c0)];
            for (c1 = c0 + 1; c1 < alphabetSize; ++c1) {
                j += bucket_B[(c0) * alphabetSize + (c1)];
                bucket_B[(c0) * alphabetSize + (c1)] = j; // end point
                i += bucket_B[(c1) * alphabetSize + (c0)];
            }
        }

        if (0 < m) {
            // Sort the type B* suffixes by their first two characters.
            PAb = n - m;// SA
            ISAb = m;// SA
            for (i = m - 2; 0 <= i; --i) {
                t = SA[PAb + i];
                c0 = T[t];
                c1 = T[t + 1];
                SA[--bucket_B[(c0) * alphabetSize + (c1)]] = i;
            }
            t = SA[PAb + m - 1];
            c0 = T[t];
            c1 = T[t + 1];
            SA[--bucket_B[(c0) * alphabetSize + (c1)]] = m - 1;

            // Sort the type B* substrings using sssort.
            buf = m;// SA
            bufsize = n - (2 * m);

            for (c0 = alphabetSize - 2, j = m; 0 < j; --c0) {
                for (c1 = alphabetSize - 1; c0 < c1; j = i, --c1) {
                    i = bucket_B[(c0) * alphabetSize + (c1)];
                    if (1 < (j - i)) {
                        ssSort(PAb, i, j, buf, bufsize, 2, n, SA[i] == (m - 1), T, SA, alphabetSize);
                    }
                }
            }

            // Compute ranks of type B* substrings.
            for (i = m - 1; 0 <= i; --i) {
                if (0 <= SA[i]) {
                    j = i;
                    do {
                        SA[ISAb + SA[i]] = i;
                    } while ((0 <= --i) && (0 <= SA[i]));
                    SA[i + 1] = i - j;
                    if (i <= 0) {
                        break;
                    }
                }
                j = i;
                do {
                    SA[ISAb + (SA[i] = ~SA[i])] = j;
                } while (SA[--i] < 0);
                SA[ISAb + SA[i]] = j;
            }
            // Construct the inverse suffix array of type B* suffixes using
            // trsort.
            trSort(ISAb, m, 1, T, SA, alphabetSize);
            // Set the sorted order of type B* suffixes.
            for (i = n - 1, j = m, c0 = T[n - 1]; 0 <= i;) {
                for (--i, c1 = c0; (0 <= i) && ((c0 = T[i]) >= c1); --i, c1 = c0) {
                }
                if (0 <= i) {
                    t = i;
                    for (--i, c1 = c0; (0 <= i) && ((c0 = T[i]) <= c1); --i, c1 = c0) {
                    }
                    SA[SA[ISAb + --j]] = ((t == 0) || (1 < (t - i))) ? t : ~t;
                }
            }

            // Calculate the index of start/end point of each bucket.
            bucket_B[(alphabetSize - 1) * alphabetSize + (alphabetSize - 1)] = n; // end
            // point
            for (c0 = alphabetSize - 2, k = m - 1; 0 <= c0; --c0) {
                i = bucket_A[c0 + 1] - 1;
                for (c1 = alphabetSize - 1; c0 < c1; --c1) {
                    t = i - bucket_B[(c1) * alphabetSize + (c0)];
                    bucket_B[(c1) * alphabetSize + (c0)] = i; // end point

                    // Move all type B* suffixes to the correct position.
                    for (i = t, j = bucket_B[(c0) * alphabetSize + (c1)]; j <= k; --i, --k) {
                        SA[i] = SA[k];
                    }
                }
                bucket_B[(c0) * alphabetSize + (c0 + 1)] = i
                        - bucket_B[(c0) * alphabetSize + (c0)] + 1; //
                bucket_B[(c0) * alphabetSize + (c0)] = i; // end point
            }
        }

        return m;
    }

    /**
     * SS sorting.
     *
     * @param PA
     * @param first
     * @param last
     * @param buf
     * @param bufsize
     * @param depth
     * @param n
     * @param lastsuffix
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssSort(final int PA, int first, int last, int buf, int bufsize,
            int depth, int n, boolean lastsuffix, byte[] T, int[] SA, int alphabetSize) {
        int a, b, middle, curbuf;// SA pointer

        int j, k, curbufsize, limit;

        int i;

        if (lastsuffix) {
            ++first;
        }

        if ((bufsize < SS_BLOCKSIZE) && (bufsize < (last - first))
                && (bufsize < (limit = ssIsqrt(last - first)))) {
            if (SS_BLOCKSIZE < limit) {
                limit = SS_BLOCKSIZE;
            }
            buf = middle = last - limit;
            bufsize = limit;
        } else {
            middle = last;
            limit = 0;
        }
        for (a = first, i = 0; SS_BLOCKSIZE < (middle - a); a += SS_BLOCKSIZE, ++i) {
            ssMintroSort(PA, a, a + SS_BLOCKSIZE, depth, T, SA, alphabetSize);
            curbufsize = last - (a + SS_BLOCKSIZE);
            curbuf = a + SS_BLOCKSIZE;
            if (curbufsize <= bufsize) {
                curbufsize = bufsize;
                curbuf = buf;
            }
            for (b = a, k = SS_BLOCKSIZE, j = i; (j & 1) != 0; b -= k, k <<= 1, j >>= 1) {
                ssSwapMerge(PA, b - k, b, b + k, curbuf, curbufsize, depth, T, SA, alphabetSize);
            }
        }
        ssMintroSort(PA, a, middle, depth, T, SA, alphabetSize);
        for (k = SS_BLOCKSIZE; i != 0; k <<= 1, i >>= 1) {
            if ((i & 1) != 0) {
                ssSwapMerge(PA, a - k, a, middle, buf, bufsize, depth, T, SA, alphabetSize);
                a -= k;
            }
        }
        if (limit != 0) {
            ssMintroSort(PA, middle, last, depth, T, SA, alphabetSize);
            ssInplaceMerge(PA, first, middle, last, depth, T, SA, alphabetSize);
        }

        if (lastsuffix) {
            int p1 = SA[PA + SA[first - 1]];
            int p11 = n - 2;
            for (a = first, i = SA[first - 1]; (a < last)
                    && ((SA[a] < 0) || (0 < ssCompare(p1, p11, PA + SA[a], depth, T, SA, alphabetSize))); ++a) {
                SA[a - 1] = SA[a];
            }
            SA[a - 1] = i;
        }

    }

    /**
     * special version of ss_compare for handling ss_compare(T, &(PAi[0]), PA +
     * *a, depth) situation.
     *
     * @param pa
     * @param pb
     * @param p2
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int ssCompare(int pa, int pb, int p2, int depth, byte[] T, int[] SA, int alphabetSize) {
        int U1, U2, U1n, U2n;// pointers to T

        for (U1 = depth + pa, U2 = depth + SA[p2], U1n = pb + 2, U2n = SA[p2 + 1] + 2; (U1 < U1n)
                && (U2 < U2n) && (T[U1] == T[U2]); ++U1, ++U2) {
        }

        return U1 < U1n ? (U2 < U2n ? T[U1] - T[U2] : 1) : (U2 < U2n ? -1
                : 0);
    }

    /**
     * SS compare.
     *
     * @param p1
     * @param p2
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int ssCompare(int p1, int p2, int depth, byte[] T, int[] SA, int alphabetSize) {
        int U1, U2, U1n, U2n;// pointers to T

        for (U1 = depth + SA[p1], U2 = depth + SA[p2], U1n = SA[p1 + 1] + 2, U2n = SA[p2 + 1] + 2; (U1 < U1n)
                && (U2 < U2n) && (T[U1] == T[U2]); ++U1, ++U2) {
        }

        return U1 < U1n ? (U2 < U2n ? T[U1] - T[U2] : 1) : (U2 < U2n ? -1
                : 0);

    }

    /**
     * ss inplece merger.
     *
     * @param PA
     * @param first
     * @param middle
     * @param last
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssInplaceMerge(int PA, int first, int middle, int last, int depth, byte[] T, int[] SA, int alphabetSize) {
        // PA, middle, first, last are pointers to SA
        int p, a, b;// pointer to SA
        int len, half;
        int q, r;
        int x;

        for (;;) {
            if (SA[last - 1] < 0) {
                x = 1;
                p = PA + ~SA[last - 1];
            } else {
                x = 0;
                p = PA + SA[last - 1];
            }
            for (a = first, len = middle - first, half = len >> 1, r = -1; 0 < len; len = half, half >>= 1) {
                b = a + half;
                q = ssCompare(PA + ((0 <= SA[b]) ? SA[b] : ~SA[b]), p, depth, T, SA, alphabetSize);
                if (q < 0) {
                    a = b + 1;
                    half -= (len & 1) ^ 1;
                } else {
                    r = q;
                }
            }
            if (a < middle) {
                if (r == 0) {
                    SA[a] = ~SA[a];
                }
                ssRotate(a, middle, last, T, SA, alphabetSize);
                last -= middle - a;
                middle = a;
                if (first == middle) {
                    break;
                }
            }
            --last;
            if (x != 0) {
                while (SA[--last] < 0) {
                    // nop
                }
            }
            if (middle == last) {
                break;
            }
        }

    }

    /**
     * SS rotating.
     *
     * @param first
     * @param middle
     * @param last
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssRotate(int first, int middle, int last, byte[] T, int[] SA, int alphabetSize) {
        // first, middle, last are pointers in SA
        int a, b, t;// pointers in SA
        int l, r;
        l = middle - first;
        r = last - middle;
        for (; (0 < l) && (0 < r);) {
            if (l == r) {
                ssBlockSwap(first, middle, l, T, SA, alphabetSize);
                break;
            }
            if (l < r) {
                a = last - 1;
                b = middle - 1;
                t = SA[a];
                do {
                    SA[a--] = SA[b];
                    SA[b--] = SA[a];
                    if (b < first) {
                        SA[a] = t;
                        last = a;
                        if ((r -= l + 1) <= l) {
                            break;
                        }
                        a -= 1;
                        b = middle - 1;
                        t = SA[a];
                    }
                } while (true);
            } else {
                a = first;
                b = middle;
                t = SA[a];
                do {
                    SA[a++] = SA[b];
                    SA[b++] = SA[a];
                    if (last <= b) {
                        SA[a] = t;
                        first = a + 1;
                        if ((l -= r + 1) <= r) {
                            break;
                        }
                        a += 1;
                        b = middle;
                        t = SA[a];
                    }
                } while (true);
            }
        }
    }

    /**
     * SS block swapping.
     *
     * @param a
     * @param b
     * @param n
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssBlockSwap(int a, int b, int n, byte[] T, int[] SA, int alphabetSize) {
        // a, b -- pointer to SA
        int t;
        for (; 0 < n; --n, ++a, ++b) {
            t = SA[a];
            SA[a] = SA[b];
            SA[b] = t;
        }
    }

    /**
     * Computes complement bitfield if necessary
     *
     * @param a
     * @return
     */
    private final static int getIDX(int a) {
        return (0 <= (a)) ? (a) : (~(a));
    }

    /**
     * Computes minimum of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    private final static int min(int a, int b) {
        return a < b ? a : b;
    }

    /**
     * D&C based merge.
     *
     * @param PA
     * @param first
     * @param middle
     * @param last
     * @param buf
     * @param bufsize
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssSwapMerge(int PA, int first, int middle, int last, int buf,
            int bufsize, int depth, byte[] T, int[] SA, int alphabetSize) {
        // Pa, first, middle, last and buf - pointers in SA array

        final int STACK_SIZE = SS_SMERGE_STACKSIZE;
        StackElement[] stack = new StackElement[STACK_SIZE];
        int l, r, lm, rm;// pointers in SA
        int m, len, half;
        int ssize;
        int check, next;

        for (check = 0, ssize = 0;;) {

            if ((last - middle) <= bufsize) {
                if ((first < middle) && (middle < last)) {
                    ssMergeBackward(PA, first, middle, last, buf, depth, T, SA, alphabetSize);
                }
                if (((check & 1) != 0)
                        || (((check & 2) != 0) && (ssCompare(PA + getIDX(SA[first - 1]), PA
                                + SA[first], depth, T, SA, alphabetSize) == 0))) {
                    SA[first] = ~SA[first];
                }
                if (((check & 4) != 0)
                        && ((ssCompare(PA + getIDX(SA[last - 1]), PA + SA[last], depth, T, SA, alphabetSize) == 0))) {
                    SA[last] = ~SA[last];
                }

                if (ssize > 0) {
                    StackElement se = stack[--ssize];
                    first = se.a;
                    middle = se.b;
                    last = se.c;
                    check = se.d;
                } else {
                    return;
                }
                continue;
            }

            if ((middle - first) <= bufsize) {
                if (first < middle) {
                    ssMergeForward(PA, first, middle, last, buf, depth, T, SA, alphabetSize);
                }
                if (((check & 1) != 0)
                        || (((check & 2) != 0) && (ssCompare(PA + getIDX(SA[first - 1]), PA
                                + SA[first], depth, T, SA, alphabetSize) == 0))) {
                    SA[first] = ~SA[first];
                }
                if (((check & 4) != 0)
                        && ((ssCompare(PA + getIDX(SA[last - 1]), PA + SA[last], depth, T, SA, alphabetSize) == 0))) {
                    SA[last] = ~SA[last];
                }

                if (ssize > 0) {
                    StackElement se = stack[--ssize];
                    first = se.a;
                    middle = se.b;
                    last = se.c;
                    check = se.d;
                } else {
                    return;
                }

                continue;
            }

            for (m = 0, len = min(middle - first, last - middle), half = len >> 1; 0 < len; len = half, half >>= 1) {
                if (ssCompare(PA + getIDX(SA[middle + m + half]), PA
                        + getIDX(SA[middle - m - half - 1]), depth, T, SA, alphabetSize) < 0) {
                    m += half + 1;
                    half -= (len & 1) ^ 1;
                }
            }

            if (0 < m) {
                lm = middle - m;
                rm = middle + m;
                ssBlockSwap(lm, middle, m, T, SA, alphabetSize);
                l = r = middle;
                next = 0;
                if (rm < last) {
                    if (SA[rm] < 0) {
                        SA[rm] = ~SA[rm];
                        if (first < lm) {
                            for (; SA[--l] < 0;) {
                            }
                            next |= 4;
                        }
                        next |= 1;
                    } else if (first < lm) {
                        for (; SA[r] < 0; ++r) {
                        }
                        next |= 2;
                    }
                }

                if ((l - first) <= (last - r)) {
                    stack[ssize++] = new StackElement(r, rm, last, (next & 3)
                            | (check & 4));

                    middle = lm;
                    last = l;
                    check = (check & 3) | (next & 4);
                } else {
                    if (((next & 2) != 0) && (r == middle)) {
                        next ^= 6;
                    }
                    stack[ssize++] = new StackElement(first, lm, l, (check & 3)
                            | (next & 4));

                    first = r;
                    middle = rm;
                    check = (next & 3) | (check & 4);
                }
            } else {
                if (ssCompare(PA + getIDX(SA[middle - 1]), PA + SA[middle], depth, T, SA, alphabetSize) == 0) {
                    SA[middle] = ~SA[middle];
                }

                if (((check & 1) != 0)
                        || (((check & 2) != 0) && (ssCompare(PA + getIDX(SA[first - 1]), PA
                                + SA[first], depth, T, SA, alphabetSize) == 0))) {
                    SA[first] = ~SA[first];
                }
                if (((check & 4) != 0)
                        && ((ssCompare(PA + getIDX(SA[last - 1]), PA + SA[last], depth, T, SA, alphabetSize) == 0))) {
                    SA[last] = ~SA[last];
                }

                if (ssize > 0) {
                    StackElement se = stack[--ssize];
                    first = se.a;
                    middle = se.b;
                    last = se.c;
                    check = se.d;
                } else {
                    return;
                }

            }

        }

    }

    /**
     * Merge-forward with internal buffer.
     */
    private final static void ssMergeForward(int PA, int first, int middle, int last, int buf,
            int depth, byte[] T, int[] SA, int alphabetSize) {
        // PA, first, middle, last, buf are pointers to SA
        int a, b, c, bufend;// pointers to SA
        int t, r;

        bufend = buf + (middle - first) - 1;
        ssBlockSwap(buf, first, middle - first, T, SA, alphabetSize);

        for (t = SA[a = first], b = buf, c = middle;;) {
            r = ssCompare(PA + SA[b], PA + SA[c], depth, T, SA, alphabetSize);
            if (r < 0) {
                do {
                    SA[a++] = SA[b];
                    if (bufend <= b) {
                        SA[bufend] = t;
                        return;
                    }
                    SA[b++] = SA[a];
                } while (SA[b] < 0);
            } else if (r > 0) {
                do {
                    SA[a++] = SA[c];
                    SA[c++] = SA[a];
                    if (last <= c) {
                        while (b < bufend) {
                            SA[a++] = SA[b];
                            SA[b++] = SA[a];
                        }
                        SA[a] = SA[b];
                        SA[b] = t;
                        return;
                    }
                } while (SA[c] < 0);
            } else {
                SA[c] = ~SA[c];
                do {
                    SA[a++] = SA[b];
                    if (bufend <= b) {
                        SA[bufend] = t;
                        return;
                    }
                    SA[b++] = SA[a];
                } while (SA[b] < 0);

                do {
                    SA[a++] = SA[c];
                    SA[c++] = SA[a];
                    if (last <= c) {
                        while (b < bufend) {
                            SA[a++] = SA[b];
                            SA[b++] = SA[a];
                        }
                        SA[a] = SA[b];
                        SA[b] = t;
                        return;
                    }
                } while (SA[c] < 0);
            }
        }

    }

    /**
     * Merge-backward with internal buffer.
     */
    private final static void ssMergeBackward(int PA, int first, int middle, int last, int buf,
            int depth, byte[] T, int[] SA, int alphabetSize) {
        // PA, first, middle, last, buf are pointers in SA
        int p1, p2;// pointers in SA
        int a, b, c, bufend;// pointers in SA
        int t, r, x;

        bufend = buf + (last - middle) - 1;
        ssBlockSwap(buf, middle, last - middle, T, SA, alphabetSize);

        x = 0;
        if (SA[bufend] < 0) {
            p1 = PA + ~SA[bufend];
            x |= 1;
        } else {
            p1 = PA + SA[bufend];
        }
        if (SA[middle - 1] < 0) {
            p2 = PA + ~SA[middle - 1];
            x |= 2;
        } else {
            p2 = PA + SA[middle - 1];
        }
        for (t = SA[a = last - 1], b = bufend, c = middle - 1;;) {
            r = ssCompare(p1, p2, depth, T, SA, alphabetSize);
            if (0 < r) {
                if ((x & 1) != 0) {
                    do {
                        SA[a--] = SA[b];
                        SA[b--] = SA[a];
                    } while (SA[b] < 0);
                    x ^= 1;
                }
                SA[a--] = SA[b];
                if (b <= buf) {
                    SA[buf] = t;
                    break;
                }
                SA[b--] = SA[a];
                if (SA[b] < 0) {
                    p1 = PA + ~SA[b];
                    x |= 1;
                } else {
                    p1 = PA + SA[b];
                }
            } else if (r < 0) {
                if ((x & 2) != 0) {
                    do {
                        SA[a--] = SA[c];
                        SA[c--] = SA[a];
                    } while (SA[c] < 0);
                    x ^= 2;
                }
                SA[a--] = SA[c];
                SA[c--] = SA[a];
                if (c < first) {
                    while (buf < b) {
                        SA[a--] = SA[b];
                        SA[b--] = SA[a];
                    }
                    SA[a] = SA[b];
                    SA[b] = t;
                    break;
                }
                if (SA[c] < 0) {
                    p2 = PA + ~SA[c];
                    x |= 2;
                } else {
                    p2 = PA + SA[c];
                }
            } else {
                if ((x & 1) != 0) {
                    do {
                        SA[a--] = SA[b];
                        SA[b--] = SA[a];
                    } while (SA[b] < 0);
                    x ^= 1;
                }
                SA[a--] = ~SA[b];
                if (b <= buf) {
                    SA[buf] = t;
                    break;
                }
                SA[b--] = SA[a];
                if ((x & 2) != 0) {
                    do {
                        SA[a--] = SA[c];
                        SA[c--] = SA[a];
                    } while (SA[c] < 0);
                    x ^= 2;
                }
                SA[a--] = SA[c];
                SA[c--] = SA[a];
                if (c < first) {
                    while (buf < b) {
                        SA[a--] = SA[b];
                        SA[b--] = SA[a];
                    }
                    SA[a] = SA[b];
                    SA[b] = t;
                    break;
                }
                if (SA[b] < 0) {
                    p1 = PA + ~SA[b];
                    x |= 1;
                } else {
                    p1 = PA + SA[b];
                }
                if (SA[c] < 0) {
                    p2 = PA + ~SA[c];
                    x |= 2;
                } else {
                    p2 = PA + SA[c];
                }
            }
        }
    }

    /**
     * Insertionsort for small size groups
     */
    private final static void ssInsertionSort(int PA, int first, int last, int depth, byte[] T, int[] SA, int alphabetSize) {
        // PA, first, last are pointers in SA
        int i, j;// pointers in SA
        int t, r;

        for (i = last - 2; first <= i; --i) {
            for (t = SA[i], j = i + 1; 0 < (r = ssCompare(PA + t, PA + SA[j], depth, T, SA, alphabetSize));) {
                do {
                    SA[j - 1] = SA[j];
                } while ((++j < last) && (SA[j] < 0));
                if (last <= j) {
                    break;
                }
            }
            if (r == 0) {
                SA[j] = ~SA[j];
            }
            SA[j - 1] = t;
        }

    }

    /**
     * Computes ssI square root
     *
     * @param x
     * @return
     */
    private final static int ssIsqrt(int x) {
        int y, e;

        if (x >= (SS_BLOCKSIZE * SS_BLOCKSIZE)) {
            return SS_BLOCKSIZE;
        }
        e = ((x & 0xffff0000) != 0) ? (((x & 0xff000000) != 0) ? 24 + lg_table[(x >> 24) & 0xff]
                : 16 + lg_table[(x >> 16) & 0xff])
                : (((x & 0x0000ff00) != 0) ? 8 + lg_table[(x >> 8) & 0xff]
                        : 0 + lg_table[(x >> 0) & 0xff]);

        if (e >= 16) {
            y = sqq_table[x >> ((e - 6) - (e & 1))] << ((e >> 1) - 7);
            if (e >= 24) {
                y = (y + 1 + x / y) >> 1;
            }
            y = (y + 1 + x / y) >> 1;
        } else if (e >= 8) {
            y = (sqq_table[x >> ((e - 6) - (e & 1))] >> (7 - (e >> 1))) + 1;
        } else {
            return sqq_table[x] >> 4;
        }

        return (x < (y * y)) ? y - 1 : y;
    }

    /**
     * Multikey introsort for medium size groups
     *
     * @param PA
     * @param first
     * @param last
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssMintroSort(int PA, int first, int last, int depth, byte[] T, int[] SA, int alphabetSize) {
        final int STACK_SIZE = SS_MISORT_STACKSIZE;
        StackElement[] stack = new StackElement[STACK_SIZE];
        int Td;// T ptr
        int a, b, c, d, e, f;// SA ptr
        int s, t;
        int ssize;
        int limit;
        int v, x = 0;
        for (ssize = 0, limit = ssIlg(last - first);;) {

            if ((last - first) <= SS_INSERTIONSORT_THRESHOLD) {
                if (1 < (last - first)) {
                    ssInsertionSort(PA, first, last, depth, T, SA, alphabetSize);
                }
                if (ssize > 0) {
                    StackElement se = stack[--ssize];
                    first = se.a;
                    last = se.b;
                    depth = se.c;
                    limit = se.d;
                } else {
                    return;
                }

                continue;
            }

            Td = depth;
            if (limit-- == 0) {
                ssHeapSort(Td, PA, first, last - first, T, SA, alphabetSize);

            }
            if (limit < 0) {
                for (a = first + 1, v = T[Td + SA[PA + SA[first]]]; a < last; ++a) {
                    if ((x = T[Td + SA[PA + SA[a]]]) != v) {
                        if (1 < (a - first)) {
                            break;
                        }
                        v = x;
                        first = a;
                    }
                }

                if (T[Td + SA[PA + SA[first]] - 1] < v) {
                    first = ssPartition(PA, first, a, depth, T, SA, alphabetSize);
                }
                if ((a - first) <= (last - a)) {
                    if (1 < (a - first)) {
                        stack[ssize++] = new StackElement(a, last, depth, -1);
                        last = a;
                        depth += 1;
                        limit = ssIlg(a - first);
                    } else {
                        first = a;
                        limit = -1;
                    }
                } else if (1 < (last - a)) {
                    stack[ssize++] = new StackElement(first, a, depth + 1, ssIlg(a
                            - first));
                    first = a;
                    limit = -1;
                } else {
                    last = a;
                    depth += 1;
                    limit = ssIlg(a - first);
                }
                continue;
            }

            // choose pivot
            a = ssPivot(Td, PA, first, last, T, SA, alphabetSize);
            v = T[Td + SA[PA + SA[a]]];
            swapInSA(first, a, T, SA, alphabetSize);

            // partition
            for (b = first; (++b < last) && ((x = T[Td + SA[PA + SA[b]]]) == v);) {
            }
            if (((a = b) < last) && (x < v)) {
                for (; (++b < last) && ((x = T[Td + SA[PA + SA[b]]]) <= v);) {
                    if (x == v) {
                        swapInSA(b, a, T, SA, alphabetSize);
                        ++a;
                    }
                }
            }

            for (c = last; (b < --c) && ((x = T[Td + SA[PA + SA[c]]]) == v);) {
            }
            if ((b < (d = c)) && (x > v)) {
                for (; (b < --c) && ((x = T[Td + SA[PA + SA[c]]]) >= v);) {
                    if (x == v) {
                        swapInSA(c, d, T, SA, alphabetSize);
                        --d;
                    }
                }
            }

            for (; b < c;) {
                swapInSA(b, c, T, SA, alphabetSize);
                for (; (++b < c) && ((x = T[Td + SA[PA + SA[b]]]) <= v);) {
                    if (x == v) {
                        swapInSA(b, a, T, SA, alphabetSize);
                        ++a;
                    }
                }
                for (; (b < --c) && ((x = T[Td + SA[PA + SA[c]]]) >= v);) {
                    if (x == v) {
                        swapInSA(c, d, T, SA, alphabetSize);
                        --d;
                    }
                }
            }

            if (a <= d) {
                c = b - 1;

                if ((s = a - first) > (t = b - a)) {
                    s = t;
                }
                for (e = first, f = b - s; 0 < s; --s, ++e, ++f) {
                    swapInSA(e, f, T, SA, alphabetSize);
                }
                if ((s = d - c) > (t = last - d - 1)) {
                    s = t;
                }
                for (e = b, f = last - s; 0 < s; --s, ++e, ++f) {
                    swapInSA(e, f, T, SA, alphabetSize);
                }

                a = first + (b - a);
                c = last - (d - c);
                b = (v <= T[Td + SA[PA + SA[a]] - 1]) ? a : ssPartition(PA, a, c,
                        depth, T, SA, alphabetSize);

                if ((a - first) <= (last - c)) {
                    if ((last - c) <= (c - b)) {
                        stack[ssize++] = new StackElement(b, c, depth + 1, ssIlg(c - b));
                        stack[ssize++] = new StackElement(c, last, depth, limit);
                        last = a;
                    } else if ((a - first) <= (c - b)) {
                        stack[ssize++] = new StackElement(c, last, depth, limit);
                        stack[ssize++] = new StackElement(b, c, depth + 1, ssIlg(c - b));
                        last = a;
                    } else {
                        stack[ssize++] = new StackElement(c, last, depth, limit);
                        stack[ssize++] = new StackElement(first, a, depth, limit);
                        first = b;
                        last = c;
                        depth += 1;
                        limit = ssIlg(c - b);
                    }
                } else if ((a - first) <= (c - b)) {
                    stack[ssize++] = new StackElement(b, c, depth + 1, ssIlg(c - b));
                    stack[ssize++] = new StackElement(first, a, depth, limit);
                    first = c;
                } else if ((last - c) <= (c - b)) {
                    stack[ssize++] = new StackElement(first, a, depth, limit);
                    stack[ssize++] = new StackElement(b, c, depth + 1, ssIlg(c - b));
                    first = c;
                } else {
                    stack[ssize++] = new StackElement(first, a, depth, limit);
                    stack[ssize++] = new StackElement(c, last, depth, limit);
                    first = b;
                    last = c;
                    depth += 1;
                    limit = ssIlg(c - b);
                }

            } else {
                limit += 1;
                if (T[Td + SA[PA + SA[first]] - 1] < v) {
                    first = ssPartition(PA, first, last, depth, T, SA, alphabetSize);
                    limit = ssIlg(last - first);
                }
                depth += 1;
            }

        }

    }

    /**
     * Returns the pivot element.
     *
     * @param Td
     * @param PA
     * @param first
     * @param last
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int ssPivot(int Td, int PA, int first, int last, byte[] T, int[] SA, int alphabetSize) {
        int middle;// SA pointer
        int t = last - first;
        middle = first + t / 2;

        if (t <= 512) {
            if (t <= 32) {
                return ssMedian3(Td, PA, first, middle, last - 1, T, SA, alphabetSize);
            } else {
                t >>= 2;
                return ssMedian5(Td, PA, first, first + t, middle, last - 1 - t, last - 1, T, SA, alphabetSize);
            }
        }
        t >>= 3;
        first = ssMedian3(Td, PA, first, first + t, first + (t << 1), T, SA, alphabetSize);
        middle = ssMedian3(Td, PA, middle - t, middle, middle + t, T, SA, alphabetSize);
        last = ssMedian3(Td, PA, last - 1 - (t << 1), last - 1 - t, last - 1, T, SA, alphabetSize);
        return ssMedian3(Td, PA, first, middle, last, T, SA, alphabetSize);
    }

    /**
     * Returns the median of five elements
     *
     * @param Td
     * @param PA
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     * @param v5
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int ssMedian5(int Td, int PA, int v1, int v2, int v3, int v4, int v5, byte[] T, int[] SA, int alphabetSize) {
        int t;
        if (T[Td + SA[PA + SA[v2]]] > T[Td + SA[PA + SA[v3]]]) {
            t = v2;
            v2 = v3;
            v3 = t;

        }
        if (T[Td + SA[PA + SA[v4]]] > T[Td + SA[PA + SA[v5]]]) {
            t = v4;
            v4 = v5;
            v5 = t;
        }
        if (T[Td + SA[PA + SA[v2]]] > T[Td + SA[PA + SA[v4]]]) {
            t = v2;
            v2 = v4;
            v4 = t;
            t = v3;
            v3 = v5;
            v5 = t;
        }
        if (T[Td + SA[PA + SA[v1]]] > T[Td + SA[PA + SA[v3]]]) {
            t = v1;
            v1 = v3;
            v3 = t;
        }
        if (T[Td + SA[PA + SA[v1]]] > T[Td + SA[PA + SA[v4]]]) {
            t = v1;
            v1 = v4;
            v4 = t;
            t = v3;
            v3 = v5;
            v5 = t;
        }
        if (T[Td + SA[PA + SA[v3]]] > T[Td + SA[PA + SA[v4]]]) {
            return v4;
        }
        return v3;
    }

    /**
     * Returns the median of three elements.
     *
     * @param Td
     * @param PA
     * @param v1
     * @param v2
     * @param v3
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int ssMedian3(int Td, int PA, int v1, int v2, int v3, byte[] T, int[] SA, int alphabetSize) {
        if (T[Td + SA[PA + SA[v1]]] > T[Td + SA[PA + SA[v2]]]) {
            int t = v1;
            v1 = v2;
            v2 = t;
        }
        if (T[Td + SA[PA + SA[v2]]] > T[Td + SA[PA + SA[v3]]]) {
            if (T[Td + SA[PA + SA[v1]]] > T[Td + SA[PA + SA[v3]]]) {
                return v1;
            } else {
                return v3;
            }
        }
        return v2;
    }

    /**
     * Binary partition for substrings.
     *
     * @param PA
     * @param first
     * @param last
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int ssPartition(int PA, int first, int last, int depth, byte[] T, int[] SA, int alphabetSize) {
        int a, b;// SA pointer
        int t;
        for (a = first - 1, b = last;;) {
            for (; (++a < b) && ((SA[PA + SA[a]] + depth) >= (SA[PA + SA[a] + 1] + 1));) {
                SA[a] = ~SA[a];
            }
            for (; (a < --b) && ((SA[PA + SA[b]] + depth) < (SA[PA + SA[b] + 1] + 1));) {
            }
            if (b <= a) {
                break;
            }
            t = ~SA[b];
            SA[b] = SA[a];
            SA[a] = t;
        }
        if (first < a) {
            SA[first] = ~SA[first];
        }
        return a;
    }

    /**
     * Simple top-down heapsort.
     *
     * @param Td
     * @param PA
     * @param sa
     * @param size
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssHeapSort(int Td, int PA, int sa, int size, byte[] T, int[] SA, int alphabetSize) {
        int i, m, t;

        m = size;
        if ((size % 2) == 0) {
            m--;
            if (T[Td + SA[PA + SA[sa + (m / 2)]]] < T[Td
                    + SA[PA + SA[sa + m]]]) {
                swapInSA(sa + m, sa + (m / 2), T, SA, alphabetSize);
            }
        }

        for (i = m / 2 - 1; 0 <= i; --i) {
            ssFixDown(Td, PA, sa, i, m, T, SA, alphabetSize);
        }
        if ((size % 2) == 0) {
            swapInSA(sa, sa + m, T, SA, alphabetSize);
            ssFixDown(Td, PA, sa, 0, m, T, SA, alphabetSize);
        }
        for (i = m - 1; 0 < i; --i) {
            t = SA[sa];
            SA[sa] = SA[sa + i];
            ssFixDown(Td, PA, sa, 0, i, T, SA, alphabetSize);
            SA[sa + i] = t;
        }

    }

    /**
     * Making a substring fixdown.
     *
     * @param Td
     * @param PA
     * @param sa
     * @param i
     * @param size
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void ssFixDown(int Td, int PA, int sa, int i, int size, byte[] T, int[] SA, int alphabetSize) {
        int j, k;
        int v;
        int c, d, e;

        for (v = SA[sa + i], c = T[Td + SA[PA + v]]; (j = 2 * i + 1) < size; SA[sa
                + i] = SA[sa + k], i = k) {
            d = T[Td + SA[PA + SA[sa + (k = j++)]]];
            if (d < (e = T[Td + SA[PA + SA[sa + j]]])) {
                k = j;
                d = e;
            }
            if (d <= c) {
                break;
            }
        }
        SA[i + sa] = v;

    }

    /**
     * Logarithmic computing.
     *
     * @param n
     * @return
     */
    private final static int ssIlg(int n) {

        return ((n & 0xff00) != 0) ? 8 + lg_table[(n >> 8) & 0xff]
                : 0 + lg_table[(n >> 0) & 0xff];
    }

    /**
     * Swap function.
     *
     * @param a
     * @param b
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void swapInSA(int a, int b, byte[] T, int[] SA, int alphabetSize) {
        int tmp = SA[a];
        SA[a] = SA[b];
        SA[b] = tmp;
    }

    /**
     * Tandem repeat sort
     *
     * @param ISA
     * @param n
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trSort(int ISA, int n, int depth, byte[] T, int[] SA, int alphabetSize) {
        TRBudget budget = new TRBudget(trIlg(n) * 2 / 3, n);
        int ISAd;
        int first, last;// SA pointers
        int t, skip, unsorted;
        for (ISAd = ISA + depth; -n < SA[0]; ISAd += ISAd - ISA) {
            first = 0;
            skip = 0;
            unsorted = 0;
            do {
                if ((t = SA[first]) < 0) {
                    first -= t;
                    skip += t;
                } else {
                    if (skip != 0) {
                        SA[first + skip] = skip;
                        skip = 0;
                    }
                    last = SA[ISA + t] + 1;
                    if (1 < (last - first)) {
                        budget.count = 0;
                        trIntroSort(ISA, ISAd, first, last, budget, T, SA, alphabetSize);
                        if (budget.count != 0) {
                            unsorted += budget.count;
                        } else {
                            skip = first - last;
                        }
                    } else if ((last - first) == 1) {
                        skip = -1;
                    }
                    first = last;
                }
            } while (first < n);
            if (skip != 0) {
                SA[first + skip] = skip;
            }
            if (unsorted == 0) {
                break;
            }
        }
    }

    /**
     * Partitioning for sorting.
     *
     * @param ISAd
     * @param first
     * @param middle
     * @param last
     * @param pa
     * @param pb
     * @param v
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static TRPartitionResult trPartition(int ISAd, int first, int middle,
            int last, int pa, int pb, int v, byte[] T, int[] SA, int alphabetSize) {
        int a, b, c, d, e, f;// ptr
        int t, s, x = 0;

        for (b = middle - 1; (++b < last) && ((x = SA[ISAd + SA[b]]) == v);) {
        }
        if (((a = b) < last) && (x < v)) {
            for (; (++b < last) && ((x = SA[ISAd + SA[b]]) <= v);) {
                if (x == v) {
                    swapInSA(a, b, T, SA, alphabetSize);
                    ++a;
                }
            }
        }
        for (c = last; (b < --c) && ((x = SA[ISAd + SA[c]]) == v);) {
        }
        if ((b < (d = c)) && (x > v)) {
            for (; (b < --c) && ((x = SA[ISAd + SA[c]]) >= v);) {
                if (x == v) {
                    swapInSA(c, d, T, SA, alphabetSize);
                    --d;
                }
            }
        }
        for (; b < c;) {
            swapInSA(c, b, T, SA, alphabetSize);
            for (; (++b < c) && ((x = SA[ISAd + SA[b]]) <= v);) {
                if (x == v) {
                    swapInSA(a, b, T, SA, alphabetSize);
                    ++a;
                }
            }
            for (; (b < --c) && ((x = SA[ISAd + SA[c]]) >= v);) {
                if (x == v) {
                    swapInSA(c, d, T, SA, alphabetSize);
                    --d;
                }
            }
        }

        if (a <= d) {
            c = b - 1;
            if ((s = a - first) > (t = b - a)) {
                s = t;
            }
            for (e = first, f = b - s; 0 < s; --s, ++e, ++f) {
                swapInSA(e, f, T, SA, alphabetSize);
            }
            if ((s = d - c) > (t = last - d - 1)) {
                s = t;
            }
            for (e = b, f = last - s; 0 < s; --s, ++e, ++f) {
                swapInSA(e, f, T, SA, alphabetSize);
            }
            first += (b - a);
            last -= (d - c);
        }
        return new TRPartitionResult(first, last);
    }

    /**
     * Sorting with Introsort.
     *
     * @param ISA
     * @param ISAd
     * @param first
     * @param last
     * @param budget
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trIntroSort(int ISA, int ISAd, int first, int last, TRBudget budget, byte[] T, int[] SA, int alphabetSize) {
        final int STACK_SIZE = TR_STACKSIZE;
        StackElement[] stack = new StackElement[STACK_SIZE];
        int a = 0, b = 0, c;// pointers
        int v, x = 0;
        int incr = ISAd - ISA;
        int limit, next;
        int ssize, trlink = -1;
        for (ssize = 0, limit = trIlg(last - first);;) {
            if (limit < 0) {
                if (limit == -1) {
                    /* tandem repeat partition */
                    TRPartitionResult res = trPartition(ISAd - incr, first, first, last,
                            a, b, last - 1, T, SA, alphabetSize);
                    a = res.a;
                    b = res.b;
                    /* update ranks */
                    if (a < last) {
                        for (c = first, v = a - 1; c < a; ++c) {
                            SA[ISA + SA[c]] = v;
                        }
                    }
                    if (b < last) {
                        for (c = a, v = b - 1; c < b; ++c) {
                            SA[ISA + SA[c]] = v;
                        }
                    }

                    /* push */
                    if (1 < (b - a)) {
                        stack[ssize++] = new StackElement(0, a, b, 0, 0);
                        stack[ssize++] = new StackElement(ISAd - incr, first, last, -2,
                                trlink);
                        trlink = ssize - 2;
                    }
                    if ((a - first) <= (last - b)) {
                        if (1 < (a - first)) {
                            stack[ssize++] = new StackElement(ISAd, b, last, trIlg(last
                                    - b), trlink);
                            last = a;
                            limit = trIlg(a - first);
                        } else if (1 < (last - b)) {
                            first = b;
                            limit = trIlg(last - b);
                        } else if (ssize > 0) {
                            StackElement se = stack[--ssize];
                            ISAd = se.a;
                            first = se.b;
                            last = se.c;
                            limit = se.d;
                            trlink = se.e;
                        } else {
                            return;
                        }
                    } else if (1 < (last - b)) {
                        stack[ssize++] = new StackElement(ISAd, first, a, trIlg(a
                                - first), trlink);
                        first = b;
                        limit = trIlg(last - b);
                    } else if (1 < (a - first)) {
                        last = a;
                        limit = trIlg(a - first);
                    } else if (ssize > 0) {
                        StackElement se = stack[--ssize];
                        ISAd = se.a;
                        first = se.b;
                        last = se.c;
                        limit = se.d;
                        trlink = se.e;
                    } else {
                        return;
                    }
                } else if (limit == -2) {
                    /* tandem repeat copy */
                    StackElement se = stack[--ssize];
                    a = se.b;
                    b = se.c;
                    if (stack[ssize].d == 0) {
                        trCopy(ISA, first, a, b, last, ISAd - ISA, T, SA, alphabetSize);
                    } else {
                        if (0 <= trlink) {
                            stack[trlink].d = -1;
                        }
                        trPartialCopy(ISA, first, a, b, last, ISAd - ISA, T, SA, alphabetSize);
                    }
                    if (ssize > 0) {
                        se = stack[--ssize];
                        ISAd = se.a;
                        first = se.b;
                        last = se.c;
                        limit = se.d;
                        trlink = se.e;
                    } else {
                        return;
                    }
                } else {
                    /* sorted partition */
                    if (0 <= SA[first]) {
                        a = first;
                        do {
                            SA[ISA + SA[a]] = a;
                        } while ((++a < last) && (0 <= SA[a]));
                        first = a;
                    }
                    if (first < last) {
                        a = first;
                        do {
                            SA[a] = ~SA[a];
                        } while (SA[++a] < 0);
                        next = (SA[ISA + SA[a]] != SA[ISAd + SA[a]]) ? trIlg(a - first
                                + 1) : -1;
                        if (++a < last) {
                            for (b = first, v = a - 1; b < a; ++b) {
                                SA[ISA + SA[b]] = v;
                            }
                        }

                        /* push */
                        if (budget.check(a - first) != 0) {
                            if ((a - first) <= (last - a)) {
                                stack[ssize++] = new StackElement(ISAd, a, last, -3,
                                        trlink);
                                ISAd += incr;
                                last = a;
                                limit = next;
                            } else if (1 < (last - a)) {
                                stack[ssize++] = new StackElement(ISAd + incr, first,
                                        a, next, trlink);
                                first = a;
                                limit = -3;
                            } else {
                                ISAd += incr;
                                last = a;
                                limit = next;
                            }
                        } else {
                            if (0 <= trlink) {
                                stack[trlink].d = -1;
                            }
                            if (1 < (last - a)) {
                                first = a;
                                limit = -3;
                            } else if (ssize > 0) {
                                StackElement se = stack[--ssize];
                                ISAd = se.a;
                                first = se.b;
                                last = se.c;
                                limit = se.d;
                                trlink = se.e;
                            } else {
                                return;
                            }
                        }
                    } else if (ssize > 0) {
                        StackElement se = stack[--ssize];
                        ISAd = se.a;
                        first = se.b;
                        last = se.c;
                        limit = se.d;
                        trlink = se.e;
                    } else {
                        return;
                    }
                }
                continue;
            }

            if ((last - first) <= TR_INSERTIONSORT_THRESHOLD) {
                trInsertionSort(ISAd, first, last, T, SA, alphabetSize);
                limit = -3;
                continue;
            }

            if (limit-- == 0) {
                trHeapSort(ISAd, first, last - first, T, SA, alphabetSize);
                for (a = last - 1; first < a; a = b) {
                    for (x = SA[ISAd + SA[a]], b = a - 1; (first <= b)
                            && (SA[ISAd + SA[b]] == x); --b) {
                        SA[b] = ~SA[b];
                    }
                }
                limit = -3;
                continue;
            }
            // choose pivot
            a = trPivot(ISAd, first, last, T, SA, alphabetSize);
            swapInSA(first, a, T, SA, alphabetSize);
            v = SA[ISAd + SA[first]];

            // partition
            TRPartitionResult res = trPartition(ISAd, first, first + 1, last, a, b, v, T, SA, alphabetSize);
            a = res.a;
            b = res.b;

            if ((last - first) != (b - a)) {
                next = (SA[ISA + SA[a]] != v) ? trIlg(b - a) : -1;

                /* update ranks */
                for (c = first, v = a - 1; c < a; ++c) {
                    SA[ISA + SA[c]] = v;
                }
                if (b < last) {
                    for (c = a, v = b - 1; c < b; ++c) {
                        SA[ISA + SA[c]] = v;
                    }
                }

                /* push */
                if ((1 < (b - a)) && ((budget.check(b - a) != 0))) {
                    if ((a - first) <= (last - b)) {
                        if ((last - b) <= (b - a)) {
                            if (1 < (a - first)) {
                                stack[ssize++] = new StackElement(ISAd + incr, a, b,
                                        next, trlink);
                                stack[ssize++] = new StackElement(ISAd, b, last, limit,
                                        trlink);
                                last = a;
                            } else if (1 < (last - b)) {
                                stack[ssize++] = new StackElement(ISAd + incr, a, b,
                                        next, trlink);
                                first = b;
                            } else {
                                ISAd += incr;
                                first = a;
                                last = b;
                                limit = next;
                            }
                        } else if ((a - first) <= (b - a)) {
                            if (1 < (a - first)) {
                                stack[ssize++] = new StackElement(ISAd, b, last, limit,
                                        trlink);
                                stack[ssize++] = new StackElement(ISAd + incr, a, b,
                                        next, trlink);
                                last = a;
                            } else {
                                stack[ssize++] = new StackElement(ISAd, b, last, limit,
                                        trlink);
                                ISAd += incr;
                                first = a;
                                last = b;
                                limit = next;
                            }
                        } else {
                            stack[ssize++] = new StackElement(ISAd, b, last, limit,
                                    trlink);
                            stack[ssize++] = new StackElement(ISAd, first, a, limit,
                                    trlink);
                            ISAd += incr;
                            first = a;
                            last = b;
                            limit = next;
                        }
                    } else if ((a - first) <= (b - a)) {
                        if (1 < (last - b)) {
                            stack[ssize++] = new StackElement(ISAd + incr, a, b,
                                    next, trlink);
                            stack[ssize++] = new StackElement(ISAd, first, a, limit,
                                    trlink);
                            first = b;
                        } else if (1 < (a - first)) {
                            stack[ssize++] = new StackElement(ISAd + incr, a, b,
                                    next, trlink);
                            last = a;
                        } else {
                            ISAd += incr;
                            first = a;
                            last = b;
                            limit = next;
                        }
                    } else if ((last - b) <= (b - a)) {
                        if (1 < (last - b)) {
                            stack[ssize++] = new StackElement(ISAd, first, a, limit,
                                    trlink);
                            stack[ssize++] = new StackElement(ISAd + incr, a, b,
                                    next, trlink);
                            first = b;
                        } else {
                            stack[ssize++] = new StackElement(ISAd, first, a, limit,
                                    trlink);
                            ISAd += incr;
                            first = a;
                            last = b;
                            limit = next;
                        }
                    } else {
                        stack[ssize++] = new StackElement(ISAd, first, a, limit,
                                trlink);
                        stack[ssize++] = new StackElement(ISAd, b, last, limit,
                                trlink);
                        ISAd += incr;
                        first = a;
                        last = b;
                        limit = next;
                    }
                } else {
                    if ((1 < (b - a)) && (0 <= trlink)) {
                        stack[trlink].d = -1;
                    }
                    if ((a - first) <= (last - b)) {
                        if (1 < (a - first)) {
                            stack[ssize++] = new StackElement(ISAd, b, last, limit,
                                    trlink);
                            last = a;
                        } else if (1 < (last - b)) {
                            first = b;
                        } else if (ssize > 0) {
                            StackElement se = stack[--ssize];
                            ISAd = se.a;
                            first = se.b;
                            last = se.c;
                            limit = se.d;
                            trlink = se.e;
                        } else {
                            return;
                        }
                    } else if (1 < (last - b)) {
                        stack[ssize++] = new StackElement(ISAd, first, a, limit,
                                trlink);
                        first = b;
                    } else if (1 < (a - first)) {
                        last = a;
                    } else if (ssize > 0) {
                        StackElement se = stack[--ssize];
                        ISAd = se.a;
                        first = se.b;
                        last = se.c;
                        limit = se.d;
                        trlink = se.e;
                    } else {
                        return;
                    }
                }
            } else if (budget.check(last - first) != 0) {
                limit = trIlg(last - first);
                ISAd += incr;
            } else {
                if (0 <= trlink) {
                    stack[trlink].d = -1;
                }
                if (ssize > 0) {
                    StackElement se = stack[--ssize];
                    ISAd = se.a;
                    first = se.b;
                    last = se.c;
                    limit = se.d;
                    trlink = se.e;
                } else {
                    return;
                }
            }

        }

    }

    /**
     * Returns the pivot element.
     *
     * @param ISAd
     * @param first
     * @param last
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int trPivot(int ISAd, int first, int last, byte[] T, int[] SA, int alphabetSize) {
        int middle;
        int t;

        t = last - first;
        middle = first + t / 2;

        if (t <= 512) {
            if (t <= 32) {
                return trMedian3(ISAd, first, middle, last - 1, T, SA, alphabetSize);
            } else {
                t >>= 2;
                return trMedian5(ISAd, first, first + t, middle, last - 1 - t, last - 1, T, SA, alphabetSize);
            }
        }
        t >>= 3;
        first = trMedian3(ISAd, first, first + t, first + (t << 1), T, SA, alphabetSize);
        middle = trMedian3(ISAd, middle - t, middle, middle + t, T, SA, alphabetSize);
        last = trMedian3(ISAd, last - 1 - (t << 1), last - 1 - t, last - 1, T, SA, alphabetSize);
        return trMedian3(ISAd, first, middle, last, T, SA, alphabetSize);
    }

    /**
     * Returns the median of five elements.
     *
     * @param ISAd
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     * @param v5
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int trMedian5(int ISAd, int v1, int v2, int v3, int v4, int v5, byte[] T, int[] SA, int alphabetSize) {
        int t;
        if (SA[ISAd + SA[v2]] > SA[ISAd + SA[v3]]) {
            t = v2;
            v2 = v3;
            v3 = t;
        }
        if (SA[ISAd + SA[v4]] > SA[ISAd + SA[v5]]) {
            t = v4;
            v4 = v5;
            v5 = t;
        }
        if (SA[ISAd + SA[v2]] > SA[ISAd + SA[v4]]) {
            t = v2;
            v2 = v4;
            v4 = t;
            t = v3;
            v3 = v5;
            v5 = t;
        }
        if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v3]]) {
            t = v1;
            v1 = v3;
            v3 = t;
        }
        if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v4]]) {
            t = v1;
            v1 = v4;
            v4 = t;
            t = v3;
            v3 = v5;
            v5 = t;
        }
        if (SA[ISAd + SA[v3]] > SA[ISAd + SA[v4]]) {
            return v4;
        }
        return v3;
    }

    /**
     * Returns the median of three elements.
     *
     * @param ISAd
     * @param v1
     * @param v2
     * @param v3
     * @param T
     * @param SA
     * @param alphabetSize
     * @return
     */
    private final static int trMedian3(int ISAd, int v1, int v2, int v3, byte[] T, int[] SA, int alphabetSize) {
        if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v2]]) {
            int t = v1;
            v1 = v2;
            v2 = t;
        }
        if (SA[ISAd + SA[v2]] > SA[ISAd + SA[v3]]) {
            if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v3]]) {
                return v1;
            } else {
                return v3;
            }
        }
        return v2;
    }

    /**
     * Making a heap sort.
     *
     * @param ISAd
     * @param sa
     * @param size
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trHeapSort(int ISAd, int sa, int size, byte[] T, int[] SA, int alphabetSize) {
        int i, m, t;

        m = size;
        if ((size % 2) == 0) {
            m--;
            if (SA[ISAd + SA[sa + m / 2]] < SA[ISAd + SA[sa + m]]) {
                swapInSA(sa + m, sa + m / 2, T, SA, alphabetSize);
            }
        }

        for (i = m / 2 - 1; 0 <= i; --i) {
            trFixDown(ISAd, sa, i, m, T, SA, alphabetSize);
        }
        if ((size % 2) == 0) {
            swapInSA(sa, sa + m, T, SA, alphabetSize);
            trFixDown(ISAd, sa, 0, m, T, SA, alphabetSize);
        }
        for (i = m - 1; 0 < i; --i) {
            t = SA[sa];
            SA[sa] = SA[sa + i];
            trFixDown(ISAd, sa, 0, i, T, SA, alphabetSize);
            SA[sa + i] = t;
        }

    }

    /**
     * Fix down for heap sort.
     *
     * @param ISAd
     * @param sa
     * @param i
     * @param size
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trFixDown(int ISAd, int sa, int i, int size, byte[] T, int[] SA, int alphabetSize) {
        int j, k;
        int v;
        int c, d, e;

        for (v = SA[sa + i], c = SA[ISAd + v]; (j = 2 * i + 1) < size; SA[sa + i] = SA[sa
                + k], i = k) {
            d = SA[ISAd + SA[sa + (k = j++)]];
            if (d < (e = SA[ISAd + SA[sa + j]])) {
                k = j;
                d = e;
            }
            if (d <= c) {
                break;
            }
        }
        SA[sa + i] = v;

    }

    /**
     * Insertion sort.
     *
     * @param ISAd
     * @param first
     * @param last
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trInsertionSort(int ISAd, int first, int last, byte[] T, int[] SA, int alphabetSize) {
        int a, b;// SA ptr
        int t, r;

        for (a = first + 1; a < last; ++a) {
            for (t = SA[a], b = a - 1; 0 > (r = SA[ISAd + t] - SA[ISAd + SA[b]]);) {
                do {
                    SA[b + 1] = SA[b];
                } while ((first <= --b) && (SA[b] < 0));
                if (b < first) {
                    break;
                }
            }
            if (r == 0) {
                SA[b] = ~SA[b];
            }
            SA[b + 1] = t;
        }

    }

    /**
     * Partial copy.
     *
     * @param ISA
     * @param first
     * @param a
     * @param b
     * @param last
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trPartialCopy(int ISA, int first, int a, int b, int last, int depth, byte[] T, int[] SA, int alphabetSize) {
        int c, d, e;// ptr
        int s, v;
        int rank, lastrank, newrank = -1;

        v = b - 1;
        lastrank = -1;
        for (c = first, d = a - 1; c <= d; ++c) {
            if ((0 <= (s = SA[c] - depth)) && (SA[ISA + s] == v)) {
                SA[++d] = s;
                rank = SA[ISA + s + depth];
                if (lastrank != rank) {
                    lastrank = rank;
                    newrank = d;
                }
                SA[ISA + s] = newrank;
            }
        }

        lastrank = -1;
        for (e = d; first <= e; --e) {
            rank = SA[ISA + SA[e]];
            if (lastrank != rank) {
                lastrank = rank;
                newrank = e;
            }
            if (newrank != rank) {
                SA[ISA + SA[e]] = newrank;
            }
        }

        lastrank = -1;
        for (c = last - 1, e = d + 1, d = b; e < d; --c) {
            if ((0 <= (s = SA[c] - depth)) && (SA[ISA + s] == v)) {
                SA[--d] = s;
                rank = SA[ISA + s + depth];
                if (lastrank != rank) {
                    lastrank = rank;
                    newrank = d;
                }
                SA[ISA + s] = newrank;
            }
        }

    }

    /**
     * sort suffixes of middle partition by using sorted order of suffixes of
     * left and right partition.
     *
     * @param ISA
     * @param first
     * @param a
     * @param b
     * @param last
     * @param depth
     * @param T
     * @param SA
     * @param alphabetSize
     */
    private final static void trCopy(int ISA, int first, int a, int b, int last, int depth, byte[] T, int[] SA, int alphabetSize) {
        int c, d, e;// ptr
        int s, v;

        v = b - 1;
        for (c = first, d = a - 1; c <= d; ++c) {
            s = SA[c] - depth;
            if ((0 <= s) && (SA[ISA + s] == v)) {
                SA[++d] = s;
                SA[ISA + s] = d;
            }
        }
        for (c = last - 1, e = d + 1, d = b; e < d; --c) {
            s = SA[c] - depth;
            if ((0 <= s) && (SA[ISA + s] == v)) {
                SA[--d] = s;
                SA[ISA + s] = d;
            }
        }
    }

    /**
     * Compute logarithmic.
     *
     * @param n
     * @return
     */
    private final static int trIlg(int n) {
        return ((n & 0xffff0000) != 0) ? (((n & 0xff000000) != 0) ? 24 + lg_table[(n >> 24) & 0xff]
                : 16 + lg_table[(n >> 16) & 0xff])
                : (((n & 0x0000ff00) != 0) ? 8 + lg_table[(n >> 8) & 0xff]
                        : 0 + lg_table[(n >> 0) & 0xff]);
    }

}
