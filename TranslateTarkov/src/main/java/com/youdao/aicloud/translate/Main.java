package com.youdao.aicloud.translate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        String folderPath = "C:\\Users\\junchen\\Desktop\\zh-cn";

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("文件不存在或路径错误");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".jsonc"));
        if (files == null || files.length == 0) {
            System.out.println("文件夹中没有json文件");
            return;
        }

        List<Map.Entry<String, String>> nameMap = new ArrayList<>();
        List<Map.Entry<String, String>> desMap = new ArrayList<>();

        int fileIndex = 0;
        for (File file : files) {
            fileIndex++;
            System.out.println("正在处理文件：" + fileIndex + ": " + file.getName());

            processJsoncFile(file, nameMap, desMap);

            System.out.println("\n所有文件处理完毕！");
            System.out.println("nameMap总大小：" + nameMap.size());
            System.out.println("desMap总大小：" + desMap.size());
        }
    }

    private static void processJsoncFile(File file, List<Map.Entry<String, String>> nameMap, List<Map.Entry<String, String>> desMap) {
        try (FileReader reader = new FileReader(file)) {
            JsonElement je = JsonParser.parseReader(reader);
            JsonObject jo = je.getAsJsonObject();

            if (jo.has("ConfigEntries")) {
                JsonObject configEntries = jo.getAsJsonObject("ConfigEntries");
                int index = 0;
                for (String key : configEntries.keySet()) {
                    JsonObject entry = configEntries.getAsJsonObject(key);
                    if (entry.has("DispName")) {
                        nameMap.add(new AbstractMap.SimpleEntry<>(key, entry.get("DispName").getAsString()));
                        System.out.println("  " + (++index) + ":  DispName: " + entry.get("DispName").getAsString());
                    }
                    if (entry.has("Description")) {
                        desMap.add(new AbstractMap.SimpleEntry<>(key, entry.get("Description").getAsString()));
                        System.out.println("  " + index + ":  Description: " + entry.get("Description").getAsString() + "\n");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("解析文件时出错：" + file.getName() + "，错误：" + e.getMessage());
        }
    }
}