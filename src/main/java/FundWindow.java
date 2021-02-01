import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.*;
import java.util.List;

import utils.LogUtil;
import utils.TianTianFundHandler;
import utils.XConstant;

public class FundWindow implements ToolWindowFactory {
    private JPanel mPanel;

    static TianTianFundHandler fundRefreshHandler;

    private StockWindow stockWindow = new StockWindow();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mPanel, "Fund", false);

        Content content_stock = contentFactory.createContent(stockWindow.getmPanel(), "Stock", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.getContentManager().addContent(content_stock);
        LogUtil.setProject(project);
//        ((ToolWindowManagerEx) ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerListener() {
//            @Override
//            public void stateChanged() {
//                if (toolWindow.isVisible()){
//                    fundRefreshHandler.handle(loadFunds());
//                }
//            }
//        });
    }

    @Override
    public void init(ToolWindow window) {
        //一进入Android  Studio 就会走这里

        JLabel refreshTimeLabel = new JLabel();
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        JBTable table = new JBTable();
        fundRefreshHandler = new TianTianFundHandler(table, refreshTimeLabel);
        AnActionButton refreshAction = new AnActionButton("停止刷新当前表格数据", AllIcons.Actions.StopRefresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                fundRefreshHandler.stopHandle();
                this.setEnabled(false);
            }
        };
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table)
                .addExtraAction(new AnActionButton("持续刷新当前表格数据", AllIcons.Actions.Refresh) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        refresh();
                        refreshAction.setEnabled(true);
                    }
                })
                .addExtraAction(refreshAction)
                .setToolbarPosition(ActionToolbarPosition.TOP);
        JPanel toolPanel = toolbarDecorator.createPanel();
        toolbarDecorator.getActionsPanel().add(refreshTimeLabel, BorderLayout.EAST);
        toolPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        mPanel.add(toolPanel, BorderLayout.CENTER);
        apply();
    }

    private static List<String> loadFunds() {
        if (XConstant.IS_DEBUG) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("162605");
            return strings;
        } else {
            return getConfigList("key_funds", "[,，]");
        }
    }

    public static List<String> getConfigList(String key, String split) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        String[] codes = value.split(split);
        for (String code : codes) {
            if (!code.isEmpty()) {
                set.add(code.trim());
            }
        }
        return new ArrayList<>(set);
    }


    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }

    public static void apply() {
        if (fundRefreshHandler != null) {
            PropertiesComponent instance = PropertiesComponent.getInstance();
            fundRefreshHandler.setStriped(instance.getBoolean("key_table_striped"));
            fundRefreshHandler.setThreadSleepTime(instance.getInt("key_funds_thread_time", fundRefreshHandler.getThreadSleepTime()));
            fundRefreshHandler.refreshColorful(instance.getBoolean("key_colorful"));
            fundRefreshHandler.clearRow();
            fundRefreshHandler.setupTable(loadFunds());
            fundRefreshHandler.handle(loadFunds());
        }
    }

    public static void refresh() {
        if (fundRefreshHandler != null) {
            boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
            fundRefreshHandler.refreshColorful(colorful);
            fundRefreshHandler.handle(loadFunds());
        }
    }
}
