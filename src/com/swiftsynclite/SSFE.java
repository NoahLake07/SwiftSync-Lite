package com.swiftsynclite;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import static com.swiftsynclite.OperatingSystem.MACOS;
import static com.swiftsynclite.SwiftSyncLITE.ERROR_TEXT_COLOR;

public class SSFE {

    private SwiftSyncLITE app;
    private ConsolePane console;
    private FileIndexer indexer;
    private Boolean stopReference;

    public SSFE(SwiftSyncLITE app){
        this.app = app;
    }

    public void setConsolePane(ConsolePane cp){
        this.console = cp;
    }

    public void createIndexer(OperatingSystem os,File parent, File local, ConsolePane consolePane, Boolean stop){
        this.indexer = new FileIndexer(os,parent,local,consolePane);
        this.stopReference = stop;
    }

    public void startIndexing(){
        this.indexer.index();
    }

    public void sync(ArrayList<SyncTask> tasks,Boolean stopReference){
        if(app.getSyncMode() == Profile.Mode.DEFAULT){
            double progress = 0;
            for (int i = 0; i < tasks.size(); i++) {
                defaultSync(tasks.get(i));
                progress = (double) i /tasks.size();
                console.setProgress(progress);
                if(stopReference){
                    console.append("Sync process stopped.", Color.ORANGE);
                    stopReference = false;
                    return;
                }
            }
        }
    }

    private void defaultSync(SyncTask task){
        File childFile = new File(String.valueOf(task.getChildPath()));
        File parentFile = task.getParentFile();

        try {
            Files.createDirectories(task.getChildPath().getParent());
            Files.createFile(task.getChildPath());
            FileInputStream fis = new FileInputStream(parentFile);
            FileOutputStream fos = new FileOutputStream(childFile);
            byte[] buffer = new byte[1024]; // 1KB at a time
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
        } catch (FileNotFoundException e) {
            console.append("ERROR: A FileNotFoundException occurred while syncing " + parentFile.getName() + " -> " + e.getMessage(), ERROR_TEXT_COLOR);
        } catch (IOException e) {
            console.append("ERROR: An IOException occurred while syncing " + parentFile.getName() + " -> "  + e.getMessage(), ERROR_TEXT_COLOR);
        }
    }


    public ArrayList<SyncTask> getIndexedTasks(){
        return this.indexer.tasksFound;
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

    public class FileIndexer {

        private boolean fileOverwriteEnabled = false;
        private int filesScanned = 0;
        private ArrayList<SyncTask> tasksFound;
        private OperatingSystem userOS;
        private File master, local;
        private String localRootName, masterRootName;
        private ConsolePane console;
        private int taskNum = 0;

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

        public void index(){
            taskNum = 0;
            indexLocal(local);
            indexMaster(master);
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

    public class SyncTask {

        private File parent;
        private Path childPath;
        private String debugID = "--";

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

        public String getID(){
            return this.debugID;
        }
    }


}
