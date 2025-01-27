package com.chenjun.split;

import java.io.File;

public class Split1 {
    public static void main(String[] args) {
        String filePath = "C:\\Development\\JavaProject\\MyJavaDemo\\_test\\originalFiles\\五书模板v4.docx";
        File file = new File(filePath);

        SplitWords1.splitWords(file);
    }
}