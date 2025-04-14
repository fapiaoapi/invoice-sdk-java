# 发票SDK

这是一个用于对接发票接口(数电发票)的java SDK，支持发票开具、红冲、查询等功能。
发票 Java SDK 电子发票/数电发票/全电发票/数电票/开票
基础

[中文文档](https://github.com/fapiaoapi/invoice "文档")

* 获取授权
* 登录数电发票平台
* 获取人脸二维码
* 获取人脸二维码认证状态
* 获取认证状态

发票开具

* 数电蓝票开具接口
* 获取销项数电版式文件

发票红冲

* 申请红字前查蓝票信息接口
* 申请红字信息表
* 开负数发票

## 安装

通过maven安装:
[maven地址](https://central.sonatype.com/artifact/io.github.fapiaoapi/invoice "发票sdk")

```bash
<dependency>
    <groupId>io.github.fapiaoapi</groupId>
    <artifactId>invoice</artifactId>
    <version>1.0.3</version>
</dependency>
```
```bash
import tax.invoice.InvoiceClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;
import tax.invoice.util.OtherUtil;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * 数电发票SDK使用示例
 */
public class InvoiceExample {

    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            // 配置信息
            String appKey = "";
            String appSecret = "";

            String nsrsbh = "91500112MADFAQ9xxx";//统一社会信用代码
            String title = "重庆悦江河科技有限公司";//名称（营业执照）
            String username = "19122840406";//手机号码（电子税务局）
            String password = "";//个人用户密码（电子税务局）
            String sf = "01";//身份（电子税务局）
            String fphm = "24502000000045823936";

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
//                    //授信额度查询
//                    Map<String, String> creditParams = new HashMap<>();
//                    creditParams.put("nsrsbh", nsrsbh);
//                    creditParams.put("username", username);
//                    ApiResponse<Map<String, Object>> creditLimitResponse = client.queryCreditLimit(creditParams);
//                    System.out.println(creditLimitResponse.getCode());
//                    if (creditLimitResponse.getCode() == 200) {
//                        System.out.println(creditLimitResponse.getData());
//                    }


                    // 开具蓝票示例
                    Map<String, String> invoiceParams = new HashMap<>();
                    invoiceParams.put("fpqqlsh", appKey + System.currentTimeMillis());
                    invoiceParams.put("fplxdm", "82");
                    invoiceParams.put("kplx", "0");
                    invoiceParams.put("xhdwsbh", nsrsbh);
                    invoiceParams.put("xhdwmc", title);
                    invoiceParams.put("xhdwdzdh", "重庆市渝北区仙桃街道汇业街1号17-2 19122840xxxx");
                    invoiceParams.put("xhdwyhzh", "中国工商银行 310008670920023xxxx");

                    invoiceParams.put("ghdwmc", "个人");
//                    invoiceParams.put("ghdwsbh", "914401017397375296");
                    invoiceParams.put("zsfs", "0");

//                    double amount = 200;
//                    double taxRate = 0.01;
//                    boolean isIncludeTax = true;//是否含税
//                    //税额计算
//                    String se =  OtherUtil.calculateTax(amount,taxRate,isIncludeTax);
//                    System.out.println("价税合计："+amount);
//                    System.out.println("税率："+taxRate);
//                    System.out.println("合计金额："+(amount-Double.parseDouble(se)));
//                    System.out.println((isIncludeTax ? "含税" : "不含税")+" 合计税额："+se);

                    // 添加商品明细
                    invoiceParams.put("fyxm[0][fphxz]", "0");
                    invoiceParams.put("fyxm[0][spmc]", "*信息技术服务*软件开发服务");
                    invoiceParams.put("fyxm[0][je]", "10");
                    invoiceParams.put("fyxm[0][sl]", "0.01");
                    invoiceParams.put("fyxm[0][se]", "0.1");
                    invoiceParams.put("fyxm[0][hsbz]", "1");
                    invoiceParams.put("fyxm[0][spbm]", "3040201010000000000");

                    // 合计金额
                    invoiceParams.put("hjje", "9.9");
                    invoiceParams.put("hjse", "0.1");
                    invoiceParams.put("jshj", "10");

                    ApiResponse<Map<String, Object>> invoiceResponse = client.blueTicket(invoiceParams);
                    System.out.println(invoiceResponse.getCode()+ "开票结果: " + invoiceResponse.getMsg());

                    if (invoiceResponse.isSuccess()) {
                        System.out.println("发票号码: " + invoiceResponse.getData().get("Fphm"));
                        System.out.println("开票日期: " + invoiceResponse.getData().get("Kprq"));
                        fphm = invoiceResponse.getData().get("Fphm").toString();
                        sleep(10000);
                    }
                    //下载发票
                    Map<String, String> pdfParams = new HashMap<>();
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
//                        for (Map.Entry<String, Object> entry : pdfResponse.getData().entrySet()) {
//                            System.out.println(entry.getKey() + ": " + entry.getValue());
//                        }
                    }

                    break;
                case 420:
                    System.out.println("登录(短信认证)");
                    // 登录数电发票平台 短信

//                    //1 发短信验证码
//                    String smsCode = "";
//                    ApiResponse<String> loginResponse = client.loginDppt(nsrsbh, username, password,null);
//                    if(loginResponse.isSuccess()){
//                        System.out.println(loginResponse.getMsg());
//                        System.out.println("请"+username+"接收验证码");
//                        sleep(60000);//模拟 等待60秒
//                    }
//
//                    //2 输入验证码
//                    System.out.println("请输入验证码");
//                    ApiResponse<String> loginResponse2 = client.loginDppt(nsrsbh, username, password, smsCode);
//                    if(loginResponse2.isSuccess()){
//                        System.out.println(loginResponse2.getData());
//                        System.out.println("验证成功");
//                    }
                    break;
                case 430:
                    System.out.println("人脸认证");
                    //1 获取人脸二维码
                    ApiResponse<Map<String, Object>> qrCodeResponse = client.getFaceImg(nsrsbh, username, "1");
                    System.out.println("二维码: " + qrCodeResponse.getData());
                    //
                    switch (qrCodeResponse.getData().get("ewmlyx").toString()) {
                        case "swj" -> System.out.println("请使用税务局app扫码");//ewm自己生成二维码
                        case "grsds" -> System.out.println("个人所得税app扫码"); //ewm是二维码的base64
                    }

                    //2  认证完成后  获取人脸二维码认证状态
                    String rzid = qrCodeResponse.getData().get("rzid").toString();
//                    String rzid = "5c028e62f23e4b5ca57668bc74c0de98";
                    ApiResponse<Map<String, Object>> faceStatusResponse = client.getFaceState(nsrsbh, rzid, username, "1");
                    System.out.println("code: " + faceStatusResponse.getCode());
                    System.out.println("data: " + faceStatusResponse.getData());
                    if(faceStatusResponse.getData() != null){
                        switch (faceStatusResponse.getData().get("slzt").toString()) {
                            case "1" -> System.out.println("未认证");
                            case "2" -> System.out.println("成功");
                            case "3" -> System.out.println("二维码过期-->重新获取人脸二维码");
                        }
                    }
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
```

[发票红冲](src/main/java/tax/invoice/example/RedInvoiceExample.java "发票红冲")


