package com.logster;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HigilightTest {

    public static void main(String[] arg){
        String test="081109 213618 32 INFO dfs.FSNamesystem: BLOCK* NameSystem.allocateBlock: /user/root/rand/_temporary/_task_200811092030_0001_m_002010_0/part-02010. blk_-5081117124755398613";
        Pattern pattern = Pattern.compile(" 08111\\d");
        Matcher matcher = pattern.matcher(test);
        ArrayList<MatchPosition> matchPositions = new ArrayList<>();
        while(matcher.find()){
            matchPositions.add(new MatchPosition(matcher.start(), matcher.end()));
            System.out.println(matcher.start()+":"+matcher.end());
        }
        JTextPane textPane = createHighlightedTextPane(test, matchPositions);
        JFrame r = new JFrame("asdasd");
        r.getContentPane().add(textPane);
        r.setSize(100,100);
        r.setVisible(true);

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
