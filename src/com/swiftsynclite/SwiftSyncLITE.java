package com.swiftsynclite;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.imageio.ImageIO;
import javax.sound.midi.Sequencer;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Font.BOLD;

public class SwiftSyncLITE {

    public static final String VERSION = "0.5 beta";

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
    private JFrame mainframe;

    private Controller SSFE_controller = null;

    private boolean isDarkMode = true;

    private OperatingSystem os;

    public SwiftSyncLITE(String profileInstance){
        this();
        File file =new File(profileInstance);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Profile obj = (Profile) ois.readObject();
            SSFE_controller.setProfile(obj);
            replaceContentPane(splitPane, myPanes.consolePane);
            SSFE_controller.console.append("Profile \"" + SSFE_controller.getCurrentProfile().getProfileName() + "\" was opened successfully.", new Color(25, 115, 11));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public SwiftSyncLITE() {
        String fetchedOS = System.getProperty("os.name").toLowerCase();
        if(fetchedOS.contains("mac")){
            this.os = OperatingSystem.MACOS;
        } else if(fetchedOS.contains("win")){
            this.os = OperatingSystem.WINDOWS;
        } else if(fetchedOS.contains("linux")){
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
        mainframe = new JFrame("SwiftSync LITE");
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
            boolean loadForIntelliJ = false;
            InputStream logoInputStream = SwiftSyncLITE.class.getClassLoader().getResourceAsStream("res/SSL_logo.png");
            if (logoInputStream != null) {
                BufferedImage logoImage = ImageIO.read(logoInputStream);
                int logoW = logoImage.getWidth(), logoH = logoImage.getHeight();
                double logoScaleFactor = 4.5;
                Image resizedLogoImg = logoImage.getScaledInstance((int) (logoW / logoScaleFactor), (int) (logoH / logoScaleFactor), Image.SCALE_SMOOTH);
                ImageIcon logoIcon = new ImageIcon(resizedLogoImg);
                logo.setIcon(logoIcon);
                logo.setIconTextGap(7);
                Dimension logoSpace = new Dimension((int) (logoW / logoScaleFactor), (int) (logoH / logoScaleFactor) + 10);
                logo.setMaximumSize(logoSpace);
                logo.setMinimumSize(logoSpace);
            } else {
                loadForIntelliJ = true;
            }

            InputStream consoleIconInputStream = SwiftSyncLITE.class.getClassLoader().getResourceAsStream("res/consoleicon.png");
            if (consoleIconInputStream != null) {
                BufferedImage consoleIconImage = ImageIO.read(consoleIconInputStream);
                int iconW = consoleIconImage.getWidth(), iconH = consoleIconImage.getHeight();
                double scaleFactor = 3;
                Image newimg = consoleIconImage.getScaledInstance((int) (iconW / scaleFactor), (int) (iconH / scaleFactor), Image.SCALE_SMOOTH);
                ImageIcon consoleIcon = new ImageIcon(newimg);
                consoleButton.setIcon(consoleIcon);
                consoleButton.setIconTextGap(7);
            }else {
                loadForIntelliJ = true;
            }

            InputStream profilesIconInputStream = SwiftSyncLITE.class.getClassLoader().getResourceAsStream("res/profilesicon.png");
            if (profilesIconInputStream != null) {
                BufferedImage profilesIconImage = ImageIO.read(profilesIconInputStream);
                int iconW = profilesIconImage.getWidth(), iconH = profilesIconImage.getHeight();
                double scaleFactor = 3;
                Image newimg = profilesIconImage.getScaledInstance((int) (iconW / scaleFactor), (int) (iconH / scaleFactor), Image.SCALE_SMOOTH);
                ImageIcon consoleIcon = new ImageIcon(newimg);
                profilesButton.setIcon(consoleIcon);
                profilesButton.setIconTextGap(7);
            }else {
                loadForIntelliJ = true;
            }

            InputStream settingsIconInputStream = SwiftSyncLITE.class.getClassLoader().getResourceAsStream("res/settingsicon.png");
            if (settingsIconInputStream != null) {
                BufferedImage settingsIconImage = ImageIO.read(settingsIconInputStream);
                int iconW = settingsIconImage.getWidth(), iconH = settingsIconImage.getHeight();
                double scaleFactor = 3;
                Image newimg = settingsIconImage.getScaledInstance((int) (iconW / scaleFactor), (int) (iconH / scaleFactor), Image.SCALE_SMOOTH);
                ImageIcon consoleIcon = new ImageIcon(newimg);
                settingsButton.setIcon(consoleIcon);
                settingsButton.setIconTextGap(7);
            }else {
                loadForIntelliJ = true;
            }

            if(loadForIntelliJ){
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
            }
        } catch (Exception e) {
            myPanes.consolePane.append("Error loading application resources.", ERROR_TEXT_COLOR);
        }


        //sidebarPanel.add(logo); // TODO FIX LOGO LOADING
        sidebarPanel.add(consoleButton);
        sidebarPanel.add(profilesButton);
        sidebarPanel.add(settingsButton);

        JPanel welcomePane = new JPanel();
        welcomePane.setLayout(new BorderLayout());
        welcomePane.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // Add padding

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeText = new JLabel("Welcome to SwiftSync LITE!");
        welcomeText.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", BOLD, 30));
        welcomeText.setForeground(new Color(50, 122, 183));

        JLabel instructionText = new JLabel("Click a menu on the sidebar to get started.");
        instructionText.setHorizontalAlignment(SwingConstants.CENTER);
        instructionText.setFont(new Font("Arial", Font.PLAIN, 20));
        instructionText.setForeground(new Color(79, 129, 189));

        textPanel.add(welcomeText);
        textPanel.add(instructionText);

        welcomePane.add(textPanel, BorderLayout.CENTER);

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

    private void reloadBasicUI(){
        Color splitPaneColor = (myPanes.settingsPane.getTheme()==0 ? SIDEBAR_BACKGROUND_COLOR:new Color(201, 201, 201));
        splitPane.setBackground(splitPaneColor);
        settingsButton.setBackground(splitPaneColor);
        consoleButton.setBackground(splitPaneColor);
        profilesButton.setBackground(splitPaneColor);
        consoleButton.setBackground((myPanes.settingsPane.getTheme()==0 ? CONSOLE_COLOR:new Color(145, 145, 145)));
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

    public void setSyncMode(Profile.Mode mode){
        SSFE_controller.setMode(mode);
    }

    public OperatingSystem getOS(){
        return this.os;
    }

    class Controller {

        private Profile currentProfile;
        private SwiftSyncLITE ui;
        private ConsolePane console;
        private SSFE fileEngine;
        private Boolean stopProcess = false;
        private Profile.Mode profileDefaultMode;

        Controller(Profile profile, SwiftSyncLITE parentUI){
            this.currentProfile = profile;
            this.ui = parentUI;
            if(profile == null){
                this.profileDefaultMode = null;
            } else {
                this.profileDefaultMode = currentProfile.getMode();
            }

            this.fileEngine = new SSFE(ui);
        }

        public JFrame getFrame(){
            return ui.mainframe;
        }

        public void setConsolePane(ConsolePane cp){
            this.console = cp;
            fileEngine.setConsolePane(cp);
        }

        void setMode(Profile.Mode mode){
            currentProfile.setMode(mode);
            console.append("Mode now set to " + mode, new Color(164, 68, 178));
        }

        void reloadBasicUI(){
            ui.reloadBasicUI();
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
                case "stop":
                    console.append("Stopping process...", new Color(236, 200, 90));
                    this.stopProcess = true;
                    break;
                case "index":
                    indexOnly();
                    break;
                case "swiftsync mode":
                    setSyncMode(Profile.Mode.SWIFTSYNC);
                    console.append("Sync mode now set to '" + getSyncMode() + "'",new Color(150, 32, 166));
                    break;
                case "default mode":
                    setSyncMode(Profile.Mode.DEFAULT);
                    console.append("Sync mode now set to '" + getSyncMode() + "'",new Color(150, 32, 166));
                    break;
                case "nio2 mode":
                    setSyncMode(Profile.Mode.NIO2);
                    console.append("Sync mode now set to '" + getSyncMode() + "'",new Color(150, 32, 166));
                    break;
                default:
                    console.append("That command was not recognized. Please try again...", ERROR_TEXT_COLOR);
                    break;
            }
        }

        void openHelpMenu() {
            String helpContent = getHelpManualContent();

            try (InputStream inputStream = SwiftSyncLITE.class.getClassLoader().getResourceAsStream("docs/help.html")) {
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                         BufferedWriter writer = new BufferedWriter(new FileWriter("help.html"))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }

                        // Write the dynamically generated help content to the file
                        writer.write(helpContent);
                    }
                } else {
                    console.append("Error reading help template: Resource not found.", ERROR_TEXT_COLOR);
                    return;
                }
            } catch (IOException e) {
                console.append("Error creating help file: " + e.getMessage(), ERROR_TEXT_COLOR);
                return;
            }

            // Open the generated HTML file in the default web browser
            try {
                File helpFile = new File("help.html");
                Desktop.getDesktop().open(helpFile);
            } catch (IOException e) {
                console.append("Error opening help file: " + e.getMessage(), ERROR_TEXT_COLOR);
            }
        }

        private String getHelpManualContent(){
            String helpContent = "<html><head><style>"
                    + "body { background-color: #2B2B2B; color: #A9B7C6; font-family: Arial, sans-serif; padding-left: 20px; }"
                    + "h1 { color: #61AFEF; }"
                    + "h2 { color: #61AFEF; }"
                    + "h3 { color: #61AFEF; }"
                    + "table { width: 80%; margin-top: 20px; border-collapse: collapse; }"
                    + "th, td { border: 1px solid #3E3E3E; padding: 10px; text-align: left; }"
                    + "th { background-color: #3E3E3E; color: #61AFEF; }"
                    + "a { color: #61AFEF; text-decoration: none; }"
                    + "</style></head><body>"
                    + "<h1>SwiftSyncLITE - User Manual</h1>"

                    + "<h2><br>App Overview</h2>"
                    + "<p>SwiftSyncLITE is a lightweight synchronization tool designed to keep your files up-to-date between two directories. "
                    + "It provides <br>quick and efficient synchronization features for managing your data. </p><p>When you run the synchronize command, "
                    + "your master <br>and local directory will begin syncing. Any differences that the app notices between your local and master file "
                    + "directories, <br>SwiftSyncLITE will then resolve all the differences.</p>"

                    + "<h2>Profiles</h2>"
                    + "<p>Profiles store your data for that instance of the directories you're syncing, the mode you're syncing with, and other settings.<br>"
                    + "This way, when you want to switch between your sync instances, you can simply go to the Profiles tab and load one from a file.<p>"
                    + "<p>To create a new profile, follow these steps:</p>"
                    + "<ol>"
                    + "<li>Go to Profiles > Manage Profiles.</li>"
                    + "<li>Click on 'Create New.'</li>"
                    + "<li>Follow the on-screen instructions to set up your master and local directories.</li>"
                    + "<li>Save the profile to a file location of choice for future use. If you do not save your profile after creating it, the data will be lost upon closing the app.</li>"
                    + "</ol>"

                    + "<h2>Commands</h2>"
                    + "<table>"
                    + "<tr><th>Command</th><th>Description</th></tr>"
                    + "<tr><td>sync (or synchronize)</td><td>Start synchronization. Make sure to load a profile first.</td></tr>"
                    + "<tr><td>master root</td><td>Open the master directory in the file browser.</td></tr>"
                    + "<tr><td>local root</td><td>Open the local directory in the file browser.</td></tr>"
                    + "<tr><td>master size</td><td>Show the size of the master directory.</td></tr>"
                    + "<tr><td>local size</td><td>Show the size of the local directory.</td></tr>"
                    + "<tr><td>status hide (or hide status)</td><td>Hide the status/process bar.</td></tr>"
                    + "<tr><td>status show (or show status)</td><td>Show the status/process bar.</td></tr>"
                    + "<tr><td>help</td><td>Show this help menu.</td></tr>"
                    + "<tr><td>profile page</td><td>Switch to the profile management page.</td></tr>"
                    + "</table>"

                    + "<h3>GitHub Repository</h3>"
                    + "<p>For more information and to contribute to the project, visit the "
                    + "<a href='https://github.com/NoahLake07/SwiftSyncLITE'>GitHub repository</a>.</p>"
                    + "<p>SwiftSyncLITE is licensed under the Apache License 2.0.</p>"
                    + "</body></html>";

            return helpContent;
        }

        private void indexOnly(){
            fileEngine.createIndexer(getOS(),getCurrentProfile().getMaster(),getCurrentProfile().getLocal(),getMyPanes().consolePane,stopProcess);
            long size = fileEngine.startIndexing();
            console.showProcessBar();
            console.setStatus("Indexing...");
            console.append("Indexing directories...", new Color(136, 165, 199));

            if(fileEngine.getIndexedTasks().isEmpty()){
                console.append("No tasks found.");
                console.setStatus("Ready");
            } else {
                console.append("Found " + fileEngine.getIndexedTasks().size() + " tasks. [~"+((int)size / 1024 / 1024 / 1024) + " GB]");
            }
        }

        private void synchronize(){
            stopProcess = false;
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    fileEngine.createIndexer(getOS(),getCurrentProfile().getMaster(),getCurrentProfile().getLocal(),getMyPanes().consolePane,stopProcess);
                    long size = fileEngine.startIndexing();
                    console.showProcessBar();
                    console.setStatus("Indexing...");
                    console.append("Indexing directories...", new Color(136, 165, 199));

                    if(fileEngine.getIndexedTasks().isEmpty()){
                        console.append("No tasks found.");
                        console.setStatus("Ready");
                    } else {
                        console.append("Found " + fileEngine.getIndexedTasks().size() + " tasks. [~"+((int)size / 1024 / 1024 / 1024) + " GB]");
                        console.setStatus("Syncing");
                        console.getProgressBar().setIndeterminate(false);
                        console.setProgress(0);
                    }

                    console.showDetailsBtn(true);
                    fileEngine.sync(fileEngine.getIndexedTasks(), stopProcess);
                    console.setProgress(1);
                    console.setStatus("Ready");
                    console.append("Sync complete. System ready.", new Color(51, 169, 17));
                    console.showDetailsBtn(false);
                }
            });
        }

        public void setProfile(Profile p){
            this.currentProfile = p;
            this.profileDefaultMode = currentProfile.getMode();
            myPanes.settingsPane = new SettingsPane(this);
        }

        public Profile.Mode getMode(){
            if(this.currentProfile == null){
                return Profile.Mode.DEFAULT;
            }
            return this.currentProfile.getMode();
        }

        public Profile getCurrentProfile(){
            return this.currentProfile;
        }

        public boolean isDarkMode(){
            return ui.isDarkMode;
        }

        public void setDarkMode(boolean b){
            ui.isDarkMode = b;
        }

        public Profile.Mode getDefaultMode(){
            return this.profileDefaultMode;
        }

        public void setBufferSize(int size){
            fileEngine.setBufferSize(size);
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
        if(args.length==0){
            new SwiftSyncLITE();
        } else if (args[0].endsWith("ssl")){
            new SwiftSyncLITE(args[0]);
        }
    }
}