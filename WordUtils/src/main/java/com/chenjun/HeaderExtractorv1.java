package com.chenjun;


import com.aspose.words.Document;
import com.aspose.words.HeaderFooter;
import com.aspose.words.HeaderFooterType;
import com.aspose.words.Section;

import java.io.File;

public class HeaderExtractorv1 {

    public static void headerExtractor(File file) {
        try {
            Document doc = new Document(file.getPath());

            // 获取页眉
            for (Section section : doc.getSections()) {
                HeaderFooter header = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_PRIMARY);
                if (header != null) {
                    System.out.println(header.getText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
