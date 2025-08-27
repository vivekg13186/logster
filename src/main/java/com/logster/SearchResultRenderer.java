package com.logster;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import java.awt.*;

public class SearchResultRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(value instanceof SearchResult searchResult){
            return createHighlightedTextPane(searchResult.getLineContent(),searchResult.matchPositions);
        }
        return new JLabel("");
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
            int start = range.start;
            int end = range.end;
            try {
                doc.setCharacterAttributes(start, end - start, highlightStyle, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return textPane;
 }

}
