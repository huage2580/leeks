package leeks.handler;

import leeks.bean.FundBean;
import leeks.ui.LeeksTableModel;

import java.util.List;

public abstract class FundRefreshHandler extends AbstractHandler {

    public FundRefreshHandler(LeeksTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void setupTable(List<String> code) {
        for (String s : code) {
            updateData(new FundBean(s));
        }
    }
}
