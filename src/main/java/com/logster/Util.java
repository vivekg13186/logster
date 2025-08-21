package com.logster;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

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
}
