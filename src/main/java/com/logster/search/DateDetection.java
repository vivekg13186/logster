package com.logster.search;

import com.logster.config.AppConfiguration;
import com.logster.config.DateFormatPattern;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateDetection  {

    private DateDetection(){}

    public static final DateDetection dateDetection = new DateDetection();





    public LocalDateTime parseLine(String line){

        for(DateFormatPattern dateFormat : AppConfiguration.dateFormats){
            try{
                Matcher matcher = dateFormat.regExp().matcher(line);
                 if(matcher.find()){
                     String datePart  = matcher.group(1);
                     System.out.println(datePart);
                     return LocalDateTime.parse(datePart, dateFormat.dateFormat());
                 }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }




}
