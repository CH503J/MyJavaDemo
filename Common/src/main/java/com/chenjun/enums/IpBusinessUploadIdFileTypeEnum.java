package com.chenjun.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : gly
 * @ClassName : IpBusinessUploadIdFileTypeEnum
 * @Date : 2024/11/30
 * @since :
 **/
@Getter
@AllArgsConstructor
public enum IpBusinessUploadIdFileTypeEnum {

    REQUEST_RIGHT(100001, 1, "/templates/vms/100001.vm", "权利要求书"),
    INSTRUCTION(100002, 2, "/templates/vms/100002.vm", "说明书"),
    INSTRUCTION_PICTURE(100003, 3, "/templates/vms/100003.vm", "说明书附图"),
    ABSTRACT(100004, 4, "/templates/vms/100004.vm", "说明书摘要"),
    ABSTRACT_PICTURE(100005, 5, null, "摘要附图"),
    /**
     * 100007专利代理委托书
     */
    PROXY_ENTRUST(100007, 6, "/templates/vms/proxyEntrust.vm", "专利代理委托书"),
    /**
     * 100016著录项目变更申报书
     */
    CHANGE_REQUEST(100016, 7, "/templates/vms/changeRequest.vm", "著录项目变更申报书"),
    /**
     * 100104著录项目变更理由证明
     */
    CHANGE_REASON(100104, 8, "/templates/vms/changeReason.vm", "著录项目变更理由证明"),
    /**
     * 110101发明专利请求书
     */
    INVENTION_REQUEST(110101, 9, "/templates/vms/inventionRequest.vm", "发明专利请求书"),
    /**
     * 110401实质审查请求书
     */
    ACTUAL_REVIEW(110401, 10, "/templates/vms/actualReview.vm", "实质审查请求书"),
    /**
     * 120101实用新型专利请求书
     */
    PRACTICAL_REQUEST(120101, 11, "/templates/vms/practicalRequest.vm", "实用新型专利请求书"),
    /**
     * 130001外观设计图片或照片
     */
    APPEARANCE_IMAGE(130001, 12, "/templates/vms/appearanceImage.vm", "外观设计图片或照片"),
    /**
     * 130002外观设计简要说明
     */
    APPEARANCE_DESCRIPTION(130002, 13, "/templates/vms/appearanceDescription.vm", "外观设计简要说明"),
    /**
     * 130101外观设计专利请求书
     */
    APPEARANCE_REQUEST(130101, 14, "/templates/vms/appearanceRequest.vm", "外观设计专利请求书"),
    /**
     * list外层文件模版
     */
    PATENT_FILES(99999, 15, "/templates/vms/patentFiles.vm", "list外层文件模版"),
    ;
    private final Integer code;
    private final Integer fileType;
    private final String templatePath;
    private final String desc;

    /**
     * 定义文件夹名称 100001 100002 100003 100004
     */
    public static final IpBusinessUploadIdFileTypeEnum[] FOLDERS = {
            REQUEST_RIGHT,
            INSTRUCTION,
            INSTRUCTION_PICTURE,
            ABSTRACT
    };
    /**
     * 获取发明申请的全部文件名称
     */
    public static final IpBusinessUploadIdFileTypeEnum[] INVENTION_REQUEST_FILE_NAME = {
            PROXY_ENTRUST,
            INVENTION_REQUEST,
            ACTUAL_REVIEW
    };
    /**
     * 获取实用新型申请的全部文件名称
     */
    public static final IpBusinessUploadIdFileTypeEnum[] PRACTICAL_REQUEST_FILE_NAME = {
            PROXY_ENTRUST,
            PRACTICAL_REQUEST
    };
    /**
     * 获取外观设计的全部文件名称
     */
    public static final IpBusinessUploadIdFileTypeEnum[] APPEARANCE_REQUEST_FILE_NAME = {
            PROXY_ENTRUST,
            APPEARANCE_IMAGE,
            APPEARANCE_DESCRIPTION,
            APPEARANCE_REQUEST
    };

    // 根据desc 查询对应的枚举
    public static Integer getByDesc(String desc) {
        for (IpBusinessUploadIdFileTypeEnum fileTypeEnum : IpBusinessUploadIdFileTypeEnum.values()) {
            if (fileTypeEnum.getDesc().equals(desc)) {
                return fileTypeEnum.getFileType();
            }
        }
        return null;
    }


    // 根据code，查fileType
    public static Integer getFileTypeByCode(Integer code) {
        for (IpBusinessUploadIdFileTypeEnum fileTypeEnum : IpBusinessUploadIdFileTypeEnum.values()) {
            if (fileTypeEnum.getCode().equals(code)) {
                return fileTypeEnum.getFileType();
            }
        }
        return null;
    }
}
