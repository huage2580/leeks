package leeks.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import leeks.bean.TabConfig;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.quartz.HandlerJob;
import leeks.quartz.QuartzManager;
import leeks.utils.WindowUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.List;

/**
 * 抽象内容页, 3种投资类型内容页高度相似,作抽象处理.
 */
public abstract class AbstractTab {

    protected JPanel panel;
    protected JBTable table;
    protected JLabel refreshTimeLabel;

    protected LeeksTableModel tableModel;

    protected JPanel toolPanel;
    protected AnActionButton refreshAction;
    protected AnActionButton pauseAction;
    protected AnActionButton resumeAction;

    /**
     * @return 选项卡的名字
     */
    public abstract String getName();

    public JPanel getPanel() {
        return this.panel;
    }

    public AbstractTab() {
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setLayout(new BorderLayout());

        refreshTimeLabel = new JLabel();
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        table = new JBTable();
        tableModel = new LeeksTableModel(getConfig(), table, refreshTimeLabel);
        //记录列名的变化
        table.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                StringBuilder tableHeadChange = new StringBuilder();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    tableHeadChange.append(table.getColumnName(i)).append(",");
                }
                PropertiesComponent instance = PropertiesComponent.getInstance();
                //将列名的修改放入环境中 key:coin_table_header_key
                instance.setValue(getConfig().getTableHeaderKey(), tableHeadChange
                        .substring(0, tableHeadChange.length() > 0 ? tableHeadChange.length() - 1 : 0));

                //LogUtil.info(instance.getValue(WindowUtils.COIN_TABLE_HEADER_KEY));
            }
        });

        refreshAction = new AnActionButton("立刻刷新当前表格数据", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                this.setEnabled(false);
                refresh();
                this.setEnabled(true);
                panel.updateUI();
            }
        };
        pauseAction = new AnActionButton("停止刷新当前表格数据", AllIcons.Actions.Pause) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                stop();
                this.setVisible(false);
                resumeAction.setVisible(true);
                panel.updateUI();
            }
        };
        resumeAction = new AnActionButton("恢复刷新当前表格数据", AllIcons.Actions.Resume) {
            {
                this.setVisible(false);
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                start();
                this.setVisible(false);
                pauseAction.setVisible(true);
                panel.updateUI();
            }
        };
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table)
                .addExtraAction(refreshAction)
                .addExtraAction(pauseAction)
                .addExtraAction(resumeAction)
                .setToolbarPosition(ActionToolbarPosition.TOP);
        toolPanel = toolbarDecorator.createPanel();
        toolbarDecorator.getActionsPanel().add(refreshTimeLabel, BorderLayout.EAST);
        toolPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(toolPanel, BorderLayout.NORTH);
    }

    /**
     * 获取选项卡配置
     *
     * @return 选项卡配置
     */
    protected abstract TabConfig getConfig();

    /**
     * 获取当前选项卡的handler
     *
     * @return 数据处理器
     */
    protected abstract AbstractHandler getHandler();

    protected List<String> getCodes() {
        return SettingsWindow.getConfigList(getConfig().getCodesKey());
    }

    public void apply() {
        if (getHandler() != null) {
            PropertiesComponent instance = PropertiesComponent.getInstance();
            tableModel.setStriped(instance.getBoolean(Constants.Keys.TABLE_STRIPED));
            getHandler().clearRow();
            getHandler().setupTable(getCodes());
            refresh();
            start();
        }
    }

    public void start() {
        if (getHandler() != null) {
            PropertiesComponent instance = PropertiesComponent.getInstance();

            QuartzManager quartzManager = QuartzManager.getInstance(getName());
            HashMap<String, Object> dataMap = new HashMap<>(2);
            dataMap.put(HandlerJob.KEY_HANDLER, getHandler());
            dataMap.put(HandlerJob.KEY_CODES, getCodes());
            String cronExpression = instance.getValue(getConfig().getCronExpressionKey());
            if (StringUtils.isEmpty(cronExpression)) {
                cronExpression = "0 * * * * ?";
            }
            quartzManager.runJob(HandlerJob.class, cronExpression, dataMap);
        }
    }

    public void stop() {
        QuartzManager.getInstance(getName()).stopJob();
    }

    public void refresh() {
        if (getHandler() != null) {
            List<String> codes = getCodes();
            if (CollectionUtils.isEmpty(codes)) {
                stop(); //如果没有数据则不需要启动时钟任务浪费资源
            } else {
                getHandler().handle(codes);
            }
        }
    }
}
