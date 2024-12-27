package com.chenjun.utils.converter;

import com.my.common.constant.Constants;
import com.my.common.constant.WordToXmlConstant;
import com.my.common.enums.IpBusinessUploadIdFileTypeEnum;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

/**
 * 说明书 docx转xml
 */
public class ManualToXml {

    private static int imageCount = 0;

    public static void manualToXml(File docxFile) throws Exception {
        // 加载 DOCX 文件
        Document doc = new Document(docxFile.getAbsolutePath());

        // 初始化变量
        String inventionTitle = ""; // 默认标题
        List<String> sectionOrder = new ArrayList<>();
        Map<String, List<Map<String, String>>> sections = new LinkedHashMap<>();

        String currentSection = null;
        int titleCount = 0; // 大标题计数器
        int headingCount = 0; // 二级标题计数器
        int paragraphIdCount = 0; // 用于生成段落 id
        int paragraphNumCount = 1; // 用于生成段落 num

        // 临时变量用于合并段落
        List<String> tempParagraphs = new ArrayList<>(); // 临时收集段落
        boolean isItalic = false; // 是否有斜体
        StringBuilder tempParagraphXml = new StringBuilder();

        // 遍历段落
        for (Paragraph paragraph : doc.getSections().get(0).getBody().getParagraphs()) {
            String text = paragraph.getText().trim();
            if (text.isEmpty()) continue; // 跳过空段落

            boolean isBold = paragraph.getRuns().getCount() > 0 && paragraph.getRuns().get(0).getFont().getBold();
            boolean isCentered = paragraph.getParagraphFormat().getAlignment() == ParagraphAlignment.CENTER;

            // 处理大标题
            if (isBold && isCentered && currentSection == null) {
                inventionTitle = text;
                titleCount++;
                continue;
            }

            // 处理章节标题（二级标题）
            if (isBold) {
                //技术领域
                if (text.contains(WordToXmlConstant.TECHNICAL_FIELD)) {
                    currentSection = WordToXmlConstant.TECHNICAL_FIELD_TAG;
                }
                //背景技术
                else if (text.contains(WordToXmlConstant.BACKGROUND_ART)) {
                    currentSection = WordToXmlConstant.BACKGROUND_ART_TAG;
                }
                //发明内容
                else if (text.contains(WordToXmlConstant.DISCLOSURE)) {
                    currentSection = WordToXmlConstant.DISCLOSURE_TAG;
                }
                //附图说明
                else if (text.contains(WordToXmlConstant.DESCRIPTION_OF_DRAWINGS)) {
                    currentSection = WordToXmlConstant.DESCRIPTION_OF_DRAWINGS_TAG;
                }
                //具体实施方式
                else if (text.contains(WordToXmlConstant.MODE_FOR_INVENTION)) {
                    currentSection = WordToXmlConstant.MODE_FOR_INVENTION_TAG;
                }

                if (currentSection != null && !sectionOrder.contains(currentSection)) {
                    sectionOrder.add(currentSection);
                    sections.put(currentSection, new ArrayList<>());
                }

                headingCount++; // 增加二级标题计数器
                Map<String, String> headingElement = new HashMap<>();
                headingElement.put(WordToXmlConstant.TYPE, WordToXmlConstant.HEADING);
                headingElement.put(WordToXmlConstant.ID, WordToXmlConstant.HEADING_ID_HEAD + String.format("%04d", headingCount)); // 二级标题 ID
                headingElement.put(WordToXmlConstant.TEXT, text);
                sections.get(currentSection).add(headingElement);
                continue;
            }

            // 处理正文段落
            if (currentSection != null) {
                StringBuilder paragraphXml = new StringBuilder();
                for (Node node : (Iterable<Node>) paragraph.getChildNodes()) {
                    if (node.getNodeType() == NodeType.RUN) {
                        // 处理文字和格式
                        paragraphXml.append(processRun((Run) node));
                    } else if (node.getNodeType() == NodeType.SHAPE) {
                        // 处理图片
                        Shape shape = (Shape) node;
                        paragraphXml.append(processShape(shape));
                    } else if (node.getNodeType() == NodeType.OFFICE_MATH) {
                        // 处理公式
                        paragraphXml.append(processMath((OfficeMath) node));
                    }
                }

                tempParagraphs.add(paragraphXml.toString()); // 临时保存当前段落内容
                if (text.endsWith("。")) { // 检测段落末尾是否是句号

                    for (int i = 0; i < tempParagraphs.size(); i++) {
                        Map<String, String> paragraphElement = new HashMap<>();
                        paragraphElement.put(WordToXmlConstant.TYPE, WordToXmlConstant.P);
                        paragraphElement.put(WordToXmlConstant.ID, WordToXmlConstant.P + String.format("%04d", titleCount + headingCount + paragraphIdCount++)); // 增加段落 id 计数器
                        paragraphElement.put(WordToXmlConstant.NUM, (i == 0) ? String.format("%04d", paragraphNumCount++) : WordToXmlConstant.XXXX);
                        paragraphElement.put(WordToXmlConstant.ITALIC, isItalic ? "1" : "0"); // 斜体属性
                        paragraphElement.put(WordToXmlConstant.TEXT, tempParagraphs.get(i));
                        sections.get(currentSection).add(paragraphElement);
                    }

                    tempParagraphs.clear(); // 清空临时段落集合
                    tempParagraphXml.setLength(0); // 重置临时段落内容
                    isItalic = false; // 重置斜体标志
                }
            }
        }

        // 使用 Velocity 渲染模板
        StringWriter writer = new StringWriter();
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("inventionTitle", inventionTitle);
        context.put("sections", sections);
        context.put("sectionOrder", sectionOrder);
        org.apache.velocity.app.Velocity.evaluate(context, writer, "template", loadTemplate());

        // 保存到文件
        saveXmlFile(WordToXmlConstant.MANUAL, writer.toString());
    }

    // 处理 Run（文字和格式）
    private static String processRun(Run run) {
        if (run.getFont().getSuperscript()) {
            return WordToXmlConstant.LEFT_SUP + run.getText() + WordToXmlConstant.RIGHT_SUP;
        } else if (run.getFont().getSubscript()) {
            return WordToXmlConstant.LEFT_SUB + run.getText() + WordToXmlConstant.RIGHT_SUB;
        } else {
            return run.getText();
        }
    }

    // 处理 Shape（图片）
    private static String processShape(Shape shape) throws Exception {
        imageCount++;
        if (shape.hasImage()) {
            // 获取图片的字节数组
            byte[] imageBytes = shape.getImageData().getImageBytes();

            // 根据图片计数器生成图片文件名
            String imageFileName = WordToXmlConstant.PIC_PREFIX + imageCount + ".jpg";

            File fileFolder = new File(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode());
            if (!fileFolder.exists()) {
                fileFolder.mkdirs();
            }
            File imageFile = new File(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode(), imageFileName);

            // 使用 FileOutputStream 写入字节数组并保存为 JPG 格式
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageBytes);
            }

            // 获取图片的宽度和高度（以 pt 为单位，需要转换为 px，1 pt ≈ 1.333 px）
            String width = String.format("%.2f", shape.getWidth());
            String height = String.format("%.2f", shape.getHeight());

            int dotIndex = imageFileName.lastIndexOf(".");
            String suffixName = imageFileName.substring(dotIndex + 1).toLowerCase();

            String imageId = WordToXmlConstant.PIC_ID_HEAD + imageCount;
            // 构造 <img> 标签
            return String.format(
                    "<img id=\"%s\" file=\"%s\" img-format=\"%s\" inline=\"yes\" wi=\"%s\" he=\"%s\" />",
                    imageId, imageFileName, suffixName, width, height
            );
        }
        return "";
    }

    // 处理公式
    private static String processMath(OfficeMath officeMath) throws Exception {
        imageCount++;
        OfficeMathRenderer renderer = officeMath.getMathRenderer();
        float widthInPoints = (float) renderer.getSizeInPoints().getX();
        float heightInPoints = (float) renderer.getSizeInPoints().getY();

        int width = (int) (widthInPoints * 1.5);
        int height = (int) (heightInPoints * 1.5);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, width, height);

        renderer.renderToScale(graphics2D, 0, 0, 1.0f);
        graphics2D.dispose();

        // 确保图像为 RGB 模式，避免 JPEG 不支持透明通道的问题
        if (bufferedImage.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgbImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(bufferedImage, 0, 0, null);
            g.dispose();
            bufferedImage = rgbImage;  // 使用新的 RGB 图像
        }

        // 创建图片文件名，使用计数器
        String imageFileName = WordToXmlConstant.PIC_PREFIX + imageCount + ".jpg";
        File imageFile = new File(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode(), imageFileName);

        File fileFolder = new File(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode());
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        // 将图片保存为 JPG 格式
        ImageIO.write(bufferedImage, "JPG", imageFile);

        // 获取图片的宽度和高度（像素）
        double formulaWidth = bufferedImage.getWidth();
        double formulaHeight = bufferedImage.getHeight();

        // 格式化宽高到两位小数
        String formattedWidth = String.format("%.2f", formulaWidth);
        String formattedHeight = String.format("%.2f", formulaHeight);

        int dotIndex = imageFileName.lastIndexOf(".");
        String suffixName = imageFileName.substring(dotIndex + 1).toLowerCase();

        String imageId = WordToXmlConstant.PIC_ID_HEAD + imageCount;

        // 返回图片信息的 XML 标签
        return String.format(
                "<img id=\"%s\" file=\"%s\" img-format=\"%s\" inline=\"yes\" wi=\"%s\" he=\"%s\" />",
                imageId, imageFileName, suffixName, formattedWidth, formattedHeight
        );
    }

    // 加载模板
    private static String loadTemplate() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"/dtdandxsl/showxml.xsl\"?>\n" +
                "<!DOCTYPE cn-application-body SYSTEM \"/dtdandxsl/cn-application-body-20080416.dtd\">\n" +
                "<cn-application-body lang=\"zh\" country=\"CN\">\n" +
                "    <description>\n" +
                "        <invention-title id=\"t0001\"><b>$inventionTitle</b></invention-title>\n" +
                "        #foreach($sectionName in $sectionOrder)\n" +
                "        #set($section = $sections.get($sectionName))\n" +
                "        <$sectionName>\n" +
                "            #foreach($element in $section)\n" +
                "            #if($element.type == \"heading\")\n" +
                "            <heading id=\"$element.id\" level=\"2\">$element.text</heading>\n" +
                "            #elseif($element.type == \"p\")\n" +
                "            <p id=\"$element.id\" num=\"$element.num\" Italic=\"$element.Italic\">$element.text</p>\n" +
                "            #end\n" +
                "            #end\n" +
                "        </$sectionName>\n" +
                "        #end\n" +
                "    </description>\n" +
                "</cn-application-body>\n";
    }

    // 保存 XML 文件
    private static void saveXmlFile(String content, String xmlContent) throws IOException {
        // 创建输出文件路径
        String outputFilePath = Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode() + File.separator + content;
        File outputFile = new File(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode());

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
