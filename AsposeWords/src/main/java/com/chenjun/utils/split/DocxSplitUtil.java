package com.chenjun.utils.split;

import com.my.common.enums.IpBusinessUploadIdFileTypeEnum;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
public class DocxSplitUtil {

    /**
     * 根据标题拆分 DOCX 文档并返回拆分后的文档 Map
     *
     * @param inputDocxFile 输入的 DOCX 文件
     * @return 返回拆分后的文档 Map，键为文件名，值为文件对象
     */
    public static Map<String, File> splitDocx(File inputDocxFile) {
        Map<String, File> docMap = new HashMap<>();

        try {
            // 加载 DOCX 文档
            Document doc = new Document(inputDocxFile.getAbsolutePath());

            // 删除页眉和页脚
            removeHeadersFooters(doc);

            // 获取所有段落
            NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);

            // 用于记录当前文档的开始段落索引
            int startParagraphIndex = 0;

            // 用于记录当前的文档名称（标题）
            String currentTitle = null;

            for (int i = 0; i < paragraphs.getCount(); i++) {
                Paragraph para = (Paragraph) paragraphs.get(i);

                // 使用大纲级别检测标题，而不是样式名称
                if (para.getParagraphFormat().getOutlineLevel() != OutlineLevel.BODY_TEXT) {
                    // 获取标题文本
                    String title = para.getText().trim();

                    // 如果有前一个标题，保存到新文件
                    if (currentTitle != null) {
                        String fileName = getFileName(currentTitle);
                        File docFile = splitAndSaveDocument(doc, startParagraphIndex, i - 1, currentTitle);
                        docMap.put(fileName, docFile);
                    }

                    // 更新当前的标题和起始段落索引
                    currentTitle = title.replaceAll("[\\\\/:*?\"<>|]", ""); // 去除文件名中的非法字符
                    startParagraphIndex = i;
                }
            }

            // 处理最后一个标题部分
            if (currentTitle != null) {
                String fileName = getFileName(currentTitle);
                File docFile = splitAndSaveDocument(doc, startParagraphIndex, paragraphs.getCount() - 1, currentTitle);
                docMap.put(fileName, docFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return docMap;
    }

    /**
     * 拆分文档并返回文件对象
     *
     * @param originalDoc       原始文档
     * @param startParagraphIdx 起始段落索引
     * @param endParagraphIdx   结束段落索引
     * @param title             文件标题
     * @return 返回拆分后的文档文件对象
     */
    private static File splitAndSaveDocument(
            Document originalDoc,
            int startParagraphIdx,
            int endParagraphIdx,
            String title) {
        File tempFile = null;

        try {
            // 创建一个新的空文档
            Document newDoc = new Document();
            DocumentBuilder builder = new DocumentBuilder(newDoc);

            // 设置纸张大小为A4
            PageSetup pageSetup = newDoc.getFirstSection().getPageSetup();
            pageSetup.setPaperSize(PaperSize.A4);

            // 设置页边距
            pageSetup.setTopMargin(2.5 * 72 / 2.54);
            pageSetup.setBottomMargin(1.5 * 72 / 2.54);
            pageSetup.setLeftMargin(2.5 * 72 / 2.54);
            pageSetup.setRightMargin(1.67 * 72 / 2.54);

            // 使用 NodeImporter 来导入段落
            NodeImporter importer = new NodeImporter(originalDoc, newDoc, ImportFormatMode.KEEP_SOURCE_FORMATTING);

            // 复制指定段落范围的内容
            for (int i = startParagraphIdx; i <= endParagraphIdx; i++) {
                Paragraph para = (Paragraph) originalDoc.getChildNodes(NodeType.PARAGRAPH, true).get(i);
                removePageBreaks(para);
                Node importedNode = importer.importNode(para, true);
                newDoc.getFirstSection().getBody().appendChild(importedNode);
            }

            // 检测页数，如果有多页则添加页脚
            LayoutCollector layoutCollector = new LayoutCollector(newDoc);
            int pageCount = layoutCollector.getEndPageIndex(newDoc.getLastSection().getBody().getLastParagraph());
            if (pageCount > 1) {
                addCenteredFooter(newDoc);
            }

            // 创建临时文件保存文档
            String fileName = getFileName(title);
            tempFile = new File(fileName + ".docx");
            tempFile.deleteOnExit(); // 程序退出时自动删除临时文件

            // 保存新文档为 DOCX 到临时文件
            newDoc.save(tempFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempFile;
    }

    /**
     * @param title 文件标题
     * @return 文件名对应的代码
     */
    private static String getFileName(String title) {
        //权利要求书
        if (title.equals(IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getDesc())) {
            return IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getCode().toString();
        }
        // 说明书
        else if (title.equals(IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getDesc())) {
            return IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode().toString();
        }
        // 说明书附图
        else if (title.equals(IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getDesc())) {
            return IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getCode().toString();
        }
        // 说明书摘要
        else if (title.equals(IpBusinessUploadIdFileTypeEnum.ABSTRACT.getDesc())) {
            return IpBusinessUploadIdFileTypeEnum.ABSTRACT.getCode().toString();
        }
        return "";
    }

    /**
     * 删除源DOCX文件的页眉和页脚
     *
     * @param doc 原始文档
     */
    private static void removeHeadersFooters(Document doc) {
        for (Section section : doc.getSections()) {
            // 获取文档中页眉
            HeaderFooter header = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_PRIMARY);
            // 获取文档中页脚
            HeaderFooter footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_PRIMARY);
            // 如果有页眉页脚则删除
            if (header != null) {
                header.remove();
            }
            if (footer != null) {
                footer.remove();
            }
        }
    }

    /**
     * 删除分页符
     *
     * @param para 段落
     */
    private static void removePageBreaks(Paragraph para) {
        // 检查段落是否设置了“段前分页”
        if (para.getParagraphFormat().getPageBreakBefore()) {
            // 如果存在“段前分页”，将其设置为 false，移除分页符
            para.getParagraphFormat().setPageBreakBefore(false);
        }

        // 遍历段落中的所有运行（Run）对象
        for (Run run : para.getRuns()) {
            // 检查当前运行中的文本是否包含分页符
            if (run.getText().contains(ControlChar.PAGE_BREAK)) {
                try {
                    // 如果包含分页符，则替换为一个空字符串，移除分页符
                    run.setText(run.getText().replace(ControlChar.PAGE_BREAK, ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 描述：在生成的DOCX文件中添加页脚
     *
     * @param doc 生成的DOCX文档
     */
    private static void addCenteredFooter(Document doc) {
        try {
            // 遍历文档的所有部分
            for (Section section : doc.getSections()) {
                // 创建新的页脚对象，页脚类型为主页脚
                HeaderFooter footer = new HeaderFooter(doc, HeaderFooterType.FOOTER_PRIMARY);
                // 将页脚添加到当前部分的页眉和页脚集合中
                section.getHeadersFooters().add(footer);

                // 创建新的段落，用于在页脚中显示
                Paragraph footerParagraph = new Paragraph(doc);
                // 设置段落居中对齐
                footerParagraph.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
                // 在段落中添加页码域，显示当前页码
                footerParagraph.appendField(FieldType.FIELD_PAGE, true);

                // 设置页码字体和大小
                Run pageRun = new Run(doc);
                // 设置字体为 Arial
                pageRun.getFont().setName("Arial");
                // 设置字号为 9
                pageRun.getFont().setSize(9);

                // 将 Run 对象添加到段落中
                footerParagraph.appendChild(pageRun);
                // 将段落添加到页脚中
                footer.appendChild(footerParagraph);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
