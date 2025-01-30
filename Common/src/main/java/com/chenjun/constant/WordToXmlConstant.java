package com.chenjun.constant;

/**
 * 五书转xml格式常量类
 */
public class WordToXmlConstant {

    /** 权利要求书 */
    public static final String CLAIM = "100001.xml";

    /** 说明书 */
    public static final String MANUAL = "100002.xml";

    /** 说明书附图 */
    public static final String MANUAL_DRAWINGS = "100003.xml";

    /** 说明书摘要 */
    public static final String MANUAL_SUMMARY = "100004.xml";

    /** word标题 技术领域 */
    public static final String TECHNICAL_FIELD = "技术领域";

    /** xml标签 技术领域 */
    public static final String TECHNICAL_FIELD_TAG = "technical-field";

    /** word标题 背景技术 */
    public static final String BACKGROUND_ART = "背景技术";

    /** xml标签 背景技术 */
    public static final String BACKGROUND_ART_TAG = "background-art";

    /** word标题 发明内容 */
    public static final String DISCLOSURE = "发明内容";

    /** xml标签 发明内容 */
    public static final String DISCLOSURE_TAG = "disclosure";

    /** word标题 附图说明 */
    public static final String DESCRIPTION_OF_DRAWINGS = "附图说明";

    /** xml标签 附图说明 */
    public static final String DESCRIPTION_OF_DRAWINGS_TAG = "description-of-drawings";

    /** word标题 具体实施方式 */
    public static final String MODE_FOR_INVENTION = "具体实施方式";

    /** xml标签 具体实施方式 */
    public static final String MODE_FOR_INVENTION_TAG = "mode-for-invention";

    /** xml标签 */
    public static final String TYPE = "type";

    /** xml标签id属性 */
    public static final String ID = "id";

    /** 二级标题标签 */
    public static final String HEADING = "heading";

    /** 二级标题下正文标签 */
    public static final String TEXT = "text";

    /**
     * 1.二级标题标签id头
     * 2.二级标题的id属性是由头和序号组成的，id头固定，序号是全局计数器
     */
    public static final String HEADING_ID_HEAD = "h";

    /**
     * 1.xml正文标签
     * 2.正文标签不止是标签，同时也是属性id头
     */
    public static final String P = "p";

    /** xml正文标签num属性 */
    public static final String NUM = "num";

    /** xml正文标签num属性的替代值 */
    public static final String XXXX = "XXXX";

    /** xml正文标签的斜体属性 */
    public static final String ITALIC = "Italic";

    /** xml上标左标签 */
    public static final String LEFT_SUP = "<sup>";

    /** xml上标右标签 */
    public static final String RIGHT_SUP = "</sup>";

    /** xml下标左标签 */
    public static final String LEFT_SUB = "<sub>";

    /** xml下标右标签 */
    public static final String RIGHT_SUB = "</sub>";

    /** 除附图外所有图片名的前缀 */
    public static final String PIC_PREFIX = "sms_";

    /** 附图文件名前缀 */
    public static final String PIC_DRAWINGS_PREFIX = "ft_";

    /** 图片标签属性id组成头 */
    public static final String PIC_ID_HEAD = "idf_";

    /** 权利要求书claim标签 */
    public static final String CLAIM_TAG = "claims";

    /** 权利要求书claim标签id属性头 */
    public static final String CLAIM_ID_HEAD = "c1_cl";

    /** 说明书附图figure标签labels属性 */
    public static final String LABEL = "label";

    /** 说明书附图img标签file属性 */
    public static final String FILE_NAME = "file";

    /** 说明书附图img标签format属性 */
    public static final String FORMAT = "format";

    /** 说明书附图img标签width属性 */
    public static final String WIDTH = "width";

    /** 说明书附图img标签height属性 */
    public static final String HEIGHT = "height";

    /** 图片最大宽度 */
    public static final double MAX_WIDTH = 624.0;

    /** 附图最大高度 */
    public static final double MAX_HEIGHT = 926.4;

    /** 附图缩放因子 */
    public static final double SCALE = 0.265;

}
