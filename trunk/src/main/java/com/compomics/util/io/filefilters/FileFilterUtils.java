package com.compomics.util.io.filefilters;

import java.io.File;
import javax.swing.ImageIcon;

/**
 * Organizes the file filters.
 *
 * @author Harald Barsnes
 */
public class FileFilterUtils {

    public final static String dat = "dat";
    public final static String xml = "xml";
    public final static String mgf = "mgf";
    public final static String dta = "dta";
    public final static String pkl = "pkl";
    public final static String pkx = "pkx";
    public final static String spo = "spo";
    public final static String out = "out";
    public final static String mzXML = "mzXML";
    public final static String mzxml = "mzxml";
    public final static String mzML = "mzML";
    public final static String mzml = "mzml";
    public final static String mzdata = "mzdata";
    public final static String mzData = "mzData";
    public final static String properties = "properties";
    public final static String prot_xml = "prot.xml";
    public final static String protxml = "protxml";
    public final static String pep_xml = "pep.xml";
    public final static String pepxml = "pepxml";
    public final static String txt = "txt";
    public final static String mzDATA = "mzDATA";
    public final static String omx = "omx";
    public final static String ms2 = "ms2";
    public final static String gif = "gif";
    public final static String png = "png";
    public final static String svg = "svg";
    public final static String pdf = "pdf";
    public final static String tiff = "tiff";
    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String DAT = "DAT";
    public final static String XML = "XML";
    public final static String MGF = "MGF";
    public final static String DTA = "DTA";
    public final static String PKL = "PKL";
    public final static String PKX = "PKX";
    public final static String SPO = "SPO";
    public final static String OUT = "OUT";
    public final static String MZXML = "MZXML";
    public final static String MZML = "MZML";
    public final static String OMX = "OMX";
    public final static String MZDATA = "MZDATA";
    public final static String MS2 = "MS2";
    public final static String PROPERTIES = "PROPERTIES";
    public final static String PROT_XML = "PROT.XML";
    public final static String PROTXML = "PROTXML";
    public final static String PEP_XML = "PEP.XML";
    public final static String PEPXML = "PEPXML";
    public final static String TXT = "TXT";
    public final static String GIF = "GIF";
    public final static String PNG = "PNG";
    public final static String SVG = "SVG";
    public final static String PDF = "PDF";
    public final static String TIFF = "TIFF";
    public final static String JPG = "JPG";
    public final static String JPEG = "JPEG";
    public final static String FASTA = "FASTA";
    public final static String fasta = "fasta";
    public final static String FAS = "FAS";
    public final static String fas = "fas";
    public final static String PEFF = "PEFF";
    public final static String peff = "peff";

    /**
     * Get the extension of a file.
     *
     * @param f the file
     * @return String - the extension of the file f
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path the path
     * @return ImageIcon the image icon
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FileFilterUtils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
