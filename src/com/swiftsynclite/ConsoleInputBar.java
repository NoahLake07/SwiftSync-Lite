package com.swiftsynclite;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class ConsoleInputBar extends JPanel {
    JButton push, help;
    Icon pushIcon, helpIcon;
    JTextField input;
    private boolean isWaiting = false;
    private ConsolePane parentPane = null;

    ConsoleInputBar(ConsolePane parentPane){
        this.parentPane = parentPane;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        input = new JTextField();

        try {
            loadIcons();
            push = new JButton("Push", pushIcon);
            help = new JButton(helpIcon);
        } catch (IOException e) {
            push = new JButton("Push");
            help = new JButton("?");
        }

        push.putClientProperty("JButton.buttonType", "roundRect");
        help.putClientProperty("JButton.buttonType", "roundRect");
        input.putClientProperty("JComponent.roundRect",true);

        input.setPreferredSize(new Dimension(400,30));
        push.setPreferredSize(new Dimension(push.getPreferredSize().width, input.getPreferredSize().height));
        help.setPreferredSize(new Dimension(input.getPreferredSize().height, input.getPreferredSize().height));

        push.addActionListener(e->{
            tryEntryPush();
        });

        help.setToolTipText("Help");
        help.addActionListener(e-> parentPane.openHelpMenu());

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tryEntryPush();
                }
            }
        });

        add(input);
        add(push);
        add(help);
    }

    private void tryEntryPush(){
        if(isEntryValid()){
            if(isWaiting){
                isWaiting = false;
            } else {
                String entry = input.getText();
                if(entry.startsWith(" ")){
                    entry = entry.substring(1,entry.length());
                }
                input.setText("");
                parentPane.analyze(entry);
            }
        }
    }

    private boolean isEntryValid(){
        return !input.getText().isEmpty();
    }

    public String getUserInput(){
        isWaiting = true;
        waitForUser();
        String userInput = input.getText();
        if(userInput.startsWith(" ")){
            userInput = userInput.substring(0,userInput.length());
        }
        input.setText("");
        return userInput;
    }

    private void waitForUser(){
        while(isWaiting){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void loadIcons() throws IOException {
        pushIcon = new ImageIcon(ImageIO.read(new File("res/pushIcon.png/")));
        helpIcon = new ImageIcon(ImageIO.read(new File("res/helpIcon.png/")));
    }
}
