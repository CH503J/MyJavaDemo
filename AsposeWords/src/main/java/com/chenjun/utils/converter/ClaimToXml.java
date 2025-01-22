package com.chenjun.utils.converter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.*;

/**
 * 权利要求书 docx转xml
 */
public class ClaimToXml {

    // 缓存公式与公式图片之间的映射
    private static Map<String, String> formulaImageMap = new HashMap<>();

    // 图片序号计数器
    private static int imageCounter = 1;

    public static void claimToXml(File docxFile) throws Exception {
        // 读取 DOCX 文件并提取有效段落
        Document doc = new Document(docxFile.getAbsolutePath());
        List<Paragraph> paragraphs = getNonEmptyParagraphs(doc);

        // 初始化 Velocity 模板引擎
        VelocityEngine velocity = initializeVelocityEngine();

        // 创建上下文并生成 claims 列表
        VelocityContext context = createVelocityContext(paragraphs, doc);

        // 生成 XML 内容并保存到文件
        String xmlContent = generateXmlContent(context, velocity);
        saveXmlFile(Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getCode(), WordToXmlConstant.CLAIM, xmlContent);
    }

    // 获取文档中非空的段落
    private static List<Paragraph> getNonEmptyParagraphs(Document doc) {
        List<Paragraph> paragraphs = new ArrayList<>();
        for (Paragraph paragraph : doc.getFirstSection().getBody().getParagraphs()) {
            if (!paragraph.getText().trim().isEmpty()) {
                paragraphs.add(paragraph);
            }
        }
        return paragraphs;
    }

    // 初始化 Velocity 引擎
    private static VelocityEngine initializeVelocityEngine() {
        Properties props = new Properties();
        props.setProperty("file.resource.loader.path", "src/main/resources"); // 资源路径
        VelocityEngine velocity = new VelocityEngine(props);
        velocity.init();
        return velocity;
    }

    // 创建 Velocity 上下文并生成 claims 列表
    private static VelocityContext createVelocityContext(List<Paragraph> paragraphs, Document doc) throws Exception {
        VelocityContext context = new VelocityContext();
        context.put(WordToXmlConstant.CLAIM_TAG, generateClaims(paragraphs, doc, Constants.FIVE_RESOURCE_TMP_PATH + IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getCode()));
        return context;
    }

    // 生成 XML 内容
    private static String generateXmlContent(VelocityContext context, VelocityEngine velocity) {
        String template = loadXmlTemplate();
        StringWriter writer = new StringWriter();
        velocity.evaluate(context, writer, "template", template);
        return writer.toString();
    }

    // 加载 XML 模板
    private static String loadXmlTemplate() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"/dtdandxsl/showxml.xsl\"?>\n" +
                "<!DOCTYPE cn-application-body SYSTEM \"/dtdandxsl/cn-application-body-20080416.dtd\">\n" +
                "<cn-application-body lang=\"zh\" country=\"CN\">\n" +
                "    <cn-claims>\n" +
                "        #foreach($claim in $claims)\n" +
                "        <claim id=\"$claim.id\" num=\"$claim.num\">\n" +
                "            #foreach($text in $claim.texts)\n" +
                "            #if($text.startsWith(\"<img\"))\n" +
                "            <claim-text>$text</claim-text>\n" +
                "            #else\n" +
                "            <claim-text>$text</claim-text>\n" +
                "            #end\n" +
                "            #end\n" +
                "        </claim>\n" +
                "        #end\n" +
                "    </cn-claims>\n" +
                "</cn-application-body>";
    }

    // 生成 claims 列表
    private static List<Claim> generateClaims(List<Paragraph> paragraphs, Document doc, String outputDir) throws Exception {
        List<Claim> claims = new ArrayList<>();
        int idCounter = 1;  // 用于公式图片的计数
        int claimCounter = 1;  // 用于 claim 的计数
        Claim currentClaim = new Claim();
        Set<OfficeMath> processedFormulas = new HashSet<>(); // 用于记录已处理的公式
        Map<String, String> formulaToImgTagMap = new HashMap<>(); // 公式文本到 img 标签的映射

        // 遍历每个段落
        for (Paragraph paragraph : paragraphs) {

            // 获取段落文本
            String cleanedParagraph = removeNumberingFromText(paragraph.getText());

            if (cleanedParagraph.equals(IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getDesc())) {
                continue;
            }

            // 在这里插入处理上下标的部分
            cleanedParagraph = handleSupSub(cleanedParagraph, paragraph);

            // 遍历段落中的每个 OfficeMath 公式节点
            NodeCollection mathNodes = doc.getChildNodes(NodeType.OFFICE_MATH, true); // 获取所有公式节点
            for (Object node : mathNodes) {
                if (node instanceof OfficeMath) {
                    OfficeMath math = (OfficeMath) node;

                    // 如果该公式已经处理过，跳过
                    if (processedFormulas.contains(math)) {
                        continue;
                    }

                    // 判断公式是否完整，避免处理子公式
                    if (isCompleteFormula(math)) {
                        // 提取公式并转换为图片
                        String formulaImage = formulaToImg(math, outputDir);

                        // 获取公式的文本内容并将公式与图片对应
                        String formulaText = math.toString(SaveFormat.TEXT);  // 提取公式文本

                        // 生成完整的 img 标签
                        String imgTag = generateImgTag(formulaImage, idCounter);

                        // 将公式文本与 img 标签对应
                        formulaToImgTagMap.put(formulaText, imgTag);

                        // 标记公式为已处理
                        processedFormulas.add(math);

                        // 增加 img 标签 ID 计数器
                        idCounter++;
                    }
                }
            }

            // 替换公式文本为图片标签
            for (Map.Entry<String, String> entry : formulaToImgTagMap.entrySet()) {
                String formulaText = entry.getKey();
                String imgTag = entry.getValue();

                // 将公式文本替换为 <img> 标签
                if (cleanedParagraph.contains(formulaText)) {
                    cleanedParagraph = cleanedParagraph.replace(formulaText, imgTag);
                }
            }

            // 将带有公式和上下标的段落文本合并，并加入当前的 claim
            currentClaim.addText(cleanedParagraph);

            // 如果段落末尾有句号，认为当前的 claim 结束
            if (cleanedParagraph.endsWith("。")) {
                // 设置 claim 的 id 和 num
                currentClaim.setId(WordToXmlConstant.CLAIM_ID_HEAD + String.format("%03d", claimCounter));
                currentClaim.setNum(claimCounter);
                claims.add(currentClaim);
                currentClaim = new Claim();
                claimCounter++; // 增加 claimCounter
            }
        }

        // 如果最后一个 claim 还有内容，添加它
        if (!currentClaim.getTexts().isEmpty()) {
            currentClaim.setId(WordToXmlConstant.CLAIM_ID_HEAD + String.format("%03d", claimCounter));
            currentClaim.setNum(claimCounter);
            claims.add(currentClaim);
        }

        return claims;
    }

    // 处理上下标
    private static String handleSupSub(String text, Paragraph paragraph) {
        // 使用 StringBuilder 来构建文本
        StringBuilder modifiedText = new StringBuilder(text);

        // 遍历段落中的 Run，检查是否有上下标
        for (Run run : paragraph.getRuns()) {
            String runText = run.getText();
            // 检查该 Run 是否为上标
            if (run.getFont().getSuperscript()) {
                String supTag = WordToXmlConstant.LEFT_SUP + runText + WordToXmlConstant.RIGHT_SUP;
                // 将 Run 中的上标文本替换为 <sup> 标签
                modifiedText.replace(modifiedText.indexOf(runText), modifiedText.indexOf(runText) + runText.length(), supTag);
            }
            // 检查该 Run 是否为下标
            if (run.getFont().getSubscript()) {
                String subTag = WordToXmlConstant.LEFT_SUB + runText + WordToXmlConstant.RIGHT_SUB;
                // 将 Run 中的下标文本替换为 <sub> 标签
                modifiedText.replace(modifiedText.indexOf(runText), modifiedText.indexOf(runText) + runText.length(), subTag);
            }
        }

        // 返回处理后的文本
        return modifiedText.toString();
    }

    // 生成 img 标签
    private static String generateImgTag(String formulaImageName, int idCounter) {
        // 获取图片的路径
        String formulaImagePath = formulaImageMap.get(formulaImageName);

        if (formulaImagePath == null) {
            throw new IllegalArgumentException("图片路径未找到: " + formulaImageName);
        }

        String fileName = formulaImageName;  // 仅使用文件名，而不是完整路径
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);  // 获取文件扩展名
        String imgId = WordToXmlConstant.PIC_ID_HEAD + idCounter;  // 生成 img id，例如 idf_0001

        // 获取图片的宽度和高度（像素）
        int width = 0;
        int height = 0;
        try (ImageInputStream iis = ImageIO.createImageInputStream(new File(formulaImagePath))) {
            ImageReader reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis, true);
            width = reader.getWidth(0);  // 获取图片的宽度
            height = reader.getHeight(0);  // 获取图片的高度
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 生成 img 标签，使用真实的宽度和高度（像素）
        return String.format("<img id=\"%s\" file=\"%s\" img-format=\"%s\" inline=\"yes\" wi=\"%d\" he=\"%d\"/>",
                imgId, fileName, fileExtension, width, height);
    }

    // 判断公式是否完整
    private static boolean isCompleteFormula(OfficeMath officeMath) {
        // 判断公式是否是一个完整的公式
        return officeMath.getParentNode() instanceof Paragraph;
    }

    // 保存 XML 内容formulaToImg
    private static String formulaToImg(OfficeMath formula, String outputDir) throws Exception {

        // 使用公式渲染器将公式转换为图片
        BufferedImage formulaImage = renderFormulaToImage(formula);

        // 确保图像为 RGB 模式，避免 JPEG 不支持透明通道的问题
        if (formulaImage.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgbImage = new BufferedImage(formulaImage.getWidth(), formulaImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(formulaImage, 0, 0, null);
            g.dispose();
            formulaImage = rgbImage;  // 使用新的 RGB 图像
        }

        // 生成公式图片的文件名，使用 sms_加上序号
        String imageName = WordToXmlConstant.PIC_PREFIX + imageCounter++ + ".jpg";  // 图片序号递增
        String imagePath = outputDir + File.separator + imageName;

        // 确保目录存在，如果不存在则创建目录
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();  // 创建目录
        }

        // 保存图片为 JPG 格式
        ImageIO.write(formulaImage, "JPEG", new File(imagePath));

        // 将文件名和路径映射存储在 Map 中
        formulaImageMap.put(imageName, imagePath);

        System.out.println("公式已保存为图片: " + imagePath);

        return imageName;  // 返回文件名
    }

    // 删除权利要求书中的序号
    private static String removeNumberingFromText(String text) {
        // 匹配以数字开头的序号，如 "1."、"2."、"10." 等，并去除
        return text.replaceAll("^[0-9]+[\\.]\\s*", "").trim();
    }

    // 将公式转换为图片
    private static BufferedImage renderFormulaToImage(OfficeMath officeMath) throws Exception {
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

        return bufferedImage;
    }

    // Claim 类用于存储单个 claim 的数据
    @Getter
    public static class Claim {

        private String id;
        private int num;
        private List<String> texts = new ArrayList<>();

        public void setId(String id) {
            this.id = id;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public void setTexts(List<String> texts) {
            this.texts = texts;
        }

        // 添加 claim-text
        public void addText(String text) {
            this.texts.add(text);
        }
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
