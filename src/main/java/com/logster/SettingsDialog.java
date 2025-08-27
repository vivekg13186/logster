package com.logster;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.prefs.Preferences;

public class SettingsDialog extends JPanel {

    public static final String KEY_EXTENSION="extensions";
    public static final String KEY_DATE_FORMATS="date-formats";
    public static final   Preferences prefs = Preferences.userNodeForPackage(com.logster.Logster.class);
    private static final Logger LOGGER = LogManager.getLogger();

    public SettingsDialog() throws IOException {

        loadIcon();

        String default_date_formats  = Util.readResource("date-formats.csv");
        String extensions = prefs.get(KEY_EXTENSION,".log,.txt");
        String date_formats = prefs.get(KEY_DATE_FORMATS,default_date_formats);

        JTextArea textArea =new JTextArea(extensions);
        JTextArea dateFormatsTA =new JTextArea(date_formats);
        JButton saveButton  =new JButton("Save");
        JButton cancelButton  =new JButton("Cancel");
        cancelButton.addActionListener(_->setVisible(false));
        saveButton.addActionListener(_->{
            prefs.put(KEY_EXTENSION,textArea.getText());
            prefs.put(KEY_DATE_FORMATS,dateFormatsTA.getText());
            setVisible(false);
        });
        JLabel label1 = new JLabel("File extensions to index(comma separated)");
        JLabel label2 = new JLabel("Date formats (length of date string,use java.text.SimpleDateFormat)");
        JLabel label3 = new JLabel("Log level");

        final JComboBox<String> logLevel = getStringJComboBox();
        JPanel buttonPanel = Util.rows(saveButton,cancelButton);
        JPanel mainPanel = Util.columns(label1,new JScrollPane(textArea),label2,new JScrollPane(dateFormatsTA),label3,logLevel,buttonPanel);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(mainPanel);
        //setTitle("Settings");
        setSize(400,400);
    }

    public   void loadIcon(){

        try (InputStream is = Logster.class.getClassLoader().getResourceAsStream("icons/logo.png")) {
            if (is == null) {
                throw new IllegalStateException("Font not found!");
            }

            //setIconImage(ImageIO.read(is));
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
    private static JComboBox<String> getStringJComboBox() {
        String[] choices = { "ERROR","INFO", "ALL"};

        final JComboBox<String> logLevel = new JComboBox<>(choices);
        logLevel.addActionListener((_)->{
            int i =logLevel.getSelectedIndex();
            Level level=Level.ERROR;
            if(i==1){
                level  = Level.INFO;
            }
            if(i==2){
                level = Level.ALL;
            }
            Configurator.setLevel("com.logster",level);
        });
        logLevel.setPreferredSize(new Dimension(200, 30));
        logLevel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        return logLevel;
    }
}
