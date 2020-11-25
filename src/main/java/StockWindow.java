import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import utils.ButtonEnableUtil;
import utils.StockRefreshHandler;
import utils.TencentStockHandler;

import javax.swing.*;
import java.util.List;

public class StockWindow {
    private JPanel mPanel;
    private JTable table1;
    private JButton refreshButton;
    private JPanel toolPanel;
    private JButton stopButton;
    private JLabel refreshTime;

    static StockRefreshHandler handler;

    public JPanel getPanel1() {
        return mPanel;
    }

    public StockWindow() {
        handler = new TencentStockHandler(table1, refreshTime);
        // 刷新
        refreshButton.setIcon(AllIcons.Actions.Refresh);
        refreshButton.addActionListener(e -> {
            apply();
            ButtonEnableUtil.disableByTime(refreshButton, 2);
        });
        // 停止
        stopButton.setIcon(AllIcons.Actions.StopRefresh);
        stopButton.addActionListener(e -> {
            handler.stopHandle();
            ButtonEnableUtil.disableByTime(stopButton, 2);
        });
        // 非主要tab，需要创建，创建时立即应用数据
        apply();
    }

    public static void apply() {
        if (handler != null) {
            boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
            handler.refreshColorful(colorful);
            List<String> key_stocks = FundWindow.getConfigList("key_stocks", "[,，]");
            handler.handle(key_stocks);
        }
    }
}
