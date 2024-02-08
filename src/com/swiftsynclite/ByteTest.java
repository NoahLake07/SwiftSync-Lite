package com.swiftsynclite;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executor;
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

        while(frame.getWidth()>325){
            frame.setSize(frame.getWidth()-1,frame.getHeight()-1);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Function<Void, Void> loadAll = e -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose location to perform the test");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userChoice = fileChooser.showDialog(frame, "Select");

            if (userChoice == JFileChooser.APPROVE_OPTION) {

                File selectedDirectory = fileChooser.getSelectedFile();
                File mainDirectory = new File(selectedDirectory, "fileprocessingtest");
                File localFolder = new File(mainDirectory, "local");
                File masterFolder = new File(mainDirectory, "master");

                if (!mainDirectory.exists()) {
                    mainDirectory.mkdirs();
                    localFolder.mkdirs();
                    masterFolder.mkdirs();
                }

                String benchmarkFilePath = new File(mainDirectory, "benchmark_file.txt").getAbsolutePath();
                createBenchmarkFile(benchmarkFilePath, 100);

                doTest(benchmarkFilePath, localFolder, masterFolder);
            }
            return null;
        };

        new Thread(() -> loadAll.apply(null)).start();
    }

    private void doTest(String benchmarkFilePath, File local, File master){
        int before = SSFE.getByteTransfer();
         // make a loop and transfer the file with different byte transfers whilst tracking the time
    }

    private static void createBenchmarkFile(String filePath, int fileSizeMB) {
        try {
            File file = new File(filePath);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // Set the file size
            randomAccessFile.setLength(fileSizeMB * 1024L * 1024L);

            // Fill the file with some data (optional)
            byte[] data = "Benchmarking file content.".getBytes();
            for (long i = 0; i < fileSizeMB * 1024L * 1024L / data.length; i++) {
                randomAccessFile.write(data);
            }

            randomAccessFile.close();
            System.out.println("Benchmark file created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
