package com.chenjun.split;


import com.aspose.words.Document;
import com.aspose.words.HeaderFooter;
import com.aspose.words.HeaderFooterType;
import com.aspose.words.Section;

import java.io.File;

public class SplitWords1 {

    public static void splitWords(File file) {
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
            throw new RuntimeException(e.getMessage());
        }
    }
}
