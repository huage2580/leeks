package leeks.constant;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 常量类.
 */
public class Constants {

    public final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(3,
            15,
            5,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1),
            new ThreadFactoryBuilder()
                    .setNameFormat("LEEKS-POOL-%d").build(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public final class Keys {
        private Keys() {
        }

        public static final String FUNDS = "key_funds";
        public static final String STOCKS = "key_stocks";
        public static final String COINS = "key_coins";
        public static final String COLORFUL = "key_colorful";
        public static final String CRON_EXPRESSION_FUND = "key_cron_expression_fund";
        public static final String CRON_EXPRESSION_STOCK = "key_cron_expression_stock";
        public static final String CRON_EXPRESSION_COIN = "key_cron_expression_coin";
        public static final String TABLE_STRIPED = "key_table_striped";
        public static final String STOCKS_SINA = "key_stocks_sina";
        public static final String CLOSE_LOG = "key_close_log";
        public static final String PROXY = "key_proxy";
    }
}
