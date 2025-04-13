package tax.invoice.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * 签名工具类
 */
public class SignatureUtil {
    
    /**
     * 生成随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        
        for(int i=0; i<length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * 计算签名
     * @param method HTTP方法
     * @param path 请求路径
     * @param randomString 随机字符串
     * @param timestamp 时间戳
     * @param appKey 应用密钥
     * @param appSecret 应用密钥
     * @return 签名字符串
     * @throws Exception 签名计算异常
     */
    public static String calculateSignature(String method, String path, 
                                          String randomString, String timestamp,
                                          String appKey, String appSecret) 
                                          throws Exception {
        String content = String.format(
            "Method=%s&Path=%s&RandomString=%s&TimeStamp=%s&AppKey=%s",
            method, path, randomString, timestamp, appKey
        );
        
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256.init(secretKey);
        
        byte[] hash = sha256.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash).toUpperCase();
    }
    
    /**
     * 字节数组转十六进制字符串
     * @param hash 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * 获取当前时间戳（秒）
     * @return 时间戳字符串
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(Instant.now().getEpochSecond());
    }
}