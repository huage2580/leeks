package utils;

import com.google.gson.Gson;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TianTianFundHandler extends FundRefreshHandler {
    public static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static Gson gson = new Gson();
    private List<String> codes = new ArrayList<>();

    private Thread worker;
    private JButton refreshButton;
    public TianTianFundHandler(JTable table, JButton refreshButton) {
        super(table);
        this.refreshButton = refreshButton;
    }

    @Override
    public void handle(List<String> code) {
        LogUtil.info("Leeks 更新基金编码数据.");
        if (worker!=null){
            worker.interrupt();
        }
        if (code.isEmpty()){
            return;
        }
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (worker!=null && worker.hashCode() == Thread.currentThread().hashCode() && !worker.isInterrupted()){
                    stepAction();
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        //移除了中断线程的警告
                    }
                }
            }
        });
        codes.clear();
        codes.addAll(code);
        worker.start();
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
                        FundBean bean = gson.fromJson(json,FundBean.class);
                        updateData(bean);
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
                refreshButton.setText("最后刷新时间: "+ LocalDateTime.now().format(timeFormatter));
            }
        });
    }
}
