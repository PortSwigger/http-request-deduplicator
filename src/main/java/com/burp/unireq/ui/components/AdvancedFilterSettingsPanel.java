package com.burp.unireq.ui.components;

import burp.api.montoya.ui.settings.SettingsPanel;
import com.burp.unireq.model.FilterCriteria;
import com.burp.unireq.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Advanced filter settings panel, shown via the "Advanced Filters" button.
 *
 * @author Harshit Shah
 */
public class AdvancedFilterSettingsPanel extends JPanel implements SettingsPanel {

    private static final int FILTER_FIELD_COLUMNS = 20;

    private static final String[] HTTP_METHODS = {
        "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"
    };
    private static final String[] STATUS_RANGES = {"2xx", "3xx", "4xx", "5xx"};
    private static final Integer[] STATUS_PREFIXES = {2, 3, 4, 5};
    private static final String[] MIME_TYPES = {
        "text/html", "application/json", "application/xml", "text/css",
        "application/javascript", "image/png", "image/jpeg", "image/gif"
    };

    private JCheckBox[] methodCheckboxes;
    private JCheckBox[] statusCheckboxes;
    private JCheckBox[] mimeCheckboxes;
    private PatternFilterPanel patternFilterPanel;
    private JTextField includeExtensionsField;
    private JTextField excludeExtensionsField;
    private JCheckBox requireResponseCheckbox;
    private JCheckBox onlyInScopeCheckbox;

    private final List<Runnable> changeListeners = new ArrayList<>();

    public AdvancedFilterSettingsPanel() {
        initializeComponents();
        buildLayout();
        wireChangeListeners();
    }

    // ==================== Public API ====================

    public FilterCriteria getCurrentCriteria() {
        FilterCriteria criteria = new FilterCriteria();

        Set<String> methods = new HashSet<>();
        for (int i = 0; i < methodCheckboxes.length; i++) {
            if (methodCheckboxes[i].isSelected()) methods.add(HTTP_METHODS[i]);
        }
        criteria.setAllowedMethods(methods);

        Set<Integer> statusPrefixes = new HashSet<>();
        for (int i = 0; i < statusCheckboxes.length; i++) {
            if (statusCheckboxes[i].isSelected()) statusPrefixes.add(STATUS_PREFIXES[i]);
        }
        criteria.setAllowedStatusPrefixes(statusPrefixes);

        Set<String> mimeTypes = new HashSet<>();
        for (int i = 0; i < mimeCheckboxes.length; i++) {
            if (mimeCheckboxes[i].isSelected()) mimeTypes.add(MIME_TYPES[i]);
        }
        criteria.setAllowedMimeTypes(mimeTypes);

        criteria.setHostPattern(patternFilterPanel.getHostPattern());
        criteria.setPathPattern(patternFilterPanel.getPathPattern());
        criteria.setRegexMode(patternFilterPanel.isRegexMode());
        criteria.setCaseSensitive(patternFilterPanel.isCaseSensitive());
        criteria.setInvertHostFilter(patternFilterPanel.isInvertHostFilter());

        criteria.setIncludedExtensions(parseExtensions(includeExtensionsField.getText()));
        criteria.setExcludedExtensions(parseExtensions(excludeExtensionsField.getText()));

        criteria.setRequireResponse(requireResponseCheckbox.isSelected());
        criteria.setOnlyInScope(onlyInScopeCheckbox.isSelected());

        return criteria;
    }

    public void loadCriteria(FilterCriteria criteria) {
        if (criteria == null) {
            clearAll();
            return;
        }
        Set<String> methods = criteria.getAllowedMethods();
        for (int i = 0; i < methodCheckboxes.length; i++) {
            methodCheckboxes[i].setSelected(methods.contains(HTTP_METHODS[i]));
        }
        Set<Integer> prefixes = criteria.getAllowedStatusPrefixes();
        for (int i = 0; i < statusCheckboxes.length; i++) {
            statusCheckboxes[i].setSelected(prefixes.contains(STATUS_PREFIXES[i]));
        }
        Set<String> mimeTypes = criteria.getAllowedMimeTypes();
        for (int i = 0; i < mimeCheckboxes.length; i++) {
            mimeCheckboxes[i].setSelected(mimeTypes.contains(MIME_TYPES[i]));
        }
        patternFilterPanel.setHostPattern(criteria.getHostPattern());
        patternFilterPanel.setPathPattern(criteria.getPathPattern());
        patternFilterPanel.setRegexMode(criteria.isRegexMode());
        patternFilterPanel.setCaseSensitive(criteria.isCaseSensitive());
        patternFilterPanel.setInvertHostFilter(criteria.isInvertHostFilter());
        includeExtensionsField.setText(formatExtensions(criteria.getIncludedExtensions()));
        excludeExtensionsField.setText(formatExtensions(criteria.getExcludedExtensions()));
        requireResponseCheckbox.setSelected(criteria.isRequireResponse());
        onlyInScopeCheckbox.setSelected(criteria.isOnlyInScope());
    }

    public void clearAll() {
        for (JCheckBox cb : methodCheckboxes) cb.setSelected(false);
        for (JCheckBox cb : statusCheckboxes) cb.setSelected(false);
        for (JCheckBox cb : mimeCheckboxes) cb.setSelected(false);
        patternFilterPanel.clear();
        includeExtensionsField.setText("");
        excludeExtensionsField.setText("");
        requireResponseCheckbox.setSelected(false);
        onlyInScopeCheckbox.setSelected(false);
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) changeListeners.add(listener);
    }

    // ==================== Private ====================

    private void initializeComponents() {
        methodCheckboxes = new JCheckBox[HTTP_METHODS.length];
        for (int i = 0; i < HTTP_METHODS.length; i++) methodCheckboxes[i] = new JCheckBox(HTTP_METHODS[i]);

        statusCheckboxes = new JCheckBox[STATUS_RANGES.length];
        for (int i = 0; i < STATUS_RANGES.length; i++) statusCheckboxes[i] = new JCheckBox(STATUS_RANGES[i]);

        mimeCheckboxes = new JCheckBox[MIME_TYPES.length];
        for (int i = 0; i < MIME_TYPES.length; i++) mimeCheckboxes[i] = new JCheckBox(MIME_TYPES[i]);

        patternFilterPanel = new PatternFilterPanel();

        includeExtensionsField = SwingUtils.createModernTextField("e.g., php,jsp,asp", FILTER_FIELD_COLUMNS);
        excludeExtensionsField = SwingUtils.createModernTextField("e.g., css,js,png", FILTER_FIELD_COLUMNS);

        requireResponseCheckbox = new JCheckBox("Hide items without responses");
        onlyInScopeCheckbox = new JCheckBox("Show only in-scope items");
    }

    private void buildLayout() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.add(new JLabel("Configure request filters. Changes are applied immediately."), BorderLayout.WEST);

        JButton clearButton = new JButton("Reset all filters");
        clearButton.setToolTipText("Reset all advanced request filters");
        clearButton.addActionListener(e -> {
            clearAll();
            notifyListeners();
        });
        header.add(clearButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addTab("Methods", buildMethodPanel());
        tabs.addTab("Status", buildStatusPanel());
        tabs.addTab("MIME Types", buildMimePanel());
        tabs.addTab("Patterns", buildPatternPanel());
        tabs.addTab("Extensions", buildExtensionPanel());
        tabs.addTab("Options", buildOptionsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private void wireChangeListeners() {
        javax.swing.event.ChangeListener cl = e -> notifyListeners();
        for (JCheckBox cb : methodCheckboxes) cb.addChangeListener(cl);
        for (JCheckBox cb : statusCheckboxes) cb.addChangeListener(cl);
        for (JCheckBox cb : mimeCheckboxes) cb.addChangeListener(cl);
        requireResponseCheckbox.addChangeListener(cl);
        onlyInScopeCheckbox.addChangeListener(cl);

        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyListeners(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyListeners(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyListeners(); }
        };
        includeExtensionsField.getDocument().addDocumentListener(dl);
        excludeExtensionsField.getDocument().addDocumentListener(dl);
        patternFilterPanel.addChangeListener(this::notifyListeners);
    }

    private void notifyListeners() {
        for (Runnable r : changeListeners) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    private JPanel buildMethodPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(2, 5, 2, 5);
        JLabel title = new JLabel("Show only these HTTP methods:");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        p.add(title, gbc);
        gbc.gridwidth = 1;
        for (int i = 0; i < methodCheckboxes.length; i++) {
            gbc.gridx = i % 4; gbc.gridy = 1 + i / 4;
            p.add(methodCheckboxes[i], gbc);
        }
        return topAligned(p);
    }

    private JPanel buildStatusPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(2, 5, 2, 5);
        JLabel title = new JLabel("Show only these status code ranges:");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        p.add(title, gbc);
        gbc.gridwidth = 1;
        for (int i = 0; i < statusCheckboxes.length; i++) {
            gbc.gridx = i; gbc.gridy = 1;
            p.add(statusCheckboxes[i], gbc);
        }
        return topAligned(p);
    }

    private JPanel buildMimePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(2, 5, 2, 5);
        JLabel title = new JLabel("Show only these MIME types:");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        p.add(title, gbc);
        gbc.gridwidth = 1;
        for (int i = 0; i < mimeCheckboxes.length; i++) {
            gbc.gridx = i % 2; gbc.gridy = 1 + i / 2;
            p.add(mimeCheckboxes[i], gbc);
        }
        return topAligned(p);
    }

    private JPanel buildPatternPanel() {
        return topAligned(patternFilterPanel);
    }

    private JPanel buildExtensionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Show only these extensions:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; p.add(includeExtensionsField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; p.add(new JLabel("Hide these extensions:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; p.add(excludeExtensionsField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel help = new JLabel("<html><i>Separate with commas (e.g., php,jsp,asp)</i></html>");
        help.setForeground(Color.GRAY);
        p.add(help, gbc);
        return topAligned(p);
    }

    private JPanel buildOptionsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; p.add(requireResponseCheckbox, gbc);
        gbc.gridy = 1; p.add(onlyInScopeCheckbox, gbc);
        return topAligned(p);
    }

    private JPanel topAligned(Component content) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        wrapper.add(content, gbc);
        return wrapper;
    }

    private Set<String> parseExtensions(String text) {
        Set<String> result = new HashSet<>();
        if (text != null && !text.trim().isEmpty()) {
            for (String part : text.split(",")) {
                String ext = part.trim().toLowerCase();
                if (!ext.isEmpty()) result.add(ext);
            }
        }
        return result;
    }

    private String formatExtensions(Set<String> extensions) {
        return (extensions == null || extensions.isEmpty()) ? "" : String.join(", ", extensions);
    }

    @Override
    public JComponent uiComponent() {
        return this;
    }
}
