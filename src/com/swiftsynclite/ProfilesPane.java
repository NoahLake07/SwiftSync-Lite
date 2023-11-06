package com.swiftsynclite;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

import static javax.swing.SwingConstants.LEFT;

public class ProfilesPane extends DefaultPane {
    private SwiftSyncLITE.Controller parentApp;
    private JPanel currentProfile;
    private JTextField currentProfileField, masterDirInput, localDirInput;

    ProfilesPane(SwiftSyncLITE.Controller parentApp) {
        super("My Profiles");
        this.parentApp = parentApp;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // region Manage Profiles Panel

        currentProfile = new JPanel();
        currentProfile.setLayout(new FlowLayout(FlowLayout.LEFT));
        currentProfile.setBorder(BorderFactory.createTitledBorder("Manage Profiles"));
        currentProfile.setMaximumSize(new Dimension(Short.MAX_VALUE, currentProfile.getPreferredSize().height));

        JLabel currentProfileTitle = new JLabel("Current Profile:");
        currentProfileTitle.setHorizontalAlignment(LEFT);
        currentProfileField = new JTextField();
        currentProfileField.setMaximumSize(new Dimension(200, currentProfileField.getPreferredSize().height));
        try {
            currentProfileField.setText(parentApp.getCurrentProfile().getProfileName());
        } catch (NullPointerException e) {
            currentProfileField.setText("None Active");
            currentProfileField.setToolTipText("No profile loaded");
        }
        currentProfileField.setEditable(false);
        currentProfileField.setEnabled(false);
        JButton changeProfile = new JButton("Load From File");
        changeProfile.putClientProperty("JButton.buttonType", "roundRect");
        JButton createNewProfile = new JButton("Create New");
        createNewProfile.putClientProperty("JButton.buttonType", "roundRect");
        JButton saveProfile = new JButton("Save Current");
        saveProfile.putClientProperty("JButton.buttonType", "roundRect");

        currentProfile.add(currentProfileTitle);
        currentProfile.add(currentProfileField);
        currentProfile.add(saveProfile);
        currentProfile.add(changeProfile);
        currentProfile.add(createNewProfile);

        if(parentApp.getCurrentProfile() == null){
            saveProfile.setEnabled(false);
            saveProfile.setToolTipText("Can't save a non-existent profile");
        }

        // endregion

        // region Directory Locations Panel

        JPanel directoryLocations = new JPanel();
        directoryLocations.setLayout(new BoxLayout(directoryLocations, BoxLayout.Y_AXIS));
        directoryLocations.setBorder(BorderFactory.createTitledBorder("Directories"));
        Dimension inputPaneDimensions = new Dimension(Short.MAX_VALUE, 40);

        JPanel masterDirInputPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel masterDirInputLabel = new JLabel("Master Directory Path:");
        masterDirInput = new JTextField();
        JButton openMasterBtn = new JButton("Change");
        masterDirInput.putClientProperty("TextComponent.arc", 999);
        masterDirInputPane.add(masterDirInputLabel);
        masterDirInputPane.add(masterDirInput);
        masterDirInputPane.add(openMasterBtn);
        Dimension textFieldDimension = new Dimension(200, masterDirInput.getPreferredSize().height);
        masterDirInput.setPreferredSize(textFieldDimension);
        masterDirInput.setEnabled(false);
        masterDirInputPane.setMaximumSize(inputPaneDimensions);

        JPanel localDirInputPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel localDirInputLabel = new JLabel("Local Directory Path:");
        localDirInput = new JTextField();
        JButton openLocalBtn = new JButton("Change");
        localDirInput.putClientProperty("TextComponent.arc", 999);
        localDirInputPane.add(localDirInputLabel);
        localDirInputPane.add(localDirInput);
        localDirInputPane.add(openLocalBtn);
        localDirInput.setPreferredSize(textFieldDimension);
        localDirInput.setEnabled(false);
        localDirInputPane.setMaximumSize(inputPaneDimensions);

        openLocalBtn.putClientProperty("JButton.buttonType", "roundRect");
        openMasterBtn.putClientProperty("JButton.buttonType", "roundRect");
        localDirInput.putClientProperty("JComponent.roundRect", true);
        masterDirInput.putClientProperty("JComponent.roundRect", true);

        if(parentApp.getCurrentProfile() == null){
            openMasterBtn.setEnabled(false);
            openMasterBtn.setToolTipText("Create or load a profile to use this feature");
            openLocalBtn.setEnabled(false);
            openLocalBtn.setToolTipText("Create or load a profile to use this feature");
        }

        openLocalBtn.addActionListener(e -> {
            File newLocal = new ProfileFactory().chooseFile(JFileChooser.DIRECTORIES_ONLY, "Select New Local Directory");
            parentApp.setProfile(new Profile(
                    parentApp.getCurrentProfile().getProfileName(),
                    newLocal.getPath(),
                    parentApp.getCurrentProfile().getMaster().getPath(),
                    parentApp.getCurrentProfile().getMode()
            ));
        });

        openMasterBtn.addActionListener(e -> {
            File newMaster = new ProfileFactory().chooseFile(JFileChooser.DIRECTORIES_ONLY, "Select New Master Directory");
            parentApp.setProfile(new Profile(
                    parentApp.getCurrentProfile().getProfileName(),
                    parentApp.getCurrentProfile().getLocal().getPath(),
                    newMaster.getPath(),
                    parentApp.getCurrentProfile().getMode()
            ));
        });

        changeProfile.addActionListener(e -> {
            try {
                parentApp.setProfile(new ProfileFactory().chooseProfileFromFile());
                masterDirInput.setText(parentApp.getCurrentProfile().getMaster().getPath());
                localDirInput.setText(parentApp.getCurrentProfile().getLocal().getPath());
                currentProfileField.setText(parentApp.getCurrentProfile().getProfileName());
                saveProfile.setEnabled(true);
                saveProfile.setToolTipText("");
                currentProfileField.setToolTipText(parentApp.getCurrentProfile().getProfileName());
                openLocalBtn.setEnabled(true);
                openMasterBtn.setEnabled(true);
                openLocalBtn.setToolTipText("Change local directory root");
                openMasterBtn.setToolTipText("Change local directory root");
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null,"There was an error loading the profile.\n"+ioe.getMessage(),"Deserialization Error",JOptionPane.ERROR_MESSAGE);
            } catch (ClassNotFoundException ex){
                JOptionPane.showMessageDialog(null,"This profile is from an outdated version of SwiftSync LITE.\n"+ex.getMessage(),"Deserialization Error",JOptionPane.ERROR_MESSAGE);
            }
        });

        createNewProfile.addActionListener(e -> {
            new ProfileFactory(new Runnable() {
                @Override
                public void run() {
                    masterDirInput.setText(parentApp.getCurrentProfile().getMaster().getPath());
                    localDirInput.setText(parentApp.getCurrentProfile().getLocal().getPath());
                    currentProfileField.setText(parentApp.getCurrentProfile().getProfileName());
                }
            });
        });

        saveProfile.addActionListener(e -> {
            File saveDir = new ProfileFactory().chooseSaveLocation(JFileChooser.DIRECTORIES_ONLY,"Choose Save Location");
            String path = saveDir.getPath();
            path += (parentApp.getOS()==OperatingSystem.WINDOWS ? "\\" : "/") + parentApp.getCurrentProfile().getProfileName() + ".ssl/";

            ObjectOutputStream outputStream = null;
            try {
                outputStream = new ObjectOutputStream(new FileOutputStream(path));
                outputStream.writeObject(parentApp.getCurrentProfile());
                outputStream.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,"There was an error saving the current profile.\n"+ex.getMessage(),"Serialization Error",JOptionPane.ERROR_MESSAGE);
            }
        });

        directoryLocations.add(masterDirInputPane);
        directoryLocations.add(localDirInputPane);

        // endregion

        if(parentApp.getCurrentProfile() != null){
            masterDirInput.setText(parentApp.getCurrentProfile().getMaster().getPath());
            localDirInput.setText(parentApp.getCurrentProfile().getLocal().getPath());
            currentProfileField.setText(parentApp.getCurrentProfile().getProfileName());
        }

        add(currentProfile);
        add(directoryLocations);
    }

    private class ProfileFactory {
        private JTextField profileNameField;
        private JTextField masterDirField;
        private JTextField localDirField;
        private Runnable updater;

        public ProfileFactory(Runnable update) {
            this.updater = update;
            createProfileDialog();
        }

        public ProfileFactory(){}

        private void createProfileDialog() {
            JFrame frame = new JFrame("Profile Creation");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
            profileNameField = new JTextField(20);
            masterDirField = new JTextField(20);
            localDirField = new JTextField(20);

            JButton masterDirectoryButton = new JButton("Open");
            JButton localDirectoryButton = new JButton("Open");
            JButton createProfileButton = new JButton("Create Profile");

            masterDirectoryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    File newMaster = chooseFile(JFileChooser.DIRECTORIES_ONLY, "Choose Master Directory");
                    if(newMaster != null){
                        masterDirField.setText(newMaster.getPath());
                    }
                }
            });

            localDirectoryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    File newLocal = chooseFile(JFileChooser.DIRECTORIES_ONLY,"Choose Local Directory");
                    if(newLocal != null){
                        localDirField.setText(newLocal.getPath());
                    }
                }
            });

            JPanel namePanel = new JPanel();
            namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            namePanel.setBorder(BorderFactory.createTitledBorder("Identifiers"));
            namePanel.add(new JLabel("Profile Name:"));
            namePanel.add(profileNameField);

            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel,BoxLayout.Y_AXIS));
            detailsPanel.setBorder(BorderFactory.createTitledBorder("Profile Settings"));

            JPanel masterDirRow = new JPanel();
            masterDirRow.setLayout(new FlowLayout(FlowLayout.LEFT));
            masterDirRow.add(new JLabel("Master Directory:"));
            masterDirRow.add(masterDirField);
            masterDirRow.add(masterDirectoryButton);

            JPanel localDirRow = new JPanel();
            localDirRow.setLayout(new FlowLayout(FlowLayout.LEFT));
            localDirRow.add(new JLabel("Local Directory:"));
            localDirRow.add(localDirField);
            localDirRow.add(localDirectoryButton);

            JPanel modeSelector = new JPanel();
            modeSelector.setLayout(new FlowLayout(FlowLayout.LEFT));
            AtomicReference<Profile.Mode> mode = new AtomicReference<>(Profile.Mode.DEFAULT);
            final Color SELECTED_MODE = new Color(115, 115, 115);
            final Color UNSELECTED_MODE = new Color(50,50,50);
            JButton[] modes = new JButton[]{
                    new JButton("Default"),
                    new JButton("NIO2"),
                    new JButton("SwiftSync")
            };
            modes[0].addActionListener(e->{ // default
                modes[0].setBackground(SELECTED_MODE);
                modes[1].setBackground(UNSELECTED_MODE);
                modes[2].setBackground(UNSELECTED_MODE);
                mode.set(Profile.Mode.DEFAULT);
            });
            modes[1].addActionListener(e->{ // nio2
                modes[0].setBackground(UNSELECTED_MODE);
                modes[1].setBackground(SELECTED_MODE);
                modes[2].setBackground(UNSELECTED_MODE);
                mode.set(Profile.Mode.NIO2);
            });
            modes[2].addActionListener(e->{ // swiftsync
                modes[0].setBackground(UNSELECTED_MODE);
                modes[1].setBackground(UNSELECTED_MODE);
                modes[2].setBackground(SELECTED_MODE);
                mode.set(Profile.Mode.SWIFTSYNC);
            });
            modeSelector.add(new JLabel("Sync Mode: "));
            modeSelector.add(modes[0]);
            modeSelector.add(modes[1]);
            modeSelector.add(modes[2]);
            modes[0].setBackground(SELECTED_MODE);
            modes[1].setBackground(UNSELECTED_MODE);
            modes[2].setBackground(UNSELECTED_MODE);
            mode.set(Profile.Mode.DEFAULT);

            createProfileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String profileName = profileNameField.getText();
                    String masterDirectoryPath = masterDirField.getText();
                    String localDirectoryPath = localDirField.getText();
                    Profile.Mode modeSelected = mode.get();

                    Profile newProfile = new Profile(profileName, localDirectoryPath, masterDirectoryPath, modeSelected);
                    parentApp.setProfile(newProfile);
                    updater.run();

                    // Close the dialog after creating the profile
                    frame.dispose();
                }
            });

            detailsPanel.add(masterDirRow);
            detailsPanel.add(localDirRow);
            detailsPanel.add(modeSelector);

            JPanel actionRow = new JPanel();
            actionRow.setLayout(new FlowLayout(FlowLayout.RIGHT));
            actionRow.add(createProfileButton);

            JPanel headerRow = new JPanel();
            headerRow.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
            headerRow.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel header = new JLabel("New Profile");
            header.setHorizontalTextPosition(LEFT);
            header.setFont(new Font("Arial", Font.BOLD, 20));
            panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            headerRow.add(header);

            panel.add(headerRow);
            header.setHorizontalAlignment(LEFT);
            panel.add(new JSeparator(JSeparator.HORIZONTAL));
            panel.add(namePanel);
            panel.add(detailsPanel);
            panel.add(actionRow);

            frame.add(panel);
            frame.pack();
            frame.setVisible(true);
            frame.setMinimumSize(frame.getPreferredSize());
            frame.setMaximumSize(frame.getPreferredSize());
        }

        public File chooseFile(int mode, String dialogTitle){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(mode);
            chooser.setDialogTitle(dialogTitle);
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                return file;
            }
            return null;
        }

        public File chooseSaveLocation(int mode, String dialogTitle){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(mode);
            chooser.setDialogTitle(dialogTitle);
            int result = chooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                return file;
            }
            return null;
        }

        public Profile chooseProfileFromFile() throws IOException, ClassNotFoundException {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Open Profile From File");
            chooser.setFileFilter(new FileNameExtensionFilter("SwiftSyncLITE Profiles", "ssl"));
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                Profile obj = (Profile) ois.readObject();
                return obj;
            }
            return null;
        }

    }

}
