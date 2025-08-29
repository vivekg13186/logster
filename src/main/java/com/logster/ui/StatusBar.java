package com.logster.ui;

import javax.swing.*;

import java.awt.*;

import static com.logster.Util.padding;
import static com.logster.Util.rows;
import static com.logster.ui.Icons.*;

public class StatusBar extends JPanel {



    private final JLabel statusLabel = new JLabel();

    private final JLabel statusIconLabel = new JLabel(thumbsUpIcon);

    private Runnable onSearchCancel=null;

    public  enum State{
        SEARCH_CANCELLED,
        IN_PROGRESS,
        MAX_SEARCH_RESULT,
        SEARCH_COMPLETED
    }       final JButton stopSearchBtn = new JButton(stopIcon);
    public StatusBar(){

        setLayout(new BorderLayout());


        add(rows(statusIconLabel,stopSearchBtn,statusLabel  ),BorderLayout.CENTER);
        padding(this,5);
        stopSearchBtn.addActionListener((_)->{
            if(onSearchCancel!=null)onSearchCancel.run();
        });
        stopSearchBtn.setEnabled(false);
        stopSearchBtn.setToolTipText("Stop search");
    }

    public void setStatus(String message){
        statusLabel.setText(message);
    }

    public void setState(State state){
        stopSearchBtn.setEnabled(false);
        switch (state){
            case IN_PROGRESS ->  {
                statusIconLabel.setIcon(searchingIcon);
                statusIconLabel.setToolTipText("searching files");
                stopSearchBtn.setEnabled(true);
            }
            case SEARCH_CANCELLED -> {
                statusIconLabel.setToolTipText("search cancelled");
                statusIconLabel.setIcon(cancelIcon);
            }
            case SEARCH_COMPLETED -> {
                statusIconLabel.setToolTipText("search completed");
                statusIconLabel.setIcon(thumbsUpIcon);
            }
            case MAX_SEARCH_RESULT ->{
                statusIconLabel.setToolTipText("search reached max result");
                statusIconLabel.setIcon(limitIcon);
            }
        }
    }



    public void setOnSearchCancel(Runnable run){
        this.onSearchCancel= run;
    }

}
