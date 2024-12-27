package com.chenjun;

import com.chenjun.utils.converter.WordToXml;
import com.chenjun.utils.split.DocxSplitUtil;

import java.io.File;
import java.util.Map;

public class Demo {
    public static void main(String[] args) {

        /*String filePath = "C:\\Users\\junchen\\Desktop\\五书模板\\一种井场内二氧化碳产出量计量系统及方法（模板测试）.docx";
        String outFolderPath = "C:\\Users\\junchen\\IdeaProjects\\IP_management_system\\_doc\\docxTest\\docxToXml\\test";
        DocxSplitUtil.splitDocx(new File(filePath));

        WordToXml.wordToXml(outFolderPath);*/

        String filePath = "C:\\Users\\junchen\\IdeaProjects\\IP_management_system\\_doc\\docxTest\\docxToPdf\\一种药效渗透快的冠脉药物球囊v2(用于测试).docx";

        Map<String, File> stringFileMap = DocxSplitUtil.splitDocx(new File(filePath));


        stringFileMap.forEach((k, v) -> {
            System.out.println(k + ":" + v.getName());
            WordToXml.wordToXml(v);
        });
    }
}
