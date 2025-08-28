package com.logster.config;

import com.logster.ClosableTabPanel;
import com.logster.IconTabPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import static com.logster.Util.padding;
import static com.logster.ui.Icons.extensionIcon;

public class IgnoreFileExtensionPanel extends ClosableTabPanel {
    public IgnoreFileExtensionPanel(JTabbedPane tabbedPane ) {
        super(tabbedPane, "Exclude File Types", extensionIcon);
        setLayout(new BorderLayout());

        JTextArea textArea =new JTextArea();
        add(textArea,BorderLayout.CENTER);
        padding(this,10);
        textArea.setText(String.join(", ", AppConfiguration.ignoreFileExtension));
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
        }
    }
}
