package service.impl;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

import service.AlertService;
import ui.AlertDialog;
import utils.XConstant;


public class AlertServiceImpl implements AlertService {

    private final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("Groovy DSL errors", NotificationDisplayType.BALLOON, true);

    private final Notification notification = NOTIFICATION_GROUP.createNotification("温馨提醒", "",
            NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER);

    @Override
    public void showAlertDialog(Project project, String content) {
        //应用内 右下角 小通知
        notification.setContent(content);
        notification.notify(project);

        AlertDialog alertDialog = new AlertDialog(project, XConstant.Infor.TITLE, content);
        alertDialog.show();
    }

}
