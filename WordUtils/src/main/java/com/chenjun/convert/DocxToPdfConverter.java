package com.chenjun.convert;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;

import java.io.File;
import java.util.Map;

public class DocxToPdfConverter {
    public static void convertDocumentsToPdf(Map<String, Document> documents, String outputFolder) throws Exception {
        // 检查输出文件夹是否有效
        File outputDir = new File(outputFolder);
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            throw new IllegalArgumentException("输出文件夹路径无效：" + outputFolder);
        }

        // 遍历 Map，将每个 Document 保存为 PDF
        for (Map.Entry<String, Document> entry : documents.entrySet()) {
            String fileName = entry.getKey(); // 文件名
            Document document = entry.getValue(); // Document 对象

            // 确定输出 PDF 文件路径
            String outputFilePath = outputFolder + File.separator + fileName.replace(".docx", ".pdf");

            // 保存为 PDF
            document.save(outputFilePath, SaveFormat.PDF);
            System.out.println("转换成功：" + fileName + " -> " + outputFilePath);
        }
    }

}
