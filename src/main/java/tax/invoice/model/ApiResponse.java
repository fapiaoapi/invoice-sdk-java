package tax.invoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API 响应模型
 * @param <T> 响应数据类型
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;
    private int total;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public ApiResponse() {
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMsg() {
        return msg == null ? "" : msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public boolean isSuccess() {
        return code == 200;
    }
    
    /**
     * 解析 JSON 响应
     * @param json JSON 字符串
     * @param dataClass 数据类型
     * @param <T> 数据类型
     * @return API 响应对象
     * @throws Exception JSON 解析异常
     */
    public static <T> ApiResponse<T> fromJson(String json, Class<T> dataClass) throws Exception {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructParametricType(ApiResponse.class, dataClass));
        } catch (Exception e) {
            return buildErrorResponse(extractErrorMessage(json));
        }
    }
    
    /**
     * 解析 JSON 响应（数据为字符串类型）
     * @param json JSON 字符串
     * @return API 响应对象
     * @throws Exception JSON 解析异常
     */
    public static ApiResponse<String> fromJson(String json) throws Exception {
        return fromJson(json, String.class);
    }
    
    // 在 ApiResponse 类中添加以下方法
    /**
     * 解析 JSON 响应（Map类型）
     * @param json JSON 字符串
     * @return API 响应对象
     * @throws Exception JSON 解析异常
     */
    public static ApiResponse<Map<String, Object>> fromJsonMap(String json) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(json);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setCode(root.path("code").asInt(-1));
            response.setMsg(root.path("msg").asText(""));
            response.setTotal(root.path("total").asInt(0));
            JsonNode dataNode = root.get("data");
            response.setData(parseMapDataNode(dataNode));
            return response;
        } catch (Exception e) {
            return buildErrorResponse(extractErrorMessage(json));
        }
    }
    

    public static ApiResponse<List<Map<String, Object>>> fromJsonListMap(String json) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(json);
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>();
            response.setCode(root.path("code").asInt(-1));
            response.setMsg(root.path("msg").asText(""));
            response.setTotal(root.path("total").asInt(0));
            JsonNode dataNode = root.get("data");
            response.setData(parseListMapDataNode(dataNode));
            return response;
        } catch (Exception e) {
            return buildErrorResponse(extractErrorMessage(json));
        }
    }

    private static Map<String, Object> parseMapDataNode(JsonNode dataNode) throws Exception {
        if (dataNode == null || dataNode.isNull()) {
            return null;
        }
        if (dataNode.isObject()) {
            return objectMapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {});
        }
        if (dataNode.isArray()) {
            Map<String, Object> wrapped = new LinkedHashMap<>();
            wrapped.put("list", parseListMapFromArrayNode(dataNode));
            return wrapped;
        }
        if (dataNode.isTextual()) {
            String text = dataNode.asText();
            if (text == null || text.isBlank()) {
                return null;
            }
            JsonNode parsed = tryReadJsonNode(text);
            if (parsed != null) {
                if (parsed.isObject()) {
                    return objectMapper.convertValue(parsed, new TypeReference<Map<String, Object>>() {});
                }
                if (parsed.isArray()) {
                    Map<String, Object> wrapped = new LinkedHashMap<>();
                    wrapped.put("list", parseListMapFromArrayNode(parsed));
                    return wrapped;
                }
                if (parsed.isNull()) {
                    return null;
                }
                Map<String, Object> wrapped = new LinkedHashMap<>();
                wrapped.put("value", objectMapper.convertValue(parsed, Object.class));
                return wrapped;
            }
            Map<String, Object> wrapped = new LinkedHashMap<>();
            wrapped.put("value", text);
            return wrapped;
        }
        Map<String, Object> wrapped = new LinkedHashMap<>();
        wrapped.put("value", objectMapper.convertValue(dataNode, Object.class));
        return wrapped;
    }

    private static List<Map<String, Object>> parseListMapDataNode(JsonNode dataNode) throws Exception {
        if (dataNode == null || dataNode.isNull()) {
            return null;
        }
        if (dataNode.isArray()) {
            return parseListMapFromArrayNode(dataNode);
        }
        if (dataNode.isObject()) {
            List<Map<String, Object>> single = new ArrayList<>();
            single.add(objectMapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {}));
            return single;
        }
        if (dataNode.isTextual()) {
            String text = dataNode.asText();
            if (text == null || text.isBlank()) {
                return null;
            }
            JsonNode parsed = tryReadJsonNode(text);
            if (parsed != null) {
                if (parsed.isArray()) {
                    return parseListMapFromArrayNode(parsed);
                }
                if (parsed.isObject()) {
                    List<Map<String, Object>> single = new ArrayList<>();
                    single.add(objectMapper.convertValue(parsed, new TypeReference<Map<String, Object>>() {}));
                    return single;
                }
                if (parsed.isNull()) {
                    return null;
                }
                List<Map<String, Object>> single = new ArrayList<>();
                Map<String, Object> wrapped = new LinkedHashMap<>();
                wrapped.put("value", objectMapper.convertValue(parsed, Object.class));
                single.add(wrapped);
                return single;
            }
            List<Map<String, Object>> single = new ArrayList<>();
            Map<String, Object> wrapped = new LinkedHashMap<>();
            wrapped.put("value", text);
            single.add(wrapped);
            return single;
        }
        List<Map<String, Object>> single = new ArrayList<>();
        Map<String, Object> wrapped = new LinkedHashMap<>();
        wrapped.put("value", objectMapper.convertValue(dataNode, Object.class));
        single.add(wrapped);
        return single;
    }

    private static List<Map<String, Object>> parseListMapFromArrayNode(JsonNode arrayNode) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (item == null || item.isNull()) {
                continue;
            }
            if (item.isObject()) {
                result.add(objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}));
                continue;
            }
            Map<String, Object> wrapped = new LinkedHashMap<>();
            wrapped.put("value", objectMapper.convertValue(item, Object.class));
            result.add(wrapped);
        }
        return result;
    }

    private static JsonNode tryReadJsonNode(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String extractErrorMessage(String json) {
        if (json == null || json.isBlank()) {
            return "Empty response body";
        }
        try {
            return objectMapper.readTree(json).asText(json.trim());
        } catch (Exception e) {
            String trimmed = json.trim();
            if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                return trimmed.substring(1, trimmed.length() - 1).replace("\\/", "/");
            }
            return trimmed;
        }
    }

    private static <T> ApiResponse<T> buildErrorResponse(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(-1);
        response.setMsg(message);
        response.setData(null);
        return response;
    }
}
