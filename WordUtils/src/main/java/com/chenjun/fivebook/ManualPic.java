package com.chenjun.fivebook;

import com.aspose.words.Shape;
import com.aspose.words.*;
import com.chenjun.constant.Constants;
import com.chenjun.constant.WordToXmlConstant;
import com.chenjun.enums.IpBusinessUploadIdFileTypeEnum;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 说明书附图 docx转xml
 */
@Component
public class ManualPic {

    /**
     * 将说明书附图转换为xml
     *
     * @param docxFile 说明书附图的docx格式文件
     * @throws Exception 读取文件或转换异常
     */
    public void manualDrawingsToXml(File docxFile) throws Exception {
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
                        String fullImagePath = filePath + File.separator + imageFileName;

                        // 获取图片数据
                        byte[] imageData = shape.getImageData().toByteArray();
                        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));

                        // 创建空白的 BufferedImage，确保能够绘制到上面
                        BufferedImage finalImage = new BufferedImage(
                                bufferedImage.getWidth(),
                                bufferedImage.getHeight(),
                                BufferedImage.TYPE_INT_RGB);

                        double width = bufferedImage.getWidth();
                        double height = bufferedImage.getHeight();

                        // 调用 WordUtils 的 getWiAndHe 方法计算经过缩放的宽高
                        WordsUtils.WiAndHe wiAndHe = WordsUtils.getWiAndHe(width, height);
                        String wi1 = wiAndHe.getWi();
                        String he1 = wiAndHe.getHe();

                        // 使用 Graphics2D 绘制到新的空白图像
                        Graphics2D g2d = finalImage.createGraphics();
                        g2d.drawImage(bufferedImage, 0, 0, null);
                        g2d.dispose();

                        // 保存重新绘制的图片
                        ImageIO.write(finalImage, "JPG", new File(fullImagePath));

                        // 获取图片下方的文本作为 figure-labels
                        String figureLabel = getFigureLabel(paragraph);

                        Map<String, String> imageDataMap = new HashMap<>();
                        imageDataMap.put(WordToXmlConstant.ID, String.format("%04d", imageCount));
                        imageDataMap.put(WordToXmlConstant.NUM, String.format("%04d", imageCount));
                        imageDataMap.put(WordToXmlConstant.LABEL, figureLabel);  // 从文本中读取的 label
                        imageDataMap.put(WordToXmlConstant.FILE_NAME, imageFileName);  // 只写文件名
                        imageDataMap.put(WordToXmlConstant.FORMAT, "jpg");  // 图片格式
                        imageDataMap.put(WordToXmlConstant.WIDTH, wi1);
                        imageDataMap.put(WordToXmlConstant.HEIGHT, he1);
                        images.add(imageDataMap);
                        imageCount++;
                    }
                }
            }
        }

        // 创建 Velocity 引擎和上下文
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        VelocityContext context = new VelocityContext();
        context.put("images", images);

        // 使用模板生成 XML 内容
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "DocxToXml", WordsUtils.loadManualDrawingsXmlTemplate());

        WordsUtils.saveXmlFile(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getCode(), WordToXmlConstant.MANUAL_DRAWINGS, writer.toString());
    }

    // 获取图片下方的文本内容作为 figure-labels
    private String getFigureLabel(Paragraph paragraph) {
        // 获取图片所在段落之后的下一个段落
        Node nextNode = paragraph.getNextSibling();
        if (nextNode instanceof Paragraph) {
            Paragraph nextParagraph = (Paragraph) nextNode;
            // 只返回段落中的文本
            return nextParagraph.getRange().getText().trim();
        }
        return "";  // 如果没有文本段落，返回空字符串
    }
}
