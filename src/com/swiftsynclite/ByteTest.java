package com.swiftsynclite;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class ByteTest {

    private SwiftSyncLITE.Controller controller;
    private JPanel testPanel, loadingPanel;
    JFrame frame;

    public ByteTest(SwiftSyncLITE.Controller controller){
        this.controller = controller;

        frame = new JFrame("Run Byte Buffer Test");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new BoxLayout(homePanel,BoxLayout.Y_AXIS));

        testPanel = new JPanel();

        JLabel headerText = new JLabel("Byte Allocation Test");
        headerText.setFont(new Font("Arial",Font.BOLD,21));
        headerText.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        headerText.setHorizontalAlignment(SwingConstants.CENTER);
        homePanel.add(headerText);

        JLabel testDetailsHeader = new JLabel("This test will determine the best byte allocation for your device.\nIt will:");
        testDetailsHeader.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        testDetailsHeader.setFont(new Font("Arial",Font.PLAIN,14));
        JTextPane testDetails = new JTextPane();
        testDetails.setText("  - Run 10 transfers on your computer while collecting data\n" +
                "  - Analyze the data and determine which is most efficient for your device\n" +
                "  - Display results and an option to apply the found setting\n\n" +
                "Estimated time is 5 minutes. Could vary depending on your device.");
        testDetails.setPreferredSize(new Dimension(Short.MAX_VALUE,50));
        testDetails.setEditable(false);
        homePanel.add(testDetailsHeader);
        homePanel.add(testDetails);

        JPanel startRow = new JPanel();
        startRow.setLayout(new FlowLayout(FlowLayout.CENTER));
        startRow.setPreferredSize(new Dimension(Short.MAX_VALUE,30));
        JButton startTest = new JButton("Start");
        startTest.addActionListener(e->{
            homePanel.setVisible(false);
            testPanel.setVisible(true);
            this.loadTest();
        });
        startRow.add(startTest);
        homePanel.add(startRow);

        frame.add(homePanel);
        frame.setSize(500,300);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocation(400,400);

        /*
        SHRINK FRAME ANIMATION:
        executor.execute(new Runnable() {
        while (frame.getWidth() >= 300) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
                frame.repaint();
            }
        }
    }
         */
    }



    private void loadTest() {
        loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        JLabel loadingLbl = new JLabel("Loading assets...");
        loadingLbl.setFont(new Font("Arial", Font.PLAIN, 21));
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setMinimumSize(new Dimension(frame.getWidth(), loadingLbl.getHeight()));
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setMaximumSize(new Dimension(500, 20));
        progress.setIndeterminate(true);
        loadingPanel.setVisible(true);
        frame.add(loadingPanel);
        // Finish Loading UI

        progress.putClientProperty("ProgressBar.arc", 999);
        loadingPanel.add(progress);
        loadingPanel.add(loadingLbl);
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        frame.setSize(frame.getWidth() - 10, frame.getHeight());

        Function loadAll = e->{
            // TODO load the test, then perform it
            return null;
        };

        // TODO execute loadAll() with a multi thread executor
    };
}
