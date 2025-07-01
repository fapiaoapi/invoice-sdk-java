package tax.invoice;

import tax.invoice.config.InvoiceConfig;
import tax.invoice.http.HttpClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 数电发票客户端
 */
public class InvoiceClient {
    private final InvoiceConfig config;
    private final HttpClient httpClient;
    private String authorization;
    
    public InvoiceClient(String appKey, String appSecret) {
        this(new InvoiceConfig(appKey, appSecret));
    }
    
    public InvoiceClient(InvoiceConfig config) {
        this.config = config;
        this.httpClient = new HttpClient(config);
    }
    
    /**
     * 获取授权
     * @param nsrsbh 纳税人识别号
     * @return 授权响应
     * @throws Exception 请求异常
     */
    public ApiResponse<AuthorizationResponse> getAuthorization(String nsrsbh) throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("nsrsbh", nsrsbh);
        
        HttpResponse<String> response = httpClient.post("/v5/enterprise/authorization", formData, null);
        ApiResponse<AuthorizationResponse> apiResponse = ApiResponse.fromJson(
                response.body(), AuthorizationResponse.class);
        
        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
            this.authorization = apiResponse.getData().getToken();
        }
        
        return apiResponse;
    }

    /**
     * 获取授权
     * @param nsrsbh 纳税人识别号
     * @param type 类型 5 管理
     * @return 授权响应
     * @throws Exception 请求异常
     */
    public ApiResponse<AuthorizationResponse> getAuthorization(String nsrsbh,String type) throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("nsrsbh", nsrsbh);
        formData.put("type", type);
        HttpResponse<String> response = httpClient.post("/v5/enterprise/authorization", formData, null);
        ApiResponse<AuthorizationResponse> apiResponse = ApiResponse.fromJson(
                response.body(), AuthorizationResponse.class);

        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
            this.authorization = apiResponse.getData().getToken();
        }

        return apiResponse;
    }

    
    /**
     * 登录数电发票平台
     * @param nsrsbh 纳税人识别号
     * @param username 用户名
     * @param password 密码
     * @param sms 短信验证码
     * @return 登录响应
     * @throws Exception 请求异常
     */
    public ApiResponse<String> loginDppt(String nsrsbh, String username, String password, String sms) throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("nsrsbh", nsrsbh);
        formData.put("username", username);
        formData.put("password", password);
        
        if (sms != null && !sms.isEmpty()) {
            formData.put("sms", sms);
        }
        
        HttpResponse<String> response = httpClient.post("/v5/enterprise/loginDppt", formData, authorization);
        return ApiResponse.fromJson(response.body());
    }
    
    /**
     * 获取人脸二维码
     * @param nsrsbh 纳税人识别号
     * @param username 用户名
     * @param type 类型
     * @return 二维码响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> getFaceImg(String nsrsbh, String username, String type) throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("nsrsbh", nsrsbh);
        
        if (username != null && !username.isEmpty()) {
            queryParams.put("username", username);
        }
        
        if (type != null && !type.isEmpty()) {
            queryParams.put("type", type);
        }
        
        HttpResponse<String> response = httpClient.get("/v5/enterprise/getFaceImg", queryParams, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 获取人脸二维码认证状态
     * @param nsrsbh 纳税人识别号
     * @param rzid 认证ID
     * @param username 用户名
     * @param type 类型
     * @return 认证状态响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> getFaceState(String nsrsbh, String rzid, String username, String type) throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("nsrsbh", nsrsbh);
        queryParams.put("rzid", rzid);
        
        if (username != null && !username.isEmpty()) {
            queryParams.put("username", username);
        }
        
        if (type != null && !type.isEmpty()) {
            queryParams.put("type", type);
        }
        
        HttpResponse<String> response = httpClient.get("/v5/enterprise/getFaceState", queryParams, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 获取认证状态
     * @param nsrsbh 纳税人识别号
     * @param username 用户名
     * @return 认证状态响应
     * @throws Exception 请求异常
     */
    public ApiResponse<String> queryFaceAuthState(String nsrsbh, String username) throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("nsrsbh", nsrsbh);
        
        if (username != null && !username.isEmpty()) {
            formData.put("username", username);
        }
        
        HttpResponse<String> response = httpClient.post("/v5/enterprise/queryFaceAuthState", formData, authorization);
        return ApiResponse.fromJson(response.body());
    }
    
    /**
     * 数电蓝票开具接口
     * @param params 开票参数
     * @return 开票响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> blueTicket(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/blueTicket", params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }


    
    /**
     * 获取销项数电版式文件
     * @param params 查询参数
     * @return 版式文件响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> getPdfOfdXml(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/pdfOfdXml", params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 数电申请红字前查蓝票信息接口
     * @param params 查询参数
     * @return 蓝票信息响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> queryBlueTicketInfo(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/retMsg", params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 申请红字信息表
     * @param params 申请参数
     * @return 申请响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> applyRedInfo(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/hzxxbsq", params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 数电票开负数发票
     * @param params 开票参数
     * @return 开票响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> redTicket(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/hzfpkj", params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 切换电子税务局账号
     * @param params 切换参数
     * @return 切换响应
     * @throws Exception 请求异常
     */
    public ApiResponse<String> switchAccount(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/switchAccount", params, authorization);
        return ApiResponse.fromJson(response.body());
    }
    
    /**
     * 授信额度查询
     * @param params 查询参数
     * @return 查询响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> queryCreditLimit(Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post("/v5/enterprise/creditLine", params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }

    /**
     * 查询list
     * @param params 查询参数
     * @return 查询响应
     * @throws Exception 请求异常
     */
    public ApiResponse<List<Map<String, Object>>> queryList(String path,Map<String, Object> params,String method) throws Exception {
        Map<String, String> param = new HashMap<>();
        if (Objects.equals(method, "GET")) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                param.put(entry.getKey(), entry.getValue().toString());
            }
        }

        HttpResponse<String> response = Objects.equals(method, "POST") ? httpClient.post(path, params, authorization):httpClient.get(path, param, authorization);
        return ApiResponse.fromJsonListMap(response.body());
    }

    /**
     *  发起post请求
     * @param params 查询参数
     * @return 响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> httpPost(String path,Map<String, Object> params) throws Exception {
        HttpResponse<String> response = httpClient.post(path, params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }

    /**
     * 发起get请求
     * @param params 类型
     * @return 响应
     * @throws Exception 请求异常
     */
    public ApiResponse<Map<String, Object>> httpGet(String path,Map<String, String> params) throws Exception {
        HttpResponse<String> response = httpClient.get(path, params, authorization);
        return ApiResponse.fromJsonMap(response.body());
    }
    
    /**
     * 设置授权令牌
     * @param authorization 授权令牌
     */
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
    
    /**
     * 获取授权令牌
     * @return 授权令牌
     */
    public String getAuthorization() {
        return authorization;
    }
}