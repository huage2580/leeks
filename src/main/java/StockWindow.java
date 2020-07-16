import com.intellij.ide.util.PropertiesComponent;
import utils.StockRefreshHandler;
import utils.TencentStockHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class StockWindow {
    private JPanel panel1;
    private JTable table1;
    private JLabel label;

    StockRefreshHandler handler;

    public JPanel getPanel1() {
        return panel1;
    }

    public StockWindow() {
        handler = new TencentStockHandler(table1,label);
    }

    public void onInit(){
        handler.handle(loadStocks());
    }

    private List<String> loadStocks(){
        ArrayList<String> temp = new ArrayList<>();
        String value = PropertiesComponent.getInstance().getValue("key_stocks");
//        String value = "sh600519,sz000001";
        if (value == null){
            return temp;
        }
        String[] codes = value.split("[,，]");
        for (String code : codes) {
            if (!code.isEmpty()){
                temp.add(code);
            }
        }
        return temp;
    }

}
