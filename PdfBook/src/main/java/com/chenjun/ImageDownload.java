package com.chenjun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownload {
    public static void main(String[] args) {
        // 设置基础 URL 和保存路径
        String baseUrl = "https://www.misumi.com.cn/linked/archive/ebook/dianzidianqi202412/img/xxxx_00.png"; // 替换为实际的URL
        String saveDir = "C:\\Develop\\IDEA_Projects\\MyJavaDemo\\PdfBook\\src\\main\\resources\\download\\电子电气配线目录"; // 保存图片的文件夹

        // 创建保存目录
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 遍历页码并下载图片
        for (int i = 1; i <= 1222; i++) {
            String pageNum = String.format("%04d", i); // 将页码格式化为4位数字
            String imageUrl = baseUrl.replace("xxxx", pageNum);
            String savePath = saveDir + File.separator + pageNum + "_00.png";

            try {
                downloadImage(imageUrl, savePath);
                System.out.println("Downloaded: " + imageUrl);
            } catch (IOException e) {
                System.err.println("Failed to download: " + imageUrl);
                e.printStackTrace();
            }
        }

        System.out.println("All images have been processed.");
    }

    /**
     * 下载图片并保存到指定路径
     * @param imageUrl 图片的远程 URL
     * @param savePath 本地保存路径
     * @throws IOException 如果发生网络或文件 IO 错误
     */
    private static void downloadImage(String imageUrl, String savePath) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 设置连接超时
        connection.setReadTimeout(5000); // 设置读取超时

        // 检查响应码
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取输入流并保存到文件
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            throw new IOException("HTTP response code: " + responseCode);
        }

        connection.disconnect();
    }
}
