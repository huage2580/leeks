package utils;

import com.google.gson.Gson;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TianTianFundHandler extends FundRefreshHandler {
    private static Gson gson = new Gson();
    private List<String> codes = new ArrayList<>();

    private Thread worker;
    public TianTianFundHandler(JTable table) {
        super(table);
    }

    @Override
    public void handle(List<String> code) {
        LogUtil.info("Leeks 更新基金编码数据.");
        if (worker!=null){
            worker.interrupt();
            worker.stop();
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
                        e.printStackTrace();
                    }
                }
            }
        });
        clear();
        codes.clear();
        codes.addAll(code);
        //排序，按加入顺序
        for (String s : codes) {
            updateData(new FundBean(s));
        }
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
                        updateUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
