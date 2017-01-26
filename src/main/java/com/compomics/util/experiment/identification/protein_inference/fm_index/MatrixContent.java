package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Element for the matrix necessary in pattern search of the FMIndex.
 *
 * @author Dominik Kopczynski
 */
public class MatrixContent {

    /**
     * Left index.
     */
    public int left;
    /**
     * Right index.
     */
    public int right;
    /**
     * Character which was chosen.
     */
    public int character;
    /**
     * Index of the originating entry of a particular cell with the pattern
     * searching matrix.
     */
    public MatrixContent previousContent;
    /**
     * Current mass.
     */
    public double mass;
    /**
     * Current peptide sequence.
     */
    public String peptideSequence;
    /**
     * Current peptide sequence needed for search.
     */
    public String peptideSequenceSearch;
    /**
     * Current peptide sequence length.
     */
    public int length;
    /**
     * Current number of contained X's.
     */
    public int numX;
    /**
     * Index to the modifications list.
     */
    public ModificationMatch modification;
    /**
     * List of all modifications.
     */
    public ArrayList<ModificationMatch> modifications;
    /**
     * List of all modifications.
     */
    public int modificationPos;
    /**
     * List of all modifications.
     */
    public int numVariants;
    /**
     * The specific variants. (0) deletion, (1) insertion, (2) substitution.
     */
    public int[] numSpecificVariants;
    /**
     * Type of edit operation, either deletion 'd', substitution 's' or
     * insertion 'i'.
     */
    public char variant;
    /**
     * Current storing of all variant operations.
     */
    public String allVariants;
    /**
     * If original character was B, J, X, Z.
     */
    public int ambiguousChar;
    /**
     * Information about the component where the X belongs to.
     */
    public int tagComponent;
    /**
     * Information about all component where the Xs belong to.
     */
    public ArrayList<int[]> allXcomponents;
    /**
     * The X mass difference,
     */
    public double XMassDiff;
    /**
     * The all X mass differences.
     */
    public HashMap<Integer, Double> allXMassDiffs;

    /**
     * Constructor almost empty.
     *
     * @param right right index boundary
     */
    public MatrixContent(int right) {
        this.left = 0;
        this.right = right;
        this.character = 0;
        this.previousContent = null;
        this.mass = 0;
        this.peptideSequence = null;
        this.length = 0;
        this.numX = 0;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = -1;
        this.peptideSequenceSearch = null;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor for simple sequence mapping.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param numX number of current X amino acids
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, int numX) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = 0;
        this.peptideSequence = null;
        this.length = 0;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = -1;
        this.peptideSequenceSearch = null;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor for simple tag mapping.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param numX number of current X amino acids
     * @param mass current mass
     * @param length current peptide length
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, int numX, double mass, int length) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = null;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = -1;
        this.peptideSequenceSearch = null;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor for simple tag mapping with peptide sequence.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param numX number of current X amino acids
     * @param mass current mass
     * @param length current peptide length
     * @param peptideSequence peptide sequence
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, int numX, double mass, int length, String peptideSequence) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = -1;
        this.peptideSequenceSearch = null;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor for sequence with variants.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param numX number of current X amino acids
     * @param length length of the current peptide
     * @param numVariants number of edit operations
     * @param variant type of edit operation
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, int numX, int length, int numVariants, char variant) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = 0;
        this.peptideSequence = null;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
        this.numVariants = numVariants;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = variant;
        this.allVariants = null;
        this.peptideSequenceSearch = null;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor for sequence with variants.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param numX number of current X amino acids
     * @param length length of the current peptide
     * @param numSpecificVariants number of the specific edit operations
     * @param variant type of edit operation
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, int numX, int length, int[] numSpecificVariants, char variant) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = 0;
        this.peptideSequence = null;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
        this.numVariants = 0;
        this.numSpecificVariants = numSpecificVariants;
        this.variant = variant;
        this.allVariants = null;
        this.peptideSequenceSearch = null;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modifictationPos index to modification list for ptm
     * @param ambiguousChar ambiguous character
     * @param tagComponent the tag component index
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, int length, int numX, int modifictationPos, int ambiguousChar, int tagComponent) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = null;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = modifictationPos;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = ambiguousChar;
        this.peptideSequenceSearch = null;
        this.tagComponent = tagComponent;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param peptideSequence intermediate peptide sequence
     * @param peptideSequenceSearch intermediate peptide sequence for search
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modification index to modification list
     * @param modifications intermediate list of modifications
     * @param modifictationPos index to modification list for ptm
     * @param ambiguousChar ambiguous character
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, String peptideSequence, String peptideSequenceSearch,
            int length, int numX, ModificationMatch modification, ArrayList<ModificationMatch> modifications, int modifictationPos, int ambiguousChar) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
        this.peptideSequenceSearch = peptideSequenceSearch;
        this.length = length;
        this.numX = numX;
        this.modification = modification;
        this.modifications = modifications;
        this.modificationPos = modifictationPos;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = ambiguousChar;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param peptideSequence intermediate peptide sequence
     * @param peptideSequenceSearch intermediate peptide sequence for search
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param tagComponent the tag component index
     * @param modification index to modification list
     * @param modifications intermediate list of modifications
     * @param modifictationPos index to modification list for PTM
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, String peptideSequence, String peptideSequenceSearch,
            int length, int numX, int tagComponent, ModificationMatch modification, ArrayList<ModificationMatch> modifications, int modifictationPos) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
        this.peptideSequenceSearch = peptideSequenceSearch;
        this.length = length;
        this.numX = numX;
        this.modification = modification;
        this.modifications = modifications;
        this.modificationPos = modifictationPos;
        this.numVariants = 0;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = '\0';
        this.allVariants = null;
        this.ambiguousChar = -1;
        this.tagComponent = tagComponent;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modifictationPos index to modification list for PTM
     * @param numVariants number of edit operations
     * @param variant type of variant
     * @param allVariants all variants
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, int length, int numX,
            int modifictationPos, int numVariants, char variant, String allVariants) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = null;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = modifictationPos;
        this.numVariants = numVariants;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = variant;
        this.allVariants = allVariants;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modifictationPos index to modification list for PTM
     * @param numSpecificVariants number of the specific edit operations
     * @param variant type of variant
     * @param allVariants all variants
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, int length, int numX,
            int modifictationPos, int[] numSpecificVariants, char variant, String allVariants) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = null;
        this.length = length;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = modifictationPos;
        this.numVariants = 0;
        this.numSpecificVariants = numSpecificVariants;
        this.variant = variant;
        this.allVariants = allVariants;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param peptideSequence intermediate peptide sequence
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modification index to modification list
     * @param modifications intermediate list of modifications
     * @param modifictationPos index to modification list for PTM
     * @param numVariants number of edit operations
     * @param variant type of variant
     * @param allVariants all variants
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, String peptideSequence, int length, int numX, ModificationMatch modification, ArrayList<ModificationMatch> modifications,
            int modifictationPos, int numVariants, char variant, String allVariants) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
        this.length = length;
        this.numX = numX;
        this.modification = modification;
        this.modifications = modifications;
        this.modificationPos = modifictationPos;
        this.numVariants = numVariants;
        this.numSpecificVariants = new int[]{0, 0, 0};
        this.variant = variant;
        this.allVariants = allVariants;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param peptideSequence intermediate peptide sequence
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modification index to modification list
     * @param modifications intermediate list of modifications
     * @param modifictationPos index to modification list for PTM
     * @param numSpecificVariants number of the specific edit operations
     * @param variant type of variant
     * @param allVariants all variants
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, String peptideSequence, int length, int numX, ModificationMatch modification, ArrayList<ModificationMatch> modifications,
            int modifictationPos, int[] numSpecificVariants, char variant, String allVariants) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
        this.length = length;
        this.numX = numX;
        this.modification = modification;
        this.modifications = modifications;
        this.modificationPos = modifictationPos;
        this.numVariants = 0;
        this.numSpecificVariants = numSpecificVariants;
        this.variant = variant;
        this.allVariants = allVariants;
        this.tagComponent = -1;
        this.allXcomponents = null;
        this.XMassDiff = -1;
        this.allXMassDiffs = null;
    }

    /**
     * Copy constructor.
     *
     * @param foreign foreign matrix content instance
     */
    public MatrixContent(MatrixContent foreign) {
        this.left = foreign.left;
        this.right = foreign.right;
        this.character = foreign.character;
        this.previousContent = foreign.previousContent;
        this.mass = foreign.mass;
        this.peptideSequence = foreign.peptideSequence;
        this.length = foreign.length;
        this.numX = foreign.numX;
        this.modification = foreign.modification;
        this.modifications = foreign.modifications;
        this.modificationPos = foreign.modificationPos;
        this.numVariants = foreign.numVariants;
        this.numSpecificVariants = new int[]{foreign.numSpecificVariants[0], foreign.numSpecificVariants[1], foreign.numSpecificVariants[2]};
        this.variant = foreign.variant;
        this.allVariants = foreign.allVariants;
        this.tagComponent = foreign.tagComponent;
        this.allXcomponents = foreign.allXcomponents;
        this.XMassDiff = foreign.XMassDiff;
        this.allXMassDiffs = foreign.allXMassDiffs;
    }
}
