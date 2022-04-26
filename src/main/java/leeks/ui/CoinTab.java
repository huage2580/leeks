package leeks.ui;

import leeks.constant.Constants;
import leeks.handler.CoinRefreshHandler;
import leeks.handler.AbstractHandler;
import leeks.handler.YahooCoinHandler;
import leeks.bean.TabConfig;

import java.util.List;

public class CoinTab extends AbstractTab {
    public static final String NAME = "Coin";

    private static final TabConfig CONFIG = new TabConfig("coin_table_header_key2",
            "编码,当前价,涨跌,涨跌幅,最高价,最低价,更新时间",
            Constants.Keys.CRON_EXPRESSION_COIN,
            Constants.Keys.COINS);

    static CoinRefreshHandler handler;

    public CoinTab() {
        super();
        handler = new YahooCoinHandler(tableModel);
        apply();
    }

    @Override
    protected TabConfig getConfig() {
        return CONFIG;
    }

    @Override
    protected AbstractHandler getHandler() {
        return null;
    }

    @Override
    protected List<String> getCodes() {
        return SettingsWindow.getConfigList(CONFIG.getCodesKey(), "[,，]");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
