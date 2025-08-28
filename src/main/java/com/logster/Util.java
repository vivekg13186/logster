package com.logster;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Util {


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

    public static JPanel formField(String label,JComponent component){
        JPanel panel=  new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        panel.add(new JLabel(label));
            if (component instanceof JTextField) {
                component.setMaximumSize(component.getPreferredSize());
            }
            panel.add(component);
            component.setAlignmentY(Component.CENTER_ALIGNMENT);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        return panel;
    }


    public static JPanel columns(int spacing,JComponent... components){
        JPanel panel=  new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        for (JComponent component : components) {
            if (component instanceof JTextField) {
                component.setMaximumSize(component.getPreferredSize());
            }
            panel.add(component);
            component.setAlignmentY(Component.CENTER_ALIGNMENT);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(Box.createRigidArea(new Dimension(0, spacing)));
        }

        return panel;
    }
    public static JPanel rows(JComponent... components){
        JPanel panel=  new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.X_AXIS);
        panel.setLayout(layout);
        for (JComponent component : components) {
            if (component instanceof JTextField) {
                component.setMaximumSize(component.getPreferredSize());
            }
            component.setAlignmentY(Component.CENTER_ALIGNMENT);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(component);
            panel.add(Box.createRigidArea(new Dimension(10, 0)));
        }


        return panel;
    }


    public static void padding(JPanel panel,int pad){
        panel.setBorder(BorderFactory.createEmptyBorder(pad,pad,pad,pad));
    }
    public static long toEpochMilli(LocalDateTime timestamp){
        return timestamp.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

}
