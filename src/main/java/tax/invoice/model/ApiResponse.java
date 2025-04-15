package tax.invoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
        return objectMapper.readValue(json, objectMapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, dataClass));
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
        return objectMapper.readValue(json, new TypeReference<ApiResponse<Map<String, Object>>>() {});
    }
    

    public static ApiResponse<List<Map<String, Object>>> fromJsonListMap(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }

}