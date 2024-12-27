package com.chenjun.utils;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;


/**
 * 图片水印工具类
 * 代理委托书客户已盖章的扫描件加公司章水印
 */
public class ImageWatermarkUtil {

    /**
     * A4纸的标准宽
     */
    public static final int IMAGE_WIDTH = 2480;

    /**
     * A4纸的标准高
     */
    public static final int IMAGE_HEIGHT = 3508;

    /**
     * 水印透明度
     */
    public static final float TRANSPARENCY = 1f;

    /**
     * 水印缩放比例
     */
    public static final double WATERMARK_SCALE = 1.7;

    /**
     * 红色阈值，用于检测红色印章
     */
    public static final int RED_THRESHOLD = 150;

    /**
     * 随机位置偏移范围
     */
    public static final int RANDOM_OFFSET = 200;

    /**
     * 水印本地资源（公司章）
     */
    public static final String WATERMARK_IMAGE_PATH = "pictureWatermarker/watermark2.png";

    /**
     * 添加水印到图片中，返回处理后的图片。
     *
     * @param imageFile 待处理的图片文件
     * @return 带有水印的处理后图片
     * @throws IOException 文件读取或处理异常
     *
     */
    public BufferedImage addWatermark(File imageFile) throws IOException {
        // 1. 读取图片
        BufferedImage originalImage = adjustImageResolution(imageFile);

        // 2. 读取并缩放水印图片
        BufferedImage watermarkImage = loadAndScaleWatermark();

        // 3. 检测红色印章区域
        Rectangle redStampArea = detectRedStamp(originalImage);

        // 4. 设置水印的位置和透明度
        AffineTransform transform = createWatermarkTransform(watermarkImage, redStampArea);
        applyWatermark(originalImage, watermarkImage, transform);

        return originalImage;
    }

    /**
     * 调整图片分辨率至指定宽高。
     *
     * @param imageFile 上传的图片文件
     * @return 调整分辨率后的图片
     * @throws IOException 文件读取异常
     */
    private BufferedImage adjustImageResolution(File imageFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageFile);
        BufferedImage newImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        g.dispose();


        return newImage;
    }

    /**
     * 读取水印图片并按比例缩放。
     *
     * @return 缩放后的水印图片
     * @throws IOException 文件读取异常
     */
    private BufferedImage loadAndScaleWatermark() throws IOException {
        // 假设水印图片路径是本地文件
        File watermarkFile = new File(WATERMARK_IMAGE_PATH);
        BufferedImage watermarkImage = ImageIO.read(watermarkFile);
        return scaleImage(watermarkImage, WATERMARK_SCALE);
    }

    /**
     * 按照指定比例缩放图片。
     *
     * @param image 原始图片
     * @param scale 缩放比例
     * @return 缩放后的图片
     */
    private BufferedImage scaleImage(BufferedImage image, double scale) {
        int width = (int) (image.getWidth() * scale);
        int height = (int) (image.getHeight() * scale);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return scaledImage;
    }

    /**
     * 检测图片中的红色印章区域。
     *
     * @param image 待检测的图片
     * @return 红色印章区域的矩形
     */
    private Rectangle detectRedStamp(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Rectangle redArea = new Rectangle();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isRed(new Color(image.getRGB(x, y), true))) {
                    if (redArea.isEmpty()) {
                        redArea.setBounds(x, y, 1, 1);
                    } else {
                        redArea.add(x, y);
                    }
                }
            }
        }
        return redArea;
    }

    /**
     * 判断颜色是否是红色。
     *
     * @param color 颜色对象
     * @return 是否是红色
     */
    private boolean isRed(Color color) {
        return color.getRed() > RED_THRESHOLD && color.getGreen() < 100 && color.getBlue() < 100;
    }

    /**
     * 创建水印的变换矩阵，包括位置、旋转等。
     *
     * @param watermarkImage 水印图片
     * @param redStampArea   红色印章区域
     * @return 变换矩阵
     */
    private AffineTransform createWatermarkTransform(BufferedImage watermarkImage, Rectangle redStampArea) throws RuntimeException {
        double angle = Math.toRadians(randomRotationAngle());
        AffineTransform transform = new AffineTransform();

        int x = 1000;
        int y = 2700;

        if (!redStampArea.isEmpty()) {
            boolean validPositionFound = false;
            int maxAttempts = 10; // 最大尝试次数
            int attempts = 0;

            while (!validPositionFound && attempts < maxAttempts) {
                x = randomPosition(redStampArea, watermarkImage.getWidth(), IMAGE_WIDTH);
                y = (int) (redStampArea.getMaxY() + 10); // 确保水印在印章下方
                x += randomOffset();

                // 检查水印是否超出图像边界
                if (x >= 0 && y >= 0 && (x + watermarkImage.getWidth()) <= IMAGE_WIDTH && (y + watermarkImage.getHeight()) <= IMAGE_HEIGHT) {
                    validPositionFound = true;
                }

                attempts++;
            }

            if (!validPositionFound) {
                throw new RuntimeException("无法满足水印位置条件。");
            }

            transform.translate(x, y);
        } else {
            transform.translate(x, y);
        }

        transform.rotate(angle, watermarkImage.getWidth() / 2.0, watermarkImage.getHeight() / 2.0);
        return transform;
    }

    /**
     * 计算水印在图像中的随机位置。
     *
     * @param redStampArea  红色印章区域
     * @param watermarkWidth 水印宽度
     * @param imageWidth    图像宽度
     * @return 随机位置
     */
    private int randomPosition(Rectangle redStampArea, int watermarkWidth, int imageWidth) {
        int minX = (int) redStampArea.getMinX() - watermarkWidth;
        int maxX = (int) redStampArea.getMaxX();
        int x = new Random().nextInt(maxX - minX) + minX;
        return Math.max(0, Math.min(x, imageWidth - watermarkWidth)); // 确保水印在图像范围内
    }

    /**
     * 计算随机偏移量，用于水印的位置调整。
     *
     * @return 随机偏移量
     */
    private int randomOffset() {
        return new Random().nextInt(RANDOM_OFFSET) - (RANDOM_OFFSET / 2);
    }

    /**
     * 将水印应用到原始图片上。
     *
     * @param originalImage 原始图片
     * @param watermarkImage 水印图片
     * @param transform 变换矩阵
     */
    private void applyWatermark(BufferedImage originalImage, BufferedImage watermarkImage, AffineTransform transform) {
        Graphics2D g2d = originalImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, TRANSPARENCY));
        g2d.setTransform(transform);
        g2d.drawImage(watermarkImage, 0, 0, null);
        g2d.dispose();
    }

    /**
     * 生成随机的旋转角度。
     *
     * @return 旋转角度（-30到30度）
     */
    private int randomRotationAngle() {
        return new Random().nextInt(61) - 30;
    }
}
