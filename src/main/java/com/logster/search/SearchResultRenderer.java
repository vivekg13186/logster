package com.logster.search;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import java.awt.*;

public class SearchResultRenderer extends DefaultTableCellRenderer {
    public final Color lineColor = new Color(0,0,0,30);
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if(value instanceof SearchResult searchResult){
            if(isSelected)return c;
            return createHighlightedTextPane(searchResult.getLineContent(), searchResult.getMatchPositions());
        }
        if(!isSelected&& column==0){
                c.setBackground(lineColor);
        }
        return c;
    }
    private static JTextPane createHighlightedTextPane(String text, java.util.List<MatchPosition> ranges) {
        JTextPane textPane = new JTextPane();
        textPane.setText(text);
        textPane.setEditable(false);
        textPane.setBackground(Color.WHITE);

        StyledDocument doc = textPane.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet highlightStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, Color.YELLOW);

        for (MatchPosition range : ranges) {
            int start = range.start();
            int end = range.end();
            try {
                doc.setCharacterAttributes(start, end - start, highlightStyle, false);
            } catch (Exception _) {

            }
        }


        return textPane;
 }

}
