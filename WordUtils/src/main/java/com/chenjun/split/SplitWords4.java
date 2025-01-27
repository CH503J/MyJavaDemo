package com.chenjun.split;

import com.aspose.words.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SplitWords4 {
    public static Map<String, Document> splitWords(File file) {
        int claimNumCount = 0; // 编号计数器
        Map<String, Document> outputDocMap = new HashMap<>(); // 用于存储文件名和Document对象

        try {
            Document doc = new Document(file.getPath());
            // 用于存储页眉内容和对应的新文档
            Map<String, Document> headerDocMap = new HashMap<>();

            // 遍历每一节
            for (Section section : doc.getSections()) {
                // 获取当前节的主页眉
                HeaderFooter header = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_PRIMARY);
                String headerText = (header != null) ? header.getText().trim() : "NO_HEADER";

                // 如果页眉内容对应的新文档不存在，则创建一个新文档
                if (!headerDocMap.containsKey(headerText)) {
                    Document newDoc = new Document();
                    // 清空新文档中的默认内容
                    newDoc.removeAllChildren();
                    headerDocMap.put(headerText, newDoc);
                }

                // 将当前节克隆并添加到对应的新文档中
                Document targetDoc = headerDocMap.get(headerText);
                Section clonedSection = (Section) section.deepClone(true);
                targetDoc.appendChild(targetDoc.importNode(clonedSection, true));
            }

            // 保存拆分后的文档
            for (Map.Entry<String, Document> entry : headerDocMap.entrySet()) {
                String headerKey = entry.getKey();
                Document newDoc = entry.getValue();
                String fileName = (headerKey.equals("NO_HEADER") ? "NoHeader" : headerKey.replaceAll("[\\\\/:*?\"<>|]", "_"));

                // 截取uuid前4位作为新文件名的前缀
                String uuid = UUID.randomUUID().toString().substring(0, 4);
                String outputFileName = uuid + "_" + fileName + ".docx";

                // 如果是“权利要求书”，统计编号并打印编号及文本
                if (outputFileName.contains("权利要求书")) {
                    newDoc.updateListLabels(); // 更新编号标签
                    for (Paragraph paragraph : newDoc.getSections().get(0).getBody().getParagraphs()) {
                        if (paragraph.getListFormat().isListItem()) { // 判断段落是否为列表项
                            // 获取编号信息
                            ListLabel listLabel = paragraph.getListLabel();
                            String numText = listLabel.getLabelString();

                            // 获取段落文本内容
                            String paragraphText = paragraph.getText().trim();

                            // 打印编号和文本内容
                            System.out.println("编号: " + numText + paragraphText);

                            // 统计编号数量
                            claimNumCount++;
                        }
                    }
                }

                // 将新文档对象放入map中（不保存到文件）
                outputDocMap.put(outputFileName, newDoc); // 文件名与Document对象对应
            }

            // 打印总的编号数量
            System.out.println("权利要求书编号总计: " + claimNumCount);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return outputDocMap; // 返回文件名与Document对象的映射
    }
}
