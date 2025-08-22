package com.logster;

import com.formdev.flatlaf.extras.FlatSVGIcon;


import javax.swing.*;
import java.awt.*;


public class ClosableTabPanel extends JPanel {

    private JPanel tabHeader(String title){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBackground(new Color(0,0,0,0));
        Icon icon =new FlatSVGIcon("icons/close.svg", 16, 16);
        JButton button = new JButton(icon);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.addActionListener(_ -> closeTab());
        button.setBackground(new Color(0,0,0,0));
        JLabel label  =new JLabel(title);
        label.setOpaque(false);
        label.setBackground(new Color(0,0,0,0));
        panel.add(label,BorderLayout.CENTER);
        panel.add(button,BorderLayout.EAST);
        return panel;
    }

    final JTabbedPane tabbedPane ;
    private void closeTab(){
        tabbedPane.remove(this);
    }
    public ClosableTabPanel( JTabbedPane tabbedPane,String title){
        this.tabbedPane= tabbedPane;
        tabbedPane.add(this);
        int index = tabbedPane.getTabCount()-1;
        tabbedPane.setTabComponentAt(index,tabHeader(title));
        tabbedPane.setSelectedIndex(index);
    }
}
