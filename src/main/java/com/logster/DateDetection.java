package com.logster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import static com.logster.SettingsDialog.KEY_DATE_FORMATS;


public class DateDetection implements PreferenceChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String defaultValue="";

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        loadPreference();
    }

    record DateFormat(DateTimeFormatter formatter, int length) {
    }
    final List<DateFormat> formatterList =new ArrayList<>();


    public DateDetection() throws IOException {
        defaultValue =  Util.readResource("date-formats.csv");
        loadPreference();
    }
    public void parseFormat(String line){
        try{
            if(line!=null){
                int p= line.indexOf(",");
                int length = Integer.parseInt(line.substring(0,p));
                String text  = line.substring(p+1);
                LOGGER.info("Adding new date {} {}",text,length);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(text);
                DateFormat dateFormat = new DateFormat(formatter,length);
                formatterList.add(dateFormat);
            }
        }catch (Exception e){
            LOGGER.error(e);
        }

    }

    public LocalDateTime parseLine(String line){
        for(DateFormat dateFormat :formatterList){
            try{
                String datePart = line.substring(0,dateFormat.length);

                return LocalDateTime.parse(datePart, dateFormat.formatter);
            }catch (Exception _){}
        }
        return null;
    }

    private void loadPreference() {
        String text = SettingsDialog.prefs.get(KEY_DATE_FORMATS, defaultValue);
        text.lines().forEach(this::parseFormat);
    }

}
