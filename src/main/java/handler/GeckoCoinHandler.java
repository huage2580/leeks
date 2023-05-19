package handler;

import bean.CoinBean;
import bean.GeckoResponse;
import com.google.gson.Gson;
import utils.LogUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data provided by CoinGecko
 */
public class GeckoCoinHandler extends CoinRefreshHandler {
    private final String URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd";
    private final JLabel refreshTimeLabel;

    private final Gson gson = new Gson();

    public GeckoCoinHandler(JTable table, JLabel label) {
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
        if (code.isEmpty()){
            return;
        }
        try {
            List<String> refreshTimeList = new ArrayList<>();
            for (String coin : code) {
                CoinBean coinBean = CryptoPrice.getCoinData(coin);
                updateData(coinBean);
                refreshTimeList.add(coinBean.getValueByColumn("更新时间",false));
            }
            String text = refreshTimeList.stream().sorted().findFirst().orElse("");
            SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
        }
    }

    public void handleResponse(String response) {
//        System.out.println("解析虚拟币："+response);
        List<String> refreshTimeList = new ArrayList<>();
        try{
            HashMap<String, GeckoResponse> geckoResponse = gson.fromJson(response, HashMap.class);
            List<CoinBean> coinBeanList = new ArrayList<>();
            for (Map.Entry<String, GeckoResponse> stringGeckoResponseEntry : geckoResponse.entrySet()) {
                CoinBean coinBean = new CoinBean(stringGeckoResponseEntry.getKey());
                coinBean.setRegularMarketChange(stringGeckoResponseEntry.getValue().getUsd());
                coinBeanList.add(coinBean);
            }
            for (CoinBean coinBean : coinBeanList) {
                updateData(coinBean);
                refreshTimeList.add(coinBean.getValueByColumn("更新时间",false));
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }

        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
    }

    @Override
    public void stopHandle() {
        LogUtil.info("leeks stock 自动刷新关闭!");
    }
}
