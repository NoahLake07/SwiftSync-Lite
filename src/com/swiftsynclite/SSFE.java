package com.swiftsynclite;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.swiftsynclite.SwiftSyncLITE.ERROR_TEXT_COLOR;

public class SSFE {

    private SwiftSyncLITE app;
    private ConsolePane console;
    private FileIndexer indexer;
    private static boolean stopReference;
    public static final int KB = 1024;
    private static int byteTransfer = KB*8;
    private long allTaskSizes = 0, allByteProgress = 0;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public SSFE(SwiftSyncLITE app){
        this.app = app;
    }

    public void setConsolePane(ConsolePane cp){
        this.console = cp;
    }

    public void createIndexer(OperatingSystem os,File parent, File local, ConsolePane consolePane, boolean stop){
        this.indexer = new FileIndexer(os,parent,local,consolePane);
        this.stopReference = stop;
    }

    static void setByteTransfer(int bytes){
        byteTransfer = bytes;
    }

    static int getByteTransfer(){
        return byteTransfer;
    }

    public long startIndexing(){
        return this.indexer.index();
    }

    public void sync(SyncTask task, Profile.Mode syncMode){
            if (syncMode == Profile.Mode.DEFAULT) {
                defaultSync(task);
            } else if (syncMode == Profile.Mode.NIO2) {
                nio2sync(task);
            } else if (syncMode == Profile.Mode.SWIFTSYNC) {
                swiftSync(task,1,1);
            }
    }

    public void sync(ArrayList<SyncTask> tasks, Boolean stopReference) {
        long totalSize = calculateTotalSize(tasks); // Calculate total size of all tasks
        long completedSize = 0; // Variable to keep track of completed size
        double progress = 0;
        allByteProgress = 0;

        for (int i = 0; i < tasks.size(); i++) {
            SyncTask currentTask = tasks.get(i);
            long taskSize = currentTask.getFileSize(); // Get the size of the current task

            if (app.getSyncMode() == Profile.Mode.DEFAULT) {
                defaultSync(currentTask);
                completedSize += taskSize; // Update completed size after each task
                progress = (double) completedSize / totalSize;
                console.setProgress(progress);
            } else if (app.getSyncMode() == Profile.Mode.NIO2) {
                nio2sync(currentTask);
                completedSize += taskSize; // Update completed size after each task
            } else if (app.getSyncMode() == Profile.Mode.SWIFTSYNC) {
                swiftSync(currentTask,i+1,tasks.size());
            }

            if (stopReference) {
                console.append("Sync process stopped.", Color.ORANGE);
                break;
            }
        }
        console.setProgress(100);
    }

    // Calculate the total size of all tasks
    private long calculateTotalSize(ArrayList<SyncTask> tasks) {
        long totalSize = 0;
        for (SyncTask task : tasks) {
            totalSize += task.getFileSize();
        }
        return totalSize;
    }

    private void defaultSync(SyncTask task){
        File childFile = new File(String.valueOf(task.getChildPath()));
        File parentFile = task.getParentFile();

        try {
            Files.createDirectories(task.getChildPath().getParent());
            Files.createFile(task.getChildPath());
            FileInputStream fis = new FileInputStream(parentFile);
            FileOutputStream fos = new FileOutputStream(childFile);
            byte[] buffer = new byte[byteTransfer];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            int finalBytesRead = bytesRead;
            cachedThreadPool.submit(()->{
                task.setProgress((double) finalBytesRead /task.getFileSize());
                console.setTaskProgress(task.getProgress());
                console.setProgress(getCombinedProgress(getIndexedTasks())*100);
            });
            fos.close();
        } catch (FileNotFoundException e) {
            console.append("ERROR: A FileNotFoundException occurred while syncing " + parentFile.getName() + " -> " + e.getMessage(), ERROR_TEXT_COLOR);
        } catch (IOException e) {
            console.append("ERROR: An IOException occurred while syncing " + parentFile.getName() + " -> "  + e.getMessage(), ERROR_TEXT_COLOR);
        }
    }

    private void nio2sync(SyncTask task){
        File childFile = new File(String.valueOf(task.getChildPath()));
        File parentFile = task.getParentFile();
        console.setTaskLabel(task.toString());
        try {
            task.setProgress(0.01d);
            Files.createDirectories(task.getChildPath().getParent());
            Files.copy(parentFile.toPath(), childFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            cachedThreadPool.submit(()->{
                task.setProgress(1d);
                console.setTaskProgress(task.progress);
                console.setProgress(getCombinedProgress(getIndexedTasks())*100);
            });
        } catch (FileNotFoundException fnfe){
            console.append("ERROR: A FileNotFoundException occurred while syncing " + parentFile.getName() + " -> " + fnfe.getMessage(), ERROR_TEXT_COLOR);
        } catch (IOException ioe){
            console.append("ERROR: An IOException occurred while syncing " + parentFile.getName() + " -> "  + ioe.getMessage(), ERROR_TEXT_COLOR);
        }
    }

    private void swiftSync(SyncTask task, int taskID, int totalTaskQty) {
        File childFile = new File(String.valueOf(task.getChildPath()));
        File parentFile = task.getParentFile();

        console.setTaskLabel(task.toString());

        try {
            Files.createDirectories(task.getChildPath().getParent());
            AsynchronousFileChannel sourceChannel = AsynchronousFileChannel.open(parentFile.toPath(), StandardOpenOption.READ);
            AsynchronousFileChannel destinationChannel = AsynchronousFileChannel.open(childFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            ByteBuffer buffer = ByteBuffer.allocate(byteTransfer);
            long position = 0; // Initial position in the file
            long totalBytesTransferred = 0; // Total bytes transferred for the current task
            long fileSize = task.getFileSize(); // Total size of the file for the current task

            while (position < fileSize) {
                // Limit the buffer size based on the remaining bytes to transfer
                buffer.limit((int) Math.min(buffer.capacity(), fileSize - position));

                // Read from source channel into the buffer
                int bytesRead = sourceChannel.read(buffer, position).get();

                // Write from buffer to destination channel
                buffer.flip();
                int bytesWritten = destinationChannel.write(buffer, position).get();
                buffer.clear();

                // Update position and totalBytesTransferred based on bytes read and written
                position += bytesRead;
                totalBytesTransferred += bytesWritten;
                allByteProgress += bytesWritten;

                long finalTotalBytesTransferred = totalBytesTransferred;
                cachedThreadPool.submit(()->{
                    double taskProgress = (double) finalTotalBytesTransferred / (double) fileSize;
                    console.setTaskProgress(taskProgress);
                    task.setProgress(taskProgress);
                    console.setProgress(getCombinedProgress(getIndexedTasks())*100);
                });
            }

            // Close channels after transfer completion
            sourceChannel.close();
            destinationChannel.close();
            console.setProgress((double) (taskID/totalTaskQty));
        } catch (IOException | InterruptedException | ExecutionException e) {
            console.append("ERROR: An error occurred while syncing " + parentFile.getName() + " -> " + e.getMessage(), ERROR_TEXT_COLOR);
        }
    }

    private void readFromSourceChannel(AsynchronousFileChannel sourceChannel, ByteBuffer buffer, long position, AsynchronousFileChannel destinationChannel) {
        sourceChannel.read(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer attachment) {
                if (bytesRead > 0) {
                    attachment.flip();
                    writeToDestinationChannel(sourceChannel, destinationChannel, attachment, position);
                } else {
                    try {
                        sourceChannel.close();
                        destinationChannel.close();
                    } catch (IOException e) {
                        console.append(e.getMessage(),ERROR_TEXT_COLOR);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                // Handle read failure
            }
        });
    }

    private void writeToDestinationChannel(AsynchronousFileChannel sourceChannel, AsynchronousFileChannel destinationChannel, ByteBuffer buffer, long position) {
        destinationChannel.write(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.clear();
                long newPosition = position + result; // Update position after write
                readFromSourceChannel(sourceChannel, attachment, newPosition, destinationChannel);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                // Handle write failure
            }
        });
    }

    public ArrayList<SyncTask> getIndexedTasks(){
        return this.indexer.tasksFound;
    }

    public void setBufferSize(int size){
        byteTransfer = size;
    }

    public int getBufferSize(){
        return byteTransfer;
    }

    /**
     * Returns the byte size of all files and subfiles inside a given directory through recursion of this method.
     * @param directory the starting directory to sum byte sizes
     * @throws NullPointerException
     */
    public static long folderSize(File directory) throws NullPointerException {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public void revealInFileBrowser(String filePath) throws IOException {
        File folder = new File(filePath);
        if (folder.exists() && folder.isDirectory()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(folder);
            } catch (UnsupportedOperationException uoe) {
                console.append("ERROR: YOUR CURRENT PLATFORM DOES NOT SUPPORT THIS ACTION.\n");
            }
        } else {
            console.append("Invalid folder path: " + filePath + "\n", ERROR_TEXT_COLOR);
        }
    }

    public double getCombinedProgress(ArrayList<SyncTask> tasks){
        int taskQty = tasks.size();

        double progSum = 0;
        for( SyncTask task : tasks ){
            progSum += task.getProgress();
        }

        return progSum / Double.valueOf( taskQty );
    }

    public class FileIndexer {

        private boolean fileOverwriteEnabled = false;
        private int filesScanned = 0;
        private ArrayList<SyncTask> tasksFound;
        private OperatingSystem userOS;
        private File master, local;
        private String localRootName, masterRootName;
        private ConsolePane console;
        private int taskNum = 0;
        boolean debug = true;

        public FileIndexer(OperatingSystem os,File parent, File local, ConsolePane consolePane){
            tasksFound = new ArrayList<>();
            this.userOS = os;
            this.tasksFound = new ArrayList<>();
            this.master = parent;
            this.local = local;
            this.console = consolePane;
            this.localRootName = local.getName();
            this.masterRootName = master.getName();
        }

        public long index(){
            allTaskSizes = 0;
            taskNum = 0;
            indexLocal(local);
            indexMaster(master);
            for (int i = 0; i < tasksFound.size(); i++) {
                allTaskSizes += tasksFound.get(i).getFileSize();
            }
            return allTaskSizes;
        }

        private void indexLocal(File dir){
            try {
                if (dir.isDirectory()) {
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        indexLocal(file);
                    }
                } else {
                    File masterFile = new File(master, getFilePathFromRoot(dir, localRootName));
                    Path masterPath = masterFile.toPath();

                    if (!masterFile.exists()) {
                        SyncTask newTask = new SyncTask(dir, masterPath,++taskNum);
                        tasksFound.add(newTask);
                    } else if (masterFile.exists() && dir.exists() && fileOverwriteEnabled) {
                        // Check if local file size is larger than master file size
                        if (dir.length() > masterFile.length()) {
                            SyncTask newTask = new SyncTask(dir, masterPath,++taskNum);
                            tasksFound.add(newTask);
                        }
                    }
                }
            } catch (NullPointerException e) {
                // Handle null pointer exception
                console.append("ERROR: NullPointerException occurred while indexing local directory: " + e.getMessage(), ERROR_TEXT_COLOR);
            } catch (SecurityException e) {
                // Handle security exception
                console.append("ERROR: SecurityException occurred while indexing local directory: " + e.getMessage(), ERROR_TEXT_COLOR);
            }
        }

        private void indexMaster(File dir){
            try {
                if (dir.isDirectory()) {
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        indexMaster(file);
                    }
                } else {
                    File localFile = new File(local, getFilePathFromRoot(dir, masterRootName));
                    Path localPath = localFile.toPath();

                    if (!localFile.exists()) {
                        SyncTask newTask = new SyncTask(dir, localPath,++taskNum);
                        tasksFound.add(newTask);
                    } else if (localFile.exists() && dir.exists() && fileOverwriteEnabled) {
                        // Check if master file size is larger than local file size
                        if (dir.length() < localFile.length()) {
                            SyncTask newTask = new SyncTask(dir, localPath,++taskNum);
                            tasksFound.add(newTask);
                        }
                    }
                }
            } catch (NullPointerException e) {
                // Handle null pointer exception
                console.append("ERROR: NullPointerException occurred while indexing master directory: " + e.getMessage(), ERROR_TEXT_COLOR);
            } catch (SecurityException e) {
                // Handle security exception
                console.append("ERROR: SecurityException occurred while indexing master directory: " + e.getMessage(), ERROR_TEXT_COLOR);
            }
        }

        private String getFilePathFromRoot(File file, String rootKeyword){
            if(!file.exists()){

            }

            String path = file.getPath();
            String extractedPath = path.substring(
                    path.indexOf(rootKeyword)+rootKeyword.length() + 1,
                    path.length()
            );
            return extractedPath;
        }

        public void setFileOverwriteEnabled(boolean status){
            this.fileOverwriteEnabled = status;
        }
    }

    public SyncTask instantiateTask(File parent, Path childPath){
        return new SyncTask(parent,childPath);
    }

    public class SyncTask {

        private File parent;
        private Path childPath;
        private String debugID = "--";

        double progress;

        public SyncTask(File parent, Path childPath) {
            this.parent = parent;
            this.childPath = childPath;
        }

        public SyncTask(File parent, Path childPath, int debugID) {
            this.parent = parent;
            this.childPath = childPath;
            this.debugID = String.valueOf(debugID);
        }

        public double getSize() {
            return parent.length();
        }

        public void setParent(File parent) {
            this.parent = parent;
        }

        public void setChildPath(Path childPath) {
            this.childPath = childPath;
        }

        public Path getChildPath() {
            return this.childPath;
        }

        public File getParentFile() {
            return this.parent;
        }

        public String toString() {
            return parent.getPath() + "\t→\t " + childPath;
        }

        double getProgress(){
            return this.progress;
        }

        void setProgress(double d){
            this.progress = d;
        }

        public String getID(){
            return this.debugID;
        }
        public long getFileSize() {
            try {
                return Files.size(parent.toPath());
            } catch (IOException e) {
                console.append(e.getMessage()+"\n",Color.RED);
            }
            return -10L;
        }
    }


}
