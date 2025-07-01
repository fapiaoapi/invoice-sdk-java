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
public class BatchInvoiceExample {

    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            // 配置信息
            String appKey = "";
            String appSecret = "";

            String nsrsbh = "914419005901XXXXX";//统一社会信用代码
            String title = "XXX技术有限公司";//名称（营业执照）
            String username = "18973993XXXX";//手机号码（电子税务局）
            String password = "123424XXXX";//个人用户密码（电子税务局）
            String sf = "09";//身份（电子税务局）
            String fphm = "";

            String token = "";


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

                     // 开具蓝票示例
                     Map<String, Object> invoiceParams = new LinkedHashMap<>();
                     invoiceParams.put("fpqqlsh", appKey + System.currentTimeMillis());
                     invoiceParams.put("fplxdm", "82");  //
                     invoiceParams.put("kplx", "0");
                     invoiceParams.put("xhdwsbh", nsrsbh);
                     invoiceParams.put("xhdwmc", title);
                     invoiceParams.put("xhdwdzdh", "东莞市松山湖高新技术产业开发区xxx 0769-85075xxx");
                     invoiceParams.put("xhdwyhzh", "中国工商银行xxxxx 2010027xxxx");

                     // 购货方信息更新
                     invoiceParams.put("ghdwmc", "xxxxxx电器有限公司");
                     invoiceParams.put("ghdwsbh", "914419005xxxxxxx");
                     invoiceParams.put("ghdwdzdh", "东莞市xxxx 0769-389xxxx");
                     invoiceParams.put("ghdwyhzh", "中国建设银行xxxx 440017781xxxx");
                     invoiceParams.put("zsfs", "0");
                     invoiceParams.put("kpr", "唐贵银");

                     // 商品明细
                     invoiceParams.put("fyxm[0][fphxz]", "0");
                     invoiceParams.put("fyxm[0][spmc]", "*鉴证咨询服务*认证费用");
                     invoiceParams.put("fyxm[0][spsl]", "1");
                     invoiceParams.put("fyxm[0][hsbz]", "1");
                     invoiceParams.put("fyxm[0][dj]", "3200");
                     invoiceParams.put("fyxm[0][je]", "3200");
                     invoiceParams.put("fyxm[0][sl]", "0.06");
                     invoiceParams.put("fyxm[0][se]", "181.13");
                     invoiceParams.put("fyxm[0][ggxh]", "-");
                     invoiceParams.put("fyxm[0][dw]", "次");
                     invoiceParams.put("fyxm[0][spbm]", "3040601000000000000");

                     // 添加第二个商品明细
                     invoiceParams.put("fyxm[1][fphxz]", "0");
                     invoiceParams.put("fyxm[1][spmc]", "*鉴证咨询服务*检测费");
                     invoiceParams.put("fyxm[1][spsl]", "1");
                     invoiceParams.put("fyxm[1][hsbz]", "1");
                     invoiceParams.put("fyxm[1][dj]", "1650");
                     invoiceParams.put("fyxm[1][je]", "1650");
                     invoiceParams.put("fyxm[1][sl]", "0.06");
                     invoiceParams.put("fyxm[1][se]", "93.4");
                     invoiceParams.put("fyxm[1][ggxh]", "-");
                     invoiceParams.put("fyxm[1][dw]", "次");
                     invoiceParams.put("fyxm[1][spbm]", "3040601000000000000");


                     // 更新合计金额
                     invoiceParams.put("hjje", "4575.47");
                     invoiceParams.put("hjse", "274.53");
                     invoiceParams.put("jshj", "4850");
//                     invoiceParams.put("bz", "--");  // 新增备注


                     ApiResponse<Map<String, Object>> invoiceResponse = client.blueTicket(invoiceParams);
                     System.out.println(invoiceResponse.getCode()+ "开票结果: " + invoiceResponse.getMsg());

                     if (invoiceResponse.isSuccess()) {
                         System.out.println("发票号码: " + invoiceResponse.getData().get("Fphm"));
                         System.out.println("开票日期: " + invoiceResponse.getData().get("Kprq"));
                         fphm = invoiceResponse.getData().get("Fphm").toString();
                         sleep(10000);
                     }
                     //下载发票
                     Map<String, Object> pdfParams = new LinkedHashMap<>();
                     pdfParams.put("downflag", "4");
                     pdfParams.put("nsrsbh", nsrsbh);
                     pdfParams.put("username", username);
 //                    pdfParams.put("fphm", invoiceResponse.getData().get("Fphm").toString());
 //                    pdfParams.put("Kprq", invoiceResponse.getData().get("Kprq").toString());
                     pdfParams.put("fphm", fphm);
                     ApiResponse<Map<String, Object>> pdfResponse = client.getPdfOfdXml(pdfParams);
                     if (pdfResponse.isSuccess()){
                         System.out.println(pdfResponse.getData());
                         // 循环打印pdfResponse.getData()内容
                         for (Map.Entry<String, Object> entry : pdfResponse.getData().entrySet()) {
                             System.out.println(entry.getKey() + ": " + entry.getValue());
                         }
                     }


                     break;
                 case 420:
                     System.out.println("需要登录(短信认证)");

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