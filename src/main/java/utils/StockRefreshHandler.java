package utils;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class StockRefreshHandler {
    private ArrayList<StockBean> data = new ArrayList<>();
    private JTable table;
    private int[] sizes = new int[]{0,0,0,0,0};

    public StockRefreshHandler(JTable table) {
        this.table = table;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    /**
     * 从网络更新数据
     *
     * @param code
     */
    public abstract void handle(List<String> code);

    /**
     * 更新全部数据
     */
    public void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                recordTableSize();
                String[] columnNames = {"股票名称", "当前价","涨跌", "涨跌幅", "更新时间"};
                DefaultTableModel model = new DefaultTableModel(convertData(), columnNames);
                table.setModel(model);
                updateColors();
                resizeTable();

            }
        });
    }

    private void recordTableSize() {
        if (table.getColumnModel().getColumnCount() == 0){
            return;
        }
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = table.getColumnModel().getColumn(i).getWidth();
        }
    }

    private void resizeTable() {
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > 0){
                table.getColumnModel().getColumn(i).setWidth(sizes[i]);
                table.getColumnModel().getColumn(i).setPreferredWidth(sizes[i]);
            }
        }
    }

    private void updateColors() {
        table.getColumn("涨跌幅").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                double temp = 0.0;
                try {
                    String s = value.toString().substring(0,value.toString().length()-1);
                    temp = Double.parseDouble(s);
                } catch (Exception e) {

                }
                Color orgin = getForeground();
                if (temp > 0) {
                    setForeground(JBColor.RED);
                } else if (temp < 0) {
                    setForeground(JBColor.GREEN);
                } else if (temp == 0) {
                    setForeground(orgin);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

    protected void updateData(StockBean bean) {
        int index = data.indexOf(bean);
        if (index >= 0) {
            data.set(index, bean);
        } else {
            data.add(bean);
        }
    }

    private Object[][] convertData() {
        Object[][] temp = new Object[data.size()][5];
        for (int i = 0; i < data.size(); i++) {
            StockBean fundBean = data.get(i);
            String timeStr = fundBean.getTime().substring(8);
            String changeStr = "--";
            String changePercentStr = "--";
            if (fundBean.getChange()!=null){
                changeStr= fundBean.getChange().startsWith("-")?fundBean.getChange():"+"+fundBean.getChange();
            }
            if (fundBean.getChangePercent()!=null){
                changePercentStr= fundBean.getChangePercent().startsWith("-")?fundBean.getChangePercent():"+"+fundBean.getChangePercent();
            }
            temp[i] = new Object[]{fundBean.getName(), fundBean.getNow(), changeStr,changePercentStr+"%", timeStr};
        }
        return temp;
    }

    protected void clear(){
        data.clear();
    }
}
