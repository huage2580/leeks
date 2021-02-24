package utils;

import com.google.gson.Gson;
import com.intellij.openapi.progress.ProgressManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;

import service.AlertService;
import utils.thread.ThreadUtil;


/**
 * 天天基金接口
 */
public class TianTianFundHandler extends FundRefreshHandler {
    public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Gson gson = new Gson();
    private final List<String> codes = new CopyOnWriteArrayList<>();

    private Thread worker;
    private JLabel refreshTimeLabel;
    /**
     * 更新数据的间隔时间（秒）
     */
    private volatile int threadSleepTime = XConstant.FUND_UPDATE_INTERVAL;

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
        LogUtil.info("Leeks 刷新基金数据.");
        mFundBeans.clear();
        ThreadUtil.runOnBackground(new Runnable() {
            @Override
            public void run() {
                for (String code : codes) {
                    try {
                        String result =
                                HttpClientPool.getHttpClient().get("http://fundgz.1234567.com.cn/js/" + code + ".js?rt=" + System.currentTimeMillis());
                        String json = result.substring(8, result.length() - 2);
                        if (!json.isEmpty()) {
                            FundBean bean = gson.fromJson(json, FundBean.class);
                            updateData(bean);
                            addDropFundIfNeed(bean);
                        } else {
                            LogUtil.info("Fund编码:[" + code + "]无法获取数据");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //在这里面更新UI 安全
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        //看一下是否需要展示对话框
                        showDialogIfNeed();
                    }
                });
            }
        });
        updateUI();
    }

    private List<FundBean> mFundBeans = new CopyOnWriteArrayList<>();

    /**
     * 看一下 是否需要 展示提醒对话框
     */
    private void showDialogIfNeed() {
        if (mFundBeans.size() == 0) {
            return;
        }
        if (XDateUtil.isShowTime(true)) {
                        /*ProgressManager.getInstance().executeNonCancelableSection(
                                () -> AlertService.getInstance().showAlertDialog(null, period));*/
            StringBuilder stringBuilder = new StringBuilder();
            for (FundBean fundBean : mFundBeans) {
                stringBuilder.append(fundBean.getFundName()).append(" ,  ").append(fundBean.getGszzl()).append("%").append("<br>");
            }
            stringBuilder.append("该加仓了");
            //展示内容:
            ProgressManager.getInstance().executeNonCancelableSection(
                    () -> {
                        XDateUtil.updateFundShowDate();
                        AlertService.getInstance().showAlertDialog(null, stringBuilder.toString());
                    });
        } else {
            System.out.println("没在加仓时间内");
        }
    }

    /**
     * 跌了0.5以上就加进来
     */
    private void addDropFundIfNeed(FundBean bean) {
        //如果是跌了  则是 -0.92  这样样子
        String gszzl = bean.getGszzl();
        System.out.println(gszzl + "----");
        if (!gszzl.contains("-")) {
            //涨了
            return;
        }
        try {
            if (Float.parseFloat(gszzl) < XConstant.FUND_ADDING_THRESHOLD) {
                mFundBeans.add(bean);
            }
        } catch (Exception ignored) {
            //格式化失败  不管
        }
    }

    public void updateUI() {
        SwingUtilities.invokeLater(() -> {
            refreshTimeLabel.setText(LocalDateTime.now().format(timeFormatter));
            refreshTimeLabel.setToolTipText("最后刷新时间，刷新间隔" + threadSleepTime + "秒");
        });
    }

    public int getThreadSleepTime() {
        return threadSleepTime;
    }

    public void setThreadSleepTime(int threadSleepTime) {
        this.threadSleepTime = threadSleepTime;
    }
}
