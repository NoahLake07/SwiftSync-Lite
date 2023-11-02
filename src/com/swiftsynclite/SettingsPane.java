package com.swiftsynclite;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPane extends DefaultPane {
    private SwiftSyncLITE.Controller parentApp;
    private JRadioButton lightThemeRadioButton;
    private JRadioButton darkThemeRadioButton;
    private JCheckBox showStatusBarCheckBox;
    private int theme = 0;

    SettingsPane(SwiftSyncLITE.Controller parentApp) {
        super("System Settings");
        this.parentApp = parentApp;
        this.header.setHorizontalAlignment(SwingConstants.LEFT);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Theme Selection
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel themeLabel = new JLabel("User Interface Theme:");
        ButtonGroup themeGroup = new ButtonGroup();
        lightThemeRadioButton = new JRadioButton("Light");
        darkThemeRadioButton = new JRadioButton("Dark");
        themeGroup.add(lightThemeRadioButton);
        themeGroup.add(darkThemeRadioButton);
        themePanel.add(themeLabel);
        themePanel.add(lightThemeRadioButton);
        themePanel.add(darkThemeRadioButton);

        // Show Status Bar
        JPanel statusBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showStatusBarCheckBox = new JCheckBox("Always Show Status Bar");
        statusBarPanel.add(showStatusBarCheckBox);

        // About Button
        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });

        // Apply Button
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });

        // Add components to the SettingsPane
        add(themePanel);
        add(statusBarPanel);
        add(aboutButton);
        add(applyButton);
    }

    private void applySettings() {
        // Apply theme selection
        boolean isDarkMode = darkThemeRadioButton.isSelected();
        this.theme = (isDarkMode?0:1);

        // Apply status bar visibility
        boolean showStatusBar = showStatusBarCheckBox.isSelected();
        // TODO connect to app

        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            // Update the UI components to reflect the new theme
            SwingUtilities.updateComponentTreeUI(parentApp.getFrame());
            parentApp.reloadBasicUI();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        // Show a confirmation message to the user.
        JOptionPane.showMessageDialog(this, "Settings applied successfully.", "Settings Applied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String version = SwiftSyncLITE.VERSION;
        String aboutMessage = "SwiftSync LITE\nVersion: " + version + "\n\n" +
                "This application is licensed under the Apache License 2.0.\n" +
                "© 2023 SwiftSync";
        JOptionPane.showMessageDialog(this, aboutMessage, "About SwiftSync LITE", JOptionPane.INFORMATION_MESSAGE);
    }

    int getTheme(){
        return this.theme;
    }
}

