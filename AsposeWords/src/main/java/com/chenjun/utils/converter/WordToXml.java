package com.chenjun.utils.converter;

import com.my.common.enums.IpBusinessUploadIdFileTypeEnum;
import com.my.common.exception.CustomException;

import java.io.File;

/**
 * 将已拆分的docx文件以轮询的方式选择合适的处理方法
 */
public class WordToXml {

    public static void wordToXml(File file) {
        if (file.isFile()) {
            String fileName = file.getName().replaceAll("\\.docx$", "");
            // 权利要求书
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.REQUEST_RIGHT.getCode().toString())) {
                try {
                    ClaimToXml.claimToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理权利要求书时出错：" + e.getMessage());
                }
            }

            // 说明书
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.INSTRUCTION.getCode().toString())) {
                try {
                    ManualToXml.manualToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理说明书时出错：" + e.getMessage());
                }
            }

            // 说明书附图
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.INSTRUCTION_PICTURE.getCode().toString())) {
                try {
                    ManualDrawingsToXml.manualDrawingsToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理说明书附图时出错：" + e.getMessage());
                }
            }

            // 说明书摘要
            if (fileName.equals(IpBusinessUploadIdFileTypeEnum.ABSTRACT.getCode().toString())) {
                try {
                    ManualSummaryToXml.manualSummaryToXml(file);
                } catch (Exception e) {
                    throw new CustomException("处理说明书摘要时出错：" + e.getMessage());
                }
            }

        }
    }
}
