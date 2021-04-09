package utils;

import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * intellij ui 弹窗展示工具类 <br>
 * https://plugins.jetbrains.com/docs/intellij/popups.html#popups
 */
public class PopupsUiUtil {
    /**
     * 弹窗展示图片
     *
     * @param fundCode    基金编码
     * @param showByPoint 窗口显示位置
     */
    public static void showImageByFundCode(String fundCode, FundShowType type, Point showByPoint) throws MalformedURLException {
        // 图片接口
        // 带水印  http://j4.dfcfw.com/charts/pic6/590008.png
        // 无水印  http://j4.dfcfw.com/charts/pic7/590008.png
        // 暂时先硬编码，后续再优化调整
        TabInfo tabInfo = new TabInfo(new JLabel(new ImageIcon(new URL(String.format("http://j4.dfcfw.com/charts/pic7/%s.png?%s",
                fundCode, System.currentTimeMillis())))));
        tabInfo.setText(type.getDesc());
        JBTabsImpl tabs = new JBTabsImpl(LogUtil.getProject());
        tabs.addTab(tabInfo);
        JBPopupFactory.getInstance().createComponentPopupBuilder(tabs, null)
                .setMovable(true)
                .setRequestFocus(true)
                .createPopup().show(RelativePoint.fromScreen(showByPoint));
    }

    /**
     * 获取图片链接
     *
     * @param stockCode 股票编码
     * @param type      枚举类型
     * @return 可能为null
     */
    public static String getImageUrlByStock(String stockCode, StockShowType type) throws MalformedURLException {
        String prefix = StringUtils.substring(stockCode, 0, 2);
        String url = "http://image.sinajs.cn/newchart/";
        switch (prefix) {
            case "sh":
            case "sz":
                // 沪深股
                // 分时线图  http://image.sinajs.cn/newchart/min/n/sh600519.gif
                // 日K线图  http://image.sinajs.cn/newchart/daily/n/sh600519.gif
                // 周K线图  http://image.sinajs.cn/newchart/weekly/n/sh600519.gif
                // 月K线图  http://image.sinajs.cn/newchart/monthly/n/sh600519.gif
                url = String.format("%s/%s/n/%s.gif?%s", url, type.getType(), stockCode, System.currentTimeMillis());
                break;
            case "us":
                // 美股
                // 分时线图 http://image.sinajs.cn/newchart/png/min/us/AAPL.png
                // 日K线图 http://image.sinajs.cn/newchart/usstock/daily/aapl.gif
                // 周K线图 http://image.sinajs.cn/newchart/usstock/weekly/aapl.gif
                // 月K线图 http://image.sinajs.cn/newchart/usstock/monthly/aapl.gif
                if (StockShowType.min.equals(type)) {
                    url = String.format("%s/png/%s/%s/%s.png?%s", url, type.getType(), prefix, StringUtils.substring(stockCode, 2),
                            System.currentTimeMillis());
                } else {
                    url = String.format("%s/%sstock/%s/%s.gif?%s", url, prefix, type.getType(), StringUtils.substring(stockCode, 2),
                            System.currentTimeMillis());
                }
                break;
            case "hk":
                // 港股
                // 分时线图 http://image.sinajs.cn/newchart/png/min/hk/02202.png
                // 日K线图 http://image.sinajs.cn/newchart/hk_stock/daily/02202.gif
                // 周K线图 http://image.sinajs.cn/newchart/hk_stock/weekly/02202.gif
                // 月K线图 http://image.sinajs.cn/newchart/hk_stock/monthly/02202.gif
                if (StockShowType.min.equals(type)) {
                    url = String.format("%s/png/%s/%s/%s.png?%s", url, type.getType(), prefix, StringUtils.substring(stockCode, 2),
                            System.currentTimeMillis());
                } else {
                    url = String.format("%s/%s_stock/%s/%s.gif?%s", url, prefix, type.getType(), StringUtils.substring(stockCode, 2),
                            System.currentTimeMillis());
                }
                break;
            default:
                return "";
        }
        return url;
    }

    /**
     * 弹窗展示图片
     *
     * @param stockCode   编码
     * @param selectType  展示的类型
     * @param showByPoint 窗口显示位置
     */
    public static void showImageByStockCode(String stockCode, StockShowType selectType, Point showByPoint) throws MalformedURLException {
        JBTabsImpl tabs = new JBTabsImpl(LogUtil.getProject());
        for (StockShowType type : StockShowType.values()) {
            String imageUrlByStock = getImageUrlByStock(stockCode, type);
            JLabel label = new JLabel(imageUrlByStock);
            TabInfo tabInfo = new TabInfo(label);
            // 先存图片路径后续再转换为图片，避免图片网络延迟影响ui
            tabInfo.setText(type.getDesc());
            tabs.addTab(tabInfo);
            if (type.equals(selectType)) {
                tabs.select(tabInfo, true);
                label.setIcon(new ImageIcon(new URL(imageUrlByStock)));
                label.setText(null);
            }
        }
        tabs.addListener(new TabsListener.Adapter() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                JComponent component = newSelection.getComponent();
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    if (StringUtils.isNotBlank(label.getText())) {
                        try {
                            label.setIcon(new ImageIcon(new URL(label.getText())));
                            label.setText(null);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        JBPopupFactory.getInstance().createComponentPopupBuilder(tabs, null)
                .setMovable(true)
                .setRequestFocus(true)
                .createPopup().show(RelativePoint.fromScreen(showByPoint));
    }

    public enum FundShowType {
        /**
         * 净值估算图
         */
        gsz("净值估算图");
        private String desc;

        FundShowType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum StockShowType {
        /**
         * 分时线图
         */
        min("min", "分时线图"),
        /**
         * 日K线图
         */
        daily("daily", "日K线图"),
        /**
         * 周K线图
         */
        weekly("weekly", "周K线图"),
        /**
         * 月K线图
         */
        monthly("monthly", "月K线图");

        private String type;
        private String desc;

        StockShowType(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public String getType() {
            return type;
        }

        public String getDesc() {
            return desc;
        }
    }
}
