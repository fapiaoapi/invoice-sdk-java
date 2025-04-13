package tax.invoice.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;

public class OtherUtil {

    /**
     * 生成发票请求流水号
     *
     * @param prefix 前缀
     * @return String
     */
    public static String generateInvoiceSerialNumber(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = "FP";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(9000) + 1000; // 1000-9999之间的随机数

        return prefix + timestamp + random;
    }

    /**
     * 生成发票请求流水号（使用默认前缀）
     *
     * @return String
     */
    public static String generateInvoiceSerialNumber() {
        return generateInvoiceSerialNumber("FP");
    }

    /**
     * 验证税号
     *
     * @param nsrsbh 纳税人识别号
     * @return boolean
     */
    public static boolean validateTaxNumber(String nsrsbh) {
        if (nsrsbh == null) {
            return false;
        }

        // 统一社会信用代码（18位）
        if (nsrsbh.length() == 18) {
            return Pattern.matches("^[0-9A-Z]{18}$", nsrsbh);
        }

        // 纳税人识别号（15位）
        if (nsrsbh.length() == 15) {
            return Pattern.matches("^[0-9]{15}$", nsrsbh);
        }

        return false;
    }

    /**
     * 验证手机号
     *
     * @param mobile 手机号
     * @return boolean
     */
    public static boolean validateMobile(String mobile) {
        if (mobile == null) {
            return false;
        }
        return Pattern.matches("^1[3-9]\\d{9}$", mobile);
    }

    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return boolean
     */
    public static boolean validateEmail(String email) {
        if (email == null) {
            return false;
        }
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
    }

    /**
     * 格式化金额（保留2位小数）
     *
     * @param amount 金额
     * @return String
     */
    public static String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(amount);
    }

    /**
     * 格式化金额（保留2位小数）
     *
     * @param amount 金额字符串
     * @return String
     */
    public static String formatAmount(String amount) {
        try {
            return formatAmount(Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * 计算税额
     *
     * @param amount       金额
     * @param taxRate      税率
     * @param isIncludeTax 是否含税
     * @return String
     */
    public static String calculateTax(double amount, double taxRate, boolean isIncludeTax) {
        double tax;

        if (isIncludeTax) {
            // 含税计算：税额 = 金额 / (1 + 税率) * 税率
            tax = amount / (1 + taxRate) * taxRate;
        } else {
            // 不含税计算：税额 = 金额 * 税率
            tax = amount * taxRate;
        }

        return formatAmount(tax);
    }

    /**
     * 计算税额（字符串参数版本）
     *
     * @param amount       金额字符串
     * @param taxRate      税率字符串
     * @param isIncludeTax 是否含税
     * @return String
     */
    public static String calculateTax(String amount, String taxRate, boolean isIncludeTax) {
        try {
            return calculateTax(
                    Double.parseDouble(amount),
                    Double.parseDouble(taxRate),
                    isIncludeTax
            );
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * 计算不含税金额
     *
     * @param amount  含税金额
     * @param taxRate 税率
     * @return String
     */
    public static String calculateAmountWithoutTax(double amount, double taxRate) {
        // 不含税金额 = 含税金额 / (1 + 税率)
        double amountWithoutTax = amount / (1 + taxRate);
        return formatAmount(amountWithoutTax);
    }

    /**
     * 计算不含税金额（字符串参数版本）
     *
     * @param amount  含税金额字符串
     * @param taxRate 税率字符串
     * @return String
     */
    public static String calculateAmountWithoutTax(String amount, String taxRate) {
        try {
            return calculateAmountWithoutTax(
                    Double.parseDouble(amount),
                    Double.parseDouble(taxRate)
            );
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * 计算含税金额
     *
     * @param amount  不含税金额
     * @param taxRate 税率
     * @return String
     */
    public static String calculateAmountWithTax(double amount, double taxRate) {
        // 含税金额 = 不含税金额 * (1 + 税率)
        double amountWithTax = amount * (1 + taxRate);
        return formatAmount(amountWithTax);
    }

    /**
     * 计算含税金额（字符串参数版本）
     *
     * @param amount  不含税金额字符串
     * @param taxRate 税率字符串
     * @return String
     */
    public static String calculateAmountWithTax(String amount, String taxRate) {
        try {
            return calculateAmountWithTax(
                    Double.parseDouble(amount),
                    Double.parseDouble(taxRate)
            );
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * 将金额转换为中文大写
     *
     * @param amount 金额
     * @return String
     */
    public static String amountToChinese(double amount) {
        if (amount == 0) {
            return "零元整";
        }

        String[] chnNumChar = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] chnUnitChar = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万", "拾", "佰", "仟"};
        String[] chnUnitSection = {"", "万", "亿", "万亿"};

        long integerPart = (long) amount;
        int decimalPart = (int) Math.round((amount - integerPart) * 100);

        StringBuilder chineseStr = new StringBuilder();

        // 处理整数部分
        if (integerPart > 0) {
            String integerStr = String.valueOf(integerPart);
            int integerLen = integerStr.length();

            int section = 0;
            int sectionPos = 0;
            boolean zero = true;

            for (int i = integerLen - 1; i >= 0; i--) {
                int digit = integerStr.charAt(integerLen - i - 1) - '0';

                if (digit == 0) {
                    zero = true;
                } else {
                    if (zero) {
                        chineseStr.append(chnNumChar[0]);
                    }
                    zero = false;
                    chineseStr.append(chnNumChar[digit]).append(chnUnitChar[i % 16]);
                }

                sectionPos++;
                if (sectionPos == 4) {
                    section++;
                    sectionPos = 0;
                    zero = true;
                    chineseStr.append(chnUnitSection[section]);
                }
            }

            chineseStr.append("元");
        }

        // 处理小数部分
        if (decimalPart > 0) {
            int jiao = decimalPart / 10;
            int fen = decimalPart % 10;

            if (jiao > 0) {
                chineseStr.append(chnNumChar[jiao]).append("角");
            }

            if (fen > 0) {
                chineseStr.append(chnNumChar[fen]).append("分");
            }
        } else {
            chineseStr.append("整");
        }

        return chineseStr.toString();
    }

    /**
     * 将金额转换为中文大写（字符串参数版本）
     *
     * @param amount 金额字符串
     * @return String
     */
    public static String amountToChinese(String amount) {
        try {
            return amountToChinese(Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            return "零元整";
        }
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return String
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            return "";
        }

        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int charactersLength = characters.length();
        Random random = new Random();
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            randomString.append(characters.charAt(random.nextInt(charactersLength)));
        }

        return randomString.toString();
    }

    /**
     * 将Base64编码的文件保存到本地
     *
     * @param base64Content Base64编码的内容
     * @param filePath      文件保存路径
     * @return boolean
     */
    public static boolean saveBase64File(String base64Content, String filePath) {
        try {
            byte[] fileContent = Base64.getDecoder().decode(base64Content);
            Path path = Paths.get(filePath);
            Files.write(path, fileContent);
            return true;
        } catch (IllegalArgumentException | IOException e) {
            return false;
        }
    }
}