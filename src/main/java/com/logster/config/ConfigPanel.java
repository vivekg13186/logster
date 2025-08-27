package com.logster.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logster.search.DateDetection;
import com.logster.Util;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.prefs.Preferences;

public class ConfigPanel extends JPanel {

    private static Config config ;
    public static final Preferences prefs = Preferences.userNodeForPackage(ConfigPanel.class);
    public ConfigPanel(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        setLayout(new BorderLayout());
        add(sp,BorderLayout.CENTER);
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener((_)->{
            config = gson.fromJson(textArea.getText(),Config.class);
            saveSettings();
            DateDetection.dateDetection.load(config.dateFormats());
        });
        add(Util.rows(saveBtn),BorderLayout.NORTH);
        loadSettings();

        textArea.setText(gson.toJson(config));
        DateDetection.dateDetection.load(config.dateFormats());
    }

    public void loadSettings(){
        String data = prefs.get("config","none");
        if(data.equals("none")){
            try {
                data = Util.readResource("./default_config.json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Gson gson = new Gson();
        config= gson.fromJson(data,Config.class);
    }

    public void saveSettings(){
        Gson gson = new Gson();
        prefs.put("config",gson.toJson(config));
    }

    public static Config getConfig(){
        return  config;
    }
}
