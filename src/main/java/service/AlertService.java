package service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;


public interface AlertService {

    /**
     * @param content 对话框内容
     */
    void showAlertDialog(Project project, String content);

    /**
     * getInstance
     *
     * @return {@link service.impl.AlertServiceImpl}
     */
    static AlertService getInstance() {
        if (ApplicationManager.getApplication() != null) {
            return ServiceManager.getService(AlertService.class);
        } else {
            try {
                return (AlertService) AlertService.class.getClassLoader().loadClass("service.impl.AlertServiceImpl").newInstance();
            } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
