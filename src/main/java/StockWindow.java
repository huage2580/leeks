import com.intellij.ide.util.PropertiesComponent;
import utils.StockRefreshHandler;
import utils.TencentStockHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockWindow {
    private JPanel panel1;
    private JTable table1;
    private JLabel label;

    static StockRefreshHandler handler;

    public JPanel getPanel1() {
        return panel1;
    }

    public StockWindow() {
        handler = new TencentStockHandler(table1,label);
    }

    public static void apply() {
        if (handler != null) {
            boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
            handler.refreshColorful(colorful);
            handler.handle(loadStocks());
        }
    }

    public void onInit(){
        boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
        handler.refreshColorful(colorful);
        handler.handle(loadStocks());
    }

    private static List<String> loadStocks(){
        return FundWindow.getConfigList("key_stocks", "[,，]");
    }

}
