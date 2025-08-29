package com.logster.search;

import com.logster.config.AppConfiguration;
import com.logster.config.DateFormatPattern;

import java.time.LocalDateTime;
import java.util.regex.Matcher;


public class DateDetection  {

    private DateDetection(){}

    public static final DateDetection dateDetection = new DateDetection();





    public LocalDateTime parseLine(String line){

        for(DateFormatPattern dateFormat : AppConfiguration.dateFormats){
            try{
                Matcher matcher = dateFormat.regExp().matcher(line);
                 if(matcher.find()){
                     String datePart  = matcher.group(1);
                     return LocalDateTime.parse(datePart, dateFormat.dateFormat());
                 }
            }catch (Exception _){

            }
        }

        return null;
    }




}
