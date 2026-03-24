package tax.invoice.example;

import java.io.PrintStream;
import tax.invoice.InvoiceClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Base64;


public class BasicExample {
    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            // 配置信息
            String appKey = "";
            String appSecret = "";

            String nsrsbh = "91500112MADFAXXXX";//统一社会信用代码
            String title = "XXXX科技有限公司";//名称（营业执照）
            String username = "1325580XXXX";//手机号码（电子税务局）
            String password = "123456XXXX";//个人用户密码（电子税务局）
            String sf = "01";//身份（电子税务局）
            String fphm = "";
            String kprq = "";
            String token = "";

            String type = "6";//6 基础 7标准
            // Redis配置

            /*
              <dependency>
                  <groupId>redis.clients</groupId>
                  <artifactId>jedis</artifactId>
                  <version>3.9.0</version>
              </dependency>
             */
            Jedis redis = new Jedis("127.0.0.1", 6379);
            redis.auth("test123456");


            // 创建客户端
            InvoiceClient client = new InvoiceClient(appKey, appSecret);

            // 从Redis获取Token
            String redisKey = nsrsbh + "@"+username+ "@TOKEN";
            token = redis.get(redisKey);
            if (token != null && !token.isEmpty()) {
                client.setAuthorization(token);
                System.out.println("Token From Redis: ");
            } else {
                //一 获取授权
                /*
                 * 获取授权Token文档
                 * @see https://fa-piao.com/doc.html#api1?source=github
                 */
                ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh,type);
//                ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh,type,username,password);
                if (authResponse.isSuccess()) {
                    token = authResponse.getData().getToken();
                    client.setAuthorization(token);
                    redis.setex(redisKey, 3600 * 24 * 30, token); // 设置30天过期
                    System.out.println("授权成功，Token: " + token);
                } else {
                    System.out.println("授权失败: " + authResponse.getMsg());
                    return;
                }
            }

            try {
                /*
                 * 前端模拟数电发票/电子发票开具 (蓝字发票)
                 * @see https://fa-piao.com/fapiao.html?source=github
                 *
                 */

                /*
                 *
                 * 开票参数说明demo
                 * @see https://github.com/fapiaoapi/invoice-sdk-java/blob/master/examples/TaxExample.java
                 */
                    //开具蓝票参数
                    Map<String, Object> invoiceParams = new LinkedHashMap<>();
                    invoiceParams.put("fplxdm", "82");
                    invoiceParams.put("fpqqlsh", appKey + System.currentTimeMillis());
                    invoiceParams.put("ghdwmc", "个人");
//                    invoiceParams.put("ghdwsbh", "914208XXXXXXX");
                    invoiceParams.put("hjje", 396.04);
                    invoiceParams.put("hjse", 3.96);
                    invoiceParams.put("jshj", 400);
                    invoiceParams.put("kplx", 0);
                    invoiceParams.put("username", username);
                    invoiceParams.put("xhdwdzdh", "重庆市渝北区龙溪街道丽园路2号XXXX 1325580XXXX");
                    invoiceParams.put("xhdwmc", title);
                    invoiceParams.put("xhdwsbh", nsrsbh);
                    invoiceParams.put("xhdwyhzh", "工商银行XXXX 15451211XXXX");
                    invoiceParams.put("zsfs", 0);
                    invoiceParams.put("fyxm[0][fphxz]", 0);
                    invoiceParams.put("fyxm[0][spmc]", "*软件维护服务*接口服务费");
                    invoiceParams.put("fyxm[0][ggxh]", "");
                    invoiceParams.put("fyxm[0][dw]", "次");
                    invoiceParams.put("fyxm[0][spsl]", 100);
                    invoiceParams.put("fyxm[0][dj]", 1);
                    invoiceParams.put("fyxm[0][je]", 100);
                    invoiceParams.put("fyxm[0][sl]", 0.01);
                    invoiceParams.put("fyxm[0][se]", 0.99);
                    invoiceParams.put("fyxm[0][hsbz]", 1);
                    invoiceParams.put("fyxm[0][spbm]", "3040201030000000000");
                    invoiceParams.put("fyxm[1][fphxz]", 0);
                    invoiceParams.put("fyxm[1][spmc]", "*软件维护服务*接口服务费");
                    invoiceParams.put("fyxm[1][ggxh]", "");
                    invoiceParams.put("fyxm[1][spsl]", 150);
                    invoiceParams.put("fyxm[1][dj]", 2);
                    invoiceParams.put("fyxm[1][je]", 300);
                    invoiceParams.put("fyxm[1][sl]", 0.01);
                    invoiceParams.put("fyxm[1][se]", 2.97);
                    invoiceParams.put("fyxm[1][hsbz]", 1);
                    invoiceParams.put("fyxm[1][spbm]", "3040201030000000000");

                //二 开具蓝票
                /*
                 * 开具数电发票文档
                 * @see https://fa-piao.com/doc.html#api6?source=github
                 *
                 */
                ApiResponse<Map<String, Object>> invoiceResponse = client.blueTicket(invoiceParams);
                switch (invoiceResponse.getCode()){
                        case 200:
                            Map<String, Object> invoiceData = invoiceResponse.getData();
                            if (invoiceData == null || invoiceData.get("Fphm") == null || invoiceData.get("Kprq") == null) {
                                System.out.println("开票成功但返回字段缺失: " + invoiceResponse.getData());
                                break;
                            }
                            fphm = invoiceData.get("Fphm").toString();
                            kprq = invoiceData.get("Kprq").toString();
                            System.out.println("发票号码: " + fphm);
                            System.out.println("开票日期: " + kprq);

                            //三 下载发票
                            Map<String, Object> pdfParams = new HashMap<>();
                            pdfParams.put("downflag", "4");
                            pdfParams.put("nsrsbh", nsrsbh);
                            pdfParams.put("username", username);
                            pdfParams.put("fphm", fphm);
                            pdfParams.put("Kprq", kprq);

                            ApiResponse<Map<String, Object>> pdfResponse = client.getPdfOfdXml(pdfParams);
                            if (pdfResponse.isSuccess()) {
                                System.out.println("发票下载成功");
                                System.out.println(pdfResponse.getData());
                            }
                            break;

                        case 420:
                            System.out.println("登录(短信认证)");
                            /*
                             * 前端模拟短信认证弹窗
                             * @see https://fa-piao.com/fapiao.html?action=sms&source=github
                             */
                            // 1. 发短信验证码
                            /*
                             * @see https://fa-piao.com/doc.html#api2?source=github
                             */
                            // ApiResponse<Map<String, Object>> loginResponse = client.loginDppt(nsrsbh, username, password, "");
                            // if (loginResponse.getCode() == 200) {
                            //     System.out.println(loginResponse.getMsg());
                            //     System.out.println("请" + username + "接收验证码");
                            //     try {
                            //         sleep(60000); // 等待60秒
                            //     } catch (InterruptedException ex) {
                            //         Thread.currentThread().interrupt();
                            //     }
                            // }
                            // 2. 输入验证码
                            /*
                             * @see https://fa-piao.com/doc.html#api2?source=github
                             */
                            // System.out.println("请输入验证码");
                            // String smsCode = ""; // 这里应该获取用户输入的验证码
                            // ApiResponse<Map<String, Object>> loginResponse2 = client.loginDppt(nsrsbh, username, password, smsCode);
                            // if (loginResponse2.getCode() == 200) {
                            //     System.out.println(loginResponse2.getData());
                            //     System.out.println("验证成功");
                            // }
                            break;

                        case 430:
                            System.out.println("人脸认证");
                            /*
                             * 前端模拟人脸认证弹窗
                             * @see https://fa-piao.com/fapiao.html?action=face&source=github
                             */
                            // 1. 获取人脸二维码
                            /*
                             * @see https://fa-piao.com/doc.html#api3?source=github
                             */
//                             ApiResponse<Map<String, Object>> qrCodeResponse = client.getFaceImg(nsrsbh, username, "1");
//                             Map<String, Object> qrData = qrCodeResponse.getData();
//                             if (qrData == null) {
//                                 System.out.println("人脸二维码返回为空: " + qrCodeResponse.getMsg());
//                                 break;
//                             }
//                             Object ewmlyObj = qrData.get("ewmly");
//                             String ewmly = ewmlyObj == null ? "" : ewmlyObj.toString();
//                             System.out.println("swj".equals(ewmly) ? "请使用税务局app扫码" : "个人所得税app扫码");
//                             Object ewmObj = qrData.get("ewm");
//                             if (ewmObj != null && ewmObj.toString().length() < 500) {
//                                 // 需要引入二维码生成库来构建二维码
//                                 /*
//                                    <dependency>
//                                        <groupId>com.google.zxing</groupId>
//                                        <artifactId>core</artifactId>
//                                        <version>3.5.3</version>
//                                    </dependency>
//                                    <dependency>
//                                        <groupId>com.google.zxing</groupId>
//                                        <artifactId>javase</artifactId>
//                                        <version>3.5.3</version>
//                                    </dependency>
//                                  */
//                                String base64 = toBase64((String) ewmObj,300);
//                                qrData.put("ewm", base64);
//                                 System.out.println("data:image/png;base64," + base64);
//                                 // String base64Uri = "data:image/png;base64," + base64;
//                                 // 前端使用示例: <img src="base64Uri" />
//                             }

                            // 2. 认证完成后获取人脸二维码认证状态
                            /*
                             * @see https://fa-piao.com/doc.html#api4?source=github
                             */
                            // String rzid = qrCodeResponse.getData().get("rzid").toString();
                            // ApiResponse<Map<String, Object>> faceStatusResponse = client.getFaceState(nsrsbh, rzid, username, "1");
                            // System.out.println("code: " + faceStatusResponse.getCode());
                            // System.out.println("data: " + faceStatusResponse.getData());
                            //
                            // if (faceStatusResponse.getData() != null) {
                            //     String slzt = faceStatusResponse.getData().get("slzt").toString();
                            //     String status = "1".equals(slzt) ? "未认证" : ("2".equals(slzt) ? "成功" : "二维码过期");
                            //     System.out.println("认证状态: " + status);
                            // }
                            break;
                        case 401:
                            // token过期 重新获取并缓存token
                            System.out.println("授权失败:" + invoiceResponse.getMsg());
                            // 重新获取token的逻辑
                            break;
                        default:
//                            System.out.println("参数:" + invoiceParams);
                            System.out.println(invoiceResponse.getCode() + " " + invoiceResponse.getMsg());
                            break;
                }

            } catch (Exception e) {
                // 处理开票异常
                String errorMsg = e.getMessage();
                System.out.println("错误: " + errorMsg);
            }

        } catch (Exception e) {
            System.out.println("系统错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static String toBase64(String text, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }

}
