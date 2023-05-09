import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import quartz.QuartzManager;
import utils.HttpClientPool;
import utils.LogUtil;
import utils.StockUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsWindow implements Configurable {
    private JPanel panel1;
    private JTextArea textAreaFund;
    private JTextArea textAreaStock;
    private JCheckBox checkbox;
    /**
     * 使用tab界面，方便不同的设置分开进行控制
     */
    private JTabbedPane tabbedPane1;
    private JCheckBox checkBoxTableStriped;
    private JTextField cronExpressionFund;
    private JTextField cronExpressionStock;
    private JTextField cronExpressionCoin;
    private JCheckBox checkboxSina;
    private JCheckBox checkboxLog;
    private JTextArea textAreaCoin;
    private JLabel proxyLabel;
    private JTextField inputProxy;
    private JButton proxyTestButton;
    private JTextField searchInput;
    private JButton searchBtn;
    private JTextField searchResult;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Leeks";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        String value = instance.getValue("key_funds");
        String value_stock = instance.getValue("key_stocks");
        String value_coin = instance.getValue("key_coins");
        boolean value_color = instance.getBoolean("key_colorful");
        textAreaFund.setText(value);
        textAreaStock.setText(value_stock);
        textAreaCoin.setText(value_coin);
        checkbox.setSelected(!value_color);
        checkBoxTableStriped.setSelected(instance.getBoolean("key_table_striped"));
        checkboxSina.setSelected(instance.getBoolean("key_stocks_sina"));
        checkboxLog.setSelected(instance.getBoolean("key_close_log"));
        cronExpressionFund.setText(instance.getValue("key_cron_expression_fund", "0 * * * * ?")); //默认每分钟执行
        cronExpressionStock.setText(instance.getValue("key_cron_expression_stock", "*/10 * * * * ?")); //默认每10秒执行
        cronExpressionCoin.setText(instance.getValue("key_cron_expression_coin", "*/10 * * * * ?")); //默认每10秒执行
        //代理设置
        inputProxy.setText(instance.getValue("key_proxy"));
        proxyTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String proxy = inputProxy.getText().trim();
                testProxy(proxy);
            }
        });
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String keyWord = searchInput.getText().trim();
                searchStock(keyWord);
            }
        });
        return panel1;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        String errorMsg = checkConfig();
        if (StringUtils.isNotEmpty(errorMsg)) {
            throw new ConfigurationException(errorMsg);
        }
        PropertiesComponent instance = PropertiesComponent.getInstance();
        instance.setValue("key_funds", textAreaFund.getText());
        instance.setValue("key_stocks", textAreaStock.getText());
        instance.setValue("key_coins", textAreaCoin.getText());
        instance.setValue("key_colorful", !checkbox.isSelected());
        instance.setValue("key_cron_expression_fund", cronExpressionFund.getText());
        instance.setValue("key_cron_expression_stock", cronExpressionStock.getText());
        instance.setValue("key_cron_expression_coin", cronExpressionCoin.getText());
        instance.setValue("key_table_striped", checkBoxTableStriped.isSelected());
        instance.setValue("key_stocks_sina", checkboxSina.isSelected());
        instance.setValue("key_close_log", checkboxLog.isSelected());
        String proxy = inputProxy.getText().trim();
        instance.setValue("key_proxy", proxy);
        HttpClientPool.getHttpClient().buildHttpClient(proxy);
        StockWindow.apply();
        FundWindow.apply();
        CoinWindow.apply();
    }

    private void searchStock(String query) {
        if (query == null || query.length() == 0) {
            LogUtil.notify("请输入股票名称或者股票代码", false);
            return;
        }
        try {
            String url = "https://suggest.eastmoney.com/suggest/default.aspx?type=81&input=" + URLEncoder.encode(query, "UTF-8") + "&t=1620608167844";
            URL yahooFinance = new URL(url);
            Scanner webpageScanner = new Scanner(yahooFinance.openStream());
            String result = "";
            while (webpageScanner.hasNextLine()) {
                String line = webpageScanner.nextLine();

                try {
                    result = parseResponseBody(line);
                } catch (Exception e) {
                    result = line;
                }
            }
            webpageScanner.close();
            searchResult.setText(result);
            LogUtil.notify("搜索成功：" + result, true);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.notify("搜索异常!", false);
        }
    }

    private String parseResponseBody(String responseBody) {
        String[] stocks = responseBody.substring(responseBody.indexOf("\"") + 1, responseBody.lastIndexOf("\"")).split(";");

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < stocks.length; i++) {
            String[] stock = stocks[i].split(",");
            String code = stock[1];
            String name = stock[0];
            result.append(name).append(",").append(code).append(";");
        }
        return result.toString();
    }

    private void testProxy(String proxy) {
        if (proxy.indexOf('：') > 0) {
            LogUtil.notify("别用中文分割符啊!", false);
            return;
        }
        HttpClientPool httpClientPool = HttpClientPool.getHttpClient();
        httpClientPool.buildHttpClient(proxy);
        try {
            httpClientPool.get("https://www.baidu.com");
            LogUtil.notify("代理测试成功!请保存", true);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.notify("测试代理异常!", false);
        }
    }

    public static List<String> getConfigList(String key, String split) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        String[] codes = value.split(split);
        for (String code : codes) {
            if (!code.isEmpty()) {
                set.add(code.trim());
            }
        }
        return new ArrayList<>(set);
    }

    public static List<String> getConfigList(String key) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        String[] codes = null;
        if (value.contains(";")) {//包含分号
            codes = value.split("[;]");
        } else {
            codes = value.split("[,，]");
        }
        for (String code : codes) {
            if (!code.isEmpty()) {
                String targetCode = StockUtils.autoCompleteCode(code);
                set.add(targetCode);
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 检查配置项
     *
     * @return 返回提示的错误信息
     */
    private String checkConfig() {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append(getConfigList(cronExpressionFund.getText(), ";").stream().map(s -> {
            if (!QuartzManager.checkCronExpression(s)) {
                return "Fund请配置正确的cron表达式[" + s + "]、";
            } else {
                return "";
            }
        }).collect(Collectors.joining()));
        errorMsg.append(getConfigList(cronExpressionStock.getText(), ";").stream().map(s -> {
            if (!QuartzManager.checkCronExpression(s)) {
                return "Stock请配置正确的cron表达式[" + s + "]、";
            } else {
                return "";
            }
        }).collect(Collectors.joining()));
        errorMsg.append(getConfigList(cronExpressionCoin.getText(), ";").stream().map(s -> {
            if (!QuartzManager.checkCronExpression(s)) {
                return "Coin请配置正确的cron表达式[" + s + "]、";
            } else {
                return "";
            }
        }).collect(Collectors.joining()));
        return errorMsg.toString();
    }
}
