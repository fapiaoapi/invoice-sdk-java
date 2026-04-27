package tax.invoice.http;

import tax.invoice.config.InvoiceConfig;
import tax.invoice.util.SignatureUtil;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 客户端
 */
public class HttpClient {
    private final InvoiceConfig config;
    private final java.net.http.HttpClient client;
    private final boolean debug;
    
    public HttpClient(InvoiceConfig config) {
        this(config, false);
    }

    public HttpClient(InvoiceConfig config, boolean debug) {
        this.config = config;
        this.debug = debug;
        this.client = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(150))
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
    public HttpResponse<String> post(String path, Map<String, Object> formData, String authorization) throws Exception {
        String method = "POST";
        String randomString = SignatureUtil.generateRandomString(20);
        String timestamp = SignatureUtil.getCurrentTimestamp();
        
        String signature = SignatureUtil.calculateSignature(
            method, path, randomString, timestamp, 
            config.getAppKey(), config.getAppSecret()
        );
        String boundary = "----InvoiceSdkBoundary" + SignatureUtil.generateRandomString(20);
        byte[] requestBody = buildMultipartBody(formData, boundary);
        HttpRequest request = buildRequestBuilder(
                path,
                signature,
                timestamp,
                randomString,
                "multipart/form-data; boundary=" + boundary,
                authorization
        )
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        logRequest(method, request.uri().toString(), request.headers().map(), formData);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        logResponse(response);
        return response;
    }

    /**
     * 发送 POST 请求
     * @param path 请求路径
     * @param formData 表单数据
     * @param authorization 授权令牌
     * @return HTTP响应
     * @throws Exception 请求异常
     */
    public HttpResponse<String> postJson(String path, Map<String, Object> formData, String authorization) throws Exception {
        String method = "POST";
        String randomString = SignatureUtil.generateRandomString(20);
        String timestamp = SignatureUtil.getCurrentTimestamp();

        String signature = SignatureUtil.calculateSignature(
                method, path, randomString, timestamp,
                config.getAppKey(), config.getAppSecret()
        );

        String requestBody = buildJsonBody(formData);

        HttpRequest request = buildRequestBuilder(
                path,
                signature,
                timestamp,
                randomString,
                "application/json; charset=UTF-8",
                authorization
        )
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        logRequest(method, request.uri().toString(), request.headers().map(), formData);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        logResponse(response);
        return response;
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
                .header("Accept-Charset", "UTF-8")
                .header("Sdk", "java1024")
                .timeout(Duration.ofSeconds(150)); // 添加字符集
        
        if (authorization != null && !authorization.isEmpty()) {
            requestBuilder.header("Authorization", authorization);
        }
        
        HttpRequest request = requestBuilder.GET().build();

        logRequest(method, request.uri().toString(), request.headers().map(), queryParams);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        logResponse(response);
        return response;
    }

    
    /**
     * 构建多部分表单数据
     * @param data 表单数据
     * @param boundary 分隔符
     * @return 表单数据字符串
     */
    private byte[] buildMultipartBody(Map<String, Object> data, String boundary) {
        String lineEnd = "\r\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        if (data != null && !data.isEmpty()) {
            Map<String, Object> orderedData = new LinkedHashMap<>(data);
            for (Map.Entry<String, Object> entry : orderedData.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                writeUtf8(output, "--" + boundary + lineEnd);
                writeUtf8(output, "Content-Disposition: form-data; name=\"" + escapeFormFieldName(entry.getKey()) + "\"" + lineEnd);
                writeUtf8(output, lineEnd);
                writeUtf8(output, String.valueOf(value) + lineEnd);
            }
        }

        writeUtf8(output, "--" + boundary + "--" + lineEnd);
        return output.toByteArray();
    }

    private void writeUtf8(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escapeFormFieldName(String fieldName) {
        return fieldName.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "");
    }

    private HttpRequest.Builder buildRequestBuilder(
            String path,
            String signature,
            String timestamp,
            String randomString,
            String contentType,
            String authorization
    ) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + path))
                .header("AppKey", config.getAppKey())
                .header("Sign", signature)
                .header("TimeStamp", timestamp)
                .header("RandomString", randomString)
                .header("Content-Type", contentType)
                .header("Accept-Charset", "UTF-8")
                .header("Sdk", "java1024")
                .timeout(Duration.ofSeconds(150));

        if (authorization != null && !authorization.isEmpty()) {
            builder.header("Authorization", authorization);
        }
        return builder;
    }

    /**
     * 构建JSON请求体
     * @param data 数据Map
     * @return JSON字符串
     */
    private String buildJsonBody(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;

            json.append("\"")
                    .append(escapeJson(entry.getKey()))
                    .append("\":")
                    .append(valueToJson(entry.getValue()));
        }

        return json.append("}").toString();
    }

    /**
     * 值类型转换
     */
    private String valueToJson(Object value) {
        if (value == null) return "null";

        if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map) {
            return buildJsonBody((Map<String, Object>) value).replaceFirst("\\{", "<").replace("}", ">");
        }
        if (value instanceof Collection) {
            return arrayToJson((Collection<?>) value);
        }
        if (value.getClass().isArray()) {
            return arrayToJson(Arrays.asList((Object[]) value));
        }
        // 其他类型转为字符串
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    /**
     * 数组/集合转换
     */
    private String arrayToJson(Collection<?> collection) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Object item : collection) {
            if (!first) json.append(",");
            first = false;
            json.append(valueToJson(item));
        }
        return json.append("]").toString();
    }

    /**
     * JSON特殊字符转义
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void logRequest(String method, String url, Map<String, java.util.List<String>> headers, Object params) {
        if (!debug) {
            return;
        }
        System.out.println("[InvoiceSDK][DEBUG] REQUEST");
        System.out.println("URL: " + url);
        System.out.println("METHOD: " + method);
        System.out.println("HEADERS: " + headers);
        System.out.println("PARAMS: " + String.valueOf(params));
    }

    private void logResponse(HttpResponse<String> response) {
        if (!debug) {
            return;
        }
        System.out.println("[InvoiceSDK][DEBUG] RESPONSE");
        System.out.println("STATUS: " + response.statusCode());
        System.out.println("BODY: " + response.body());
    }
}
