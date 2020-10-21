package utils;

import com.intellij.ui.JBColor;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public abstract class StockRefreshHandler {
    private static String[] columnNames;
    private static String[] columnNamesPy;

    private static String[] useColumnNames;

    static {
        columnNames = new String[]{"编码", "股票名称", "当前价", "涨跌", "涨跌幅", "更新时间"};
        columnNamesPy = new String[columnNames.length];
        for (int i = 0; i < columnNamesPy.length; i++) {
            columnNamesPy[i] = PinYinUtils.toPinYin(columnNames[i]);
        }
        useColumnNames = columnNames;
    }

    private DefaultTableModel model;
    private JTable table;
    private boolean colorful = true;

    public StockRefreshHandler(JTable table) {
        this.table = table;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));
        Object[][] tableData = new Object[0][useColumnNames.length];
        model = new DefaultTableModel(tableData, useColumnNames);
        table.setModel(model);
        columnColors(colorful);
    }

    public void refreshColorful(boolean colorful) {
        this.colorful = colorful;
        // 刷新表头
        if (colorful) {
            useColumnNames = columnNames;
        } else {
            useColumnNames = columnNamesPy;
        }
        model.setColumnIdentifiers(useColumnNames);
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(model);
        Comparator<Object> dobleComparator = (o1, o2) -> {
            Double v1 = Double.parseDouble(StringUtils.remove((String) o1, '%'));
            Double v2 = Double.parseDouble(StringUtils.remove((String) o2, '%'));
            System.out.println(v1 + " " + v2);
            return v1.compareTo(v2);
        };
        rowSorter.setComparator(2, dobleComparator);
        rowSorter.setComparator(3, dobleComparator);
        rowSorter.setComparator(4, dobleComparator);
        table.setRowSorter(rowSorter);
        columnColors(colorful);
    }

    /**
     * 从网络更新数据
     *
     * @param code
     */
    public abstract void handle(List<String> code);

    private void columnColors(boolean colorful) {
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                double temp = 0.0;
                try {
                    String s = value.toString().substring(0,value.toString().length()-1);
                    temp = Double.parseDouble(s);
                } catch (Exception e) {

                }
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
                    Color orgin = getForeground();
                    setForeground(orgin);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        table.getColumn(useColumnNames[3]).setCellRenderer(cellRenderer);
        table.getColumn(useColumnNames[4]).setCellRenderer(cellRenderer);
    }

    protected void updateData(StockBean bean) {
        Object[] convertData = convertData(bean);
        // 获取行
        int index = findRowIndex(0, bean.getCode());
        if (index >= 0) {
            for (int columnIndex = 0; columnIndex < convertData.length; columnIndex++) {
                model.setValueAt(convertData[columnIndex], index, columnIndex);
            }
        } else {
            model.addRow(convertData);
        }
    }

    /**
     * 查找列项中的valueName所在的行
     *
     * @param columnIndex 列号
     * @param value       值
     * @return 如果不存在返回-1
     */
    protected int findRowIndex(int columnIndex, String value) {
        int rowCount = model.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object valueAt = model.getValueAt(rowIndex, columnIndex);
            if (StringUtils.equalsIgnoreCase(value, valueAt.toString())) {
                return rowIndex;
            }
        }
        return -1;
    }

    private Object[] convertData(StockBean fundBean) {
        String timeStr = fundBean.getTime().substring(8);
        String changeStr = "--";
        String changePercentStr = "--";
        if (fundBean.getChange()!=null){
            changeStr= fundBean.getChange().startsWith("-")?fundBean.getChange():"+"+fundBean.getChange();
        }
        if (fundBean.getChangePercent()!=null){
            changePercentStr= fundBean.getChangePercent().startsWith("-")?fundBean.getChangePercent():"+"+fundBean.getChangePercent();
        }
        return new Object[]{fundBean.getCode(),colorful?fundBean.getName():PinYinUtils.toPinYin(fundBean.getName()),
                fundBean.getNow(), changeStr, changePercentStr+"%", timeStr};
    }
}
