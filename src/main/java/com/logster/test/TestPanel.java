package com.logster.test;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.logster.ClosableTabPanel;
import com.logster.Util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.logster.Util.*;
import static com.logster.ui.Icons.labIcon;

public class TestPanel extends ClosableTabPanel
{
    DateTimeFormatter standardFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    JTextField datePattern  =new JTextField("EEE MMM d HH:mm:ss yyyy",100);
    JTextField convertedDateValue = new JTextField(100);
    JTextField matchedDateString = new JTextField(100);
    JButton gen=new JButton("Test");
    JTextField input = new JTextField(" [Sun Dec 04 07:11:05 2005] [notice] workerEnv.init() ok",100);
    JTextField regExp =new JTextField("\\[(\\w\\w\\w \\w\\w\\w \\d\\d \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d)\\]",100);

    JTextPane textPane = new JTextPane();


    public TestPanel(JTabbedPane tabbedPane ){
        super(  tabbedPane,  "Test",labIcon);
        convertedDateValue.setEditable(false);
        matchedDateString.setEditable(false);
        FontMetrics met = textPane.getFontMetrics(textPane.getFont());
        textPane.setPreferredSize(new Dimension(met.charWidth('m')*101, met.getHeight() + 7));
        textPane.setMaximumSize(textPane.getPreferredSize());
        textPane.setEditable(false);

textPane.setBorder(BorderFactory.createLineBorder(Color.lightGray,1));

        gen.addActionListener((_)->{
            try{
                Pattern pattern = Pattern.compile(datePattern.getText());
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern.getText());
                Matcher matcher = pattern.matcher(input.getText());
                if(matcher.find()){
                    String datePart  = matcher.group();
                    LocalDateTime myDate = LocalDateTime.parse(datePart, dateTimeFormatter);
                    convertedDateValue.setText(myDate.format(standardFormat));
                }

            } catch (Exception e) {
                 convertedDateValue.setText(e.getMessage());
            }
        });


        JPanel panel =
                columns(15,
                        formField("Enter input text",input),
                        formField("Text highlighted by regular expression",textPane),
                        formField("Date string matched by regular expression",matchedDateString),
                        formField("Date value parsed from string",convertedDateValue),
                        formField("Enter regular expression to highlight text",regExp),
                        formField("Enter date format to parse string",datePattern)

                        );


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
                        String dateString  = matcher.group(1);
                        matchedDateString.setText(dateString);
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
                        String dateString  = matchedDateString.getText();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern.getText());
                        LocalDateTime myDate = LocalDateTime.parse(dateString, dateTimeFormatter);
                        convertedDateValue.setText( myDate.format(standardFormat));
                    }catch (Exception _){

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
