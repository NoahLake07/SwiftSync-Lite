package com.swiftsynclite;

import javax.swing.*;

import java.awt.*;

import static com.swiftsynclite.SwiftSyncLITE.*;

public class ConsolePane extends DefaultPane {
    private ConsoleTextArea console;
    private SwiftSyncLITE.Controller parentApp;
    private JScrollPane scrollPane;
    private JPanel processPanel;
    private JProgressBar progressBar;
    private JLabel currentStatusLabel;
    private JTextField currentStatusField;
    public String currentStatus = "";

    public static final Color PROGRESS_BAR_BACKGROUND_COLOR = new Color(213, 213, 213);
    public static final Color PROGRESS_BAR_FOREGROUND_COLOR = new Color(93, 156, 217);

    ConsolePane(SwiftSyncLITE.Controller parentApp) {
        super("Console");
        this.parentApp = parentApp;
        this.header.setHorizontalAlignment(SwingConstants.LEFT);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        console = new ConsoleTextArea();
        console.setBackground(CONSOLE_COLOR);
        console.setDefaultColor(DEFAULT_CONSOLE_TEXT_COLOR);
        console.append("SwiftSync LITE Console Running...\n––––––––––––––––––––––––––\n", new Color(192, 121, 180));
        console.append("Enter a command to get started.\n", new Color(182, 76, 171));

        scrollPane = new JScrollPane(console);
        scrollPane.putClientProperty("ScrollBar.thumbArc", 999 );
        scrollPane.putClientProperty("ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ));
        add(scrollPane);

        processPanel = new JPanel();
        processPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        currentStatusLabel = new JLabel("Current Status:");
        progressBar = new JProgressBar(0,100);
        progressBar.setIndeterminate(true);
        progressBar.setBackground(PROGRESS_BAR_BACKGROUND_COLOR);
        progressBar.setForeground(PROGRESS_BAR_FOREGROUND_COLOR);
        currentStatusField = new JTextField();
        currentStatusField.setText("n/a");
        currentStatusField.setMaximumSize(new Dimension(200,currentStatusLabel.getPreferredSize().height));

        processPanel.add(currentStatusLabel);
        processPanel.add(currentStatusField);
        processPanel.add(progressBar);
        processPanel.setVisible(false);
        add(processPanel);

        ConsoleInputBar inputBar = new ConsoleInputBar(this);
        add(inputBar);
    }

    void analyze(String s){
        this.console.append(s + "\n", DEFAULT_CONSOLE_USER_TEXT_COLOR);
        scrollToBottom();
        parentApp.analyze(s);
        scrollToBottom();
    }

    void showProcessBar(){
        this.processPanel.setVisible(true);

        currentStatusField.setEnabled(false);
        if(currentStatus == null || currentStatus.equals("")){
            progressBar.setIndeterminate(true);
            currentStatusLabel.setText("Current Status:");
            currentStatusField.setText("N/A");
        } else {
            currentStatusLabel.setText("Current Status:");
            currentStatusField.setText(currentStatus);
        }
    }

    void hideProcessBar(){
        this.processPanel.setVisible(false);
    }

    void openHelpMenu(){
        parentApp.openHelpMenu();
    }

    public void setStatus(String s){
        this.currentStatus = s;
        currentStatusField.setText(s);

        if(currentStatus == null || currentStatus.equals("")){
            progressBar.setIndeterminate(true);
            currentStatusLabel.setText("Current Status:");
            currentStatusField.setText("N/A");
        } else {
            currentStatusLabel.setText("Current Status:");
            currentStatusField.setText(currentStatus);
        }
    }

    public void setProgress(double progress){
        this.progressBar.setValue((int) (progress * 100));
        this.progressBar.setString("%.2f" + progress*100);
    }

    public JProgressBar getProgressBar (){
        return this.progressBar;
    }


    public void append(String s){
        console.append(s + "\n");
        scrollToBottom(scrollPane);
    }

    public void append(String s, Color c){
        console.append(s + "\n",c);
        scrollToBottom(scrollPane);
    }

    void scrollToBottom(JScrollPane scrollPane){
        JScrollBar scrollbar = scrollPane.getVerticalScrollBar();
        scrollbar.setValue(scrollbar.getMaximum());
    }

    void scrollToBottom(){
        scrollToBottom(scrollPane);
    }
}
