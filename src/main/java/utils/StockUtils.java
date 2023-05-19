package utils;

public class StockUtils {
    /**
     * 自动根据股票代码补全交易所前缀
     */
    public static String autoCompleteCode(String sourceCode) {
        if (sourceCode == null) return null;
        //自动去除前后空格并兼容大小写
        sourceCode = sourceCode.trim().toLowerCase();

        if (!isNumeric(sourceCode)) return sourceCode;
        //非A股代码长度
        if (sourceCode.length() != 6) return sourceCode;

        if (sourceCode.startsWith("6") || sourceCode.startsWith("7") || sourceCode.startsWith("9")) {
            return "sh" + sourceCode;
        } else if (sourceCode.startsWith("0") || sourceCode.startsWith("2") || sourceCode.startsWith("3")) {
            return "sz" + sourceCode;
        } else if (sourceCode.startsWith("4") || sourceCode.startsWith("8")) {
            return "bj" + sourceCode;
        } else {
            return sourceCode;
        }
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return str.matches("\\d+");
    }
}
