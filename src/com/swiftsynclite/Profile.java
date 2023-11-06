package com.swiftsynclite;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;

public class Profile implements Serializable {

    private String profileName;
    private String localDirPath, masterDirPath;
    private Mode mode;

    public Profile(String localDir, String masterDir){
        this("unnamed profile",localDir,masterDir,Mode.DEFAULT);
    }
    public Profile(String name, String localDir, String masterDir, Mode mode){
        this.profileName = name;
        this.localDirPath = localDir;
        this.masterDirPath = masterDir;
        this.mode = mode;
    }

    public File getLocal(){
        File fetched = new File(localDirPath);
        return fetched;
    }

    public File getMaster(){
        File fetched = new File(masterDirPath);
        return fetched;
    }

    public String getProfileName(){
        return this.profileName;
    }

    public Mode getMode(){
        return this.mode;
    }
    void setMode(Mode mode){
        this.mode = mode;
    }

    public enum Mode {
        DEFAULT,
        SWIFTSYNC,
        NIO2;

        static String getDescription(Mode m){
            if(m==DEFAULT){
                return "DefaultSync is a basic and most common method for copying files, " +
                        "suitable for everyday use and small to medium-sized transfers where " +
                        "simplicity is key.";
            }
            if(m==NIO2){
                return "NIO2Sync offers improved performance for medium-sized transfers by " +
                        "efficiently handling file operations, making it ideal for scenarios " +
                        "requiring a balance between speed and simplicity.";
            }
            if(m==SWIFTSYNC){
                return "SwiftSync provides the fastest file transfers, especially for large files " +
                        "and multiple concurrent operations, making it perfect for large-scale data " +
                        "backups and time-sensitive tasks.";
            }
            return "not supported";
        }
    }

}
