package com.swiftsynclite;

import javax.swing.*;
import java.awt.*;

public class DefaultPane extends JPanel {
    JLabel header;
    static final Font HEADER_FONT = new Font("Arial",Font.BOLD, 35);
    DefaultPane(String headerText){
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));

        header = new JLabel((headerText == null ? "DefaultPane" : headerText));
        header.setFont(HEADER_FONT);
        header.setHorizontalAlignment(SwingConstants.LEFT);
        header.setHorizontalTextPosition(SwingConstants.LEFT);

        headerPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,header.getPreferredSize().height + 20));
        headerPanel.add(header);
        add(headerPanel);
    }
}