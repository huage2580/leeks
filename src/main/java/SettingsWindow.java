import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsWindow  implements Configurable {
    private JPanel panel1;
    private JLabel label;
    private JTextField textField1;

    private String orgin;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Leeks";
    }

    @Override
    public @Nullable JComponent createComponent() {
        String value = PropertiesComponent.getInstance().getValue("key_funds");
        orgin = value;
        textField1.setText(value);
        return panel1;
    }

    @Override
    public boolean isModified() {
        return !textField1.getText().equals(orgin);
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent.getInstance().setValue("key_funds",textField1.getText());
    }
}
