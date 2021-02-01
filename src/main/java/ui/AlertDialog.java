package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

public class AlertDialog extends DialogWrapper {
    /**
     * 窗体文本
     */
    private String text;

    private CustomOKAction okAction;
    private CustomRestartAction exitAction;

    /**
     * 图片路径
     */
    private String imagePath;

    public AlertDialog(@Nullable Project project, String title, String text) {
        super(project, false, true);
        setTitle(title);
        this.text = "<html>" + text + "</html>";
        this.init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return createFrame();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        exitAction = new CustomRestartAction("梭哈");
        okAction = new CustomOKAction("看看");
        // 设置默认的焦点按钮
        exitAction.putValue(DialogWrapper.DEFAULT_ACTION, true);
        return new Action[]{okAction, exitAction};
    }

    /**
     * 再战
     *
     * @return {@link JComponent}
     */

    protected class CustomOKAction extends DialogWrapperAction {

        protected CustomOKAction(@NotNull String name) {
            super(name);
        }

        @Override
        protected void doAction(ActionEvent actionEvent) {
            // 点击ok的时候进行数据校验
            /*ScheduledService.getInstance().removeTask();
            ScheduledService.getInstance().addTask(Constant.Infor.FIGHT_TIME, false);*/

            close(CANCEL_EXIT_CODE);
        }
    }

    /**
     * 休息
     *
     * @return {@link JComponent}
     */
    protected class CustomRestartAction extends DialogWrapperAction {

        protected CustomRestartAction(@NotNull String name) {
            super(name);
        }

        @Override
        protected void doAction(ActionEvent actionEvent) {
            // 点击ok的时候进行数据校验
            /*ScheduledService.getInstance().removeTask();
            ScheduledService.getInstance().addTask(Constant.Infor.REST_TIME, true);*/
            close(CANCEL_EXIT_CODE);
        }
    }

    @Override
    public void show() {
        if (Messages.isMacSheetEmulation()) {
            this.setInitialLocationCallback(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(this.getWindow().getParent());
                if (rootPane == null) {
                    rootPane = SwingUtilities.getRootPane(this.getWindow().getOwner());
                }

                Point p = rootPane.getLocationOnScreen();
                p.x += (rootPane.getWidth() - this.getWindow().getWidth()) / 2;
                return p;
            });
            this.getPeer().getWindow().setOpacity(0.8F);
            this.setAutoAdjustable(false);
            this.setSize(this.getPreferredSize().width, 0);
        }

        super.show();
    }

    /**
     * 创建容器
     *
     * @return {@link JComponent}
     */
    private JComponent createFrame() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel text = new JLabel(this.text);
        text.setFont(new Font("微软雅黑", Font.BOLD, 15));
        //添加进容器
        panel.add(text, BorderLayout.CENTER);
        panel.setSize(600, 300);
        SwingUtil.setLocationCenter(panel);
        return panel;
    }

}
