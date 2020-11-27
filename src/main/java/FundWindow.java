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
import com.intellij.ui.content.ContentManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.FundRefreshHandler;
import utils.LogUtil;
import utils.TianTianFundHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FundWindow implements ToolWindowFactory {
    private JPanel mPanel;

    static FundRefreshHandler fundRefreshHandler;

    private StockWindow stockWindow = new StockWindow();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mPanel, "Fund", false);

        Content content_stock = contentFactory.createContent(stockWindow.getPanel1(), "Stock", false);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);
        contentManager.addContent(content_stock);
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
        JLabel refreshTimeLabel = new JLabel();
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        JTable table = new JTable();
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
                        apply();
                        refreshAction.setEnabled(true);
                    }
                })
                .addExtraAction(refreshAction)
                .setToolbarPosition(ActionToolbarPosition.TOP);
        JPanel toolPanel = toolbarDecorator.createPanel();
        toolbarDecorator.getActionsPanel().add(refreshTimeLabel, BorderLayout.EAST);
        toolPanel.setBorder(new EmptyBorder(0,0,0,0));
        mPanel.add(toolPanel, BorderLayout.CENTER);
        apply();
    }

    public static List<String> getConfigList(String key, String split) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new HashSet<>();
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
            boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
            fundRefreshHandler.refreshColorful(colorful);
            List<String> key_funds = getConfigList("key_funds", "[,，]");
            fundRefreshHandler.handle(key_funds);
        }
    }
}
