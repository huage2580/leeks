package handler;

import bean.CoinBean;
import bean.YahooResponse;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import utils.HttpClientPool;
import utils.LogUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YahooCoinHandler extends CoinRefreshHandler {
    private static final String URL = "https://query1.finance.yahoo.com/v7/finance/quote?&symbols=";
    private static final String KEYS = "&fields=regularMarketChange,regularMarketChangePercent,regularMarketPrice,regularMarketTime,regularMarketDayHigh,regularMarketDayLow";
    private final JLabel refreshTimeLabel;

    private static ScheduledExecutorService mSchedulerExecutor = Executors.newSingleThreadScheduledExecutor();

    private static Gson gson = new Gson();

    public YahooCoinHandler(JTable table, JLabel label) {
        super(table);
        this.refreshTimeLabel = label;
    }

    @Override
    public void handle(List<String> code) {
        if (code.isEmpty()) {
            return;
        }

        useScheduleThreadExecutor(code);
    }

    public void useScheduleThreadExecutor(List<String> code) {
        if (mSchedulerExecutor.isShutdown()){
            mSchedulerExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        mSchedulerExecutor.scheduleAtFixedRate(getWork(code), 0, threadSleepTime, TimeUnit.SECONDS);
    }

    private Runnable getWork(List<String> code) {
        return () -> {
            pollStock(code);
        };
    }

    private void pollStock(List<String> code) {
        if (code.isEmpty()){
            return;
        }
        String params = Joiner.on(",").join(code);
        try {
            String res = HttpClientPool.getHttpClient().get(URL + params + KEYS);
//            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
//            System.out.printf("%s,%s%n", time, res);
            handleResponse(res);
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
        }
    }

    public void handleResponse(String response) {
//        System.out.println("解析虚拟币："+response);
        List<String> refreshTimeList = new ArrayList<>();
        try{
            YahooResponse yahooResponse = gson.fromJson(response, YahooResponse.class);
            for (CoinBean coinBean : yahooResponse.getQuoteResponse().getResult()) {
                updateData(coinBean);
                refreshTimeList.add(coinBean.getValueByColumn("更新时间",false));
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }

        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
    }

    @Override
    public void stopHandle() {
        mSchedulerExecutor.shutdown();
        LogUtil.info("leeks stock 自动刷新关闭!");
    }
}
