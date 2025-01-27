package com.chenjun;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String filePath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\五书模板v4.docx";
        File file = new File(filePath);

        HeaderExtractor.headerExtractor(file);
    }
}