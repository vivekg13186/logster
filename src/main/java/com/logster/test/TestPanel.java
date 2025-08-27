package com.logster.test;

import com.logster.Util;
import com.logster.search.MatchPosition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPanel extends JPanel
{
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    JTextPane textPane = new JTextPane();
    public TestPanel(){
        JTextArea regExp =new JTextArea("\\[(\\w\\w\\w \\w\\w\\w \\d\\d \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d)\\]");


        JTextArea datePattern  =new JTextArea("EEE MMM d HH:mm:ss yyyy");
        JLabel result = new JLabel();
        JButton gen=new JButton("Test");
        JTextArea input = new JTextArea(" [Sun Dec 04 07:11:05 2005] [notice] workerEnv.init() ok /etc/httpd/conf/workers2.properties");
        gen.addActionListener((_)->{
            try{
                Pattern pattern = Pattern.compile(datePattern.getText());
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern.getText());
                Matcher matcher = pattern.matcher(input.getText());
                if(matcher.find()){
                    String datePart  = matcher.group();
                    LocalDateTime myDate = LocalDateTime.parse(datePart, dateTimeFormatter);
                    result.setText(myDate.format(formatter));
                }

            } catch (Exception e) {
                 result.setText(e.getMessage());
            }
        });


        JPanel panel = Util.columns(
                Util.rows(
                        Util.columns(new JLabel("Input text"),input),
                        Util.columns(new JLabel("Match"),textPane)
                        ),
                Util.columns(new JLabel("Regular Expression"),regExp),
        Util.columns(new JLabel("Data Format"),datePattern),
        Util.rows(result,gen));
        Util.padding(panel,10);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        regExp.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                try{
                    Pattern pattern = Pattern.compile(regExp.getText());
                    Matcher matcher=pattern.matcher(input.getText());
                    if(matcher.find()){
                        textPane.setText(input.getText());
                        highlight(matcher.start(),matcher.end());
                    }else{
                        textPane.setText("no");
                    }}catch (Exception eee){
                    textPane.setText(eee.getMessage());
                }
            }
        });
        datePattern.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                try{
                    Pattern pattern = Pattern.compile(regExp.getText());
                    Matcher matcher=pattern.matcher(input.getText());
                    if(matcher.find()){
                        String dateString  = matcher.group(1);
                        result.setText("Date String "+dateString);
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern.getText());
                        LocalDateTime myDate = LocalDateTime.parse(dateString, dateTimeFormatter);
                        result.setText("Date String "+dateString +" Formatted date "+myDate.format(formatter));

                    }else{
                        result.setText("no");
                    }}catch (Exception eee){
                    result.setText(eee.getMessage());
                }
            }
        });
    }


    private void highlight(int start ,int end){
        StyledDocument doc = textPane.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet highlightStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, Color.YELLOW);


            try {
                doc.setCharacterAttributes(start, end - start, highlightStyle, false);
            } catch (Exception _) {

            }
        }

}
