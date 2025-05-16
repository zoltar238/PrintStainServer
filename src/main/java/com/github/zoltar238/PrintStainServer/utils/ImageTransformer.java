package com.github.zoltar238.PrintStainServer.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@UtilityClass
public class ImageTransformer {
    public static String transformImageToBase64(String path) throws IOException {
        Path imagePath = Paths.get(path);
        byte[] imageByte = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageByte);
    }

    public static byte[] transformBase64ToImage(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    public static void saveImageToDisk(String path, String base64) throws IOException {
        byte[] imageByte = transformBase64ToImage(base64);
        Path imagePath = Paths.get(path);
        Files.write(imagePath, imageByte);
    }
}
