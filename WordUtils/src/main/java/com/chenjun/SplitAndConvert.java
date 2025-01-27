package com.chenjun;

import com.aspose.words.Document;
import com.chenjun.convert.DocxToPdfConverter;
import com.chenjun.split.SplitWords4;

import java.io.File;
import java.util.Map;

public class SplitAndConvert {
    public static void main(String[] args) {
        String docxFilePath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\originalFiles\\五书模板v4.docx";

        String outputPath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\outputPdfFiles";

        Map<String, Document> stringDocumentMap = SplitWords4.splitWords(new File(docxFilePath));

        try {
            DocxToPdfConverter.convertDocumentsToPdf(stringDocumentMap, outputPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
