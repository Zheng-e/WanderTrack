package com.example.mappractice;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class GpxCryptoUtils {
    //加密、解密GPX文件的工具类
    private static final String TAG = "GpxCryptoUtils";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String PASSWORD = "WanderTrack123456";
    private static final byte[] IV = new byte[16]; // Zero IV for simplicity, not recommended in production

    public static File decryptGpxFile(File inputFile, File outputDir) throws Exception {

        //从 inputFile 读取加密的 .gpx 文件内容；
        //使用 AES 解密算法解密内容；
        //将解密后的内容写入 outputDir 目录下的新文件中，文件名形如 decrypted_xxx.gpx；
        //如果解密成功，返回这个解密后的文件对象。

        File outputFile = new File(outputDir, "decrypted_" + inputFile.getName());

        SecretKeySpec secretKey = generateKey(PASSWORD);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        try (
            FileInputStream fis = new FileInputStream(inputFile);
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            FileOutputStream fos = new FileOutputStream(outputFile)
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            Log.e(TAG, "Decryption failed: " + e.getMessage());
            throw e;
        }

        return outputFile;
    }

    private static SecretKeySpec generateKey(String password) throws Exception {
        //根据密码生成 AES 加密用的密钥
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(key, "AES");
    }

    public static File encryptGpxFile(File inputFile, File outputFile) throws Exception {
        SecretKeySpec secretKey = generateKey(PASSWORD);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            CipherOutputStream cos = new CipherOutputStream(fos, cipher)
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed: " + e.getMessage());
            throw e;
        }

        return outputFile;
    }
}
