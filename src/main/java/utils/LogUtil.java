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

    public static void info(String text){
        boolean closeLog = PropertiesComponent.getInstance().getBoolean("key_close_log");
        if (!closeLog){
            new NotificationGroup("Gradle sync", NotificationDisplayType.NONE, true).createNotification(text, MessageType.INFO).notify(project);
        }
    }
}
