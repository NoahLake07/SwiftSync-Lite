package com.swiftsynclite;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Font.BOLD;

public class SwiftSyncLITE {

    public static final Font SELECTED_FONT = new Font("Arial", BOLD,15);
    public static final Font DEFAULT_BTN_FONT = new Font("Arial",Font.PLAIN,15);
    public static final int SIDEBAR_PADDING = 7;

    public static final Color SIDEBAR_BTN_DEFAULT = new Color(70, 70, 70);
    public static final Color SIDEBAR_BTN_SELECTED = new Color(94, 94, 94);
    public static final Color CONSOLE_COLOR = new Color(37, 37, 37);
    public static final Color SIDEBAR_BACKGROUND_COLOR = new Color(50, 50, 50);
    public static final Color DEFAULT_CONSOLE_TEXT_COLOR = new Color(215, 215, 183);
    public static final Color DEFAULT_CONSOLE_USER_TEXT_COLOR = new Color(220, 214, 138);
    public static final Color ERROR_TEXT_COLOR = new Color(231, 54, 54);

    private Panes myPanes = null;
    private JButton consoleButton, profilesButton, settingsButton;
    private JSplitPane splitPane;

    private Controller SSFE_controller = null;

    private OperatingSystem os;

    public SwiftSyncLITE() {
        String fetchedOS = System.getProperty("os.name");
        if(fetchedOS.contains("Mac")){
            this.os = OperatingSystem.MACOS;
        } else if(fetchedOS.contains("Windows")){
            this.os = OperatingSystem.WINDOWS;
        } else if(fetchedOS.contains("Linux")){
            this.os = OperatingSystem.LINUX;
        } else {
            this.os = OperatingSystem.UNSUPPORTED;
        }

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SSFE_controller = new Controller(null, this);

        launch();
        SSFE_controller.setConsolePane(myPanes.consolePane);
    }

    private void launch() {
        JFrame mainframe = new JFrame("SwiftSync LITE");
        mainframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainframe.setSize(800, 600);
        mainframe.setMinimumSize(new Dimension(660,500));

        myPanes = new Panes();

        consoleButton = createSidebarButton("Console");
        profilesButton = createSidebarButton("Profiles");
        settingsButton = createSidebarButton("Settings");

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(SIDEBAR_PADDING, SIDEBAR_PADDING, SIDEBAR_PADDING, SIDEBAR_PADDING));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BACKGROUND_COLOR);

        Dimension buttonDimension = new Dimension(Short.MAX_VALUE,consoleButton.getPreferredSize().height+10);
        consoleButton.setMaximumSize(buttonDimension);
        profilesButton.setMaximumSize(buttonDimension);
        settingsButton.setMaximumSize(buttonDimension);

        consoleButton.setFont(DEFAULT_BTN_FONT);
        profilesButton.setFont(DEFAULT_BTN_FONT);
        settingsButton.setFont(DEFAULT_BTN_FONT);

        JLabel logo = new JLabel("SwiftSync");
        logo.setHorizontalAlignment(SwingConstants.LEFT);
        logo.setOpaque(true);
        logo.setBackground(SIDEBAR_BACKGROUND_COLOR);
        logo.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        logo.setLayout(new FlowLayout(FlowLayout.LEFT));

        try {
            ImageIcon logoIcon = new ImageIcon(ImageIO.read(new File("res/SSL_logo.png")));
            Image logoImage = logoIcon.getImage();
            int logoW = logoIcon.getIconWidth(), logoH = logoIcon.getIconHeight();
            double logoScaleFactor = 4.5;
            Image resizedLogoImg = logoImage.getScaledInstance((int) ((int) logoW/logoScaleFactor), (int) ((int) logoH/logoScaleFactor),  java.awt.Image.SCALE_SMOOTH);
            logoIcon = new ImageIcon(resizedLogoImg);
            logo.setIcon(logoIcon);
            logo.setIconTextGap(7);
            Dimension logoSpace = new Dimension((int) (logoW/logoScaleFactor), (int) (logoH/logoScaleFactor) + 10);
            logo.setMaximumSize(logoSpace);
            logo.setMinimumSize(logoSpace);

            ImageIcon consoleIcon = new ImageIcon(ImageIO.read(new File("res/consoleicon.png")));
            Image image = consoleIcon.getImage();
            int iconW = consoleIcon.getIconWidth(), iconH = consoleIcon.getIconHeight();
            double scaleFactor = 3;
            Image newimg = image.getScaledInstance((int) ((int) iconW/scaleFactor), (int) ((int) iconH/scaleFactor),  java.awt.Image.SCALE_SMOOTH);
            consoleIcon = new ImageIcon(newimg);
            consoleButton.setIcon(consoleIcon);
            consoleButton.setIconTextGap(7);

            ImageIcon profilesIcon = new ImageIcon(ImageIO.read(new File("res/profilesicon.png")));
            Image image2 = profilesIcon.getImage();
            int iconW2 = profilesIcon.getIconWidth(), iconH2 = profilesIcon.getIconHeight();
            double scaleFactor2 = 3;
            Image newimg2 = image2.getScaledInstance((int) ((int) iconW2/scaleFactor2), (int) ((int) iconH2/scaleFactor2),  java.awt.Image.SCALE_SMOOTH);
            profilesIcon = new ImageIcon(newimg2);
            profilesButton.setIcon(profilesIcon);
            profilesButton.setIconTextGap(7);

            ImageIcon settingsIcon = new ImageIcon(ImageIO.read(new File("res/settingsicon.png")));
            Image image3 = settingsIcon.getImage();
            int iconW3 = settingsIcon.getIconWidth(), iconH3 = settingsIcon.getIconHeight();
            double scaleFactor3 = 3;
            Image newimg3 = image3.getScaledInstance((int) ((int) iconW3/scaleFactor3), (int) ((int) iconH3/scaleFactor3),  java.awt.Image.SCALE_SMOOTH);
            settingsIcon = new ImageIcon(newimg3);
            settingsButton.setIcon(settingsIcon);
            settingsButton.setIconTextGap(7);
        } catch (IOException e) {
            //nothing
        }

        //sidebarPanel.add(logo); // TODO FIX LOGO LOADING
        sidebarPanel.add(consoleButton);
        sidebarPanel.add(profilesButton);
        sidebarPanel.add(settingsButton);

        JPanel welcomePane = new JPanel();
        JLabel welcomeText = new JLabel("Welcome to SwiftSync LITE!\n\nClick a menu on the sidebar to get started.");
        welcomeText.setPreferredSize(welcomePane.getPreferredSize());
        welcomePane.add(welcomeText);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, welcomePane);
        splitPane.setEnabled(false);
        adjustSplitPane(splitPane);

        mainframe.add(splitPane, BorderLayout.CENTER);

        consoleButton.addActionListener(e -> {
            sidebarItemClicked("console");
        });

        profilesButton.addActionListener(e -> {
            sidebarItemClicked("profiles");
        });

        settingsButton.addActionListener(e -> {
            sidebarItemClicked("settings");
        });

        mainframe.setVisible(true);
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(SIDEBAR_BTN_DEFAULT);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setSize(200,40);
        return button;
    }

    private void sidebarItemClicked(String key){
        switch (key){
            case "console":
                replaceContentPane(splitPane, myPanes.consolePane);

                consoleButton.setFont(SELECTED_FONT);
                consoleButton.setBackground(SIDEBAR_BTN_SELECTED);

                profilesButton.setFont(DEFAULT_BTN_FONT);
                profilesButton.setBackground(SIDEBAR_BTN_DEFAULT);

                settingsButton.setFont(DEFAULT_BTN_FONT);
                settingsButton.setBackground(SIDEBAR_BTN_DEFAULT);
                break;
            case "profiles":
                myPanes.profilesPane = new ProfilesPane(SSFE_controller);
                replaceContentPane(splitPane, myPanes.profilesPane);

                consoleButton.setFont(DEFAULT_BTN_FONT);
                consoleButton.setBackground(SIDEBAR_BTN_DEFAULT);

                profilesButton.setFont(SELECTED_FONT);
                profilesButton.setBackground(SIDEBAR_BTN_SELECTED);

                settingsButton.setFont(DEFAULT_BTN_FONT);
                settingsButton.setBackground(SIDEBAR_BTN_DEFAULT);
                break;

            case "settings":
                replaceContentPane(splitPane, myPanes.settingsPane);

                consoleButton.setFont(DEFAULT_BTN_FONT);
                consoleButton.setBackground(SIDEBAR_BTN_DEFAULT);

                profilesButton.setFont(DEFAULT_BTN_FONT);
                profilesButton.setBackground(SIDEBAR_BTN_DEFAULT);

                settingsButton.setFont(SELECTED_FONT);
                settingsButton.setBackground(SIDEBAR_BTN_SELECTED);
                break;
        }

        adjustSplitPane(splitPane);
    }

    private void replaceContentPane(JSplitPane splitPane, JPanel newContentPane) {
        splitPane.setRightComponent(newContentPane);
        splitPane.revalidate();
        splitPane.repaint();
    }

    private void adjustSplitPane(JSplitPane splitPane){
        splitPane.setDividerLocation(200);
    }
    public Panes getMyPanes(){
        return myPanes;
    }

    public Profile.Mode getSyncMode(){
        return SSFE_controller.getCurrentProfile().getMode();
    }

    public OperatingSystem getOS(){
        return this.os;
    }

    class Controller {

        private Profile currentProfile;
        private SwiftSyncLITE ui;
        private ConsolePane console;
        private SSFE fileEngine;

        Controller(Profile profile, SwiftSyncLITE parentUI){
            this.currentProfile = profile;
            this.ui = parentUI;

            this.fileEngine = new SSFE(ui);
        }

        public void setConsolePane(ConsolePane cp){
            this.console = cp;
            fileEngine.setConsolePane(cp);
        }

        public OperatingSystem getOS(){
            return ui.os;
        }

        protected void analyze(String s){
            switch(s){
                case "sync":
                case "synchronize":
                    if(this.currentProfile == null){
                        console.append("You need to load a profile before starting a sync." +
                                "\nLoad one at: Profiles > Manage Profiles > Load From File", ERROR_TEXT_COLOR);
                    } else {
                        this.synchronize();
                    }
                    break;
                case "master root":
                    try {
                        fileEngine.revealInFileBrowser(currentProfile.getMaster().getPath());
                    } catch (IOException e) {
                        console.append(e.getMessage(), ERROR_TEXT_COLOR);
                        throw new RuntimeException(e);
                    }
                    break;
                case "local root":
                    try {
                        fileEngine.revealInFileBrowser(currentProfile.getLocal().getPath());
                    } catch (IOException e) {
                        console.append(e.getMessage(), ERROR_TEXT_COLOR);
                        throw new RuntimeException(e);
                    }
                    break;
                case "master size":
                    console.append("SIZE OF MASTER DIRECTORY: " + (SSFE.folderSize(currentProfile.getMaster()) / (1024.0 * 1024.0 * 1024.0)) + " GB");
                    break;
                case "local size":
                    console.append("SIZE OF LOCAL DIRECTORY: " + (SSFE.folderSize(currentProfile.getLocal()) / (1024.0 * 1024.0 * 1024.0)) + " GB");
                    break;
                case "status hide":
                case "hide status":
                    console.hideProcessBar();
                    break;
                case "status show":
                case "show status":
                    console.showProcessBar();
                    break;
                case "help":
                    openHelpMenu();
                    break;
                case "profile page":
                    replaceContentPane(splitPane,myPanes.profilesPane);
                    break;
                default:
                    console.append("That command was not recognized. Please try again...", ERROR_TEXT_COLOR);
                    break;
            }
        }

        void openHelpMenu(){
            // open a help menu
        }

        private void synchronize(){
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    fileEngine.createIndexer(getOS(),getCurrentProfile().getMaster(),getCurrentProfile().getLocal(),getMyPanes().consolePane);
                    fileEngine.startIndexing();
                    console.showProcessBar();
                    console.setStatus("Indexing...");
                    console.append("Indexing directories...", new Color(136, 165, 199));

                    if(fileEngine.getIndexedTasks().isEmpty()){
                        console.append("No tasks found.");
                        console.setStatus("Ready");
                    } else {
                        console.append("Found " + fileEngine.getIndexedTasks().size() + " tasks. Syncing now.");
                        console.setStatus("Syncing");
                        console.getProgressBar().setIndeterminate(false);
                        console.setProgress(0);
                    }

                    fileEngine.sync(fileEngine.getIndexedTasks());
                    console.setProgress(1);
                    console.setStatus("Ready");
                    console.append("Sync complete. System ready.", new Color(51, 169, 17));
                }
            });
        }

        public void setProfile(Profile p){
            this.currentProfile = p;
        }

        public Profile getCurrentProfile(){
            return this.currentProfile;
        }
    }

    class Panes {
        ConsolePane consolePane;
        ProfilesPane profilesPane;
        SettingsPane settingsPane;
         Panes(ConsolePane cp, ProfilesPane pp, SettingsPane sp){
            this.consolePane = cp;
            this.profilesPane = pp;
            this.settingsPane = sp;
        }

        Panes(){
            this.consolePane = new ConsolePane(SSFE_controller);
            this.profilesPane = new ProfilesPane(SSFE_controller);
            this.settingsPane = new SettingsPane(SSFE_controller);
        }
    }

    public static void main(String[] args) {
        new SwiftSyncLITE();
    }
}