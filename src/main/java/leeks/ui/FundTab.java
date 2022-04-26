package leeks.ui;

import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.handler.TianTianFundHandler;
import leeks.bean.TabConfig;

public class FundTab extends AbstractTab {
    public static final String NAME = "Fund";

    private static final TabConfig CONFIG = new TabConfig("fund_table_header_key2",
            "编码,基金名称,估算涨跌,当日净值,估算净值,持仓成本价,持有份额,收益率,收益,更新时间",
            Constants.Keys.CRON_EXPRESSION_FUND,
            Constants.Keys.FUNDS);

    private final TianTianFundHandler handler;

    public FundTab() {
        super();
        handler = new TianTianFundHandler(tableModel);
        apply();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected TabConfig getConfig() {
        return CONFIG;
    }

    @Override
    protected AbstractHandler getHandler() {
        return this.handler;
    }
}
