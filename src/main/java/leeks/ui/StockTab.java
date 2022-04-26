package leeks.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.awt.RelativePoint;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.handler.SinaStockHandler;
import leeks.handler.StockRefreshHandler;
import leeks.handler.TencentStockHandler;
import leeks.bean.TabConfig;
import leeks.utils.LogUtil;
import leeks.utils.PopupsUiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;

public class StockTab extends AbstractTab {

    protected JPanel panel;
    private static final String NAME = "Stock";

    static StockRefreshHandler handler;

    private static final TabConfig CONFIG = new TabConfig("stock_table_header_key2",
            "编码,股票名称,涨跌,涨跌幅,最高价,最低价,当前价,成本价,持仓,收益率,收益,更新时间",
            Constants.Keys.CRON_EXPRESSION_STOCK,
            Constants.Keys.STOCKS);

    public StockTab() {
        super();
        handler = factoryHandler();

        // 只有股票才支持表格事件
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                String code = String.valueOf(table.getModel().getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), tableModel.getCodeColumnIndex()));
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
                    // 鼠标左键双击
                    try {
                        PopupsUiUtil.showImageByStockCode(code, PopupsUiUtil.StockShowType.min, new Point(e.getXOnScreen(), e.getYOnScreen()));
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        LogUtil.info(ex.getMessage());
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    //鼠标右键
                    JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<PopupsUiUtil.StockShowType>("",
                            PopupsUiUtil.StockShowType.values()) {
                        @Override
                        public @NotNull String getTextFor(PopupsUiUtil.StockShowType value) {
                            return value.getDesc();
                        }

                        @Override
                        public @Nullable PopupStep<?> onChosen(PopupsUiUtil.StockShowType selectedValue, boolean finalChoice) {
                            try {
                                PopupsUiUtil.showImageByStockCode(code, selectedValue, new Point(e.getXOnScreen(), e.getYOnScreen()));
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                                LogUtil.info(ex.getMessage());
                            }
                            return super.onChosen(selectedValue, finalChoice);
                        }
                    }).show(RelativePoint.fromScreen(new Point(e.getXOnScreen(), e.getYOnScreen())));
                }
            }
        });

        apply();
    }

    private StockRefreshHandler factoryHandler() {
        boolean useSinaApi = PropertiesComponent.getInstance().getBoolean(Constants.Keys.STOCKS_SINA);
        if (useSinaApi) {
            if (handler instanceof SinaStockHandler) {
                return handler;
            }
            return new SinaStockHandler(tableModel);
        }
        if (handler instanceof TencentStockHandler) {
            return handler;
        }
        return new TencentStockHandler(tableModel);
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
        return handler;
    }
}
