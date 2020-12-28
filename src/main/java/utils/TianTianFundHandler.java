package utils;

import com.google.gson.Gson;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TianTianFundHandler extends FundRefreshHandler {
    public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Gson gson = new Gson();
    private final List<String> codes = new ArrayList<>();

    private Thread worker;
    private JLabel refreshTimeLabel;
    public TianTianFundHandler(JTable table, JLabel refreshTimeLabel) {
        super(table);
        this.refreshTimeLabel = refreshTimeLabel;
    }

    @Override
    public void handle(List<String> code) {
        if (worker!=null){
            worker.interrupt();
        }
        LogUtil.info("Leeks 更新Fund编码数据.");

        if (code.isEmpty()){
            return;
        }

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (worker!=null && worker.hashCode() == Thread.currentThread().hashCode() && !worker.isInterrupted()){
                    synchronized (codes){
                        stepAction();
                    }
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        LogUtil.info("Leeks 已停止更新Fund编码数据.");
                        refreshTimeLabel.setText("stop");
                        return;
                    }
                }
            }
        });
        synchronized (codes){
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

    private void stepAction(){
//        LogUtil.info("Leeks 刷新基金数据.");
        for (String s : codes) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = HttpClientPool.getHttpClient().get("http://fundgz.1234567.com.cn/js/"+s+".js?rt="+System.currentTimeMillis());
                        String json = result.substring(8,result.length()-2);
                        if(!json.isEmpty()){
                            FundBean bean = gson.fromJson(json,FundBean.class);
                            updateData(bean);
                        }else {
                            LogUtil.info("Fund编码:["+s+"]无法获取数据");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
            }
        });
    }
}
