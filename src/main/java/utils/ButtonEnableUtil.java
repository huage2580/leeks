package utils;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class ButtonEnableUtil {
    /**
     * @param button  按钮
     * @param timeout 禁用时间，单位是秒
     */
    public static void disableByTime(JButton button, long timeout) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 防止频繁点击，等待3秒
                button.setEnabled(false);
                TimeUnit.SECONDS.sleep(timeout);
            } catch (InterruptedException ex) {
            } finally {
                button.setEnabled(true);
            }
        });
    }
}
