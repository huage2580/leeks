package utils;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class StockRefreshHandler {
    private ArrayList<StockBean> data = new ArrayList<>();
    private JTable table;
    private int[] sizes = new int[]{0,0,0,0,0};

    private boolean colorful = true;

    public StockRefreshHandler(JTable table) {
        this.table = table;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));
    }

    public void setColorful(boolean colorful) {
        this.colorful = colorful;
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
                if (!colorful){
                    for (int i = 0; i < columnNames.length; i++) {
                        columnNames[i] = PinYinUtils.toPinYin(columnNames[i]);
                    }
                }
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
        table.getColumn(table.getColumnName(3)).setCellRenderer(new DefaultTableCellRenderer() {
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
                    if (colorful){
                        setForeground(JBColor.RED);
                    }else {
                        setForeground(JBColor.DARK_GRAY);
                    }
                } else if (temp < 0) {
                    if (colorful){
                        setForeground(JBColor.GREEN);
                    }else {
                        setForeground(JBColor.GRAY);
                    }
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
            temp[i] = new Object[]{colorful?fundBean.getName():PinYinUtils.toPinYin(fundBean.getName())+" ("+fundBean.getCode()+")", fundBean.getNow(), changeStr,changePercentStr+"%", timeStr};
        }
        return temp;
    }

    protected void clear(){
        data.clear();
    }
}
