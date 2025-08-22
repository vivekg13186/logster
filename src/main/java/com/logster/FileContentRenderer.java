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

        int modelRow = table.convertRowIndexToModel(row);
        long lineNumber = (long) table.getModel().getValueAt(modelRow, 0);

        if (lineNumber == highlightLine) {
            c.setBackground(Color.YELLOW); // highlight color
            c.setForeground(Color.BLACK);
        } else {
            // Respect selection coloring
            if (isSelected) {
                c.setBackground( table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            } else {
                c.setBackground( table.getBackground());
                c.setForeground(table.getForeground());
            }
        }
        if(column==0){
            c.setBackground(lineColor);
        }


        return c;
    }
}
