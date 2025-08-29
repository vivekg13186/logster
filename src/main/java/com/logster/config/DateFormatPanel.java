package com.logster.config;

import com.logster.ClosableTabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.format.DateTimeFormatter;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static com.logster.Util.padding;
import static com.logster.ui.Icons.dateIcon;


public class DateFormatPanel extends ClosableTabPanel {
    final JTextArea textArea =new JTextArea();

    public DateFormatPanel(JTabbedPane tabbedPane ) {
        super(tabbedPane, "Date formats", dateIcon);
        setLayout(new BorderLayout());


        add(textArea,BorderLayout.CENTER);
        padding(this,10);
        setText();
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
            String[] extensions =text.trim().split("\n");
            if(extensions.length%2==0){
                AppConfiguration.dateFormats.clear();
                for(int i=0;i<extensions.length;i++){
                    Pattern regexp = Pattern.compile(extensions[i++]);
                    DateTimeFormatter formatter  = DateTimeFormatter.ofPattern(extensions[i]);
                    AppConfiguration.dateFormats.add(new DateFormatPattern(regexp,formatter));
                }
            }

        }
    }

    public void setText(){
        StringBuilder stringBuilder = new StringBuilder();
        for(DateFormatPattern d:AppConfiguration.dateFormats){
            stringBuilder.append(d.regExp().toString()).append("\n");
            stringBuilder.append(d.dateFormat().toString()).append("\n");
        }
        textArea.setText(stringBuilder.toString());

    }
}
