package com.logster;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Util {

    public static String readResource(String name) throws IOException {
        try (InputStream is = Util.class.getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + name);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    public static void setLineColWidth(JTable table){
        int charWidth = table.getFontMetrics(table.getFont()).charWidth('0'); // width of a single char
        int colWidth = charWidth * 7; // 10 characters
        table.getColumnModel().getColumn(0).setPreferredWidth(colWidth);
        table.getColumnModel().getColumn(0).setMinWidth(colWidth);
        table.getColumnModel().getColumn(0).setMaxWidth(colWidth);
    }

    public static void setTableRenderer(JTable table, TableCellRenderer cellRenderer){
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }
    static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
    static JPanel columns(JComponent... components){
        JPanel panel=  new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        for (JComponent component : components) {
            panel.add(component);
            component.setAlignmentY(Component.CENTER_ALIGNMENT);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    static JPanel rows(JComponent... components){
        JPanel panel=  new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.X_AXIS);
        panel.setLayout(layout);
        for (JComponent component : components) {
            component.setAlignmentY(Component.CENTER_ALIGNMENT);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(component);
            panel.add(Box.createRigidArea(new Dimension(10, 0)));
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }


    public static long toEpochMilli(LocalDateTime timestamp){
        return timestamp.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

}
