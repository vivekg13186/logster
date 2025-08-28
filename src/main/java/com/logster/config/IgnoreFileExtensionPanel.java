package com.logster.config;

import com.logster.ClosableTabPanel;


import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.prefs.Preferences;

import static com.logster.Util.padding;
import static com.logster.ui.Icons.extensionIcon;

public class IgnoreFileExtensionPanel extends ClosableTabPanel {
    final Preferences preferences  = Preferences.userNodeForPackage(IgnoreFileExtensionPanel.class);
    public IgnoreFileExtensionPanel(JTabbedPane tabbedPane ) {
        super(tabbedPane, "Exclude File Types", extensionIcon);
        setLayout(new BorderLayout());

        JTextArea textArea =new JTextArea();
        add(textArea,BorderLayout.CENTER);
        padding(this,10);
        if(AppConfiguration.ignoreFileExtension.isEmpty()) {
            textArea.setText(preferences.get("IgnoreFileExtensionPanel",""));
        }else {
            textArea.setText(String.join(", ", AppConfiguration.ignoreFileExtension));
        }
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                validateAndUpdate(textArea.getText());
            }
        });
    }
    public void  validateAndUpdate(String text){
        if(text!=null){
            String[] extensions =text.split(",");
            if(extensions.length>0) {
                AppConfiguration.ignoreFileExtension.clear();
                AppConfiguration.ignoreFileExtension.addAll(Arrays.asList(extensions));
            }
            preferences.put("IgnoreFileExtensionPanel",text);
        }
    }
}
