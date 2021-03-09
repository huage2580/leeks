package bean;

import utils.PinYinUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CoinBean {
    private String code;
    private String name;
    private String timeStamp;
    private String price;

    public CoinBean(String code) {
        this.code = code;
        this.name = "--";
    }

    public CoinBean(String code, String name, String timeStamp, String price) {
        this.code = code;
        this.name = name;
        this.timeStamp = timeStamp;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    /**
     * 返回列名的VALUE 用作展示
     * @param colums 字段名
     * @param colorful 隐蔽模式
     * @return 对应列名的VALUE值 无法匹配返回""
     */
    public String getValueByColumn(String colums, boolean colorful) {
        switch (colums) {
            case "编码":
                return this.getCode();
            case "名称":
                return colorful ? this.getName() : PinYinUtils.toPinYin(this.getName());
            case "当前价":
                return this.getPrice();
            case "更新时间":
                String timeStr = "--";
                if (this.getTimeStamp()!=null){
                    timeStr = this.getTimeStamp();
                }
                return timeStr;

            default:
                return "";

        }
    }
}
