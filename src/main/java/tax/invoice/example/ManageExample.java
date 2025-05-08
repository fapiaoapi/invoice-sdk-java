package tax.invoice.example;

import tax.invoice.InvoiceClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * 商户进件使用示例
 */
public class ManageExample {

    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));


            String appKey = "";
            String appSecret = "";

            String nsrsbh = "91500108MA619JCxx";//统一社会信用代码
            String title = "重庆xxx科技有限公司";//名称（营业执照）
            String username = "1912284xxxx";//手机号码（电子税务局）
            String password = "6668xxxx";//个人用户密码（电子税务局）
            String sf = "01";//身份（电子税务局）

            String token = "";

            // 创建客户端
            InvoiceClient client = new InvoiceClient(appKey, appSecret);
            if (!token.isEmpty()) {
                client.setAuthorization(token);
            } else {
                // 获取授权
                ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization("92500103MAD7F8H17D","5");
                if (authResponse.isSuccess()) {
                    System.out.println("授权成功，Token: " + authResponse.getData().getToken());
                }else{
                    System.out.println(authResponse.getCode()+":"+authResponse.getMsg());
                    return;
                }
            }

            // 添加
//            Map<String, String> addParams = new HashMap<>();
//            addParams.put("kpdwdm", nsrsbh);
//            addParams.put("kpdwmc", title);
//            addParams.put("prov", "重庆");
//            addParams.put("fwqlx", "4");
//            addParams.put("dzswjmm", username);
//            addParams.put("dzswjzh", password);
//            addParams.put("dzswjsf", sf);
//            addParams.put("insertFullFlag", "1");
//
//            ApiResponse<Map<String, Object>> addResponse = client.httpPost("/v5/manage/enterprise/add",addParams);
//            System.out.println(addResponse.getCode()+":"+addResponse.getMsg());
//            System.out.println(addResponse.getData());

//            // 编辑
//            Map<String, String> editParams = new HashMap<>();
//            editParams.put("kpdwdm", nsrsbh);
//            editParams.put("kpdwmc", title);
//            editParams.put("dzswjzh", username);
//            editParams.put("dzswjmm", password);
//            editParams.put("dzswjsf", sf);
//            editParams.put("fwqlx", "4");
//
//            ApiResponse<Map<String, Object>> editResponse = client.httpPost("/v5/manage/enterprise/edit",editParams);
//            System.out.println(editResponse.getCode()+":"+editResponse.getMsg());
//            System.out.println(editResponse.getData());

//            // 查询
//            Map<String, String> queryParams = new HashMap<>();
//            queryParams.put("kpdwdm", nsrsbh);
//            queryParams.put("kpdwmc", title);
//            queryParams.put("page", "1");
//            queryParams.put("limit", "10");
//            ApiResponse<List<Map<String, Object>>> queryResponse = client.queryList("/v5/manage/enterprise/query",queryParams,"POST");
//            System.out.println(queryResponse.getCode()+":"+queryResponse.getMsg());
//            System.out.println(queryResponse.getData());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}