package handler;

import bean.FundBean;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import utils.HttpClientPool;
import utils.LogUtil;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TianTianFundHandler extends FundRefreshHandler {
    public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Gson gson = new Gson();
    private final List<String> codes = new ArrayList<>();

    private Thread worker;
    private JLabel refreshTimeLabel;
    /**
     * 更新数据的间隔时间（秒）
     */
    private volatile int threadSleepTime = 60;

    public TianTianFundHandler(JTable table, JLabel refreshTimeLabel) {
        super(table);
        this.refreshTimeLabel = refreshTimeLabel;
    }

    @Override
    public void handle(List<String> code) {
        if (worker != null) {
            worker.interrupt();
        }
        LogUtil.info("Leeks 更新Fund编码数据.");

        if (code.isEmpty()) {
            return;
        }

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (worker != null && worker.hashCode() == Thread.currentThread().hashCode() && !worker.isInterrupted()) {
                    synchronized (codes) {
                        stepAction();
                    }
                    try {
                        Thread.sleep(threadSleepTime * 1000);
                    } catch (InterruptedException e) {
                        LogUtil.info("Leeks 已停止更新Fund编码数据.");
                        refreshTimeLabel.setText("stop");
                        return;
                    }
                }
            }
        });
        synchronized (codes) {
            codes.clear();
            codes.addAll(code);
        }
        worker.start();
    }

    @Override
    public void stopHandle() {
        if (worker != null) {
            worker.interrupt();
            LogUtil.info("Leeks 准备停止更新Fund编码数据.");
        }
    }

    private void stepAction() {
//        LogUtil.info("Leeks 刷新基金数据.");
        List<String> codeList = new ArrayList<>();
        Map<String, String[]> codeMap = new HashMap<>();
        for (String str : codes) {
            //兼容原有设置
            String[] strArray;
            if (str.contains(",")) {
                strArray = str.split(",");
            } else {
                strArray = new String[]{str};
            }
            codeList.add(strArray[0]);
            codeMap.put(strArray[0], strArray);
        }

        for (String code : codeList) {
            new Thread(() -> {
                try {
                    String result = HttpClientPool.getHttpClient().get("http://fundgz.1234567.com.cn/js/" + code + ".js?rt=" + System.currentTimeMillis());
                    String json = result.substring(8, result.length() - 2);
                    if (!json.isEmpty()) {
                        FundBean bean = gson.fromJson(json, FundBean.class);
                        FundBean.loadFund(bean, codeMap);

                        BigDecimal now = new BigDecimal(bean.getGsz());
                        String costPriceStr = bean.getCostPrise();
                        if (StringUtils.isNotEmpty(costPriceStr)) {
                            BigDecimal costPriceDec = new BigDecimal(costPriceStr);
                            BigDecimal incomeDiff = now.add(costPriceDec.negate());
                            BigDecimal incomePercentDec = incomeDiff.divide(costPriceDec, 8, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.TEN)
                                    .multiply(BigDecimal.TEN)
                                    .setScale(3, RoundingMode.HALF_UP);
                            bean.setIncomePercent(incomePercentDec.toString());

                            String bondStr = bean.getBonds();
                            if (StringUtils.isNotEmpty(bondStr)) {
                                BigDecimal bondDec = new BigDecimal(bondStr);
                                BigDecimal incomeDec = incomeDiff.multiply(bondDec)
                                        .setScale(2, RoundingMode.HALF_UP);
                                bean.setIncome(incomeDec.toString());
                            }
                        }

                        updateData(bean);
                    } else {
                        LogUtil.info("Fund编码:[" + code + "]无法获取数据");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        updateUI();
    }

    public void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshTimeLabel.setText(LocalDateTime.now().format(timeFormatter));
                refreshTimeLabel.setToolTipText("最后刷新时间，刷新间隔" + threadSleepTime + "秒");
            }
        });
    }

    public int getThreadSleepTime() {
        return threadSleepTime;
    }

    public void setThreadSleepTime(int threadSleepTime) {
        this.threadSleepTime = threadSleepTime;
    }
}
