package handler;

import bean.FundBean;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import utils.PinYinUtils;
import utils.WindowUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class FundRefreshHandler extends DefaultTableModel {
    private static String[] columnNames;
    /**
     * 存放【编码】的位置，更新数据时用到
     */
    public int codeColumnIndex;

    private JTable table;
    private boolean colorful = true;

    static {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        String tableHeader = instance.getValue(WindowUtils.FUND_TABLE_HEADER_KEY);
        if (StringUtils.isBlank(tableHeader)) {
            instance.setValue(WindowUtils.FUND_TABLE_HEADER_KEY, WindowUtils.FUND_TABLE_HEADER_VALUE);
            tableHeader = WindowUtils.FUND_TABLE_HEADER_VALUE;
        }
        String[] configStr = tableHeader.split(",");
        columnNames = new String[configStr.length];
        for (int i = 0; i < configStr.length; i++) {
            columnNames[i] = WindowUtils.remapPinYin(configStr[i]);
        }
    }

    {
        for (int i = 0; i < columnNames.length; i++) {
            if ("编码".equals(columnNames[i])) {
                codeColumnIndex = i;
            }
        }
    }

    public FundRefreshHandler(JTable table) {
        this.table = table;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));
        table.setModel(this);
        refreshColorful(!colorful);
    }

    public void refreshColorful(boolean colorful) {
        if (this.colorful == colorful) {
            return;
        }
        this.colorful = colorful;
        // 刷新表头
        if (colorful) {
            setColumnIdentifiers(columnNames);
        } else {
            setColumnIdentifiers(PinYinUtils.toPinYin(columnNames));
        }
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(this);
        Comparator<Object> doubleComparator = (o1, o2) -> {
            Double v1 = NumberUtils.toDouble(StringUtils.remove((String) o1, '%'));
            Double v2 = NumberUtils.toDouble(StringUtils.remove((String) o2, '%'));
            return v1.compareTo(v2);
        };
        Arrays.stream("估算净值,估算涨跌".split(",")).map(name -> WindowUtils.getColumnIndexByName(columnNames, name))
                .filter(index -> index >= 0).forEach(index -> rowSorter.setComparator(index, doubleComparator));
        table.setRowSorter(rowSorter);
        columnColors(colorful);
    }

    /**
     * 从网络更新数据
     *
     * @param code
     */
    public abstract void handle(List<String> code);

    /**
     * 设置表格条纹（斑马线）
     *
     * @param striped true设置条纹
     * @throws RuntimeException 如果table不是{@link JBTable}类型，请自行实现setStriped
     */
    public void setStriped(boolean striped) {
        if (table instanceof JBTable) {
            ((JBTable) table).setStriped(striped);
        } else {
            throw new RuntimeException("table不是JBTable类型，请自行实现setStriped");
        }
    }

    /**
     * 按照编码顺序初始化，for 每次刷新都乱序，没办法控制显示顺序
     *
     * @param code
     */
    public void setupTable(List<String> code) {
        for (String s : code) {
            updateData(new FundBean(s));
        }
    }

    /**
     * 停止从网络更新数据
     */
    public abstract void stopHandle();

    private void columnColors(boolean colorful) {
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                double temp = NumberUtils.toDouble(StringUtils.remove(Objects.toString(value), "%"));
                if (temp > 0) {
                    if (colorful) {
                        setForeground(JBColor.RED);
                    } else {
                        setForeground(JBColor.DARK_GRAY);
                    }
                } else if (temp < 0) {
                    if (colorful) {
                        setForeground(JBColor.GREEN);
                    } else {
                        setForeground(JBColor.GRAY);
                    }
                } else if (temp == 0) {
                    Color orgin = getForeground();
                    setForeground(orgin);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
//        table.getColumn(getColumnName(2)).setCellRenderer(cellRenderer);
        int columnIndex = WindowUtils.getColumnIndexByName(columnNames, "估算涨跌");
        int columnIndex3 = WindowUtils.getColumnIndexByName(columnNames, "收益率");
        int columnIndex4 = WindowUtils.getColumnIndexByName(columnNames, "收益");
        int columnIndex5 = WindowUtils.getColumnIndexByName(columnNames, "今日收益");

        table.getColumn(getColumnName(columnIndex)).setCellRenderer(cellRenderer);
        table.getColumn(getColumnName(columnIndex3)).setCellRenderer(cellRenderer);
        table.getColumn(getColumnName(columnIndex4)).setCellRenderer(cellRenderer);
        table.getColumn(getColumnName(columnIndex5)).setCellRenderer(cellRenderer);
    }

    protected void updateData(FundBean bean) {
        if (bean.getFundCode() == null) {
            return;
        }
        Vector<Object> convertData = convertData(bean);
        if (convertData == null) {
            return;
        }
        // 获取行
        int index = findRowIndex(codeColumnIndex, bean.getFundCode());
        if (index >= 0) {
            updateRow(index, convertData);
        } else {
            addRow(convertData);
        }
    }

    /**
     * 参考源码{@link DefaultTableModel#setValueAt}，此为直接更新行，提高点效率
     *
     * @param rowIndex
     * @param rowData
     */
    protected void updateRow(int rowIndex, Vector<Object> rowData) {
        dataVector.set(rowIndex, rowData);
        // 通知listeners刷新ui
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * 参考源码{@link DefaultTableModel#removeRow(int)}，此为直接清除全部行，提高点效率
     */
    public void clearRow() {
        int size = dataVector.size();
        if (0 < size) {
            dataVector.clear();
            // 通知listeners刷新ui
            fireTableRowsDeleted(0, size - 1);
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
        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object valueAt = getValueAt(rowIndex, columnIndex);
            if (StringUtils.equalsIgnoreCase(value, valueAt.toString())) {
                return rowIndex;
            }
        }
        return -1;
    }

    private Vector<Object> convertData(FundBean fundBean) {
        if (fundBean.getFundCode() == null) {
            return null;
        }
        // 与columnNames中的元素保持一致
        Vector<Object> v = new Vector<Object>(columnNames.length);
        for (int i = 0; i < columnNames.length; i++) {
            v.addElement(fundBean.getValueByColumn(columnNames[i], colorful));
        }
        return v;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
