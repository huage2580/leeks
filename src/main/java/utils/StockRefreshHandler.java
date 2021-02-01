package utils;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import service.AlertService;

public abstract class StockRefreshHandler extends DefaultTableModel {
    private static String[] columnNames = new String[]{"编码", "股票名称", "当前价", "涨跌", "涨跌幅", "最高价", "最低价", "更新时间"};

    private JTable table;
    private boolean colorful = true;

    /**
     * 股票 更新数据的间隔时间（秒）
     */
    protected volatile int threadSleepTime = XConstant.STOCK_UPDATE_INTERVAL;

    public StockRefreshHandler(JTable table) {
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
        Comparator<Object> dobleComparator = (o1, o2) -> {
            Double v1 = Double.parseDouble(StringUtils.remove((String) o1, '%'));
            Double v2 = Double.parseDouble(StringUtils.remove((String) o2, '%'));
            return v1.compareTo(v2);
        };
        rowSorter.setComparator(2, dobleComparator);
        rowSorter.setComparator(3, dobleComparator);
        rowSorter.setComparator(4, dobleComparator);
        rowSorter.setComparator(5, dobleComparator);
        rowSorter.setComparator(6, dobleComparator);
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
     * 设置表格条纹（斑马线）<br>
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

    public void setupTable(List<String> code) {
        for (String s : code) {
            updateData(new StockBean(s));
        }
    }

    /**
     * 停止从网络更新数据
     */
    public abstract void stopHandle();

    private void columnColors(boolean colorful) {
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                           int column) {
                double temp = 0.0;
                try {
                    String s = value.toString().replace("%", "");
                    temp = Double.parseDouble(s);
                } catch (Exception e) {

                }
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
        table.getColumn(getColumnName(3)).setCellRenderer(cellRenderer);
        table.getColumn(getColumnName(4)).setCellRenderer(cellRenderer);
    }

    protected void updateData(StockBean bean) {
        if (bean.getCode() == null) {
            return;
        }
        Vector<Object> convertData = convertData(bean);
        if (convertData == null) {
            return;
        }
        // 获取行
        int index = findRowIndex(0, bean.getCode());
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

    private Vector<Object> convertData(StockBean fundBean) {
        if (fundBean == null) {
            return null;
        }
        String timeStr = "--";
        if (fundBean.getTime() != null) {
            timeStr = fundBean.getTime().substring(8);
        }
        String changeStr = "--";
        String changePercentStr = "--";
        if (fundBean.getChange() != null) {
            changeStr = fundBean.getChange().startsWith("-") ? fundBean.getChange() : "+" + fundBean.getChange();
        }
        if (fundBean.getChangePercent() != null) {
            changePercentStr = fundBean.getChangePercent().startsWith("-") ? fundBean.getChangePercent() :
                    "+" + fundBean.getChangePercent();
        }
        // 与columnNames中的元素保持一致
        Vector<Object> v = new Vector<Object>(columnNames.length);
        v.addElement(fundBean.getCode());
        v.addElement(colorful ? fundBean.getName() : PinYinUtils.toPinYin(fundBean.getName()));
        v.addElement(fundBean.getNow());
        v.addElement(changeStr);
        v.addElement(changePercentStr + "%");
        v.addElement(fundBean.getMax());
        v.addElement(fundBean.getMin());
        v.addElement(timeStr);
        return v;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int getThreadSleepTime() {
        return threadSleepTime;
    }

    public void setThreadSleepTime(int threadSleepTime) {
        this.threadSleepTime = threadSleepTime;
    }

    protected List<StockBean> mStockBeans = new CopyOnWriteArrayList<>();

    /**
     * 看一下 是否需要 展示提醒对话框
     */
    protected void showDialogIfNeed() {
        if (mStockBeans.size() == 0) {
            return;
        }
        if (XDateUtil.isShowTime(false)) {
                        /*ProgressManager.getInstance().executeNonCancelableSection(
                                () -> AlertService.getInstance().showAlertDialog(null, period));*/
            StringBuilder stringBuilder = new StringBuilder();
            for (StockBean stockBean : mStockBeans) {
                stringBuilder.append(stockBean.getName()).append(" ,  ").append(stockBean.getChangePercent()).append("%").append("<br>");
            }
            stringBuilder.append("该加仓了");
            //展示内容:
            ProgressManager.getInstance().executeNonCancelableSection(
                    () -> {
                        XDateUtil.updateStockShowDate();
                        AlertService.getInstance().showAlertDialog(null, stringBuilder.toString());
                    });
            System.out.println("该加仓了");
        } else {
            System.out.println("没在加仓时间内");
        }
    }

    /**
     * 跌了0.5以上就加进来
     */
    protected void addDropFundIfNeed(StockBean bean) {
        //如果是跌了  则是 -0.5 ?  这样子
        String gszzl = bean.getChangePercent();
        System.out.println(gszzl + "----");
        if (!gszzl.contains("-")) {
            //涨了
            return;
        }
        try {
            if (Float.parseFloat(gszzl) < XConstant.STOCK_ADDING_THRESHOLD) {
                mStockBeans.add(bean);
            }
        } catch (Exception ignored) {
            //格式化失败  不管
        }
    }

}
