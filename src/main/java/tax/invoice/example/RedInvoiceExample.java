package tax.invoice.example;

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
public class RedInvoiceExample {

    public static void main(String[] args) {
        try {
            // 显式设置 System.out 的编码为 UTF-8
            System.setOut(new PrintStream(System.out, true, "UTF-8"));


            String appKey = "";
            String appSecret = "";

            String nsrsbh = "91500112MADFAQ9J2P";//统一社会信用代码
            String username = "19122840xxx";//手机号码（电子税务局）

            String fphm = "25502000000038381718";
            String kprq = "2025-04-13 13:35:27";

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

            // 发票红冲
            // 1 数电申请红字前查蓝票信息接口
            Map<String, String> queryInvoiceParams = new HashMap<>();
            queryInvoiceParams.put("nsrsbh", nsrsbh);
            queryInvoiceParams.put("fphm", fphm);
            queryInvoiceParams.put("username", username);
            queryInvoiceParams.put("kprq", kprq);
            queryInvoiceParams.put("sqyy", "2");
            ApiResponse<Map<String, Object>> queryInvoiceResponse = client.queryBlueTicketInfo(queryInvoiceParams);

            if (queryInvoiceResponse.isSuccess()) {
                System.out.println("1 可以申请红字");
                sleep(2000);
                // 2 申请红字信息表
                Map<String, String> applyRedParams = new HashMap<>();
                applyRedParams.put("xhdwsbh", nsrsbh);
                applyRedParams.put("yfphm", fphm);
                applyRedParams.put("username", username);
                applyRedParams.put("sqyy", "2");
                applyRedParams.put("chyydm", "01");

                ApiResponse<Map<String, Object>> applyRedResponse = client.applyRedInfo(applyRedParams);
                if (applyRedResponse.isSuccess()) {
                    System.out.println("2 申请红字信息表" );
                    sleep(2000);
                    // 3 数电票负数开具
                    Map<String, String> redInvoiceParams = new HashMap<>();
                    redInvoiceParams.put("fpqqlsh", "red" + fphm);
                    redInvoiceParams.put("username", username);
                    redInvoiceParams.put("xhdwsbh", nsrsbh);
                    redInvoiceParams.put("tzdbh", applyRedResponse.getData().get("xxbbh").toString());
                    redInvoiceParams.put("yfphm", fphm);
                    ApiResponse<Map<String, Object>> redInvoiceResponse = client.redTicket(redInvoiceParams);
                    if (redInvoiceResponse.isSuccess()) {
                        System.out.println("3 负数开具成功");
                    }else {
                        System.out.println(redInvoiceResponse.getCode()+"数电票负数开具失败:"+redInvoiceResponse.getMsg());
                        System.out.println(redInvoiceResponse.getData());
                    }
                }else {
                    System.out.println(applyRedResponse.getCode()+"申请红字信息表失败:"+applyRedResponse.getMsg());
                    System.out.println(applyRedResponse.getData());
                }
            }else {
                System.out.println(queryInvoiceResponse.getCode()+"查询发票信息失败:"+queryInvoiceResponse.getMsg());
                System.out.println(queryInvoiceResponse.getData());
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}