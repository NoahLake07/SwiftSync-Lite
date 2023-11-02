package com.swiftsynclite.registry;

import java.util.prefs.Preferences;

public class RegistrySetup {

    private static void setFileAssociation(String fileExtension, String jarFilePath) {

        try {
            // Use Preferences.userRoot() for current user or Preferences.systemRoot() for all users
            Preferences userRoot = Preferences.userRoot();
            Preferences node = userRoot.node("Software\\Classes\\" + fileExtension);
            node.put("", "SwiftSyncLITE.FileType");

            // Modify this line to point to your Java executable and JAR file
            String command = "java -jar \"" + jarFilePath + "\" %1";

            Preferences applicationNode = userRoot.node("Software\\Classes\\SwiftSyncLITE.FileType\\shell\\open\\command");
            applicationNode.put("", command);

            // Notify the system of the changes
            Runtime.getRuntime().exec("cmd /c assoc " + fileExtension.substring(1) + "=SwiftSyncLITE.FileType");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
