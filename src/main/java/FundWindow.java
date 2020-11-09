import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.FundRefreshHandler;
import utils.LogUtil;
import utils.TianTianFundHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FundWindow implements ToolWindowFactory {
    private JPanel mPanel;
    private JTable table1;
    private JButton refreshButton;

    static FundRefreshHandler fundRefreshHandler;

    private StockWindow stockWindow = new StockWindow();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mPanel, "Fund", false);

        Content content_stock = contentFactory.createContent(stockWindow.getPanel1(), "Stock", false);
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
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
                fundRefreshHandler.refreshColorful(colorful);
                fundRefreshHandler.handle(loadFunds());
                stockWindow.onInit();
                // 防止频繁点击，等待3秒
                refreshButton.setEnabled(false);
                new Thread(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ex) {
                    }finally {
                        refreshButton.setEnabled(true);
                    }
                }).start();
            }
        });

    }

    @Override
    public void init(ToolWindow window) {
        boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
        fundRefreshHandler = new TianTianFundHandler(table1, refreshButton);
        fundRefreshHandler.refreshColorful(colorful);
        fundRefreshHandler.handle(loadFunds());
        stockWindow.onInit();
    }

    private static List<String> loadFunds(){
        return getConfigList("key_funds", "[,，]");
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
            fundRefreshHandler.handle(loadFunds());
        }
    }
}
