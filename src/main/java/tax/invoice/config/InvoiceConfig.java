package tax.invoice.config;

/**
 * 数电发票 SDK 配置类
 */
public class InvoiceConfig {
    private String baseUrl = "https://api.fa-piao.com";
    private String appKey;
    private String appSecret;
    
    public InvoiceConfig(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }
    
    public InvoiceConfig(String baseUrl, String appKey, String appSecret) {
        this.baseUrl = baseUrl;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }
}