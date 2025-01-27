package com.chenjun;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;

import java.io.File;
import java.util.Map;

public class Demo2 {
    public static void main(String[] args) {
        String filePath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\originalFiles\\五书模板v4.docx";
        String outputPath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\outputFiles\\";

        File file = new File(filePath);

        Map<String, Document> resultMap = HeaderExtractorv4.headerExtractor(file);
        for (Map.Entry<String, Document> entry : resultMap.entrySet()) {
            String fileName = entry.getKey();
            Document doc = entry.getValue();

            try {
                doc.save(outputPath+ fileName , SaveFormat.DOCX);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            System.out.println("文件名：" + fileName + "\n文件路径：" + outputPath + fileName);
        }
    }
}
