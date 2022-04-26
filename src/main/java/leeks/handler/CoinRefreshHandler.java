package leeks.handler;

import leeks.bean.CoinBean;
import leeks.ui.LeeksTableModel;

import java.util.List;
import java.util.Vector;

public abstract class CoinRefreshHandler extends AbstractHandler {

    public CoinRefreshHandler(LeeksTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void setupTable(List<String> code) {
        for (String s : code) {
            updateData(new CoinBean(s));
        }
    }

}
