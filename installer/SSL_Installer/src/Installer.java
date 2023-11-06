import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.util.prefs.Preferences;

public class Installer {

    private static final String APP_NAME = "SwiftSyncLITE";
    private static final String VERSION = "0.5.3";
    private static final String JAR_FILE_NAME = "SwiftSyncLITE v" + VERSION + ".jar";
    private static final String BAT_FILE_NAME = "OpenSSLFile.bat";
    private static final String REG_FILE_NAME = "registerSSLFileAssociation.reg";
    private static final String FILE_EXTENSION = ".ssl";

    public static void main(String[] args) {
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("SwiftSyncLITE Installer for Windows");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.setSize(300,100);

                JLabel descriptionLabel = new JLabel("SwiftSyncLITE Installer (version " + VERSION + ")");
                descriptionLabel.setFont(new Font("Arial", Font.BOLD, 20));
                descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JButton installButton = new JButton("Install SwiftSyncLITE");
                installButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        performSetup();
                    }
                });

                JPanel panel = new JPanel(new GridLayout(2, 1));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                panel.add(descriptionLabel);
                panel.add(installButton);

                frame.add(panel, BorderLayout.CENTER);
                frame.setVisible(true);
                Dimension lockedSize = new Dimension(450, 300);
                frame.setMinimumSize(lockedSize);
                frame.setMaximumSize(lockedSize);
                frame.setSize(lockedSize);
            });
        } else {
            JOptionPane.showMessageDialog(null,"This SwiftSyncLITE installer is not supported by your operating system.");
        }
    }

    private static void performSetup() {
        // Destination directory in Program Files/SwiftSyncLITE
        Path installationDir = Paths.get(System.getProperty("user.home"), "Documents", APP_NAME);
        Path jarAppPath = installationDir.resolve(JAR_FILE_NAME);

        try (InputStream inputStream = Installer.class.getClassLoader().getResourceAsStream(JAR_FILE_NAME)) {
            if (inputStream != null) {
                Files.createDirectories(installationDir);
                Files.copy(inputStream, jarAppPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                System.out.println("! INPUT STREAM IS NULL");
                failed();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            failed();
            return;
        }

        // Create batch file in the same directory as the JAR file
        Path batchFilePath = installationDir.resolve(BAT_FILE_NAME);
        try (BufferedWriter batchWriter = Files.newBufferedWriter(batchFilePath)) {
            batchWriter.write("javaw -jar \"" + jarAppPath.toString() + "\" %1");
        } catch (IOException e) {
            e.printStackTrace();
            failed();
            return;
        }

        // Create registry file in the same directory as the JAR file
        Path regFilePath = installationDir.resolve(REG_FILE_NAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(regFilePath.toFile()))) {
            writer.write("Windows Registry Editor Version 5.00\n\n");
            writer.write("[HKEY_CLASSES_ROOT\\.ssl\\shell\\open\\command]\n");
            String realPath = String.valueOf(installationDir.resolve(BAT_FILE_NAME));
            String regPath = realPath.replace("\\","\\\\");
            writer.write("@=\"\\\"" + regPath + "\\\" \\\"%1\\\"\"");
        } catch (IOException e) {
            e.printStackTrace();
            failed();
            return;
        }

        // Execute the generated .reg file to update the Registry
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", REG_FILE_NAME);
            processBuilder.directory(installationDir.toFile());
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
            failed();
            return;
        }

        // Installation completed
        success();
        System.exit(0);
    }

    static void success() {
        JOptionPane.showMessageDialog(null, "SwiftSyncLITE has been successfully installed!\nPlease restart to see changes.\nRunnable jar is in your documents folder.", "Installation Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    static void failed() {
        JOptionPane.showMessageDialog(null, "Installation failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
