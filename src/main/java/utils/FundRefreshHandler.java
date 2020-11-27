package utils;

import com.intellij.ui.JBColor;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public abstract class FundRefreshHandler extends DefaultTableModel{
    private static String[] columnNames = {"编码", "基金名称", "当日净值", "估算净值", "估算涨跌", "更新时间"};

    private JTable table;
    private boolean colorful = true;


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
        if (this.colorful == colorful){
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
        Comparator<Object> dobleComparator = (o1, o2) -> {
            Double v1 = Double.parseDouble(StringUtils.remove((String) o1, '%'));
            Double v2 = Double.parseDouble(StringUtils.remove((String) o2, '%'));
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
        table.getColumn(getColumnName(2)).setCellRenderer(cellRenderer);
        table.getColumn(getColumnName(3)).setCellRenderer(cellRenderer);
    }

    protected void updateData(FundBean bean) {
        Vector<Object> convertData = convertData(bean);
        if (convertData==null){
            return;
        }
        // 获取行
        int index = findRowIndex(0, bean.getFundCode());
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
        String timeStr = fundBean.getGztime();
        if(timeStr == null){
            return null;
        }
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        if (timeStr.startsWith(today)) {
            timeStr = timeStr.substring(timeStr.indexOf(" "));
        }
        String gszzlStr = "--";
        String gszzl = fundBean.getGszzl();
        if (gszzl !=null){
            gszzlStr= gszzl.startsWith("-")? gszzl :"+"+ gszzl;
        }
        // 与columnNames中的元素保持一致
        Vector<Object> v = new Vector<Object>(columnNames.length);
        v.addElement(fundBean.getFundCode());
        v.addElement(colorful ? fundBean.getFundName() : PinYinUtils.toPinYin(fundBean.getFundName()));
        v.addElement(fundBean.getDwjz());
        v.addElement(fundBean.getGsz());
        v.addElement( gszzlStr+"%");
        v.addElement(timeStr);
        return v;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
