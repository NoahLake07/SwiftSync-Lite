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

    public SSFE(SwiftSyncLITE app){
        this.app = app;
    }

    public void setConsolePane(ConsolePane cp){
        this.console = cp;
    }

    public void createIndexer(OperatingSystem os,File parent, File local, ConsolePane consolePane){
        this.indexer = new FileIndexer(os,parent,local,consolePane);
    }

    public void startIndexing(){
        this.indexer.index();
    }

    public void sync(ArrayList<SyncTask> tasks){
        if(app.getSyncMode() == Profile.Mode.DEFAULT){
            double progress = 0;
            for (int i = 0; i < tasks.size(); i++) {
                defaultSync(tasks.get(i));
                progress = (double) i /tasks.size();
                console.setProgress(progress);
            }
        }
    }

    private void defaultSync(SyncTask task){
        File childFile, parentFile;
        childFile = new File(String.valueOf(task.getChildPath()));
        parentFile = task.getParentFile();
        try (
                FileInputStream fis = new FileInputStream(parentFile);
                FileOutputStream fos = new FileOutputStream(childFile);
        ) {
            byte[] buffer = new byte[1024]; // 1KB at a time
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            console.append("ERROR: A FileNotFoundException occurred while syncing " + parentFile.getName() +" -> " + e.getMessage(),ERROR_TEXT_COLOR);
        } catch (IOException e) {
            console.append("ERROR: An IOException occurred while syncing " + parentFile.getName() +" -> " + e.getMessage(), ERROR_TEXT_COLOR);
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
            indexLocal(local);
            indexMaster(master);
        }

        private void index(File child, File parentSubDir){
            try {
                if (parentSubDir.isDirectory()) { // narrow all parent files down to their individual paths
                    for (int i = 0; i < parentSubDir.listFiles().length; i++) {
                        index(child, Objects.requireNonNull(parentSubDir.listFiles())[i]);
                    }
                } else {
                    // find the theoretical pathname of the file inside the child folder
                    String pathOfChildSubDir = child.getPath() + (userOS == MACOS ? "/" : "\\") + getFilePathFromRoot(parentSubDir, parentSubDir.getName());
                    Path childPath = Paths.get(pathOfChildSubDir);

                    if (!Files.exists(childPath)) {
                        SyncTask newTask = new SyncTask(parentSubDir,childPath);
                        tasksFound.add(newTask);
                    } else if (Files.exists(childPath) && parentSubDir.exists() && fileOverwriteEnabled) {
                        SyncTask newTask = new SyncTask(parentSubDir,childPath);
                        tasksFound.add(newTask);
                    }
                }
            } catch (NullPointerException e){
            }
        }

        private void indexLocal(File dir){
            try {
                if (dir.isDirectory()) {
                    for (int j = 0; j < dir.listFiles().length; j++) {
                        indexLocal(Objects.requireNonNull(dir.listFiles())[j]);
                    }
                } else {
                    String pathOfMasterFile = master.getPath() + (userOS == MACOS ? "/" : "\\") + getFilePathFromRoot(dir,localRootName);

                    // convert String to a Path object
                    Path masterPath = Paths.get(pathOfMasterFile);
                    boolean exists = Files.exists(masterPath);

                    // check to see if the file exists in the master directory
                    if(!exists){
                        // if it doesn't exist, add it to the list of tasks
                        SyncTask newTask = new SyncTask(dir,masterPath);
                        tasksFound.add(newTask);
                    } else if (Files.exists(masterPath) && dir.exists() && fileOverwriteEnabled){
                        File masterFile = new File(String.valueOf(masterPath));
                        // if it does exist and the local file is larger than the master file, overwrite it
                        if(dir.length()>masterFile.length()){
                            SyncTask newTask = new SyncTask(dir,masterPath);
                            tasksFound.add(newTask);
                        }
                    }
                }
            } catch (NullPointerException e){
            }
        }

        private void indexMaster(File dir){
            try {
                if (dir.isDirectory()) {
                    for (int j = 0; j < dir.listFiles().length; j++) {
                        indexMaster(Objects.requireNonNull(dir.listFiles())[j]);
                    }
                } else {
                    String pathOfLocalFile = local.getPath() + (userOS == MACOS ? "/" : "\\") + getFilePathFromRoot(dir,masterRootName);

                    // find the corresponding path for the local directory
                    Path localPath = Paths.get(pathOfLocalFile);
                    boolean exists = Files.exists(localPath);

                    // check to see if the file exists in the opposite directory
                    if(!exists){
                        // if it doesn't exist, add it to the list of tasks
                        SyncTask newTask = new SyncTask(dir,localPath);
                        tasksFound.add(newTask);
                    } else if (Files.exists(localPath) && dir.exists() && fileOverwriteEnabled){
                        File localFile = new File(String.valueOf(localPath));
                        // if it does exist and the local file is larger than the master file, overwrite it
                        if(dir.length()>localFile.length()){
                            SyncTask newTask = new SyncTask(dir,localPath);
                            tasksFound.add(newTask);
                        }
                    }
                }
            } catch (NullPointerException e){
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

        public SyncTask(File parent, Path childPath) {
            this.parent = parent;
            this.childPath = childPath;
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
    }


}
