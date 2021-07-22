package utils;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

import java.util.ArrayList;

public class LogUtil {
    // 解决github中的bug #122，暂时没有其它方案监听到project的变化，先预存进行逻辑校验
    private static final ArrayList<Project> PROJECT_LIST = new ArrayList<>(3);

    public static Project getProject() {
        Project project = null;
        if (!PROJECT_LIST.isEmpty()) {
            project = PROJECT_LIST.get(0);
            if (project.isDisposed()) {
                PROJECT_LIST.remove(0);
                return getProject();
            }
        }
        return project;
    }

    public static void setProject(Project project) {
        PROJECT_LIST.add(project);
    }

    public static void info(String text){
        boolean closeLog = PropertiesComponent.getInstance().getBoolean("key_close_log");
        if (!closeLog){
            new NotificationGroup("Gradle sync", NotificationDisplayType.NONE, true).createNotification(text, MessageType.INFO).notify(getProject());
        }
    }
}
