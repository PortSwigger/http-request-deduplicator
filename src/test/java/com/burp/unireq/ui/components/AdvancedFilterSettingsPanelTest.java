package com.burp.unireq.ui.components;

import com.burp.unireq.model.FilterCriteria;
import org.junit.jupiter.api.Test;

import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedFilterSettingsPanelTest {

    private static final int SETTINGS_VIEWPORT_WIDTH = 800;

    @Test
    void settingsContentFitsWithinTypicalBurpViewport() throws Exception {
        AtomicReference<AdvancedFilterSettingsPanel> panelReference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() ->
            panelReference.set(new AdvancedFilterSettingsPanel())
        );

        AdvancedFilterSettingsPanel panel = panelReference.get();
        JTabbedPane tabs = findTabbedPane(panel);

        assertNotNull(tabs);
        assertEquals(6, tabs.getTabCount());
        assertEquals(JTabbedPane.SCROLL_TAB_LAYOUT, tabs.getTabLayoutPolicy());
        assertTrue(
            panel.getPreferredSize().width <= SETTINGS_VIEWPORT_WIDTH,
            () -> "Settings panel is too wide: " + panel.getPreferredSize().width + "px"
        );

        for (int i = 0; i < tabs.getTabCount(); i++) {
            Dimension preferredSize = tabs.getComponentAt(i).getPreferredSize();
            assertTrue(
                preferredSize.width <= SETTINGS_VIEWPORT_WIDTH,
                () -> "A settings tab is too wide: " + preferredSize.width + "px"
            );
        }
    }

    @Test
    void patternTabUsesClearLabeledControls() throws Exception {
        AtomicReference<AdvancedFilterSettingsPanel> panelReference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() ->
            panelReference.set(new AdvancedFilterSettingsPanel())
        );

        PatternFilterPanel patterns = findComponent(panelReference.get(), PatternFilterPanel.class);
        assertNotNull(patterns);
        assertEquals(2, findComponents(patterns, JTextField.class).size());
        assertTrue(
            findComponents(patterns, JToggleButton.class).stream()
                .allMatch(JCheckBox.class::isInstance)
        );

        List<String> checkboxLabels = findComponents(patterns, JCheckBox.class).stream()
            .map(JCheckBox::getText)
            .collect(Collectors.toList());
        assertEquals(
            Arrays.asList(
                "Use regular expressions",
                "Case-sensitive matching",
                "Exclude matching hosts"
            ),
            checkboxLabels
        );
    }

    @Test
    void criteriaRoundTripsThroughPatternControls() throws Exception {
        AtomicReference<AdvancedFilterSettingsPanel> panelReference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            AdvancedFilterSettingsPanel panel = new AdvancedFilterSettingsPanel();
            FilterCriteria criteria = new FilterCriteria();
            criteria.setHostPattern("api\\.example\\.com");
            criteria.setPathPattern("/v[12]/users");
            criteria.setRegexMode(true);
            criteria.setCaseSensitive(true);
            criteria.setInvertHostFilter(true);
            panel.loadCriteria(criteria);
            panelReference.set(panel);
        });

        FilterCriteria result = panelReference.get().getCurrentCriteria();
        assertEquals("api\\.example\\.com", result.getHostPattern());
        assertEquals("/v[12]/users", result.getPathPattern());
        assertTrue(result.isRegexMode());
        assertTrue(result.isCaseSensitive());
        assertTrue(result.isInvertHostFilter());

        SwingUtilities.invokeAndWait(panelReference.get()::clearAll);
        FilterCriteria cleared = panelReference.get().getCurrentCriteria();
        assertEquals("", cleared.getHostPattern());
        assertEquals("", cleared.getPathPattern());
        assertFalse(cleared.isRegexMode());
        assertFalse(cleared.isCaseSensitive());
        assertFalse(cleared.isInvertHostFilter());
    }

    @Test
    void patternEditsNotifyAdvancedFilterListeners() throws Exception {
        AtomicInteger notifications = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            AdvancedFilterSettingsPanel panel = new AdvancedFilterSettingsPanel();
            panel.addChangeListener(notifications::incrementAndGet);
            PatternFilterPanel patterns = findComponent(panel, PatternFilterPanel.class);
            assertNotNull(patterns);
            patterns.setHostPattern("example.com");
            patterns.setPathPattern("/api");

            for (JCheckBox checkbox : findComponents(patterns, JCheckBox.class)) {
                checkbox.doClick();
            }
        });

        assertTrue(notifications.get() >= 5);
    }

    private JTabbedPane findTabbedPane(Container root) {
        return findComponent(root, JTabbedPane.class);
    }

    private <T extends Component> T findComponent(Container root, Class<T> type) {
        List<T> components = findComponents(root, type);
        return components.isEmpty() ? null : components.get(0);
    }

    private <T extends Component> List<T> findComponents(Container root, Class<T> type) {
        List<T> results = new java.util.ArrayList<>();
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                results.add(type.cast(component));
            }
            if (component instanceof Container container) {
                results.addAll(findComponents(container, type));
            }
        }
        return results;
    }
}
