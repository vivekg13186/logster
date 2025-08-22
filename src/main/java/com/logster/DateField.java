package com.logster;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateField extends JFormattedTextField {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");
    public DateField(){
        MaskFormatter mask = null;
        try {
            mask = new MaskFormatter("##:##:#### ##:##:##");
            mask.setPlaceholderCharacter('#'); // show underscores for empty fields
        } catch ( ParseException _) {

        }

         this.setFormatter(mask);

        setColumns(16);

          setPreferredSize(new Dimension(100, getPreferredSize().height));
         setMaximumSize(new Dimension(100,Short.MAX_VALUE));


        getDocument().addDocumentListener((SimpleDocumentListener) () -> {
            if (getDate() != null) {
                setBackground(Color.WHITE);
            } else {
                setBackground(new Color(255, 102, 102)); // light red
            }
        });
    }




    public   void setDate(  LocalDateTime date) {
        if (date != null) {
            setText(date.format(FORMATTER));
        } else {
            setText("");
        }
    }


    public   LocalDateTime getDate( ) {
        String text = getText();
        if (text.contains("#")) return null;
        try {
            return LocalDateTime.parse( getText(), FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();
        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }
}
