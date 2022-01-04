package handler;

import com.google.common.base.Joiner;
import bean.CoinBean;
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
@Deprecated
public class SinaCoinHandler extends CoinRefreshHandler {
    private final String URL = "http://hq.sinajs.cn/list=";
    //private static final Pattern DEFAULT_STOCK_PATTERN = Pattern.compile("var hq_str_(\\w+?)=\"(.*?)\";");
    private final JLabel refreshTimeLabel;

    public SinaCoinHandler(JTable table, JLabel label) {
        super(table);
        this.refreshTimeLabel = label;
    }

    @Override
    public void handle(List<String> code) {
        if (code.isEmpty()) {
            return;
        }

        pollStock(code);
    }

    private void pollStock(List<String> code) {
        String params = Joiner.on(",").join(code);
        try {
            String res = HttpClientPool.getHttpClient().get(URL + params);
//            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
//            System.out.printf("%s,%s%n", time, res);
            handleResponse(res);
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
        }
    }

    public void handleResponse(String response) {
//        List<String> refreshTimeList = new ArrayList<>();
//        for (String line : response.split("\n")) {
//            Matcher matcher = DEFAULT_STOCK_PATTERN.matcher(line);
//            if (!matcher.matches()) {
//                continue;
//            }
//            String code = matcher.group(1);
//            String[] split = matcher.group(2).split(",");
//            if (split.length < 2) {//空数据跳过
//                continue;
//            }
//            CoinBean bean = new CoinBean(code);
//            bean.setName(split[9]);
//            bean.setPrice(split[8]);
//            bean.setTimeStamp(split[0]);
//            updateData(bean);
//            refreshTimeList.add(split[0]);
//        }
//
//        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
//        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
    }

    @Override
    public void stopHandle() {
        LogUtil.info("leeks stock 自动刷新关闭!");
    }
}
