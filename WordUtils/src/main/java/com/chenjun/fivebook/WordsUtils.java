package com.chenjun.fivebook;

import com.chenjun.constant.WordToXmlConstant;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class WordsUtils {

    private static final Logger log = LoggerFactory.getLogger(Claim.class);

    /**
     * 权利要求书 vm模板
     *
     * @return String
     */
    public static String loadClaimXmlTemplate() {
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

    /**
     * 说明书 vm模板
     *
     * @return String
     */
    public static String loadManualXmlTemplate() {
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

    /**
     * 说明书附图 vm模板
     *
     * @return String
     */
    public static String loadManualDrawingsXmlTemplate() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
    }

    /**
     * 说明书摘要 vm模板
     *
     * @return String
     */
    public static String loadManualSummaryXmlTemplate() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"/dtdandxsl/showxml.xsl\"?>\n" +
                "<!DOCTYPE cn-application-body SYSTEM \"/dtdandxsl/cn-application-body-20080416.dtd\">\n" +
                "<cn-application-body lang=\"${lang}\" country=\"${country}\">\n" +
                "    <cn-abstract>\n" +
                "        ${summary}" +
                "    </cn-abstract>\n" +
                "</cn-application-body>";
    }

    /**
     * 保存 xml文件
     *
     * @param folderPath 文件夹路径
     * @param fileName   文件名
     * @param xmlContent xml内容
     * @throws Exception 异常
     */
    public static void saveXmlFile(String folderPath, String fileName, String xmlContent) throws Exception {
        // 创建输出文件路径
        String outputFilePath = folderPath + File.separator + fileName;
        File outputFile = new File(folderPath);

        // 确保文件夹存在
        if (!outputFile.exists()) {
            outputFile.mkdirs();
        }

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(outputFilePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
             BufferedWriter writer = new BufferedWriter(osw)) {
            writer.write(xmlContent);
        }

        log.info("XML 文件已生成并保存到：" + outputFilePath);
    }

    /**
     * 获取图片最终宽高
     *
     * @param width  宽度
     * @param height 高度
     * @return WiAndHe
     */
    public static WiAndHe getWiAndHe(double width, double height) {
        // 如果图片的宽度和高度都大于标准值，对宽高都进行缩放到标准值计算缩放因子，然后根据大的缩放因子来进行计算宽高
        if (width > WordToXmlConstant.MAX_WIDTH && height > WordToXmlConstant.MAX_HEIGHT) {
            double widthScale = WordToXmlConstant.MAX_WIDTH / width;
            double heightScale = WordToXmlConstant.MAX_HEIGHT / height;
            double scaleRatio = Math.min(widthScale, heightScale);
            width *= scaleRatio;
            height *= scaleRatio;
        }
        //如果图片宽度大于标准值，则将宽度缩放到标准值，并计算高度的缩放因子，然后根据缩放因子来进行计算高
        else if (width > WordToXmlConstant.MAX_WIDTH) {
            double scaleRatio = WordToXmlConstant.MAX_WIDTH / width;
            width = WordToXmlConstant.MAX_WIDTH;
            height *= scaleRatio;
        }
        //如果图片高度大于标准值，则将高度缩放到标准值，并计算宽度的缩放因子，然后根据缩放因子来进行计算宽
        else if (height > WordToXmlConstant.MAX_HEIGHT) {
            double scaleRatio = WordToXmlConstant.MAX_HEIGHT / height;
            height = WordToXmlConstant.MAX_HEIGHT;
            width *= scaleRatio;
        }
        //根据标准值计算第一次的宽高，然后再根据固定缩放因子计算最终宽高值
        width *= WordToXmlConstant.SCALE;
        height *= WordToXmlConstant.SCALE;

        // 计算的宽高保留两位小数
        String wi = String.format("%.2f", width);
        String he = String.format("%.2f", height);

        return new WiAndHe(wi, he);
    }

    @Getter
    static class WiAndHe {
        private final String wi;
        private final String he;

        public WiAndHe(String wi, String he) {
            this.wi = wi;
            this.he = he;
        }
    }
}


