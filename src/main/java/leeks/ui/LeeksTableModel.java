package leeks.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import leeks.constant.Constants;
import leeks.utils.PinYinUtils;
import leeks.bean.TabConfig;
import leeks.utils.WindowUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 韭菜专用表格模型
 */
public class LeeksTableModel extends DefaultTableModel {

    private final String[] columnNames;

    /**
     * 存放【编码】的位置，更新数据时用到
     */
    public int codeColumnIndex = 0;

    private final JBTable table;

    private final JLabel refreshTimeLabel;

    public LeeksTableModel(TabConfig config, JBTable table, JLabel refreshTimeLabel) {
        this.table = table;
        this.refreshTimeLabel = refreshTimeLabel;
        PropertiesComponent instance = PropertiesComponent.getInstance();
        String tableHeader = instance.getValue(config.getTableHeaderKey());
        if (StringUtils.isBlank(tableHeader)) {
            instance.setValue(config.getTableHeaderKey(), config.getTableHeaderValue());
            tableHeader = config.getTableHeaderValue();
        }

        columnNames = tableHeader.split(",");
        WindowUtils.reg(Arrays.asList(columnNames));
        for (int i = 0; i < columnNames.length; i++) {
            if ("编码".equals(columnNames[i])) {
                codeColumnIndex = i;
            }
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));
        table.setModel(this);

        refreshColorful(instance.getBoolean(Constants.Keys.COLORFUL));
    }

    public void refreshColorful(boolean colorful) {
        // 刷新表头
        if (colorful) {
            setColumnIdentifiers(columnNames);
        } else {
            setColumnIdentifiers(PinYinUtils.toPinYin(columnNames));
        }
        refreshCellColor(colorful);
    }

    public void refreshCellColor(boolean colorful) {
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                double profit = NumberUtils.toDouble(StringUtils.remove(Objects.toString(value), "%"));
                if (profit > 0) {
                    setForeground(colorful ? JBColor.RED : JBColor.DARK_GRAY);
                } else if (profit < 0) {
                    setForeground(colorful ? JBColor.GREEN : JBColor.GRAY);
                } else {
                    setForeground(getForeground());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };

        final List<String> columnNameList = Arrays.stream(columnNames).collect(Collectors.toList());
        Arrays.asList("涨跌", "涨跌幅", "收益率", "收益", "估算涨跌").forEach(
                e -> {
                    int index;
                    if ((index = columnNameList.indexOf(e)) > -1) {
                        table.getColumn(getColumnName(index)).setCellRenderer(cellRenderer);
                    }
                }
        );
    }

    public JLabel getRefreshTimeLabel() {
        return refreshTimeLabel;
    }

    public int getCodeColumnIndex() {
        return codeColumnIndex;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }


    /**
     * 设置表格条纹（斑马线）<br>
     *
     * @param striped true设置条纹
     * @throws RuntimeException 如果table不是{@link JBTable}类型，请自行实现setStriped
     */
    public void setStriped(boolean striped) {
        table.setStriped(striped);
    }
}
