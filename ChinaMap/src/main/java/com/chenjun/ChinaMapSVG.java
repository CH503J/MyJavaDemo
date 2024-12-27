package com.chenjun;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class ChinaMapSVG {
    public static void main(String[] args) {
        try {
            // Step 1: Create an SVG document
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

            // Step 2: Draw the map (use an example path, replace with actual China map path data)
            svgGenerator.setPaint(Color.BLACK);
            Path2D path = new Path2D.Double();
            path.moveTo(100, 100);
            path.lineTo(200, 100);
            path.lineTo(150, 200);
            path.closePath();
            svgGenerator.draw(path);

            // Add more paths here to complete the map
            // Example: replace the above path with China's actual SVG path data
            svgGenerator.setPaint(Color.BLUE);
            svgGenerator.fill(path);

            // Step 3: Save the SVG to a file
            boolean useCSS = true; // we want to use CSS style attributes
            File svgFile = new File("china-map.svg");
            Writer out = new FileWriter(svgFile);
            svgGenerator.stream(out, useCSS);

            System.out.println("SVG file created successfully: " + svgFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
