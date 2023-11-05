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
    private static final Color SELECTED_COLOR_DARK_MODE = new Color(115, 115, 115);
    private static final Color UNSELECTED_COLOR_DARK_MODE = new Color(56, 56, 56);
    private static final Color SELECTED_COLOR_LIGHT_MODE = new Color(220, 220, 220);
    private static final Color UNSELECTED_COLOR_LIGHT_MODE = new Color(143, 143, 143);

    private JTextPane aboutText;

    SettingsPane(SwiftSyncLITE.Controller parentApp) {
        super("System Settings");
        this.parentApp = parentApp;
        this.header.setHorizontalAlignment(SwingConstants.LEFT);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // * UI SETTINGS
        JPanel uiSettings = new JPanel();
        uiSettings.setLayout(new BoxLayout(uiSettings,BoxLayout.Y_AXIS));
        uiSettings.setBorder(BorderFactory.createTitledBorder("User Interface Settings"));

        // UI SETTINGS > THEME
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel themeLabel = new JLabel("Theme:");
        ButtonGroup themeGroup = new ButtonGroup();
        lightThemeRadioButton = new JRadioButton("Light");
        darkThemeRadioButton = new JRadioButton("Dark");
        themeGroup.add(lightThemeRadioButton);
        themeGroup.add(darkThemeRadioButton);
        themePanel.add(themeLabel);
        themePanel.add(lightThemeRadioButton);
        themePanel.add(darkThemeRadioButton);
        darkThemeRadioButton.setSelected(parentApp.isDarkMode());
        lightThemeRadioButton.setSelected(!parentApp.isDarkMode());

        uiSettings.add(themePanel);
        uiSettings.setMaximumSize(new Dimension(Short.MAX_VALUE,20));

        // * SYNC SETTINGS
        JPanel syncSettings = new JPanel();
        syncSettings.setLayout(new BoxLayout(syncSettings, BoxLayout.Y_AXIS));
        syncSettings.setBorder(BorderFactory.createTitledBorder("Sync Settings"));

        // SYNC SETTINGS > SYNC MODE (PARENT PANEL)
        JPanel syncMode = new JPanel();
        syncMode.setLayout(new BoxLayout(syncMode,BoxLayout.Y_AXIS));

        // SYNC SETTINGS > SYNC MODE > DEFAULT SYNC MODE
        JPanel defaultSyncMode = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel defaultSyncLabel = new JLabel("Default Mode:");
        JButton profileDFSync = new JButton(String.valueOf(parentApp.getDefaultMode()).toUpperCase());
        profileDFSync.setEnabled(false);
        profileDFSync.setToolTipText("This is determined from your last saved instance of this profile.");
        defaultSyncMode.add(defaultSyncLabel);
        defaultSyncMode.add(profileDFSync);
        profileDFSync.setBorderPainted(false);

        // SYNC SETTINGS > SYNC MODE > CURRENT MODE
        JPanel setCurrentModePanel = new JPanel();
        setCurrentModePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        setCurrentModePanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        JLabel currentMode = new JLabel("Current Mode:");
        setCurrentModePanel.add(currentMode);
        JButton defaultBtn = new JButton("Default");
        JButton nio2Btn = new JButton("NIO2");
        JButton swiftsyncBtn = new JButton("SwiftSync");
        setCurrentModePanel.add(defaultBtn);
        setCurrentModePanel.add(nio2Btn);
        setCurrentModePanel.add(swiftsyncBtn);

        // SYNC SETTINGS > SYNC MODE > ABOUT CURRENT MODE
        JPanel aboutMode = new JPanel();
        aboutMode.setLayout(new FlowLayout(FlowLayout.LEFT));
        aboutText = new JTextPane();
        aboutText.setEditable(false);
        if(parentApp.getMode() != null){
            aboutText.setText(Profile.Mode.getDescription(parentApp.getMode()));
        } else {
            aboutText.setText("N/A - Load a profile to see a mode description");
        }

        // SYNC SETTINGS > SYNC MODE > CURRENT MODE BTN ACTIONS
        defaultBtn.addActionListener(e->{
            Profile.Mode newMode = Profile.Mode.DEFAULT;
            parentApp.setMode(newMode);
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,newMode);
        });
        nio2Btn.addActionListener(e->{
            Profile.Mode newMode = Profile.Mode.NIO2;
            parentApp.setMode(newMode);
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,newMode);
        });
        swiftsyncBtn.addActionListener(e->{
            Profile.Mode newMode = Profile.Mode.SWIFTSYNC;
            parentApp.setMode(newMode);
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,newMode);
        });

        // SYNC SETTINGS > SYNC MODE (ADDING PANELS)
        syncMode.add(defaultSyncMode);
        syncMode.add(setCurrentModePanel);
        syncMode.add(aboutMode);
        syncMode.setPreferredSize(new Dimension(Short.MAX_VALUE,syncMode.getPreferredSize().height));
        syncSettings.add(syncMode);
        syncSettings.setMaximumSize(new Dimension(Short.MAX_VALUE,40));
        if(parentApp.getDefaultMode()==null){
            defaultBtn.setEnabled(false);
            nio2Btn.setEnabled(false);
            swiftsyncBtn.setEnabled(false);
        } else {
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,parentApp.getDefaultMode());
        }

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
        add(uiSettings);
        add(syncSettings);
        add(applyButton);
    }

    private void updateButtons(JButton def, JButton nio, JButton ss, Profile.Mode mode){
        Color selectedColor, unselectedColor;
        if(parentApp.isDarkMode()){
            selectedColor = SELECTED_COLOR_DARK_MODE;
            unselectedColor = UNSELECTED_COLOR_DARK_MODE;
        } else {
            selectedColor = SELECTED_COLOR_LIGHT_MODE;
            unselectedColor = UNSELECTED_COLOR_LIGHT_MODE;
        }

        def.setBackground((mode == Profile.Mode.DEFAULT ? selectedColor: unselectedColor));
        nio.setBackground((mode == Profile.Mode.NIO2 ? selectedColor: unselectedColor));
        ss.setBackground((mode == Profile.Mode.SWIFTSYNC ? selectedColor: unselectedColor));

        if(parentApp.getMode() != null){
            aboutText.setText(Profile.Mode.getDescription(parentApp.getMode()));
        } else {
            aboutText.setText("N/A - Load a profile to see a mode description");
        }
    }

    private void applySettings() {
        // * THEME
        boolean isDarkMode = darkThemeRadioButton.isSelected();
        this.theme = (isDarkMode?0:1);
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

        // * SYNC SETTINGS
        // todo apply sync settings

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

