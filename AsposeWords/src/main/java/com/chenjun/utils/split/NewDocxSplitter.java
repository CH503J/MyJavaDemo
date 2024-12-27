package com.chenjun.utils.split;

import com.my.common.exception.CustomException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewDocxSplitter {

    public static void main(String[] args) {
        try {
            String inputFilePath = "C:\\Users\\junchen\\Desktop\\五书模板\\一种井场内二氧化碳产出量计量系统及方法（模板测试）.docx";

            // 加载文档
            Document doc = new Document(inputFilePath);

            // 存储非空段落
            List<Paragraph> nonEmptyParagraphs = new ArrayList<>();

            // 遍历文档中的段落，收集非空段落
            NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
            for (int i = 0; i < paragraphs.getCount(); i++) {
                Paragraph para = (Paragraph) paragraphs.get(i);
                if (!para.getText().trim().isEmpty()) {
                    nonEmptyParagraphs.add(para);
                }
            }

            // 存储文档拆分后的集合
            Map<String, File> splitDocumentsMap = new HashMap<>();

            // 临时集合，用于存储当前一级大纲及其下的段落
            List<Paragraph> tempParagraphs = new ArrayList<>();

            // 遍历非空段落集合，进行拆分
            for (Paragraph para : nonEmptyParagraphs) {
                // 检测到一级大纲（大纲级别为1）
                if (para.getParagraphFormat().getStyle().getName().equals("Heading 1")) {
                    // 如果临时集合有内容，保存为一个新的 docx 文件
                    if (!tempParagraphs.isEmpty()) {
                        saveAsNewDocument(tempParagraphs, splitDocumentsMap);
                        tempParagraphs.clear();  // 清空临时集合
                    }

                    // 将当前一级大纲段落添加到临时集合
                    tempParagraphs.add(para);
                } else {
                    // 如果当前段落不是一级大纲，则将其视为当前一级大纲的正文，加入临时集合
                    if (!tempParagraphs.isEmpty()) {
                        tempParagraphs.add(para);
                    }
                }
            }

            // 最后处理剩余的临时集合
            if (!tempParagraphs.isEmpty()) {
                saveAsNewDocument(tempParagraphs, splitDocumentsMap);
            }

            System.out.println("文档已拆分，生成了 " + splitDocumentsMap.size() + " 部分。");

        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

    // 将临时段落集合保存为一个新的 DOCX 文档并保存到Map中
    private static void saveAsNewDocument(List<Paragraph> tempParagraphs, Map<String, File> splitDocumentsMap) throws Exception {
        // 获取一级标题文本作为文件名
        String titleText = tempParagraphs.get(0).getText().trim(); // 假设第一个段落是一级标题
        String fileName = cleanFileName(titleText);  // 清理文件名中的特殊字符

        // 创建新文档
        Document newDoc = new Document();

        // 复制原文档的页面设置到新文档中
        Section originalSection = tempParagraphs.get(0).getParentSection();
        Section newSection = newDoc.getFirstSection();

        // 复制页面设置
        newSection.getPageSetup().setPaperSize(originalSection.getPageSetup().getPaperSize());
        newSection.getPageSetup().setTopMargin(originalSection.getPageSetup().getTopMargin());
        newSection.getPageSetup().setBottomMargin(originalSection.getPageSetup().getBottomMargin());
        newSection.getPageSetup().setLeftMargin(originalSection.getPageSetup().getLeftMargin());
        newSection.getPageSetup().setRightMargin(originalSection.getPageSetup().getRightMargin());
        newSection.getPageSetup().setOrientation(originalSection.getPageSetup().getOrientation());

        // 复制其他相关的页面设置（如页边距、页脚、页眉等）
        newSection.getPageSetup().setHeaderDistance(originalSection.getPageSetup().getHeaderDistance());
        newSection.getPageSetup().setFooterDistance(originalSection.getPageSetup().getFooterDistance());

        // 获取新文档的段落集合
        ParagraphCollection newParagraphs = newDoc.getFirstSection().getBody().getParagraphs();

        // 移除新文档中的默认空段落
        if (newParagraphs.getCount() > 0) {
            newParagraphs.clear(); // 清除默认空段落
        }

        // 将临时集合中的段落添加到新文档中，并检查是否有图片
        for (Paragraph para : tempParagraphs) {
            Node importedNode = newDoc.importNode(para, true);
            newParagraphs.add(importedNode);

            System.out.println(para.getText().trim());
            // 检查当前段落是否包含图片（Shape对象）
            NodeCollection shapes = para.getChildNodes(NodeType.SHAPE, true);

            // 处理所有图片
            for (int j = 0; j < shapes.getCount(); j++) {
                Shape shape = (Shape) shapes.get(j);
                if (shape.getShapeType() == ShapeType.IMAGE) {
                    // 直接将图片添加到新文档中
                    Node importedShape = newDoc.importNode(shape, true);
                    newDoc.getFirstSection().getBody().appendChild(importedShape);
                }
            }
        }

        // 将文档保存到内存中的字节流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newDoc.save(baos, SaveFormat.DOCX);

        // 将字节流转换为文件
        File tempFile = new File(fileName + ".docx");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            baos.writeTo(fos);
        }

        // 将生成的文件存入Map中
        splitDocumentsMap.put(fileName, tempFile);
        System.out.println("文件已添加到Map: " + fileName);
    }

    /**
     * 清理文件名中的特殊字符
     * @param fileName 原始文件名
     * @return 清理后的文件名
     */
    private static String cleanFileName(String fileName) {
        // 去除文件名中的特殊字符
        String cleanedFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");  // 将不允许的字符替换为下划线
        cleanedFileName = cleanedFileName.replaceAll("[\\r\\n]+", " ");  // 去掉多余的换行符
        cleanedFileName = cleanedFileName.trim();  // 去除前后空格

        // 如果文件名为空，则给它一个默认的名称
        if (cleanedFileName.isEmpty()) {
            cleanedFileName = "Untitled";
        }

        return cleanedFileName;
    }
}
