package utils;

import java.util.Calendar;

/**
 * Created by xfhy on 2021/1/27 14:44
 * Description :
 */
public class XDateUtil {

    /**
     * 上次展示基金dialog的日子
     */
    private volatile static int sLastShowFundDate = -78;
    /**
     * 上次展示股票dialog的日子
     */
    private volatile static int sLastShowStockDate = -78;

    public static boolean isShowTime(boolean isFund) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        //正确的时间
        boolean isRightTime = (hour == XConstant.TARGET_HOUR &&
                minute >= XConstant.TARGET_START_MINUTE &&
                minute <= XConstant.TARGET_END_MINUTE);

        //正确的日期
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isRightTradingDay = false;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
                isRightTradingDay = true;
                break;
            default:
                if (XConstant.IS_DEBUG) {
                    isRightTradingDay = true;
                }
                break;
        }

        //一天 只能展示一次
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        boolean isRightDate = isFund ? (dayOfYear != sLastShowFundDate) : (dayOfYear != sLastShowStockDate);

        if (isRightTime) {
            LogUtil.info("isRightTradingDay = " + isRightTradingDay + "  isRightDate = " + isRightDate);
        }

        return isRightTime && isRightTradingDay && isRightDate;
    }

    public static void updateFundShowDate() {
        sLastShowFundDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        LogUtil.info("更新展示基金dialog日期: " + sLastShowFundDate);
    }

    public static void updateStockShowDate() {
        sLastShowStockDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        LogUtil.info("更新展示股票ialog日期: " + sLastShowStockDate);
    }

}
