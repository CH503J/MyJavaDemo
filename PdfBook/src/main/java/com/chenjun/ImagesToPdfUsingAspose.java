package com.chenjun;

import com.aspose.words.*;

import java.io.File;

public class ImagesToPdfUsingAspose {
    public static void main(String[] args) {
        // 设置图片文件夹路径和输出 PDF 文件路径
        String imageDir = "C:\\Develop\\IDEA_Projects\\MyJavaDemo\\PdfBook\\src\\main\\resources\\download\\FA标准品目录"; // 图片文件夹路径
        String outputPdfPath = "C:\\Develop\\IDEA_Projects\\MyJavaDemo\\PdfBook\\src\\main\\resources\\pdf"; // 输出 PDF 文件路径

        int imagesPerPdf = 1000;

        try {
            convertImagesToPdf(imageDir, outputPdfPath, imagesPerPdf);
            System.out.println("PDF制作完成: " + outputPdfPath);
        } catch (Exception e) {
            System.err.println("PDF制作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将指定文件夹中的图片转换为多个 PDF 文件
     *
     * @param imageDir     图片文件夹路径
     * @param outputDir    输出的 PDF 文件所在目录
     * @param imagesPerPdf 每个 PDF 文件包含的图片数量
     * @throws Exception 如果发生错误
     */
    private static void convertImagesToPdf(String imageDir, String outputDir, int imagesPerPdf) throws Exception {
        // 获取图片文件夹中的所有文件
        File folder = new File(imageDir);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid image directory: " + imageDir);
        }

        // 获取所有图片文件
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            throw new IllegalArgumentException("No images found in directory: " + imageDir);
        }

        // 按文件名排序（假设文件名是按页码命名的）
        java.util.Arrays.sort(imageFiles, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        // 变量用于追踪已处理的图片数量和生成的 PDF 文件编号
        int imageCount = 0;
        int pdfFileCount = 1;

        // 创建一个初始的 Word 文档对象
        Document doc = new Document();
        DocumentBuilder builder = new DocumentBuilder(doc);

        // 遍历图片文件
        for (File imageFile : imageFiles) {
            // 插入图片
            builder.insertImage(imageFile.getAbsolutePath());

            // 插入换行符以便于每张图片在新的一页显示
            builder.insertBreak(BreakType.PAGE_BREAK);

            // 增加处理的图片计数
            imageCount++;

            // 如果已处理的图片达到 1000 张，则保存当前 PDF 并重置
            if (imageCount == imagesPerPdf) {
                String pdfPath = outputDir + File.separator + "FA标准品目录" + pdfFileCount + ".pdf";
                doc.save(pdfPath, SaveFormat.PDF); // 保存为 PDF
                System.out.println("Created PDF: " + pdfPath);

                // 重置文档和计数器
                doc = new Document();
                builder = new DocumentBuilder(doc);
                imageCount = 0;
                pdfFileCount++;
            }
        }

        // 保存剩余未满 1000 张的图片为最后一个 PDF 文件
        if (imageCount > 0) {
            String pdfPath = outputDir + File.separator + "output_" + pdfFileCount + ".pdf";
            doc.save(pdfPath, SaveFormat.PDF); // 保存为 PDF
            System.out.println("Created PDF: " + pdfPath);
        }
    }
}
