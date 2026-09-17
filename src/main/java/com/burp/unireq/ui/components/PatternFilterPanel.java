package com.burp.unireq.ui.components;

import com.burp.unireq.utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared host and path pattern controls used by both advanced-filter UIs.
 */
public class PatternFilterPanel extends JPanel {

    private static final int FIELD_COLUMNS = 32;

    private final JTextField hostField;
    private final JTextField pathField;
    private final JCheckBox regexCheckbox;
    private final JCheckBox caseSensitiveCheckbox;
    private final JCheckBox invertHostCheckbox;
    private final List<Runnable> changeListeners = new ArrayList<>();

    public PatternFilterPanel() {
        hostField = SwingUtils.createModernTextField("Example: api.example.com", FIELD_COLUMNS);
        pathField = SwingUtils.createModernTextField("Example: /api/users", FIELD_COLUMNS);
        regexCheckbox = new JCheckBox("Use regular expressions");
        caseSensitiveCheckbox = new JCheckBox("Case-sensitive matching");
        invertHostCheckbox = new JCheckBox("Exclude matching hosts");

        buildLayout();
        wireChangeListeners();
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        add(new JLabel("Host pattern:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(hostField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        add(new JLabel("Path pattern:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(pathField, gbc);

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        regexCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        caseSensitiveCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        invertHostCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        options.add(regexCheckbox);
        options.add(caseSensitiveCheckbox);
        options.add(invertHostCheckbox);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 0, 0, 10);
        add(options, gbc);
    }

    private void wireChangeListeners() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifyListeners();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifyListeners();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyListeners();
            }
        };
        hostField.getDocument().addDocumentListener(documentListener);
        pathField.getDocument().addDocumentListener(documentListener);

        regexCheckbox.addActionListener(e -> notifyListeners());
        caseSensitiveCheckbox.addActionListener(e -> notifyListeners());
        invertHostCheckbox.addActionListener(e -> notifyListeners());
    }

    private void notifyListeners() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public String getHostPattern() {
        return hostField.getText().trim();
    }

    public void setHostPattern(String pattern) {
        hostField.setText(pattern != null ? pattern : "");
    }

    public String getPathPattern() {
        return pathField.getText().trim();
    }

    public void setPathPattern(String pattern) {
        pathField.setText(pattern != null ? pattern : "");
    }

    public boolean isRegexMode() {
        return regexCheckbox.isSelected();
    }

    public void setRegexMode(boolean regexMode) {
        regexCheckbox.setSelected(regexMode);
    }

    public boolean isCaseSensitive() {
        return caseSensitiveCheckbox.isSelected();
    }

    public void setCaseSensitive(boolean caseSensitive) {
        caseSensitiveCheckbox.setSelected(caseSensitive);
    }

    public boolean isInvertHostFilter() {
        return invertHostCheckbox.isSelected();
    }

    public void setInvertHostFilter(boolean invertHostFilter) {
        invertHostCheckbox.setSelected(invertHostFilter);
    }

    public void clear() {
        hostField.setText("");
        pathField.setText("");
        regexCheckbox.setSelected(false);
        caseSensitiveCheckbox.setSelected(false);
        invertHostCheckbox.setSelected(false);
    }
}
