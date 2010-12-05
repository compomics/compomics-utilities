package com.compomics.util;

import com.compomics.util.enumeration.ImageType;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

/**
 * Includes export methods that are used by the other classes.
 *
 * @author  Harald Barsnes
 */
public class Export {

    /**
     * Exports the contents of a Component to an svg, png, gif etc.
     *
     * @param component component to export
     * @param bounds the dimensions of the viewport
     * @param exportFile the output file
     * @param imageType the image type
     * @throws IOException
     * @throws TranscoderException
     */
    public static void exportComponent(Component component, Rectangle bounds, File exportFile, ImageType imageType)
            throws IOException, TranscoderException {

        // draw the component in the SVG graphics
        SVGGraphics2D svgGenerator = drawSvgGraphics(component, bounds);

        // export the plot
        exportPlot(exportFile, imageType, svgGenerator);
    }

    /**
     * Draws the selected component (assumed to be a Component) into the provided
     * SVGGraphics2D object.
     *
     * @param component
     * @param bounds
     */
    private static SVGGraphics2D drawSvgGraphics(Object component, Rectangle bounds) {

        // Get a SVGDOMImplementation and create an XML document
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        SVGDocument svgDocument = (SVGDocument) domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(svgDocument);
        svgGenerator.setSVGCanvasSize(bounds.getSize());

        // draw the panel in the SVG generator
//        if (component instanceof JFreeChart) {    // @TODO: re-add the JFreeChart support?
//            ((JFreeChart) component).draw(svgGenerator, bounds);
//        } else if (component instanceof Component) {
            ((Component) component).paintAll(svgGenerator);
//        }

        return svgGenerator;
    }

    /**
     * Exports the selected file to the wanted format.
     *
     * @param exportFile
     * @param imageType
     * @param svgGenerator
     * @throws IOException
     * @throws TranscoderException
     */
    private static void exportPlot(File exportFile, ImageType imageType, SVGGraphics2D svgGenerator)
            throws IOException, TranscoderException {

        // write the svg file
        File svgFile = exportFile;

        if (imageType != ImageType.SVG) {
            svgFile = new File(exportFile.getAbsolutePath() + ".temp");
        }

        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);
        outputStream.flush();
        outputStream.close();

        // if selected image format is not svg, convert the image
        if (imageType != ImageType.SVG) {

            // set up the svg input
            String svgURI = svgFile.toURI().toString();
            TranscoderInput svgInputFile = new TranscoderInput(svgURI);

            OutputStream outstream = new FileOutputStream(exportFile);
            TranscoderOutput output = new TranscoderOutput(outstream);

            if (imageType == ImageType.PDF) {

                // write as pdf
                Transcoder pdfTranscoder = new PDFTranscoder();
                pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(0.084666f));
                pdfTranscoder.transcode(svgInputFile, output);

            } else if (imageType == ImageType.JPEG) {

                // write as jpeg
                Transcoder jpegTranscoder = new JPEGTranscoder();
                jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1.0));
                jpegTranscoder.transcode(svgInputFile, output);

            } else if (imageType == ImageType.TIFF) {

                // write as tiff
                Transcoder tiffTranscoder = new TIFFTranscoder();
                tiffTranscoder.addTranscodingHint(TIFFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(0.084666f));
                tiffTranscoder.addTranscodingHint(TIFFTranscoder.KEY_FORCE_TRANSPARENT_WHITE, true);
                tiffTranscoder.transcode(svgInputFile, output);

            } else if (imageType == ImageType.PNG) {

                // write as png
                Transcoder pngTranscoder = new PNGTranscoder();
                pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(0.084666f));
                pngTranscoder.transcode(svgInputFile, output);

            }

            //close the stream
            outstream.flush();
            outstream.close();

            // delete the svg file given that the selected format is not svg
            if (svgFile.exists()) {
                svgFile.delete();
            }
        }
    }
}
