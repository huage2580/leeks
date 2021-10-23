package bean;

/**
 * @author leizhijun
 * @date 2021/10/23 17:22
 */
public class StockPriceLimitBean {

    private String code;

    private String minLimit;

    private String maxLimit;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(String minLimit) {
        this.minLimit = minLimit;
    }

    public String getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(String maxLimit) {
        this.maxLimit = maxLimit;
    }
}
