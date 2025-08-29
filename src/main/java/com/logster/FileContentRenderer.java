package com.logster;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class FileContentRenderer extends DefaultTableCellRenderer {
    public final long highlightLine;
    public final Color lineColor = new Color(0,0,0,30);

    public FileContentRenderer(long highlightLine) {
        this.highlightLine = highlightLine;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            int modelRow = table.convertRowIndexToModel(row);
            Object obj = table.getModel().getValueAt(modelRow, 0);
            int lineNumber;
            if (obj instanceof Number) {
                lineNumber = ((Number) obj).intValue();
            } else {
                try {
                    lineNumber = Integer.parseInt(String.valueOf(obj));
                } catch (NumberFormatException ex) {
                    lineNumber = -1;
                }
            }

            if (lineNumber == highlightLine) {
                c.setBackground(Color.YELLOW);
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(column == 0 ? lineColor : table.getBackground());
                c.setForeground(table.getForeground());
            }
        }
        return c;
    }
}
