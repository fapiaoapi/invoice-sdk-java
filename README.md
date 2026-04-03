# 电子发票/数电发票 Java SDK | 开票、验真、红冲一站式集成

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fapiaoapi/invoice?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.fapiaoapi/invoice)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/fapiaoapi/invoice-sdk-java/blob/master/LICENSE)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://docs.oracle.com/en/java/javase/17/)

**发票 Java SDK** 专为电子发票、数电发票（全电发票）场景设计，支持**开票、红冲、版式文件下载**等核心功能，快速对接税务平台API。

**关键词**: 电子发票SDK,数电票Java,开票接口,发票api,发票开具,发票红冲,全电发票集成

---

## 📖 核心功能

### 基础认证
- ✅ **获取授权** - 快速接入税务平台身份认证
- ✅ **人脸二维码登录** - 支持数电发票平台扫码登录
- ✅ **认证状态查询** - 实时获取纳税人身份状态

### 发票开具
- 🎫 **数电蓝票开具** - 支持增值税普通/专用电子发票
- 📄 **版式文件下载** - 自动获取销项发票PDF/OFD/XML文件

### 发票红冲
- 🔍 **红冲前蓝票查询** - 精确检索待红冲的电子发票
- 🛑 **红字信息表申请** - 生成红冲凭证
- 🔄 **负数发票开具** - 自动化红冲流程

---

## 🚀 快速安装

### Maven
```xml
<dependency>
    <groupId>io.github.fapiaoapi</groupId>
    <artifactId>invoice</artifactId>
    <version>1.0.20</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.fapiaoapi:invoice:1.0.20'
```

[📦 查看Maven Central最新版本](https://central.sonatype.com/artifact/io.github.fapiaoapi/invoice)

本sdk仅支持Java 17+,其他版本请用旧版本


[java8-java16开发票demo](https://github.com/fapiaoapi/invoice/blob/master/BasicExample.java "java8-java16开发票demo")

[java8-java16红冲发票demo](https://github.com/fapiaoapi/invoice/blob/master/RedInvoiceExample.java "java8-java16红冲发票demo")

[java8-java16发票税额demo](https://github.com/fapiaoapi/invoice/blob/master/TaxExample.java "java8-java16发票税额demo")

---



[📚 查看完整中文文档](https://fa-piao.com/doc.html?source=github) | [💡 更多示例代码](https://github.com/fapiaoapi/invoice-sdk-java/tree/master/src/main/java/tax/invoice/example)

---

## 🔍 为什么选择此SDK？
- **精准覆盖中国数电发票标准** - 严格遵循国家最新接口规范
- **开箱即用** - 无需处理XML/签名等底层细节，专注业务逻辑
- **企业级验证** - 已在生产环境处理超100万张电子发票

---

## 📊 支持的开票类型
| 发票类型       | 状态   |
|----------------|--------|
| 数电发票（普通发票） | ✅ 支持 |
| 数电发票（增值税专用发票） | ✅ 支持 |
| 数电发票（铁路电子客票）  | ✅ 支持 |
| 数电发票（航空运输电子客票行程单） | ✅ 支持  |
| 数电票（二手车销售统一发票） | ✅ 支持  |
| 数电纸质发票（增值税专用发票） | ✅ 支持  |
| 数电纸质发票（普通发票） | ✅ 支持  |
| 数电纸质发票（机动车发票） | ✅ 支持  |
| 数电纸质发票（二手车发票） | ✅ 支持  |

---

## 🤝 贡献与支持
- 提交Issue: [问题反馈](https://github.com/fapiaoapi/invoice-sdk-java/issues)
- 商务合作: yuejianghe@qq.com  


## 🎯 快速开始：5分钟开出一张数电发票

```java
package tax.invoice.example;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
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

import static java.lang.Thread.sleep;

public class BasicExample {

    public static String appKey = "";
    public static String appSecret = "";

    public static String nsrsbh = "";// 统一社会信用代码
    public static String title = "";// 名称（营业执照）
    public static String username = "";// 手机号码（电子税务局）
    public static String password = "";// 个人用户密码（电子税务局）
    public static String type = "6";// 6 基础 7标准
    public static String xhdwdzdh = "重庆市渝北区龙溪街道丽园路2号XXXX 1325580XXXX"; // 地址 电话
    public static String xhdwyhzh = "工商银行XXXX 15451211XXXX";// 开户行 银行账号
    public static String fphm = "";
    public static String kprq = "";
    public static String token = "";

    // 创建客户端
    public static InvoiceClient client = new InvoiceClient(appKey, appSecret);

    // redis
    public static Jedis redisClient = new Jedis("127.0.0.1", 6379);

    public static void main(String[] args) {
        // 配置信息
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            redisClient.auth("test123456");

            System.out.println("java " + System.getProperty("java.version"));
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> new Scanner(System.in).nextLine());

            /*
             *
             * 一 获取token
             */
            getToken(false);

            // 二 开具蓝票
            /*
             * 前端模拟数电发票/电子发票开具 (蓝字发票)
             * @see https://fa-piao.com/fapiao.html?source=github
             *
             * 开具数电发票文档
             * @see https://fa-piao.com/doc.html#api6?source=github
             *
             */

            ApiResponse<Map<String, Object>> invoiceResponse = blueTicket();
            switch (invoiceResponse.getCode()) {
                case 200:
                    Map<String, Object> invoiceData = invoiceResponse.getData();
                    fphm = invoiceData.get("Fphm").toString();
                    kprq = invoiceData.get("Kprq").toString();
                    // 三 下载发票
                    downloadInvoice();
                    break;
                case 420:
                    System.out.println("420 登录(短信认证)");
                    /*
                     * 前端模拟短信认证弹窗
                     *
                     * @see https://fa-piao.com/fapiao.html?action=sms&source=github
                     */
                    // 1. 发短信验证码
                    /*
                     * @see https://fa-piao.com/doc.html#api2?source=github
                     */
                    ApiResponse<Map<String, Object>> loginResponse = client.loginDppt(nsrsbh, username, password,
                            "");
                    if (loginResponse.getCode() == 200) {
                        System.out.println(loginResponse.getMsg());
                        System.out.println("请输入验证码");
                        try {
                            System.out.print("300秒内("
                                    + LocalDateTime.now().plusSeconds(300)
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                    + "前)输入内容: ");
                            String smsCode = future.get(300, TimeUnit.SECONDS);

                            System.out.println("🎉 成功获取输入: " + smsCode + " \n" + "当前时间: " + LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            // 2. 输入验证码
                            /*
                             * @see https://fa-piao.com/doc.html#api2?source=github
                             */
                            ApiResponse<Map<String, Object>> loginResponse2 = client.loginDppt(nsrsbh, username,
                                    password, smsCode);
                            if (loginResponse2.getCode() == 200) {
                                System.out.println(loginResponse2.getData());
                                System.out.println("验证成功");
                                System.out.println("请再次调用blueTicket");
                                ApiResponse<Map<String, Object>> invoiceResponse2 = blueTicket();
                                if(invoiceResponse2.getCode() == 200) {
                                    fphm = invoiceResponse2.getData().get("Fphm").toString();
                                    kprq = invoiceResponse2.getData().get("Kprq").toString();
                                    downloadInvoice();
                                }
                            } else {
                                System.out.println(loginResponse2.getCode() + "验证失败: " + loginResponse2.getMsg());
                            }
                        } catch (TimeoutException | InterruptedException | ExecutionException e) {
                            System.out.println("\n短信认证输入超时！");
                            future.cancel(true);
                        }
                        executor.shutdown();
                    } else {
                        System.out.println(loginResponse.getCode() + "发短信验证码失败: " + loginResponse.getMsg());
                    }
                    break;
                case 430:
                    System.out.println("430 人脸认证");
                    /*
                     * 前端模拟人脸认证弹窗
                     *
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
                        // 字符串转二维码图片base64
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
                        // String base64 = toBase64((String) ewmObj,300);
                        // qrData.put("ewm", base64);
                        // System.out.println("data:image/png;base64," + base64);
                        // String base64Uri = "data:image/png;base64," + base64;
                        // 前端使用示例: <img src="base64Uri" />

                    }
                    //字符串转二维码图片 命令行终端打印
                    printQR((String) ewmObj);
                    System.out.println("成功做完人脸认证,请输入数字 1");
                    try {
                        System.out.print("300秒内(" + LocalDateTime.now().plusSeconds(300)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "前)输入内容: ");
                        String inputNum = future.get(300, TimeUnit.SECONDS);
                        System.out.println("🎉 成功获取输入: " + inputNum + " \n" + "当前时间: "
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        // 2. 认证完成后获取人脸二维码认证状态
                        /*
                         * @see https://fa-piao.com/doc.html#api4?source=github
                         */
                        String rzid = qrCodeResponse.getData().get("rzid").toString();
                        ApiResponse<Map<String, Object>> faceStatusResponse = client.getFaceState(nsrsbh, rzid,
                                username, "1");
                        System.out.println("code: " + faceStatusResponse.getCode());
                        System.out.println("data: " + faceStatusResponse.getData());
                        if (faceStatusResponse.getData() != null) {
                            String slzt = faceStatusResponse.getData().get("slzt").toString();
                            if ("2".equals(slzt)) {
                                System.out.println("认证状态: 成功");
                                System.out.println("请再次调用blueTicket");
                                ApiResponse<Map<String, Object>> invoiceResponse3 = blueTicket();
                                if(invoiceResponse3.getCode() == 200) {
                                    fphm = invoiceResponse3.getData().get("Fphm").toString();
                                    kprq = invoiceResponse3.getData().get("Kprq").toString();
                                    downloadInvoice();
                                }
                            } else {
                                System.out.println("认证状态: " + ("1".equals(slzt) ? "未认证" : "二维码过期"));
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
                    System.out.println(invoiceResponse.getCode() + "授权失败:" + invoiceResponse.getMsg());
                    System.out.println("401  token过期 重新获取并缓存token");
                    sleep(1000);
                    getToken(true);
                    System.out.println("再调用blueTicket");
                    ApiResponse<Map<String, Object>> invoiceResponse4 = blueTicket();
                    if(invoiceResponse4.getCode() == 200) {
                        fphm = invoiceResponse4.getData().get("Fphm").toString();
                        kprq = invoiceResponse4.getData().get("Kprq").toString();
                        downloadInvoice();
                    }
                    break;
                default:
                    System.out.println(invoiceResponse.getCode() + "异常" + invoiceResponse.getMsg());
                    break;
            }
        } catch (Exception e) {
            System.out.println("系统错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 获取token
    public static void getToken(Boolean ForceUpdate) throws Exception {
        /*
         * <dependency>
         * <groupId>redis.clients</groupId>
         * <artifactId>jedis</artifactId>
         * <version>3.9.0</version>
         * </dependency>
         */

        String redisKey = nsrsbh + "@" + username + "@TOKEN";
        if (ForceUpdate) {
            /*
             * 获取授权Token文档
             *
             * @see https://fa-piao.com/doc.html#api1?source=github
             */
            ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh, type);
//             ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh,type,username,password);
            if (authResponse.isSuccess()) {
                token = authResponse.getData().getToken();
                redisClient.setex(redisKey, 3600 * 24 * 30, token); // 设置30天过期
                client.setAuthorization(token);
            }
        } else {
            // 从Redis获取Token
            String token = redisClient.get(redisKey);
            if (token == null || token.isEmpty()) {
                ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh, type);
//             ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh,type,username,password);
                if (authResponse.isSuccess()) {
                    token = authResponse.getData().getToken();
                    redisClient.setex(redisKey, 3600 * 24 * 30, token); // 设置30天过期
                    client.setAuthorization(token);
                }
            }else{
                client.setAuthorization(token);
            }
        }

    }

    // 蓝票
    public static ApiResponse<Map<String, Object>> blueTicket() throws Exception {
        /*
         *
         * 开票参数说明demo
         *
         * @see
         * https://github.com/fapiaoapi/invoice-sdk-java/blob/master/examples/TaxExample
         * .java
         */
        Map<String, Object> invoiceParams = new LinkedHashMap<>();
        invoiceParams.put("fplxdm", "82");
        invoiceParams.put("fpqqlsh", appKey + System.currentTimeMillis());
        invoiceParams.put("ghdwmc", "张四");
        // invoiceParams.put("ghdwsbh", "914208XXXXXXX");
        invoiceParams.put("hjje", 978.9);
        invoiceParams.put("hjse", 0.1);
        invoiceParams.put("jshj", 1100);
        invoiceParams.put("kplx", 0);
        invoiceParams.put("username", username);
        invoiceParams.put("xhdwdzdh", xhdwdzdh);
        invoiceParams.put("xhdwmc", title);
        invoiceParams.put("xhdwsbh", nsrsbh);
        invoiceParams.put("xhdwyhzh", xhdwyhzh);
        invoiceParams.put("zsfs", 0);
        invoiceParams.put("fyxm[0][fphxz]", 0);
        invoiceParams.put("fyxm[0][spmc]", "*玩具*益智玩具");
        // invoiceParams.put("fyxm[0][ggxh]", "");
        // invoiceParams.put("fyxm[0][dw]", "个");
        // invoiceParams.put("fyxm[0][spsl]", 1);
        // invoiceParams.put("fyxm[0][dj]", 10);
        invoiceParams.put("fyxm[0][je]", 10);
        invoiceParams.put("fyxm[0][sl]", 0.01);
        invoiceParams.put("fyxm[0][se]", 0.1);
        invoiceParams.put("fyxm[0][hsbz]", 1);
        invoiceParams.put("fyxm[0][spbm]", "1060408990000000000");

        return client.blueTicket(invoiceParams);
    }
    // 发票下载
    public static void downloadInvoice() throws Exception {
        System.out.println("发票号码: " + fphm);
        System.out.println("开票日期: " + kprq);
        /*
         * 获取销项数电版式文件
         *
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
    }

    // 生成二维码图片base64
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

    // 字符串转二维码 在命令行输出
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

            // System.out.println("👇 请扫描下方二维码 (内容长度: " + content.length() + ") 👇");

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
                    if (isTop && isBottom)
                        line.append("██"); // 上下都是黑
                    else if (isTop)
                        line.append("▀▀"); // 上黑下白
                    else if (isBottom)
                        line.append("▄▄"); // 上白下黑
                    else
                        line.append("  "); // 上下都是白
                }
                System.out.println(line);
            }

            // System.out.println("👆 扫描结束 👆");

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

}



```


[发票红冲demo](src/main/java/tax/invoice/example/RedInvoiceExample.java "发票红冲demo")
[发票税额计算demo](src/main/java/tax/invoice/example/TaxExample.java "发票税额计算demo")
[先验证后开发票demo](src/main/java/tax/invoice/example/InvoiceExample.java "先验证后开票发票demo")
[先验证后开发票命令行demo](src/main/java/tax/invoice/example/InvoiceExample.java "先验证后开发票命令行demo")


