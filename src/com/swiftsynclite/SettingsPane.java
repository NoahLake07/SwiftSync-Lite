package com.swiftsynclite;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.ui.FlatArrowButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.swiftsynclite.SSFE.KB;

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
    private static final Color ABOUT_COLOR_LIGHT_MODE = new Color(218, 216, 169);
    private static final Color ABOUT_COLOR_DARK_MODE = new Color(70, 72, 73);

    private String modeDesc = "";
    private Profile.Mode selectedMode = Profile.Mode.DEFAULT;
    private JLabel modeDescLabel;
    private JPanel modeDescPanel, syncSettings, byteAllocationPanel;
    private JSlider slider;
    private Runnable applyThemeSettings;

    SettingsPane(SwiftSyncLITE.Controller parentApp) {
        super("System Settings");
        this.parentApp = parentApp;
        this.header.setHorizontalAlignment(SwingConstants.LEFT);

        // * UI SETTINGS
        JPanel uiSettings = new JPanel();
        uiSettings.setLayout(new BoxLayout(uiSettings,BoxLayout.Y_AXIS));
        uiSettings.setBorder(BorderFactory.createTitledBorder("User Interface Settings"));
        uiSettings.setMaximumSize(new Dimension(Short.MAX_VALUE,30));

        // UI SETTINGS > THEME
        JPanel themePanel = new JPanel();
        themePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel themeLabel = new JLabel("App Theme");
        themeLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,15));

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton macDark = new JRadioButton("Mac Dark");
        JRadioButton flatDark = new JRadioButton("Flat Dark");
        JRadioButton intellijDark = new JRadioButton("IntelliJ Dark");
        JRadioButton darcula = new JRadioButton("Darcula");

        LookAndFeel currentLaf = UIManager.getLookAndFeel();
        macDark.setSelected(currentLaf instanceof FlatMacDarkLaf);
        flatDark.setSelected(currentLaf instanceof FlatDarkLaf);
        intellijDark.setSelected(currentLaf instanceof FlatIntelliJLaf);
        darcula.setSelected(currentLaf instanceof FlatDarculaLaf);

        buttonGroup.add(macDark);
        buttonGroup.add(flatDark);
        buttonGroup.add(intellijDark);
        buttonGroup.add(darcula);

        themePanel.add(themeLabel);
        themePanel.add(macDark);
        themePanel.add(flatDark);
        themePanel.add(intellijDark);
        themePanel.add(darcula);

        uiSettings.add(themePanel);

        applyThemeSettings = new Runnable() {
            @Override
            public void run() {
                LookAndFeel laf;
                if(macDark.isSelected()){
                    laf = new FlatMacDarkLaf();
                } else if (flatDark.isSelected()){
                    laf = new FlatDarkLaf();
                } else if (intellijDark.isSelected()){
                    laf = new FlatIntelliJLaf();
                } else if (darcula.isSelected()){
                    laf = new FlatDarculaLaf();
                } else {
                    return;
                }

                try {
                    UIManager.setLookAndFeel(laf);
                    parentApp.repaintAll();
                } catch (UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // * SYNC SETTINGS
        syncSettings = new JPanel();
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
        modeDescPanel = new JPanel();
        modeDescLabel = new JLabel("<html><body style='width: 100%;'>" + modeDesc + "</body></html>");
        modeDescLabel.setVerticalAlignment(SwingConstants.TOP);

        modeDescPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        modeDescPanel.add(modeDescLabel);

        modeDescLabel.setMaximumSize(new Dimension(10, 20));
        modeDescLabel.setBackground(Color.BLACK);

        // SYNC SETTINGS > BYTE ALLOCATION SIZE
        byteAllocationPanel = new JPanel();
        byteAllocationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        byteAllocationPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        JLabel allocationLabel = new JLabel("Transfer Buffer Allocation (KB):");
        slider = new JSlider(JSlider.HORIZONTAL, 1, 20, 8);
        slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(5);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.addChangeListener(l->{
            if(slider.getValue()>12){
                slider.setForeground(Color.YELLOW);
            } else {
                slider.setForeground(new Color(64, 131, 197));
            }
        });
        if(slider.getValue()>14){
            slider.setForeground(Color.YELLOW);
        } else {
            slider.setForeground(new Color(64, 131, 197));
        }
        byteAllocationPanel.add(allocationLabel);
        byteAllocationPanel.add(slider);

        // SYNC SETTINGS > BYTE ALLOCATION SIZE > HARDWARE TEST
        JPanel testPanel = new JPanel();
        testPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel testDesc = new JLabel("Not sure what byte allocation you should use?");
        JButton testBtn = new JButton("Run Test");
        testBtn.addActionListener(e -> {
            parentApp.runByteTest();
        });
        testPanel.add(testDesc);
        testPanel.add(testBtn);

        if (parentApp.getCurrentProfile() != null) {
            modeDesc = (Profile.Mode.getDescription(parentApp.getMode()));
        } else {
            modeDesc = ("N/A - Load a profile to see a mode description");
        }

        modeDescPanel.setBackground((parentApp.isDarkMode() ? ABOUT_COLOR_DARK_MODE : ABOUT_COLOR_LIGHT_MODE));
        modeDescLabel.setText(modeDesc);

        // SYNC SETTINGS > SYNC MODE > CURRENT MODE BTN ACTIONS
        defaultBtn.addActionListener(e->{
            Profile.Mode newMode = Profile.Mode.DEFAULT;
            selectedMode = newMode;
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,newMode);
        });
        nio2Btn.addActionListener(e->{
            Profile.Mode newMode = Profile.Mode.NIO2;
            selectedMode = newMode;
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,newMode);
        });
        swiftsyncBtn.addActionListener(e->{
            Profile.Mode newMode = Profile.Mode.SWIFTSYNC;
            selectedMode = newMode;
            updateButtons(defaultBtn,nio2Btn,swiftsyncBtn,newMode);
        });

        // SYNC SETTINGS > SYNC MODE (ADDING PANELS)
        syncMode.add(defaultSyncMode);
        syncMode.add(setCurrentModePanel);
        syncMode.add(modeDescPanel);
        syncMode.setPreferredSize(new Dimension(Short.MAX_VALUE,syncMode.getPreferredSize().height));
        syncSettings.add(syncMode);
        syncSettings.add(byteAllocationPanel);
        syncSettings.setMaximumSize(new Dimension(Short.MAX_VALUE,70));
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
        add(uiSettings); //todo make the ui settings
        add(syncSettings);
        add(testPanel);
        add(applyButton, LEFT_ALIGNMENT);
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
        modeDescPanel.setBackground((parentApp.isDarkMode() ? ABOUT_COLOR_DARK_MODE: ABOUT_COLOR_LIGHT_MODE));
        selectedMode = mode;

        if(parentApp.getMode() != null){
            modeDesc = (Profile.Mode.getDescription(selectedMode));
        } else {
            modeDesc =("N/A - Load a profile to see a mode description");
        }
        modeDescLabel.setText(modeDesc);
    }

    public void refreshBuffer(){
        slider.setValue(parentApp.getByteBuffer());
    }

    private void applySettings() {
        // * SYNC SETTINGS
        if(parentApp.getCurrentProfile() != null) {
            parentApp.setMode(selectedMode);
            parentApp.setBufferSize(slider.getValue() * KB);
        }

        // * UI SETTINGS
        applyThemeSettings.run();

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

