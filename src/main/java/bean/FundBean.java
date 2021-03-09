package bean;

import com.google.gson.annotations.SerializedName;
import utils.PinYinUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FundBean {
    @SerializedName("fundcode")
    private String fundCode;
    @SerializedName("name")
    private String fundName;
    private String jzrq;//净值日期
    private String dwjz;//当日净值
    private String gsz; //估算净值
    private String gszzl;//估算涨跌百分比 即-0.42%
    private String gztime;//gztime估值时间

    public FundBean() {
    }

    public FundBean(String fundCode) {
        this.fundCode = fundCode;
        this.fundName = "--";

    }


    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getJzrq() {
        return jzrq;
    }

    public void setJzrq(String jzrq) {
        this.jzrq = jzrq;
    }

    public String getDwjz() {
        return dwjz;
    }

    public void setDwjz(String dwjz) {
        this.dwjz = dwjz;
    }

    public String getGsz() {
        return gsz;
    }

    public void setGsz(String gsz) {
        this.gsz = gsz;
    }

    public String getGszzl() {
        return gszzl;
    }

    public void setGszzl(String gszzl) {
        this.gszzl = gszzl;
    }

    public String getGztime() {
        return gztime;
    }

    public void setGztime(String gztime) {
        this.gztime = gztime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundBean fundBean = (FundBean) o;
        return Objects.equals(fundCode, fundBean.fundCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fundCode);
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
                return this.getFundCode();
            case "基金名称":
                return colorful ? this.getFundName() : PinYinUtils.toPinYin(this.getFundName());
            case "估算净值":
                return this.getGsz();
            case "估算涨跌":
                String gszzlStr = "--";
                String gszzl = this.getGszzl();
                if (gszzl != null) {
                    gszzlStr = gszzl.startsWith("-") ? gszzl : "+" + gszzl;
                }
                return gszzlStr + "%";
            case "更新时间":
                String timeStr = this.getGztime();
                if (timeStr == null) {
                    timeStr = "--";
                }
                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                if (timeStr.startsWith(today)) {
                    timeStr = timeStr.substring(timeStr.indexOf(" "));
                }
                return timeStr;
            case "当日净值":
                return this.getDwjz() + "[" + this.getJzrq() + "]";
            default:
                return "";

        }
    }
}
