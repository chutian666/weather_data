package com.scboanda.demo.util;

import com.jcraft.jsch.*;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;


/**
 * @author ChuTian
 * @Title 督察遥感小程序-生成缩略图
 * @since 2023/9/6 11:17
 */
public class ThumbnailUtil {

    public static void main(String[] args) {
        // 本地文件夹路径
        String localFolderPath = "D:\\usr\\local\\webapp\\filestorage\\platform";

        // 遍历文件夹及其子文件夹
        processFolder(new File(localFolderPath));
    }

    private static void processFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归处理子文件夹
                    processFolder(file);
                } else if (isImageFile(file)) {
                    try {
                        // 生成缩略图
                        File thumbnailFile = generateThumbnail(file);

                        // 修改文件名
                        renameImageFiles(file, thumbnailFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 检查文件是否为图片文件（根据需要添加更多图片格式）
     *@author ChuTian
     *@date 2023/9/6
     *@param
     *@return
     */
    private static boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    /**
     * 生成缩略图
     *@author ChuTian
     *@date 2023/9/6
     *@param
     *@return
     */
    private static File generateThumbnail(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        String originalFileName = file.getName();
        String thumbnailFileName = "缩略图_" + originalFileName;
        File thumbnailFile = new File(file.getParent(), thumbnailFileName);

        // 生成缩略图并保存
        Thumbnails.of(image).size(800, 500).toFile(thumbnailFile);

        return thumbnailFile;
    }

    /**
     * 修改文件名
     *@author ChuTian
     *@date 2023/9/6
     *@param
     *@return
     */
    private static void renameImageFiles(File originalFile, File thumbnailFile) {
        String originalFileName = originalFile.getName();
        String thumbnailFileName = thumbnailFile.getName();

        // 构建新的文件名
        String newOriginalFileName = "origin_" + originalFileName;
        String newThumbnailFileName = originalFileName;

        File newOriginalFile = new File(originalFile.getParent(), newOriginalFileName);
        File newThumbnailFile = new File(originalFile.getParent(), newThumbnailFileName);

        if (originalFile.renameTo(newOriginalFile) && thumbnailFile.renameTo(newThumbnailFile)) {
            System.out.println("文件名已修改: " + originalFileName + " -> " + newOriginalFileName + "，" + thumbnailFileName + " -> " + newThumbnailFileName);
        } else {
            System.out.println("无法修改文件名: " + originalFileName);
        }
    }

}
