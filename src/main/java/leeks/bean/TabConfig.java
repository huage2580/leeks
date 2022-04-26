package leeks.bean;

public class TabConfig {

    private final String tableHeaderKey;
    private final String tableHeaderValue;
    private final String cronExpressionKey;
    private final String codesKey;

    public TabConfig(String tableHeaderKey, String tableHeaderValue, String cronExpressionKey, String codesKey) {
        this.tableHeaderKey = tableHeaderKey;
        this.tableHeaderValue = tableHeaderValue;
        this.cronExpressionKey = cronExpressionKey;
        this.codesKey = codesKey;
    }

    public String getCronExpressionKey() {
        return cronExpressionKey;
    }

    public String getTableHeaderKey() {
        return tableHeaderKey;
    }

    public String getTableHeaderValue() {
        return tableHeaderValue;
    }

    public String getCodesKey() {
        return codesKey;
    }
}
