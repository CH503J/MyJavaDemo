package com.chenjun.utils.converter;

import com.my.common.constant.Constants;
import com.my.common.constant.WordToXmlConstant;
import com.my.common.enums.IpBusinessUploadIdFileTypeEnum;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 说明书附图 docx转xml
 */
public class ManualDrawingsToXml {

    /**
     * 将说明书附图转换为xml
     *
     * @param docxFile 说明书附图的docx格式文件
     * @throws Exception 读取文件或转换异常
     */
    public static void manualDrawingsToXml(File docxFile) throws Exception {
        // 读取 DOCX 文件
        Document doc = new Document(docxFile.getAbsolutePath());

        // 图片信息列表
        List<Map<String, String>> images = new ArrayList<>();
        int imageCount = 1;

        // 获取文档中的所有段落
        NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);

        String filePath = Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getCode();

        // 遍历段落中的图片
        for (int i = 0; i < paragraphs.getCount(); i++) {
            Paragraph paragraph = (Paragraph) paragraphs.get(i);
            for (Object node : paragraph.getChildNodes()) {
                if (node instanceof Shape) {
                    Shape shape = (Shape) node;
                    if (shape.getShapeType() == ShapeType.IMAGE) {
                        File fileFolder = new File(filePath);
                        if (!fileFolder.exists()) {
                            fileFolder.mkdirs();
                        }

                        // 提取图片信息
                        String imageFileName = WordToXmlConstant.PIC_DRAWINGS_PREFIX + imageCount + ".jpg"; // 只保存文件名
                        // 将图片保存到与XML文件同级目录
                        shape.getImageData().save(filePath + File.separator + imageFileName);

                        // 获取图片下方的文本作为 figure-labels
                        String figureLabel = getFigureLabel(paragraph);

                        Map<String, String> imageData = new HashMap<>();
                        imageData.put(WordToXmlConstant.ID, String.format("%04d", imageCount));
                        imageData.put(WordToXmlConstant.NUM, String.format("%04d", imageCount));
                        imageData.put(WordToXmlConstant.LABELS, figureLabel);  // 从文本中读取的 label
                        imageData.put(WordToXmlConstant.FILE_NAME, imageFileName);  // 只写文件名
                        imageData.put(WordToXmlConstant.FORMAT, "jpg");  // 图片格式
                        imageData.put(WordToXmlConstant.WIDTH, String.format("%06.2f", shape.getWidth()));
                        imageData.put(WordToXmlConstant.HEIGHT, String.format("%06.2f", shape.getHeight()));
                        images.add(imageData);
                        imageCount++;
                    }
                }
            }
        }

        // Velocity 模板字符串
        String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"/dtdandxsl/showxml.xsl\"?>\n" +
                "<!DOCTYPE cn-application-body SYSTEM \"/dtdandxsl/cn-application-body-20080416.dtd\">\n" +
                "<cn-application-body lang=\"zh\" country=\"CN\">\n" +
                "    <cn-drawings>\n" +
                "        #foreach($image in $images)\n" +
                "        <figure id=\"f${image.id}\" num=\"${image.num}\" figure-labels=\"${image.label}\">\n" +
                "            <img id=\"idf_${image.id}\" file=\"${image.file}\" img-format=\"${image.format}\" inline=\"yes\" wi=\"${image.width}\" he=\"${image.height}\"/>\n" +
                "        </figure>\n" +
                "        #end\n" +
                "    </cn-drawings>\n" +
                "</cn-application-body>";

        // 创建 Velocity 引擎和上下文
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        VelocityContext context = new VelocityContext();
        context.put("images", images);

        // 使用模板生成 XML 内容
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "DocxToXml", template);

        saveXmlFile(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getCode(),
                WordToXmlConstant.MANUAL_DRAWINGS, writer.toString());
    }

    // 获取图片下方的文本内容作为 figure-labels
    private static String getFigureLabel(Paragraph paragraph) {
        // 获取图片所在段落之后的下一个段落
        Node nextNode = paragraph.getNextSibling();
        if (nextNode instanceof Paragraph) {
            Paragraph nextParagraph = (Paragraph) nextNode;
            // 只返回段落中的文本
            return nextParagraph.getRange().getText().trim();
        }
        return "";  // 如果没有文本段落，返回空字符串
    }

    /**
     * 保存xml文件到指定路径
     */
    private static void saveXmlFile(String folderPath, String fileName, String xmlContent) throws IOException {
        // 创建输出文件路径
        String outputFilePath = folderPath + File.separator + fileName;
        File outputFile = new File(folderPath);

        // 确保文件夹存在
        if (!outputFile.exists()) {
            outputFile.mkdirs();
        }

        // 写入文件
        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            fileWriter.write(xmlContent);
        }

        System.out.println("XML 文件已生成并保存到：" + outputFilePath);
    }
}
