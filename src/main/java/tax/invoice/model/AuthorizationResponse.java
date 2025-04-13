package tax.invoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 授权响应模型
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizationResponse {
    private String token;
    
    public AuthorizationResponse() {
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}