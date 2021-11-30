package bean;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import utils.PinYinUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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

    private String costPrise;//持仓成本价
    private String bonds;//持有份额
    private String incomePercent;//收益率
    private String income;//收益

    public FundBean() {
    }

    public FundBean(String fundCode) {
        if (StringUtils.isNotBlank(fundCode)) {
            String[] codeStr = fundCode.split(",");
            if (codeStr.length > 2) {
                this.fundCode = codeStr[0];
                this.costPrise = codeStr[1];
                this.bonds = codeStr[2];
            } else {
                this.fundCode = codeStr[0];
                this.costPrise = "--";
                this.bonds = "--";
            }
        } else {
            this.fundCode = fundCode;
        }
        this.fundName = "--";
    }

    public static void loadFund(FundBean fund, Map<String, String[]> codeMap) {
        String code = fund.getFundCode();
        if (codeMap.containsKey(code)) {
            String[] codeStr = codeMap.get(code);
            if (codeStr.length > 2) {
                fund.setCostPrise(codeStr[1]);
                fund.setBonds(codeStr[2]);
            }
        }
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

    public String getCostPrise() {
        return costPrise;
    }

    public void setCostPrise(String costPrise) {
        this.costPrise = costPrise;
    }

    public String getBonds() {
        return bonds;
    }

    public void setBonds(String bonds) {
        this.bonds = bonds;
    }

    public String getIncomePercent() {
        return incomePercent;
    }

    public void setIncomePercent(String incomePercent) {
        this.incomePercent = incomePercent;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
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
     *
     * @param colums   字段名
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
            case "持仓成本价":
                return this.getCostPrise();
            case "持有份额":
                return this.getBonds();
            case "收益率":
                return this.getCostPrise() != null ? this.getIncomePercent() + "%" : this.getIncomePercent();
            case "收益":
                return this.getIncome();
            default:
                return "";

        }
    }
}
