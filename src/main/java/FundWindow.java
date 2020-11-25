import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.ButtonEnableUtil;
import utils.FundRefreshHandler;
import utils.LogUtil;
import utils.TianTianFundHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FundWindow implements ToolWindowFactory {
    private JPanel mPanel;
    private JTable table1;
    private JButton refreshButton;
    private JPanel toolPanel;
    private JButton stopButton;
    private JLabel refreshTime;

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
        fundRefreshHandler = new TianTianFundHandler(table1, refreshTime);
        // 刷新
        refreshButton.setIcon(AllIcons.Actions.Refresh);
        refreshButton.addActionListener(e -> {
            apply();
            ButtonEnableUtil.disableByTime(refreshButton, 2);
        });
        // 停止
        stopButton.setIcon(AllIcons.Actions.StopRefresh);
        stopButton.addActionListener(e -> {
            fundRefreshHandler.stopHandle();
            ButtonEnableUtil.disableByTime(stopButton, 2);
        });
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
