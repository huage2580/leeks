package leeks.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import leeks.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import leeks.utils.HttpClientPool;
import leeks.utils.LogUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainWindow implements ToolWindowFactory {

    public static final List<AbstractTab> TABS = Arrays.asList(new FundTab(), new StockTab(), new CoinTab());

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        //先加载代理
        loadProxySetting();

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();
        List<Content> contentList = TABS.stream().map(t -> contentFactory.createContent(t.getPanel(), t.getName(), false))
                .peek(contentManager::addContent)
                .collect(Collectors.toList());
        if (StringUtils.isEmpty(PropertiesComponent.getInstance().getValue(Constants.Keys.FUNDS))) {
            // 没有配置基金数据，选择展示股票
            contentManager.setSelectedContent(contentList.get(1));
        }
        LogUtil.setProject(project);
    }

    private void loadProxySetting() {
        String proxyStr = PropertiesComponent.getInstance().getValue(Constants.Keys.PROXY);
        HttpClientPool.getHttpClient().buildHttpClient(proxyStr);
    }

    @Override
    public void init(@NotNull ToolWindow window) {
        window.setIcon(AllIcons.General.ArrowUp);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }

}
