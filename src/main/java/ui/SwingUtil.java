package ui;

import java.awt.*;

/**
 * Created by xfhy on 2021/1/27 16:50
 * Description :
 */
public class SwingUtil {
    /**
     * 展示在屏幕正中间
     */
    public static void setLocationCenter(Component component) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension compSize = component.getSize();
        if (compSize.height > screenSize.height) {
            compSize.height = screenSize.height;
        }
        if (compSize.width > screenSize.width) {
            compSize.width = screenSize.width;
        }
        component.setLocation((screenSize.width - compSize.width) / 2,
                (screenSize.height - compSize.height) / 2);
    }
}
