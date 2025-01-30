package com.chenjun.fivebook;

import com.aspose.words.Shape;
import com.aspose.words.*;
import com.chenjun.constant.Constants;
import com.chenjun.constant.WordToXmlConstant;
import com.chenjun.enums.IpBusinessUploadIdFileTypeEnum;
import com.chenjun.exception.CustomException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 说明书 docx转xml
 */
@Component
public class Manual {

    //    private int imageCount = 0;
    /**
     * 使用 AtomicInteger 代替 int 原子性操作 线程安全
     * <p>计数器+1 使用imageCount.incrementAndGet();</wangle>
     */
    private AtomicInteger imageCount = new AtomicInteger(0);

    public void manualToXml(File docxFile) throws Exception {
        //<p>每次调用前需重置计数器</wangle>
        imageCount.set(0); // 重置图片计数器
        // 加载 DOCX 文件
        Document doc = new Document(docxFile.getAbsolutePath());
        doc.updateListLabels();
        // 初始化变量
        String inventionTitle = ""; // 默认标题
        List<String> sectionOrder = new ArrayList<>();
        Map<String, List<Map<String, String>>> sections = new LinkedHashMap<>();

        // 用于跟踪副标题是否出现
        Map<String, Boolean> sectionFlags = new HashMap<>();
        sectionFlags.put(WordToXmlConstant.TECHNICAL_FIELD_TAG, false);
        sectionFlags.put(WordToXmlConstant.BACKGROUND_ART_TAG, false);
        sectionFlags.put(WordToXmlConstant.DISCLOSURE_TAG, false);
        sectionFlags.put(WordToXmlConstant.MODE_FOR_INVENTION_TAG, false);

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

            String numberText = "";
            if (paragraph.getListFormat().isListItem()) {
                ListLabel label = paragraph.getListLabel();
                numberText = label.getLabelString() + "";
            }

            boolean isBold = paragraph.getRuns().getCount() > 0 && paragraph.getRuns().get(0).getFont().getBold();
            boolean isCentered = paragraph.getParagraphFormat().getAlignment() == ParagraphAlignment.CENTER;

            // 处理大标题
            if (isBold && isCentered && currentSection == null) {
                inventionTitle = numberText + text;
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

                // 标记副标题为已出现
                if (currentSection != null) {
                    sectionFlags.put(currentSection, true);
                }

                headingCount++; // 增加二级标题计数器
                Map<String, String> headingElement = new HashMap<>();
                headingElement.put(WordToXmlConstant.TYPE, WordToXmlConstant.HEADING);
                headingElement.put(WordToXmlConstant.ID, WordToXmlConstant.HEADING_ID_HEAD + String.format("%04d", headingCount)); // 二级标题 ID
                headingElement.put(WordToXmlConstant.TEXT, numberText + text);
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

                tempParagraphs.add(numberText + paragraphXml.toString()); // 临时保存当前段落内容
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

        // 检查是否所有副标题都已出现
        for (Map.Entry<String, Boolean> entry : sectionFlags.entrySet()) {
            if (!entry.getValue()) {
                switch (entry.getKey()) {
                    case WordToXmlConstant.TECHNICAL_FIELD_TAG:
                        throw new CustomException("未找到技术领域或附图说明部分。 请检查说明书稿件是否合规！");
                    case WordToXmlConstant.BACKGROUND_ART_TAG:
                        throw new CustomException("未找到背景技术或附图说明部分。 请检查说明书稿件是否合规！");
                    case WordToXmlConstant.DISCLOSURE_TAG:
                        throw new CustomException("未找到发明内容或附图说明部分。 请检查说明书稿件是否合规！");
                    case WordToXmlConstant.MODE_FOR_INVENTION_TAG:
                        throw new CustomException("未找到具体实施方式或附图说明部分。 请检查说明书稿件是否合规！");
                }
            }
        }

        // 使用 Velocity 渲染模板
        StringWriter writer = new StringWriter();
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("inventionTitle", inventionTitle);
        context.put("sections", sections);
        context.put("sectionOrder", sectionOrder);
        org.apache.velocity.app.Velocity.evaluate(context, writer, "template", WordsUtils.loadManualXmlTemplate());


        // 保存到文件
        WordsUtils.saveXmlFile(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode(), WordToXmlConstant.MANUAL, writer.toString());
    }

    // 处理 Run（文字和格式）
    private String processRun(Run run) {
        if (run.getFont().getSuperscript()) {
            return WordToXmlConstant.LEFT_SUP + run.getText() + WordToXmlConstant.RIGHT_SUP;
        } else if (run.getFont().getSubscript()) {
            return WordToXmlConstant.LEFT_SUB + run.getText() + WordToXmlConstant.RIGHT_SUB;
        } else {
            return run.getText();
        }
    }

    // 处理 Shape（图片）
    private String processShape(Shape shape) throws Exception {
//        imageCount++;
        imageCount.incrementAndGet();
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
            double width = shape.getWidth();
            double height = shape.getHeight();

            WordsUtils.WiAndHe wiAndHe = WordsUtils.getWiAndHe(width, height);
            String wi = wiAndHe.getWi();
            String he = wiAndHe.getHe();

            int dotIndex = imageFileName.lastIndexOf(".");
            String suffixName = imageFileName.substring(dotIndex + 1).toLowerCase();

            String imageId = WordToXmlConstant.PIC_ID_HEAD + imageCount;
            // 构造 <img> 标签
            return String.format(
                    "<img id=\"%s\" file=\"%s\" img-format=\"%s\" inline=\"yes\" wi=\"%s\" he=\"%s\" />",
                    imageId, imageFileName, suffixName, wi, he
            );
        }
        return "";
    }

    // 处理公式
    private String processMath(OfficeMath officeMath) throws Exception {
//        imageCount++;
        imageCount.incrementAndGet();
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

        // 调用 WordUtils 的 getWiAndHe 方法计算经过缩放的宽高
        WordsUtils.WiAndHe wiAndHe = WordsUtils.getWiAndHe(formulaWidth, formulaHeight);
        String he = wiAndHe.getHe();
        String wi = wiAndHe.getWi();

        int dotIndex = imageFileName.lastIndexOf(".");
        String suffixName = imageFileName.substring(dotIndex + 1).toLowerCase();

        String imageId = WordToXmlConstant.PIC_ID_HEAD + imageCount;

        // 返回图片信息的 XML 标签
        return String.format(
                "<img id=\"%s\" file=\"%s\" img-format=\"%s\" inline=\"yes\" wi=\"%s\" he=\"%s\" />",
                imageId, imageFileName, suffixName, wi, he
        );
    }
}
