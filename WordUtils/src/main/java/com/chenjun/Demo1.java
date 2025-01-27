package com.chenjun;

import java.io.File;

public class Demo1 {
    public static void main(String[] args) {
        String filePath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\originalFiles\\五书模板v4.docx";
        File file = new File(filePath);

        HeaderExtractorv3.headerExtractor(file);
    }
}