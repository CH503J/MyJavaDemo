package com.chenjun.fivebook;

import com.aspose.words.*;
import com.chenjun.constant.Constants;
import com.chenjun.constant.WordToXmlConstant;
import com.chenjun.enums.IpBusinessUploadIdFileTypeEnum;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.StringWriter;

/**
 * 说明书摘要 docx转xml
 */
@Component
public class ManualSummary {

    /**
     * 将说明书摘要转换为xml
     *
     * @param file 说明书摘要的docx格式文件
     * @throws Exception 读取文件或转换异常
     */
    public void manualSummaryToXml(File file) throws Exception {
        // 读取Word文件
        Document doc = new Document(file.getAbsolutePath());

        // 提取段落
        NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);

        // 构建摘要内容
        StringBuilder summaryText = new StringBuilder();
        int validParagraphIndex = 1;  // 有效段落计数器

        for (int i = 0; i < paragraphs.getCount(); i++) {
            Paragraph para = (Paragraph) paragraphs.get(i);

            // 判断段落是否为加粗标题
            boolean isBoldTitle = false;
            for (Run run : para.getRuns()) {
                if (run.getFont().getBold()) {
                    isBoldTitle = true;
                    break;  // 一旦发现加粗文本，就跳出循环
                }
            }

            // 如果该段落是加粗标题，跳过该段落
            if (isBoldTitle) {
                continue;
            }

            // 如果段落内容不是空的，构建XML段落
            String paraText = para.toString(SaveFormat.TEXT).trim();
            if (!paraText.isEmpty()) {
                // 处理多余的换行符或空格
                paraText = paraText.replaceAll("\\n+", "\n").trim(); // 去除多余换行符
                summaryText.append("<p id=\"p_zy_").append(String.format("%04d", validParagraphIndex))
                        .append("\" Italic=\"0\" num=\"").append(String.format("%04d", validParagraphIndex))
                        .append("\">").append(paraText).append("</p>\n");
                validParagraphIndex++;  // 增加有效段落计数器
            }
        }

        // Velocity模板引擎
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();


        // VelocityContext
        VelocityContext context = new VelocityContext();
        context.put("lang", "zh");
        context.put("country", "CN");
        context.put("summary", summaryText.toString());

        // 使用StringWriter来渲染模板字符串
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "template", WordsUtils.loadManualSummaryXmlTemplate());

        WordsUtils.saveXmlFile(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.ABSTRACT.getCode(), WordToXmlConstant.MANUAL_SUMMARY, writer.toString());
    }
}
