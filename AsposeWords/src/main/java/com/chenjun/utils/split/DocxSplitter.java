package com.chenjun.utils.split;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocxSplitter {

    public static void main(String[] args) throws Exception {
        // 加载文档
        Document doc = new Document("C:\\Develop\\WordToXml\\Manual\\WORD转XML编辑器五书模板文件.docx");

        // 定义需要拆分的部分及其页眉标记
        Map<String, String> sections = new HashMap<>();
        sections.put("权利要求书", "权利要求书");
        sections.put("说明书", "说明书");
        sections.put("说明书附图", "说明书附图");
        sections.put("说明书摘要", "说明书摘要");
        sections.put("摘要附图", "摘要附图");

        // 创建一个新的文档对象来保存拆分后的部分
        Map<String, List<Node>> sectionContent = new HashMap<>();
        for (String section : sections.keySet()) {
            sectionContent.put(section, new ArrayList<>());
        }

        // 临时存储当前正在处理的部分
        String currentSection = null;
        List<Node> tempSectionNodes = new ArrayList<>();

        // 遍历文档中的所有节点
        NodeCollection nodes = doc.getChildNodes(NodeType.ANY, true);
        for (Node node : (Iterable<Node>) nodes) {
            // 检查是否是段落且位于页眉中
            if (node.getNodeType() == NodeType.PARAGRAPH) {
                Paragraph paragraph = (Paragraph) node;
                if (paragraph.isEndOfHeaderFooter()) {
                    HeaderFooter header = (HeaderFooter) paragraph.getParentNode();
                    if (header != null && header.getHeaderFooterType() == HeaderFooterType.HEADER_PRIMARY) {
                        String headerText = paragraph.getText().trim();

                        // 如果当前节点是我们要拆分的部分，更新当前部分
                        for (Map.Entry<String, String> entry : sections.entrySet()) {
                            if (headerText.contains(entry.getValue())) {
                                if (!entry.getKey().equals(currentSection)) {
                                    // 如果换到新的部分，保存临时集合并清空
                                    if (currentSection != null && !tempSectionNodes.isEmpty()) {
                                        sectionContent.get(currentSection).addAll(tempSectionNodes);
                                        tempSectionNodes.clear();
                                    }
                                    currentSection = entry.getKey();
                                }
                                break;
                            }
                        }
                    }
                }
            }

            // 如果当前部分已被设置，保存当前节点到临时集合
            if (currentSection != null) {
                tempSectionNodes.add(node);
            }
        }

        // 在遍历结束时，将最后的临时节点集合保存
        if (currentSection != null && !tempSectionNodes.isEmpty()) {
            sectionContent.get(currentSection).addAll(tempSectionNodes);
        }

        // 为每个部分创建一个新的文档并保存
        for (Map.Entry<String, List<Node>> entry : sectionContent.entrySet()) {
            String sectionName = entry.getKey();
            List<Node> nodesToSave = entry.getValue();
            if (!nodesToSave.isEmpty()) {
                // 创建一个新的文档
                Document sectionDoc = new Document();
                Section section = sectionDoc.getFirstSection();

                // 将临时保存的节点添加到新文档
                for (Node node : nodesToSave) {
                    section.getBody().appendChild(sectionDoc.importNode(node, true));
                }

                // 保存文档

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                sectionDoc.save(baos, SaveFormat.DOCX);
                File tempFile = new File(sectionName + ".docx");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    baos.writeTo(fos);
                }
            }
        }

        System.out.println("文档拆分完成！");
    }
}
