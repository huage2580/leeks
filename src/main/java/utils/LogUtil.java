package utils;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

public class LogUtil {
    private static Project project;

    public static void setProject(Project project) {
        LogUtil.project = project;
    }

    public static void info(String text) {
        boolean closeLog = PropertiesComponent.getInstance().getBoolean("key_close_log");

        if (XConstant.IS_DEBUG) {
            closeLog = false;
        }

        if (!closeLog) {
            //打印到安装插件的idea上的event log 上
            new NotificationGroup("Gradle sync", NotificationDisplayType.NONE, true).createNotification(text, MessageType.INFO).notify(project);
            //调试的时候打印在写插件的idea控制台上
            System.out.println(text);
        }
    }
}
