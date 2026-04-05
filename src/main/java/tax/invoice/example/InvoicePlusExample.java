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

public class InvoicePlusExample {

    public static String appKey = "";
    public static String appSecret = "";

    public static String nsrsbh = "";// 统一社会信用代码  当前登录的纳税人识别号
    public static String title = "";//名称（营业执照）
    public static String username = "";//手机号码（电子税务局）
    public static String password = "";// 个人用户密码（电子税务局）
    public static String type = "7";// 6 基础 7标准

    public static String xhdwdzdh = "重庆市渝北区龙溪街道丽园路2号XXXX 1325580XXXX"; // 地址 电话
    public static String xhdwyhzh = "工商银行XXXX 15451211XXXX";// 开户行 银行账号

    public static String token = "";
    public static boolean debug = true; // 是否打印日志

    // 创建客户端
    public static InvoiceClient client = new InvoiceClient(appKey, appSecret,debug);

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
                /*
                 *
                 * 二 获取认证状态
                 * 
                 * @see https://fa-piao.com/doc.html#api5?source=github
                 */
                ApiResponse<Map<String, Object>> statusResponse = client.queryFaceAuthState(nsrsbh, username);
                switch (statusResponse.getCode()) {
                    case 200:
                        /*
                         * 前端模拟数电发票/电子发票开具 (蓝字发票)
                         *
                         * @see https://fa-piao.com/fapiao.html?source=github
                         *
                         */

                        // 三 开具蓝票
                        /*
                         * 开具数电发票文档
                         *
                         * @see https://fa-piao.com/doc.html#api6?source=github
                         *
                         */
                        blueTicket();
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
                                    System.out.println("验证成功");
                                    System.out.println("请再次调用blueTicket");
                                    blueTicket();
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
                            // String base64 = toBase64((String) ewmObj,300);
                            // qrData.put("ewm", base64);
                            // System.out.println("data:image/png;base64," + base64);
                            // String base64Uri = "data:image/png;base64," + base64;
                            // 前端使用示例: <img src="base64Uri" />

                        }
                        //命令行终端打印二维码图片
                        stringToQrcode((String) ewmObj);
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
                                    blueTicket();
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
                        System.out.println(statusResponse.getCode() + "授权失败:" + statusResponse.getMsg());
                        System.out.println("401  token过期 重新获取并缓存token");
                        sleep(1000);
                        getToken(true);
                        System.out.println("再调用blueTicket");
                        blueTicket();
                        break;
                    default:
                        System.out.println(statusResponse.getCode() + "异常" + statusResponse.getMsg());
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

    public static void blueTicket() throws Exception {
        /*
         *
         * 开票税额计算说明demo
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
        invoiceParams.put("hjje", 9.9);
        invoiceParams.put("hjse", 0.1);
        invoiceParams.put("jshj", 100);
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

        ApiResponse<Map<String, Object>> invoiceResponse = client.blueTicket(invoiceParams);
        Map<String, Object> invoiceData = invoiceResponse.getData();
        if (invoiceResponse.isSuccess()) {
            if (invoiceData == null || invoiceData.get("Fphm") == null || invoiceData.get("Kprq") == null) {
                System.out.println(invoiceResponse.getCode() + "开票成功但返回字段缺失: " + invoiceResponse.getData());
                return;
            } else {
                // 四 下载发票
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
                pdfParams.put("fphm", invoiceData.get("Fphm").toString());
                pdfParams.put("Kprq", invoiceData.get("Kprq").toString());

                ApiResponse<Map<String, Object>> pdfResponse = client.getPdfOfdXml(pdfParams);
                if (pdfResponse.isSuccess()) {
                    System.out.println("发票下载成功");
                    System.out.println(pdfResponse.getData());
                }
            }
        }else{
            System.out.println(invoiceResponse.getCode() + "开票失败: " + invoiceResponse.getMsg());
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

    // 字符串转二维码 在命令行输出
    public static void stringToQrcode(String content) {
        try {
            ErrorCorrectionLevel ecLevel = content.length() > 50 ? ErrorCorrectionLevel.L : ErrorCorrectionLevel.M;
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel);
            hints.put(EncodeHintType.MARGIN, 0);

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 1, 1, hints);
            int size = matrix.getWidth();
            int maxModules = 52;
            int scale = Math.max(1, (int) Math.ceil((double) size / maxModules));
            int renderedSize = (int) Math.ceil((double) size / scale);

            System.out.println("请扫描下方二维码 (内容长度: " + content.length() + ", 原尺寸: " + size + "x" + size + ", 输出尺寸: " + renderedSize + "x" + renderedSize + ")");
            renderQrCompact(matrix, scale);
            System.out.println("扫描结束");
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public static void renderQrCompact(BitMatrix matrix, int scale) {
        int size = matrix.getWidth();
        int colIndex;
        for (int y = 0; y < size; y += scale * 2) {
            StringBuilder line = new StringBuilder();
            colIndex = 0;
            for (int x = 0; x < size; x += scale) {
                boolean topBlack = hasBlackInBlock(matrix, x, y, scale);
                boolean bottomBlack = hasBlackInBlock(matrix, x, y + scale, scale);
                boolean useNarrowCell = (colIndex + 1) % 5 == 0;
                if (topBlack && bottomBlack) {
                    line.append(useNarrowCell ? "█" : "██");
                } else if (topBlack) {
                    line.append(useNarrowCell ? "▀" : "▀▀");
                } else if (bottomBlack) {
                    line.append(useNarrowCell ? "▄" : "▄▄");
                } else {
                    line.append(useNarrowCell ? " " : "  ");
                }
                colIndex++;
            }
            System.out.println(line);
        }
    }

    public static boolean hasBlackInBlock(BitMatrix matrix, int startX, int startY, int scale) {
        int width = matrix.getWidth();
        if (startX >= width || startY >= width) {
            return false;
        }
        int endX = Math.min(startX + scale, width);
        int endY = Math.min(startY + scale, width);
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if (matrix.get(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

}
