package tax.invoice.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import tax.invoice.InvoiceClient;
import tax.invoice.model.ApiResponse;
import tax.invoice.model.AuthorizationResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;



/**
 * 数电发票SDK使用示例
 */
public class FaceLoginExample {

    public static String appKey = "";
    public static String appSecret = "";

    public static String nsrsbh = "";// 统一社会信用代码
    public static String title = "";// 名称（营业执照）
    public static String username = "";// 手机号码（电子税务局）
    public static String password = "";// 个人用户密码（电子税务局）
    public static String type = "7";// 6 基础 7标准
    public static String sf = "09";//身份（电子税务局）

    public static  String ewmlx = "1";// 1 电子税务局app人脸二维码登录，10 电子税务局app扫码登录 2 个税app人脸二维码登录，3 个税app扫码确认登录
    public static String token = "";
    public static boolean debug = true; // 是否打印日志
    // 创建客户端
    public static InvoiceClient client = new InvoiceClient(appKey, appSecret,debug);


    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            System.out.println("java " + System.getProperty("java.version"));
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> new Scanner(System.in).nextLine());

             if (token != null && !token.isEmpty()) {
                 client.setAuthorization(token);
             } else {
                 // 获取授权
                 ApiResponse<AuthorizationResponse> authResponse = client.getAuthorization(nsrsbh,type);
                 if (authResponse.isSuccess()) {
                     client.setAuthorization(authResponse.getData().getToken());
                     System.out.println("授权成功，Token: " + authResponse.getData().getToken());
                 }
             }

             // 获取认证状态
            ApiResponse<Map<String, Object>> statusResponse = client.queryFaceAuthState(nsrsbh, username);
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
                         switch (ewmlx){
                             case "1":
                              System.out.println("请用电子税务局app人脸二维码登录");
                              break;
                              case "2":
                              System.out.println("请用个税app人脸二维码登录");
                              break;
                              case "3":
                                  System.out.println("请用个税app扫码登录");
                              case "10":
                              System.out.println("请用电子税务局app扫码登录");
                              break;
                         }
                         Map<String, Object> loginData = loginResponse.getData();
                         if (loginData == null || loginData.get("ewmid") == null || loginData.get("qrcode") == null) {
                             System.out.println("人脸登录返回字段缺失: " + loginResponse.getData());
                             break;
                         }else{
                             int length = loginData.get("qrcode").toString().length();
                             if (length < 500){
                                 stringToQrcode(loginData.get("qrcode").toString());
                             }else{
                                 printQRFromBase64(loginData.get("qrcode").toString());
                             }

                             try {
                                 System.out.println("成功做完人脸认证,请输入数字 1");
                                 System.out.print("300秒内(" + LocalDateTime.now().plusSeconds(300)
                                         .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "前)输入内容: ");
                                 String inputNum = future.get(300, TimeUnit.SECONDS);
                                 System.out.println("🎉 成功获取输入: " + inputNum + " \n" );
                                 // 2 验证是否成
                                 ApiResponse<Map<String, Object>> loginResponse2 = client.FaceLoginDppt(nsrsbh, username, password,sf,ewmlx,loginData.get("ewmid").toString());
                                 if(loginResponse2.isSuccess()){
                                     System.out.println("====登录(人脸认证)成功====");
                                     ApiResponse<Map<String, Object>> statusResponse2 = client.queryFaceAuthState(nsrsbh, username);
                                     switch (statusResponse2.getCode()) {
                                         case 200:
                                             System.out.println("认证状态: 无需认证");
                                             break;
                                         case 420://短信或者人脸认证
                                             System.out.println("需要登录(人脸认证)");
                                             break;
                                         case 430:
                                             System.out.println("需要人脸认证");
                                             break;
                                         case 401:
                                             //重新授权
                                              System.out.println("重新授权");
                                              break;
                                              default:
                                                  System.out.println(statusResponse2.getCode()+":"+statusResponse2.getMsg());
                                                  break;
                                     }
                                 }else{
                                     System.out.println(loginResponse2.getCode()+":"+loginResponse2.getMsg());
                                 }
                             } catch (TimeoutException | InterruptedException | ExecutionException e) {
                                 System.out.println("\n认证输入超时！");
                                 future.cancel(true);
                             }
                             executor.shutdown();
                         }



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

    private static void renderQrCompact(BitMatrix matrix, int scale) {
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

    private static boolean hasBlackInBlock(BitMatrix matrix, int startX, int startY, int scale) {
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

    public static void printQRFromBase64(String base64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IOException("base64 内容不是有效图片");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            String content = new MultiFormatReader().decode(bitmap).getText();
            stringToQrcode(content);
        } catch (NotFoundException e) {
            System.out.println("无法解析二维码内容，改为直接按图片像素输出：");
            printBinaryImage(base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printBinaryImage(String base64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IOException("base64 内容不是有效图片");
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int step = Math.max(1, width / 80);

            for (int y = 0; y < height; y += step * 2) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < width; x += step) {
                    boolean topBlack = isBlackPixel(image.getRGB(x, y));
                    boolean bottomBlack = false;
                    int bottomY = y + step;
                    if (bottomY < height) {
                        bottomBlack = isBlackPixel(image.getRGB(x, bottomY));
                    }

                    if (topBlack && bottomBlack) {
                        line.append("██");
                    } else if (topBlack) {
                        line.append("▀▀");
                    } else if (bottomBlack) {
                        line.append("▄▄");
                    } else {
                        line.append("  ");
                    }
                }
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isBlackPixel(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int gray = (r * 299 + g * 587 + b * 114) / 1000;
        return gray < 128;
    }
}


