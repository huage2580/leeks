import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsWindow  implements Configurable {
    private JPanel panel1;
    private JTextArea textAreaFund;
    private JTextArea textAreaStock;
    private JCheckBox checkbox;
    /**
     * 使用tab界面，方便不同的设置分开进行控制
     */
    private JTabbedPane tabbedPane1;
    private JCheckBox checkBoxTableStriped;
    private JSpinner spinnerFund;
    private JSpinner spinnerStock;
    private JCheckBox checkboxSina;
    private JCheckBox checkboxLog;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Leeks";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        String value = instance.getValue("key_funds");
        String value_stock = instance.getValue("key_stocks");
        boolean value_color = instance.getBoolean("key_colorful");
        textAreaFund.setText(value);
        textAreaStock.setText(value_stock);
        checkbox.setSelected(!value_color);
        checkBoxTableStriped.setSelected(instance.getBoolean("key_table_striped"));
        checkboxSina.setSelected(instance.getBoolean("key_stocks_sina"));
        checkboxLog.setSelected(instance.getBoolean("key_close_log"));
        spinnerFund.setModel(new SpinnerNumberModel(instance.getInt("key_funds_thread_time", 60), 1, Integer.MAX_VALUE, 1));
        spinnerStock.setModel(new SpinnerNumberModel(instance.getInt("key_stocks_thread_time", 10), 1, Integer.MAX_VALUE, 1));
        return panel1;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        instance.setValue("key_funds", textAreaFund.getText());
        instance.setValue("key_stocks", textAreaStock.getText());
        instance.setValue("key_colorful",!checkbox.isSelected());
        instance.setValue("key_funds_thread_time", spinnerFund.getValue().toString());
        instance.setValue("key_stocks_thread_time", spinnerStock.getValue().toString());
        instance.setValue("key_table_striped", checkBoxTableStriped.isSelected());
        instance.setValue("key_stocks_sina",checkboxSina.isSelected());
        instance.setValue("key_close_log",checkboxLog.isSelected());
        StockWindow.apply();
        FundWindow.apply();
    }
}
