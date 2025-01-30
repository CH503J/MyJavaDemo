package com.chenjun.fivebook;

import com.chenjun.enums.IpBusinessUploadIdFileTypeEnum;
import com.chenjun.exception.CustomException;

import javax.annotation.Resource;
import java.io.File;

public class ToXml {
    @Resource
    private static Claim claim;

    @Resource
    private static Manual manual;

    @Resource
    private static ManualPic manualPic;

    @Resource
    private static ManualSummary manualSummary;

    public static void toXml(File file) {
        if (file.isFile()) {
            String fileName = file.getName().replaceAll("\\.docx$", "");
            // 权利要求书
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getCode().toString())) {
                try {
                    claim.claimToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理权利要求书时出错：" + e.getMessage());
                }
            }

            // 说明书
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode().toString())) {
                try {
                    manual.manualToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理说明书时出错：" + e.getMessage());
                }
            }

            // 说明书附图
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getCode().toString())) {
                try {
                    manualPic.manualDrawingsToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理说明书附图时出错：" + e.getMessage());
                }
            }

            // 说明书摘要
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.ABSTRACT.getCode().toString())) {
                try {
                    manualSummary.manualSummaryToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理说明书摘要时出错：" + e.getMessage());
                }
            }
        }
    }
}
