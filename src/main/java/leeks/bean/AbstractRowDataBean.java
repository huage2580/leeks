package leeks.bean;

import java.text.DecimalFormat;

public abstract class AbstractRowDataBean {

    public abstract String getCode();

    protected static DecimalFormat decimalFormat = new DecimalFormat("#.00");

    public abstract String getValueByColumn(String colums, boolean colorful);
}
