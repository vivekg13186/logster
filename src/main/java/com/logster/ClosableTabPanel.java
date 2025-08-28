package com.logster;


import javax.swing.*;
import java.awt.*;

import static com.logster.ui.Icons.closeIcon;


public class ClosableTabPanel extends JPanel {

    private JPanel tabHeader(String title,Icon preIcon){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBackground(new Color(0,0,0,0));


        JButton button = new JButton(closeIcon);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.addActionListener(_ -> closeTab());
        button.setBackground(new Color(0,0,0,0));
        JLabel label  =new JLabel(title);
        label.setOpaque(false);
        label.setBackground(new Color(0,0,0,0));
        panel.add(new JLabel(preIcon),BorderLayout.WEST);
        label.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        panel.add(label,BorderLayout.CENTER);
        panel.add(button,BorderLayout.EAST);
        return panel;
    }

    final JTabbedPane tabbedPane ;
    private void closeTab(){
        tabbedPane.remove(this);
    }
    public ClosableTabPanel( JTabbedPane tabbedPane,String title,Icon icon){
        this.tabbedPane= tabbedPane;
        tabbedPane.add(this);
        int index = tabbedPane.getTabCount()-1;
        tabbedPane.setTabComponentAt(index,tabHeader(title,icon));
        tabbedPane.setSelectedIndex(index);
    }
}
