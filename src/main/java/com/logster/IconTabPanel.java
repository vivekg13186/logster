package com.logster;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;


import javax.swing.*;
import java.awt.*;

public class IconTabPanel   extends JPanel {

    private JPanel tabHeader(String title,Icon preIcon){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBackground(new Color(0,0,0,0));
        JLabel label  =new JLabel(title);
        label.setOpaque(false);
        label.setBackground(new Color(0,0,0,0));
        panel.add(new JLabel(preIcon),BorderLayout.WEST);
        label.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        panel.add(label,BorderLayout.CENTER);

        return panel;
    }



    public IconTabPanel( JTabbedPane tabbedPane,String title,Icon icon){

        tabbedPane.add(this);
        int index = tabbedPane.getTabCount()-1;
        tabbedPane.setTabComponentAt(index,tabHeader(title,icon));
        tabbedPane.setSelectedIndex(index);
    }
}
