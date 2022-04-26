package leeks.handler;

import com.intellij.ide.util.PropertiesComponent;
import leeks.bean.StockBean;
import leeks.ui.LeeksTableModel;

import java.util.List;
import java.util.Vector;

public abstract class StockRefreshHandler extends AbstractHandler {

    public StockRefreshHandler(LeeksTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void setupTable(List<String> code) {
        for (String s : code) {
            updateData(new StockBean(s));
        }
    }
}
