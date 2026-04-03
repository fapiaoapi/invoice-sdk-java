package tax.invoice.example;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import redis.clients.jedis.Jedis;
import tax.invoice.InvoiceClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;


public class InvoicePlusExample {
    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.out.println("java " + System.getProperty("java.version"));
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() ->
                    new Scanner(System.in).nextLine()
            );
            // 配置信息
            String appKey = "";
            String appSecret = "";

            String nsrsbh = "";//统一社会信用代码
            String title = "";//名称（营业执照）
            String username = "";//手机号码（电子税务局）
            String password = "";//个人用户密码（电子税务局）
            String sf = "01";//身份（电子税务局）
            String fphm = "460102198305042114";
            String kprq = "";
            String token = "";

            String type = "7";//6 基础 7标准
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
                    System.out.println(authResponse.getCode()+ "授权失败: " + authResponse.getMsg());
                    return;
                }
            }

            try {

                /*
                 *
                 *二 获取认证状态
                 * @see https://fa-piao.com/doc.html#api5?source=github
                 */
             ApiResponse<Map<String, Object>> statusResponse = client.queryFaceAuthState(nsrsbh, username);
             switch (statusResponse.getCode()){
                 case 200:
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
                     invoiceParams.put("ghdwmc", "张四");
//                    invoiceParams.put("ghdwsbh", "914208XXXXXXX");
                     invoiceParams.put("hjje", 97.9);
                     invoiceParams.put("hjse", 0.1);
                     invoiceParams.put("jshj", 100);
                     invoiceParams.put("kplx", 0);
                     invoiceParams.put("username", username);
                     invoiceParams.put("xhdwdzdh", "重庆市渝北区龙溪街道丽园路2号XXXX 1325580XXXX");
                     invoiceParams.put("xhdwmc", title);
                     invoiceParams.put("xhdwsbh", nsrsbh);
                     invoiceParams.put("xhdwyhzh", "工商银行XXXX 15451211XXXX");
                     invoiceParams.put("zsfs", 0);
                     invoiceParams.put("fyxm[0][fphxz]", 0);
                     invoiceParams.put("fyxm[0][spmc]", "*玩具*益智玩具");
//                     invoiceParams.put("fyxm[0][ggxh]", "");
//                     invoiceParams.put("fyxm[0][dw]", "个");
//                     invoiceParams.put("fyxm[0][spsl]", 1);
//                     invoiceParams.put("fyxm[0][dj]", 10);
                     invoiceParams.put("fyxm[0][je]", 10);
                     invoiceParams.put("fyxm[0][sl]", 0.01);
                     invoiceParams.put("fyxm[0][se]", 0.1);
                     invoiceParams.put("fyxm[0][hsbz]", 1);
                     invoiceParams.put("fyxm[0][spbm]", "1060408990000000000");

                     //三 开具蓝票
                     /*
                      * 开具数电发票文档
                      * @see https://fa-piao.com/doc.html#api6?source=github
                      *
                      */
                     ApiResponse<Map<String, Object>> invoiceResponse = client.blueTicket(invoiceParams);
                     Map<String, Object> invoiceData = invoiceResponse.getData();
                     if (!invoiceResponse.isSuccess()) {
                         System.out.println(invoiceResponse.getCode()+"开票失败: " + invoiceResponse.getMsg());
                         break;
                     }
                     if (invoiceData == null || invoiceData.get("Fphm") == null || invoiceData.get("Kprq") == null) {
                         System.out.println(invoiceResponse.getCode()+"开票成功但返回字段缺失: " + invoiceResponse.getData());
                         break;
                     }
                     fphm = invoiceData.get("Fphm").toString();
                     kprq = invoiceData.get("Kprq").toString();
                     System.out.println("发票号码: " + fphm);
                     System.out.println("开票日期: " + kprq);

                     //四 下载发票
                     /*
                      * 获取销项数电版式文件
                      * @see https://fa-piao.com/doc.html#api7?source=github
                      *
                      */
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
                     System.out.println("420 登录(短信认证)");
                     /*
                      * 前端模拟短信认证弹窗
                      * @see https://fa-piao.com/fapiao.html?action=sms&source=github
                      */
                     // 1. 发短信验证码
                     /*
                      * @see https://fa-piao.com/doc.html#api2?source=github
                      */
                     ApiResponse<Map<String, Object>> loginResponse = client.loginDppt(nsrsbh, username, password, "");
                     if (loginResponse.getCode() == 200) {
                         System.out.println(loginResponse.getMsg());
                         System.out.println("请" + username + "接收验证码");
                         System.out.println("请输入验证码");
                         try {
                             System.out.print("300秒内("+ LocalDateTime.now().plusSeconds(300).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"前)输入内容: ");
                             String smsCode = future.get(300, TimeUnit.SECONDS);;
                             System.out.println("🎉 成功获取输入: " + smsCode+" \n"+ "当前时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                             // 2. 输入验证码
                             /*
                              * @see https://fa-piao.com/doc.html#api2?source=github
                              */
                             ApiResponse<Map<String, Object>> loginResponse2 = client.loginDppt(nsrsbh, username, password, smsCode);
                             if (loginResponse2.getCode() == 200) {
                                 System.out.println(loginResponse2.getData());
                                 System.out.println("验证成功");
                                 System.out.println("请再次调用client.blueTicket");
                             }else{
                                 System.out.println(loginResponse2.getCode()+"验证失败: " + loginResponse2.getMsg());
                             }
                         } catch (TimeoutException | InterruptedException | ExecutionException e) {
                             System.out.println("\n短信认证输入超时！");
                             future.cancel(true);
                         }
                         executor.shutdown();
                     }else{
                         System.out.println(loginResponse.getCode()+"发短信验证码失败: " + loginResponse.getMsg());
                     }

                     break;

                 case 430:
                     System.out.println("430 人脸认证");
                     /*
                      * 前端模拟人脸认证弹窗
                      * @see https://fa-piao.com/fapiao.html?action=face&source=github
                      */
                     // 1. 获取人脸二维码
                     /*
                      * @see https://fa-piao.com/doc.html#api3?source=github
                      */
                     ApiResponse<Map<String, Object>> qrCodeResponse = client.getFaceImg(nsrsbh, username, "1");
                     Map<String, Object> qrData = qrCodeResponse.getData();
                     if (qrData == null) {
                         System.out.println("人脸二维码返回为空: " + qrCodeResponse.getMsg());
                         break;
                     }
                     Object ewmlyObj = qrData.get("ewmly");
                     String ewmly = ewmlyObj == null ? "" : ewmlyObj.toString();
                     System.out.println("swj".equals(ewmly) ? "请使用税务局app扫码" : "个人所得税app扫码");
                     Object ewmObj = qrData.get("ewm");
                     if (ewmObj != null && ewmObj.toString().length() < 500) {
                         // 需要引入二维码生成库来构建二维码
                                 /*
                                    <dependency>
                                        <groupId>com.google.zxing</groupId>
                                        <artifactId>core</artifactId>
                                        <version>3.5.3</version>
                                    </dependency>
                                    <dependency>
                                        <groupId>com.google.zxing</groupId>
                                        <artifactId>javase</artifactId>
                                        <version>3.5.3</version>
                                    </dependency>
                                  */
//                         String base64 = toBase64((String) ewmObj,300);
//                         qrData.put("ewm", base64);
//                         System.out.println("data:image/png;base64," + base64);
                         // String base64Uri = "data:image/png;base64," + base64;
                         // 前端使用示例: <img src="base64Uri" />

                     }
                     printQR((String) ewmObj);
                     System.out.println("成功做完人脸认证,请输入数字1");
                     try {
                         System.out.print("300秒内("+ LocalDateTime.now().plusSeconds(300).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"前)输入内容: ");
                         String inputNum = future.get(300, TimeUnit.SECONDS);
                         System.out.println("🎉 成功获取输入: " + inputNum+" \n"+ "当前时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                         // 2. 认证完成后获取人脸二维码认证状态
                         /*
                          * @see https://fa-piao.com/doc.html#api4?source=github
                          */
                         String rzid = qrCodeResponse.getData().get("rzid").toString();
                         ApiResponse<Map<String, Object>> faceStatusResponse = client.getFaceState(nsrsbh, rzid, username, "1");
                         System.out.println("code: " + faceStatusResponse.getCode());
                         System.out.println("data: " + faceStatusResponse.getData());
                         if (faceStatusResponse.getData() != null) {
                             String slzt = faceStatusResponse.getData().get("slzt").toString();
                             if ("2".equals(slzt)){
                                 System.out.println("认证状态: 成功");
                                 System.out.println("请再次调用client.blueTicket");
                             }else{
                                 System.out.println("认证状态: " +("1".equals(slzt) ? "未认证" : "二维码过期"));
                             }
                         }
                     } catch (TimeoutException | InterruptedException | ExecutionException e) {
                         System.out.println("\n人脸认证输入超时！");
                         future.cancel(true);
                     }
                     executor.shutdown();
                     break;
                 case 401:
                     // token过期 重新获取并缓存token
                     System.out.println(statusResponse.getCode()+"授权失败:" + statusResponse.getMsg());
                     System.out.println("401  token过期 重新获取并缓存token");
                     System.out.println("再调用client.blueTicket");
                     break;
                 default:
                     System.out.println(statusResponse.getCode() + "异常" + statusResponse.getMsg());
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

    public static void printQR(String content) {
        try {
            // 1. 计算容错率
            ErrorCorrectionLevel ecLevel = content.length() > 50 ? ErrorCorrectionLevel.L : ErrorCorrectionLevel.M;

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel);
            // 默认边距设为1，保证紧凑
            hints.put(EncodeHintType.MARGIN, 1);

            // 2. 【关键修改】尺寸缩小1倍
            // 将 requestedWidth/Height 从 1 改为 2。
            // 这会让 ZXing 生成更紧凑的矩阵，而不是强制最小尺寸（最小尺寸通常模块很大）。
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 2, 2, hints);

            int width = matrix.getWidth();

            // 3. 警告信息
            if (width > 55) {
                System.out.println("⚠️ 警告：内容过长，二维码尺寸过大(" + width + "x" + width + ")，可能无法识别。建议使用短链接！");
            }

//            System.out.println("👇 请扫描下方二维码 (内容长度: " + content.length() + ") 👇");

            // 4. 【关键修改】统一渲染逻辑，强制正方形
            // 使用 Unicode 块字符 (▀▄) 可以在一行内表示两个像素的高度，
            // 这样既保证了输出是正方形（避免终端行高导致的变形），又保持了高分辨率。
            for (int y = 0; y < width; y += 2) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    // 获取当前行 (Top) 和下一行 (Bottom) 的像素状态
                    boolean isTop = matrix.get(x, y);
                    boolean isBottom = false;

                    // 防止数组越界（当高度为奇数时）
                    if (y + 1 < width) {
                        isBottom = matrix.get(x, y + 1);
                    }

                    // 根据上下两个像素的状态选择字符
                    if (isTop && isBottom)      line.append("██"); // 上下都是黑
                    else if (isTop)             line.append("▀▀"); // 上黑下白
                    else if (isBottom)          line.append("▄▄"); // 上白下黑
                    else                        line.append("  "); // 上下都是白
                }
                System.out.println(line);
            }

//            System.out.println("👆 扫描结束 👆");

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }




}
