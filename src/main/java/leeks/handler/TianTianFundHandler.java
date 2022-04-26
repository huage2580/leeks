package leeks.handler;

import leeks.bean.FundBean;
import com.google.gson.Gson;
import leeks.constant.Constants;
import leeks.ui.LeeksTableModel;
import org.apache.commons.lang.StringUtils;
import leeks.utils.HttpClientPool;
import leeks.utils.LogUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class TianTianFundHandler extends FundRefreshHandler {

    private static final String URL = "http://fundgz.1234567.com.cn/js/%s.js?rt=%d";

    private static final Gson GSON = new Gson();

    public TianTianFundHandler(LeeksTableModel tableModel) {
        super(tableModel);
    }

    @Override
    public void handleInternal(List<String> code) {
        //LogUtil.info("Leeks 更新Fund编码数据.");

        if (code.isEmpty()) {
            return;
        }

        stepAction(code);
    }

    private void stepAction(List<String> codes) {
        LogUtil.info("Leeks 刷新基金数据." + String.join(",", codes));
        List<String> codeList = new ArrayList<>();
        Map<String, String[]> codeMap = new HashMap<>();
        for (String str : codes) {
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

        for (String code : codeList) {
            Constants.EXECUTOR_SERVICE.submit(() -> {
                try {
                    String result = HttpClientPool.getHttpClient().get(String.format(URL, code, System.currentTimeMillis()));
                    String json = result.substring(8, result.length() - 2);
                    if (!json.isEmpty()) {
                        FundBean bean = GSON.fromJson(json, FundBean.class);
                        FundBean.loadFund(bean, codeMap);

                        BigDecimal now = new BigDecimal(bean.getGsz());
                        String costPriceStr = bean.getCostPrise();
                        if (StringUtils.isNotEmpty(costPriceStr)) {
                            BigDecimal costPriceDec = new BigDecimal(costPriceStr);
                            BigDecimal incomeDiff = now.add(costPriceDec.negate());
                            BigDecimal incomePercentDec = incomeDiff.divide(costPriceDec, 8, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.TEN)
                                    .multiply(BigDecimal.TEN)
                                    .setScale(3, RoundingMode.HALF_UP);
                            bean.setIncomePercent(incomePercentDec.toString());

                            String bondStr = bean.getBonds();
                            if (StringUtils.isNotEmpty(bondStr)) {
                                BigDecimal bondDec = new BigDecimal(bondStr);
                                BigDecimal incomeDec = incomeDiff.multiply(bondDec)
                                        .setScale(2, RoundingMode.HALF_UP);
                                bean.setIncome(incomeDec.toString());
                            }
                        }

                        updateData(bean);
                        updateUI();
                    } else {
                        LogUtil.info("Fund编码:[" + code + "]无法获取数据");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
