package utils;

public class XConstant {

    public static final boolean IS_DEBUG = true;

    public static class Infor {
        /**
         * title
         */
        public static final String TITLE = "温馨提醒!";

    }

    public final static int TARGET_HOUR = 15;
    public final static int TARGET_START_MINUTE = IS_DEBUG ? 0 : 45;
    public final static int TARGET_END_MINUTE = IS_DEBUG ? 59 : 58;

    /**
     * 基金更新时间间隔
     */
    public final static int FUND_UPDATE_INTERVAL = IS_DEBUG ? 10 : 5 * 60;
    /**
     * 股票更新时间间隔
     */
    public final static int STOCK_UPDATE_INTERVAL = IS_DEBUG ? 10 : 5 * 60;

    /**
     * 股票加仓阈值
     */
    public final static double STOCK_ADDING_THRESHOLD = -0.3;
    /**
     * 基金加仓阈值
     */
    public final static double FUND_ADDING_THRESHOLD = -0.5;

}
