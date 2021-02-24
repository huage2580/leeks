package utils;

public class CoinBean {
    private String code;
    private String name;
    private String timeStamp;
    private String price;

    public CoinBean(String code) {
        this.code = code;
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
}
