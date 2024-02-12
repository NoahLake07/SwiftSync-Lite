package com.swiftsynclite;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static javax.swing.JOptionPane.showMessageDialog;

public class ByteTest {

    private SwiftSyncLITE.Controller controller;
    private JPanel testPanel, loadingPanel, resultsPanel;
    JFrame frame;
    JProgressBar progress;
    JLabel loadingLbl;

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
        loadingLbl = new JLabel("Loading assets...");
        loadingLbl.setFont(new Font("Arial", Font.PLAIN, 21));
        loadingLbl.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setMinimumSize(new Dimension(frame.getWidth(), loadingLbl.getHeight()));
        progress = new JProgressBar(0, 100);
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
                Thread.sleep(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Runnable loadAll = () -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose location to perform the test");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userChoice = fileChooser.showDialog(frame, "Select");

            if (userChoice == JFileChooser.APPROVE_OPTION) {

                loadingLbl.setText("Creating resources...");

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

                HashMap<Integer,Long> results = doBenchmark(benchmarkFilePath, localFolder,3);
                int fastestTest = -1; long fastestTestTime = Long.MAX_VALUE;
                for (int i = 0; i < results.size(); i++) {
                    if(results.get(i+1)<fastestTestTime){
                        fastestTest = i+1; fastestTestTime = results.get(i+1);
                    }
                }

                loadingPanel.setVisible(false);
                resultsPanel = new JPanel();

                Object[][] data = new Object[results.size()][3];
                Object[] columnNames = new Object[]{"Test Number","Byte Transfer (KB)","Time (ms)"};
                for (int i = 0; i < results.size(); i++) {
                    data[i][0] = (i+1);
                    data[i][1] = i+1;
                    data[i][2] = results.get(i+1);
                }

                DefaultTableModel tableModel = new DefaultTableModel() {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                JTable resultTable = new JTable(data,columnNames);
                //resultTable.setModel(tableModel);

                JLabel bestByteAllocation = new JLabel();
                bestByteAllocation.setText("Optimal Byte Allocation Setting: " + fastestTest);
                bestByteAllocation.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                JPanel applyPanel = new JPanel();
                applyPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                    JButton apply = new JButton("Apply");
                int finalFastestTest = fastestTest;
                apply.addActionListener(e->{
                        controller.setBufferSize(finalFastestTest);
                        showMessageDialog(null,"Settings applied successfully.");
                    });
                applyPanel.add(apply);

                resultsPanel.setLayout(new BoxLayout(resultsPanel,BoxLayout.Y_AXIS));
                resultsPanel.add(resultTable);
                resultsPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

                resultsPanel.add(bestByteAllocation);
                resultsPanel.add(applyPanel);

                JScrollPane scrollPane = new JScrollPane(resultsPanel);

                frame.add(scrollPane);
                frame.setSize(300,500);
                frame.setTitle("Benchmark Results");
                frame.repaint();
                frame.setResizable(true);
            }
        };
        Executors.newCachedThreadPool().submit(loadAll);
    }

    private HashMap<Integer,Long> doBenchmark(String benchmarkFilePath, File local, int testQty){

        ArrayList<HashMap<Integer,Long>> results = new ArrayList<>();
        int numOfTests = 18;

        for (int i = 0; i < testQty; i++) {
            loadingLbl.setText("Running benchmark...");
            progress.setValue((int) ((double) i+1 / (double) testQty)*100);
            progress.setString("Running test "+ (i+1) + " out of " + testQty);
            File testLocal = new File(local.getPath() + "/Test " + i+1 + "/");
            testLocal.mkdir();

            results.add(doTest(benchmarkFilePath,testLocal,numOfTests));
        }

        loadingLbl.setText("Compiling results...");

        return averageOfHashMaps(results);
    }

    private HashMap<Integer,Long> doTest(String benchmarkFilePath, File local, int numOfTests){
        progress.setIndeterminate(false);
        progress.setString("Initializing...");
        int before = SSFE.getByteTransfer();
        SSFE fileEngine = new SSFE(null);
        File benchmarkFile = new File(benchmarkFilePath);

        HashMap<Integer,Long> results = new HashMap<>();

        for (int i = 0; i < numOfTests; i++) {
            System.out.println("> Testing buffer size of " + (i+1)*SSFE.KB);
            fileEngine.setBufferSize((i+1)*SSFE.KB);

            SSFE.SyncTask task = fileEngine.instantiateTask(benchmarkFile, Path.of(local.getPath() + "/benchmarkfile " + i + "/"));
            LocalDateTime timeStart = LocalDateTime.now();
            fileEngine.sync(task, Profile.Mode.DEFAULT);
            LocalDateTime timeEnd = LocalDateTime.now();

            long ms = ChronoUnit.MILLIS.between(timeStart,timeEnd);
            results.put(i+1,ms);
        }

        print(results);

        return results;
    }

    private static void print(HashMap<Integer,Long> hashMap){
        for (Integer key : hashMap.keySet()) {
            System.out.println(key + " = " + hashMap.get(key));
        }
    }

    public static HashMap<Integer, Long> averageOfHashMaps(ArrayList<HashMap<Integer, Long>> arrayList) {
        HashMap<Integer, Long> averages = new HashMap<>();

        for (HashMap<Integer, Long> hashMap : arrayList) {
            for (Integer key : hashMap.keySet()) {
                Long value = hashMap.get(key);

                if (!averages.containsKey(key)) {
                    averages.put(key, 0L);
                }

                averages.put(key, averages.get(key) + value);
            }
        }

        for (Integer key : averages.keySet()) {
            averages.put(key, averages.get(key) / arrayList.size());
        }

        return averages;
    }

    private static void createBenchmarkFile(String filePath, int fileSizeMB) {
        try {
            Path path = Path.of(filePath);
            if(Files.exists(path)){
                Files.delete(path);
            }

            File file = new File(filePath);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // Set the file size
            randomAccessFile.setLength(fileSizeMB * 1024L * 1024L);

            // Fill the file with some data (optional)
            byte[] data = "﷽ SSL BENCHMARK CONTENT ﷽ |".getBytes();
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
