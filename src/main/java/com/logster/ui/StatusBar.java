package com.logster.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.logster.Util;

import javax.swing.*;

import java.awt.*;

import static com.logster.Util.padding;
import static com.logster.Util.rows;
import static com.logster.ui.Icons.*;

public class StatusBar extends JPanel {



    private final JLabel statusLabel = new JLabel();

    private final JLabel statusIconLabel = new JLabel(thumbsUpIcon);
    private final JProgressBar progressBar = new JProgressBar();
    private final  JButton stopSearchBtn =new JButton(stopIcon);
    private Runnable onSearchCancel=null;

    public  enum State{
        SEARCH_CANCELLED,
        IN_PROGRESS,
        MAX_SEARCH_RESULT,
        SEARCH_COMPLETED
    }
    public StatusBar(){

        setLayout(new BorderLayout());
        add(rows(statusIconLabel,statusLabel,progressBar,stopSearchBtn),BorderLayout.CENTER);
        padding(this,5);
        stopSearchBtn.addActionListener((_)->{
            if(onSearchCancel!=null)onSearchCancel.run();
        });
    }

    public void setStatus(String message){
        statusLabel.setText(message);
    }

    public void setState(State state){
        switch (state){
            case IN_PROGRESS ->  statusIconLabel.setIcon(searchingIcon);
            case SEARCH_CANCELLED -> statusIconLabel.setIcon(cancelIcon);
            case SEARCH_COMPLETED -> statusIconLabel.setIcon(thumbsUpIcon);
            case MAX_SEARCH_RESULT -> statusIconLabel.setIcon(limitIcon);
        }
    }

    public void setProgress(int progress){
        progressBar.setValue(progress);
    }

    public void setOnSearchCancel(Runnable run){
        this.onSearchCancel= run;
    }

}
