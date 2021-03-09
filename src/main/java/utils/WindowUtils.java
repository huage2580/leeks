package utils;

/**
 * @Created by DAIE
 * @Date 2021/3/8 20:26
 * @Description leek面板TABLE工具类
 */
public class WindowUtils {
    //基金表头
    public static final String FUND_TABLE_HEADER_KEY = "fund_table_header_key";
    public static final String FUND_TABLE_HEADER_VALUE = "编码,基金名称,估算净值,估算涨跌,更新时间,当日净值";
    //股票表头
    public static final String STOCK_TABLE_HEADER_KEY = "stock_table_header_key";
    public static final String STOCK_TABLE_HEADER_VALUE = "编码,股票名称,当前价,涨跌,涨跌幅,最高价,最低价,更新时间";
    //货币表头
    public static final String COIN_TABLE_HEADER_KEY = "coin_table_header_key";
    public static final String COIN_TABLE_HEADER_VALUE = "编码,名称,当前价,更新时间";


    /**
     * 通过列名 获取该TABLE的列的数组下标
     *
     * @param columnNames 列名数组
     * @param columnName  要获取的列名
     * @return 返回给出列名的数组下标 匹配失败返回-1
     */
    public static int getColumnIndexByName(String[] columnNames, String columnName) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(columnName)) {
                return i;
            }
        }
        return -1;
    }


}
