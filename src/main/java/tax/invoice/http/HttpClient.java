package tax.invoice.http;

import tax.invoice.config.InvoiceConfig;
import tax.invoice.util.SignatureUtil;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 客户端
 */
public class HttpClient {
    private final InvoiceConfig config;
    private final java.net.http.HttpClient client;
    
    public HttpClient(InvoiceConfig config) {
        this.config = config;
        this.client = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_2)
                .build();
    }
    
    /**
     * 发送 POST 请求
     * @param path 请求路径
     * @param formData 表单数据
     * @param authorization 授权令牌
     * @return HTTP响应
     * @throws Exception 请求异常
     */
    public HttpResponse<String> post(String path, Map<String, String> formData, String authorization) throws Exception {
        String method = "POST";
        String randomString = SignatureUtil.generateRandomString(20);
        String timestamp = SignatureUtil.getCurrentTimestamp();
        
        String signature = SignatureUtil.calculateSignature(
            method, path, randomString, timestamp, 
            config.getAppKey(), config.getAppSecret()
        );
        
        String boundary = "----InvoiceJavaSdkBoundary" + System.currentTimeMillis();
        String requestBody = buildMultipartBody(formData, boundary);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + path))
                .header("AppKey", config.getAppKey())
                .header("Sign", signature)
                .header("TimeStamp", timestamp)
                .header("RandomString", randomString)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept-Charset", "UTF-8"); // 添加字符集
        
        if (authorization != null && !authorization.isEmpty()) {
            requestBuilder.header("Authorization", authorization);
        }
        
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8)) // 指定UTF-8编码
                .build();
        
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)); // 指定UTF-8编码
    }
    
    /**
     * 发送 GET 请求
     * @param path 请求路径
     * @param queryParams 查询参数
     * @param authorization 授权令牌
     * @return HTTP响应
     * @throws Exception 请求异常
     */
    public HttpResponse<String> get(String path, Map<String, String> queryParams, String authorization) throws Exception {
        String method = "GET";
        String randomString = SignatureUtil.generateRandomString(20);
        String timestamp = SignatureUtil.getCurrentTimestamp();
        
        StringBuilder urlBuilder = new StringBuilder(config.getBaseUrl() + path);
        
        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        
        String signature = SignatureUtil.calculateSignature(
            method, path, randomString, timestamp, 
            config.getAppKey(), config.getAppSecret()
        );
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("AppKey", config.getAppKey())
                .header("Sign", signature)
                .header("TimeStamp", timestamp)
                .header("RandomString", randomString)
                .header("Accept-Charset", "UTF-8"); // 添加字符集
        
        if (authorization != null && !authorization.isEmpty()) {
            requestBuilder.header("Authorization", authorization);
        }
        
        HttpRequest request = requestBuilder.GET().build();
        
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)); // 指定UTF-8编码
    }
    
    /**
     * 构建多部分表单数据
     * @param data 表单数据
     * @param boundary 分隔符
     * @return 表单数据字符串
     */
    private String buildMultipartBody(Map<String, String> data, String boundary) {
        if (data == null || data.isEmpty()) {
            return "--" + boundary + "--\r\n";
        }
        
        StringBuilder body = new StringBuilder();
        
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                body.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"")
                    .append(entry.getKey()).append("\"\r\n\r\n")
                    .append(entry.getValue()).append("\r\n");
            }
        }
        body.append("--").append(boundary).append("--\r\n");
        
        return body.toString();
    }
}