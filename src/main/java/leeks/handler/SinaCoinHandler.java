package leeks.handler;

import com.google.common.base.Joiner;
import leeks.utils.HttpClientPool;
import leeks.utils.LogUtil;
import leeks.ui.LeeksTableModel;

import java.util.List;

@Deprecated
public class SinaCoinHandler extends CoinRefreshHandler {
    private final String URL = "http://hq.sinajs.cn/list=";

    public SinaCoinHandler(LeeksTableModel tableModel) {
        super(tableModel);
    }

    @Override
    public void handleInternal(List<String> code) {
        if (code.isEmpty()) {
            return;
        }

        pollStock(code);
    }

    private void pollStock(List<String> code) {
        String params = Joiner.on(",").join(code);
        try {
            String res = HttpClientPool.getHttpClient().get(URL + params);
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
//            CoinBean leeks.bean = new CoinBean(code);
//            leeks.bean.setName(split[9]);
//            leeks.bean.setPrice(split[8]);
//            leeks.bean.setTimeStamp(split[0]);
//            updateData(leeks.bean);
//            refreshTimeList.add(split[0]);
//        }
//
//        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
//        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
    }
}
