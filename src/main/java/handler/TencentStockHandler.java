package handler;

import bean.StockBean;
import org.apache.commons.lang.StringUtils;
import utils.HttpClientPool;
import utils.LogUtil;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TencentStockHandler extends StockRefreshHandler {
    private String urlPara;
    private HashMap codeMap;
    private Thread worker;
    private JLabel refreshTimeLabel;


    public TencentStockHandler(JTable table1, JLabel refreshTimeLabel) {
        super(table1);
        this.refreshTimeLabel = refreshTimeLabel;
    }

    @Override
    public void handle(List<String> code) {

        if (worker != null) {
            worker.interrupt();
        }
        LogUtil.info("Leeks 更新Stock编码数据.");
//        clearRow();
        if (code.isEmpty()) {
            return;
        }
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (worker != null && worker.hashCode() == Thread.currentThread().hashCode() && !worker.isInterrupted()) {
                    stepAction();
                    try {
                        Thread.sleep(threadSleepTime * 1000);
                    } catch (InterruptedException e) {
                        LogUtil.info("Leeks 已停止更新Stock编码数据.");
                        refreshTimeLabel.setText("stop");
                        return;
                    }
                }
            }
        });

        //股票编码，英文分号分隔（成本价和成本接在编码后用逗号分隔）
        List<String> codeList = new ArrayList<>();
        codeMap = new HashMap<>();
        for (String str : code) {
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

        urlPara = String.join(",", codeList);
        worker.start();

    }

    @Override
    public void stopHandle() {
        if (worker != null) {
            worker.interrupt();
            LogUtil.info("Leeks 准备停止更新Stock编码数据.");
        }
    }

    private void stepAction() {
//        Date now = new Date();
//        if ( now.getHours() < 9 || now.getHours() > 16){//九点到下午4点才更新数据
//            try {
//                Thread.sleep(60 * 1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return;
//        }
        if (StringUtils.isEmpty(urlPara)) {
            return;
        }
        try {
            String result = HttpClientPool.getHttpClient().get("http://qt.gtimg.cn/q=" + urlPara);
            parse(result);
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse(String result) {
        String[] lines = result.split("\n");
        for (String line : lines) {
            String code = line.substring(line.indexOf("_") + 1, line.indexOf("="));
            String dataStr = line.substring(line.indexOf("=") + 2, line.length() - 2);
            String[] values = dataStr.split("~");
            StockBean bean = new StockBean(code, codeMap);
            bean.setName(values[1]);
            bean.setNow(values[3]);
            bean.setChange(values[31]);
            bean.setChangePercent(values[32]);
            bean.setTime(values[30]);
            bean.setMax(values[33]);//33
            bean.setMin(values[34]);//34

            BigDecimal now = new BigDecimal(values[3]);
            String costPriceStr = bean.getCostPrise();
            if (StringUtils.isNotEmpty(costPriceStr)) {
                BigDecimal costPriceDec = new BigDecimal(costPriceStr);
                BigDecimal incomeDiff = now.add(costPriceDec.negate());
                BigDecimal incomePercentDec = incomeDiff.divide(costPriceDec, 5, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.TEN)
                        .multiply(BigDecimal.TEN)
                        .setScale(3, RoundingMode.HALF_UP);
                bean.setIncomePercent(incomePercentDec.toString());

                String bondStr = bean.getBonds();
                if (StringUtils.isNotEmpty(bondStr)) {
                    BigDecimal bondDec = new BigDecimal(bondStr);
                    BigDecimal incomeDec = incomeDiff.divide(costPriceDec, 5, RoundingMode.HALF_UP)
                            .multiply(bondDec)
                            .multiply(now)
                            .setScale(2, RoundingMode.HALF_UP);
                    bean.setIncome(incomeDec.toString());
                }
            }

            updateData(bean);
        }
    }

    public void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshTimeLabel.setText(LocalDateTime.now().format(TianTianFundHandler.timeFormatter));
                refreshTimeLabel.setToolTipText("最后刷新时间，刷新间隔" + threadSleepTime + "秒");
            }
        });
    }


}
