package utils;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

public class TencentStockHandler extends StockRefreshHandler {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");

    private String urlPara;

    private Thread worker;
    private JLabel label;

    public TencentStockHandler(JTable table) {
        super(table);
    }

    public TencentStockHandler(JTable table1, JLabel label) {
        super(table1);
        this.label = label;
    }

    @Override
    public void handle(List<String> code) {

        LogUtil.info("Leeks 更新股票编码数据.");
        if (code.isEmpty()){
            return;
        }
        if (worker!=null){
            worker.interrupt();
        }
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (worker!=null && worker.hashCode() == Thread.currentThread().hashCode() && !worker.isInterrupted()){
                    stepAction();
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        //移除了中断线程的警告
                    }
                }
            }
        });
        urlPara = String.join(",", code);
        worker.start();

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
        if (StringUtils.isEmpty(urlPara)){
            return;
        }
        try {
            String result = HttpClientPool.getHttpClient().get("http://qt.gtimg.cn/q="+urlPara);
            parse(result);
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse(String result) {
        String[] lines = result.split("\n");
        for (String line : lines) {
            String code = line.substring(line.indexOf("_")+1,line.indexOf("="));
            String dataStr = line.substring(line.indexOf("=")+2,line.length()-2);
            String[] values = dataStr.split("~");
            StockBean bean = new StockBean(code);
            bean.setName(values[1]);
            bean.setNow(values[3]);
            bean.setChange(values[31]);
            bean.setChangePercent(values[32]);
            bean.setTime(values[30]);
            bean.setMax(values[33]);//33
            bean.setMin(values[34]);//34
            updateData(bean);
        }
    }

    public void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                label.setText("最后刷新时间: "+ LocalDateTime.now().format(TianTianFundHandler.timeFormatter));
            }
        });
    }
}
