package tax.invoice.example;

import tax.invoice.InvoiceClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * 数电发票SDK使用示例
 */
public class FaceLoginExample {

    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            // 配置信息
            String appKey = "";
            String appSecret = "";

            String title = "xxx技术有限公司";//名称（营业执照）
            String nsrsbh = "91500112MADFAQ9xxx";//统一社会信用代码
            String username = "1325580xxxx";//手机号码（电子税务局）
            String password = "123xxxx";//个人用户密码（电子税务局）
            String sf = "09";//身份（电子税务局）
            String ewmlx = "1";
            String ewmid = null;
            String qrcode = null;

            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAijoieXVlMDA1IiwibnNyc2JoNTJIjYifQ.ZVai9euLGVXJzUc6uT-40";


            // 创建客户端
             InvoiceClient client = new InvoiceClient(appKey, appSecret);
             if (token != null && !token.isEmpty()) {
                 client.setAuthorization(token);
             } else {
                 // 获取授权
                 ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh);
                 if (authResponse.isSuccess()) {
                     System.out.println("授权成功，Token: " + authResponse.getData().getToken());
                 }
             }

             // 获取认证状态
             ApiResponse<String> statusResponse = client.queryFaceAuthState(nsrsbh, username);
             switch (statusResponse.getCode()) {
                 case 200:
                     System.out.println("认证状态: 无需认证");

                     break;
                 case 420://短信或者人脸认证
                     System.out.println("需要登录(人脸认证)");
                     //1 人脸
                     ApiResponse<Map<String, Object>> loginResponse = client.FaceLoginDppt(nsrsbh, username, password,sf,ewmlx,null);

                     System.out.println("======face======"+loginResponse.getCode());
                     if(loginResponse.isSuccess()){
                         System.out.println("请"+username+"做人脸识别");
                         ewmid = loginResponse.getData().get("ewmid").toString();
                         //使用对应的app做人脸验证
                         qrcode = loginResponse.getData().get("qrcode").toString();
                         sleep(10000);//模拟 等待10秒
                         // 2 验证是否成
                         ApiResponse<Map<String, Object>> loginResponse2 = client.FaceLoginDppt(nsrsbh, username, password,sf,ewmlx,ewmid);

                         if(loginResponse2.isSuccess()){
                             System.out.println("====登录(人脸认证)成功====");
                         }else{
                             System.out.println(loginResponse2.getCode()+":"+loginResponse2.getMsg());
                         }
                         System.out.println("====face==result====2====");
                     }

                     break;
                 case 430:
                     System.out.println("需要人脸认证");
                     break;
                 case 401:
                     //重新授权
                     System.out.println(statusResponse.getCode()+"授权失败:"+statusResponse.getMsg());
                     break;
                 default:
                     System.out.println(statusResponse.getCode()+"  "+statusResponse.getMsg());
                     break;
             }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}