package com.swiftsynclite;

import javax.swing.*;
public class SettingsPane extends DefaultPane {
    private SwiftSyncLITE.Controller parentApp;

    SettingsPane(SwiftSyncLITE.Controller parentApp) {
        super("System Settings");
        this.parentApp = parentApp;
        this.header.setHorizontalAlignment(SwingConstants.LEFT);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    }
}
