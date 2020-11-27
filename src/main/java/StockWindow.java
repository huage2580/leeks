import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import utils.StockRefreshHandler;
import utils.TencentStockHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StockWindow {
    private JPanel mPanel;

    static StockRefreshHandler handler;

    public JPanel getPanel1() {
        return mPanel;
    }

    public StockWindow() {
        JLabel refreshTimeLabel = new JLabel();
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        JTable table1 = new JBTable();
        handler = new TencentStockHandler(table1, refreshTimeLabel);
        AnActionButton refreshAction = new AnActionButton("停止刷新当前表格数据", AllIcons.Actions.StopRefresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                handler.stopHandle();
                this.setEnabled(false);
            }
        };
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table1)
                .addExtraAction(new AnActionButton("持续刷新当前表格数据",
                        AllIcons.Actions.Refresh) {
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
        // 非主要tab，需要创建，创建时立即应用数据
        apply();
    }

    public static void apply() {
        if (handler != null) {
            boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
            handler.refreshColorful(colorful);
            List<String> key_stocks = FundWindow.getConfigList("key_stocks", "[,，]");
            handler.handle(key_stocks);
        }
    }
}
