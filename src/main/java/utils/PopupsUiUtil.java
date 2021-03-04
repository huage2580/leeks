package utils;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.apache.commons.lang3.StringUtils;

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
        URL url = new URL(String.format("http://j4.dfcfw.com/charts/pic7/%s.png?%s", fundCode, System.currentTimeMillis()));
        showImage(url, type.getDesc(), showByPoint);
    }

    /**
     * 弹窗展示图片
     *
     * @param stockCode   股票编码
     * @param type        展示类型
     * @param showByPoint 窗口显示位置
     */
    public static void showImageByStockCode(String stockCode, StockShowType type, Point showByPoint) throws MalformedURLException {
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
                    url = String.format("%s/%s_stock/%s/%s.png?%s", url, prefix, type.getType(), StringUtils.substring(stockCode, 2),
                            System.currentTimeMillis());
                }
                break;
            default:
                return;
        }

        // 暂时先硬编码，后续再优化调整
        showImage(new URL(url), type.getDesc(), showByPoint);
    }

    /**
     * 弹窗展示图片
     *
     * @param imageUrl    图片路径
     * @param title       窗口标题
     * @param showByPoint 窗口显示位置
     */
    public static void showImage(URL imageUrl, String title, Point showByPoint) {
        JLabel image = new JLabel(new ImageIcon(imageUrl));
        JBPopupFactory instance = JBPopupFactory.getInstance();
        JBPopup jbPopup = instance.createComponentPopupBuilder(image, null)
                .setTitle(title)
                .setMovable(true)
                .setRequestFocus(true)
                .createPopup();
        jbPopup.show(RelativePoint.fromScreen(showByPoint));
    }

    public enum FundShowType {
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
