package leeks.handler;

import com.intellij.ide.util.PropertiesComponent;
import leeks.bean.AbstractRowDataBean;
import leeks.constant.Constants;
import leeks.ui.LeeksTableModel;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 抽象的数据查询handler类,提供了一些针对{@link leeks.ui.LeeksTableModel}的公共操作.
 */
public abstract class AbstractHandler {

    private final ReentrantLock lock = new ReentrantLock();

    protected LeeksTableModel tableModel;

    /**
     * 从网络更新数据, 增加了锁防止多cron表达式重复同步执行.
     *
     * @param code 股票代码/基金代码/虚拟币代码
     */
    public void handle(List<String> code) {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                handleInternal(code);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    protected abstract void handleInternal(List<String> code);

    /**
     * 按照编码顺序初始化，for 每次刷新都乱序，没办法控制显示顺序
     *
     * @param code
     */
    public abstract void setupTable(List<String> code);

    /**
     * 参考源码{@link DefaultTableModel#setValueAt}，此为直接更新行，提高点效率
     *
     * @param rowIndex
     * @param rowData
     */
    protected void updateRow(int rowIndex, Vector<Object> rowData) {
        tableModel.getDataVector().set(rowIndex, rowData);
        // 通知listeners刷新ui
        tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * 参考源码{@link DefaultTableModel#removeRow(int)}，此为直接清除全部行，提高点效率
     */
    public void clearRow() {
        int size = tableModel.getDataVector().size();
        if (0 < size) {
            tableModel.getDataVector().clear();
            // 通知listeners刷新ui
            tableModel.fireTableRowsDeleted(0, size - 1);
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
        int rowCount = tableModel.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object valueAt = tableModel.getValueAt(rowIndex, columnIndex);
            if (StringUtils.equalsIgnoreCase(value, valueAt.toString())) {
                return rowIndex;
            }
        }
        return -1;
    }

    protected void updateUI() {
        SwingUtilities.invokeLater(() -> {
            tableModel.getRefreshTimeLabel().setText(LocalDateTime.now().format(Constants.TIME_FORMATTER));
            tableModel.getRefreshTimeLabel().setToolTipText("最后刷新时间");
        });

        PropertiesComponent instance = PropertiesComponent.getInstance();
        tableModel.refreshCellColor(instance.getBoolean(Constants.Keys.COLORFUL));
    }

    protected void updateData(AbstractRowDataBean bean) {
        if (bean.getCode() == null) {
            return;
        }
        Vector<Object> convertData = convertData(bean);
        if (convertData == null) {
            return;
        }
        // 获取行
        int index = findRowIndex(tableModel.getCodeColumnIndex(), bean.getCode());
        if (index >= 0) {
            updateRow(index, convertData);
        } else {
            tableModel.addRow(convertData);
        }
    }

    private Vector<Object> convertData(AbstractRowDataBean bean) {
        if (bean == null) {
            return null;
        }

        PropertiesComponent instance = PropertiesComponent.getInstance();
        boolean colorful = instance.getBoolean(Constants.Keys.COLORFUL);

        // 与columnNames中的元素保持一致
        Vector<Object> v = new Vector<>(tableModel.getColumnNames().length);
        for (int i = 0; i < tableModel.getColumnNames().length; i++) {
            v.addElement(bean.getValueByColumn(tableModel.getColumnNames()[i], colorful));
        }
        return v;
    }
}
