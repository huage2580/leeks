package utils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TencentStockHandler extends StockRefreshHandler {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");

    private List<String> codes = new ArrayList<>();

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
            worker.stop();
        }
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (worker!=null && worker.hashCode() == Thread.currentThread().hashCode() && !worker.isInterrupted()){
                    stepAction();
                    try {
                        Thread.sleep(10 * 1000);
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
            updateData(new StockBean(s));
        }
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
        if (codes.isEmpty()){
            return;
        }
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < codes.size(); i++) {
            stringBuffer.append(codes.get(i));
            if (i< codes.size()-1){
                stringBuffer.append(',');
            }
        }
        try {
            String result = HttpClientPool.getHttpClient().get("http://qt.gtimg.cn/q="+stringBuffer.toString());
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
            updateData(bean);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                label.setText("最后刷新时间:"+dateFormat.format(new Date()));
            }
        });
    }
}
