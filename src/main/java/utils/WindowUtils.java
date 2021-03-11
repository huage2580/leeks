package utils;

import java.util.HashMap;

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

    private static HashMap<String,String> remapPinYinMap = new HashMap<>();

    static {
        remapPinYinMap.put(PinYinUtils.toPinYin("编码"),"编码");
        remapPinYinMap.put(PinYinUtils.toPinYin("基金名称"),"基金名称");
        remapPinYinMap.put(PinYinUtils.toPinYin("估算净值"),"估算净值");
        remapPinYinMap.put(PinYinUtils.toPinYin("估算涨跌"),"估算涨跌");
        remapPinYinMap.put(PinYinUtils.toPinYin("更新时间"),"更新时间");
        remapPinYinMap.put(PinYinUtils.toPinYin("当日净值"),"当日净值");
        remapPinYinMap.put(PinYinUtils.toPinYin("股票名称"),"股票名称");
        remapPinYinMap.put(PinYinUtils.toPinYin("当前价"),"当前价");
        remapPinYinMap.put(PinYinUtils.toPinYin("涨跌"),"涨跌");
        remapPinYinMap.put(PinYinUtils.toPinYin("涨跌幅"),"涨跌幅");
        remapPinYinMap.put(PinYinUtils.toPinYin("最高价"),"最高价");
        remapPinYinMap.put(PinYinUtils.toPinYin("最低价"),"最低价");
        remapPinYinMap.put(PinYinUtils.toPinYin("名称"),"名称");

    }


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
        //考虑拼音编码

        return -1;
    }

    public static String remapPinYin(String pinyin){
        return remapPinYinMap.getOrDefault(pinyin,pinyin);
    }


}
