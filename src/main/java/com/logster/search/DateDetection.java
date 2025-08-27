package com.logster.search;

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
    private record MyDateFormat(Pattern regExpPattern, DateTimeFormatter dateTimePattern){

    }
    private final ArrayList<MyDateFormat> myDateFormats =new ArrayList<>();

    public void load( List<DateFormatPattern> dateFormatPatterns){
        if(dateFormatPatterns==null)return;
        myDateFormats.clear();
        for(DateFormatPattern dateFormatPattern :dateFormatPatterns){
            Pattern pattern = Pattern.compile(dateFormatPattern.regExp());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern.dateFormat());
            myDateFormats.add(new MyDateFormat(pattern,dateTimeFormatter));
        }
    }

    public LocalDateTime parseLine(String line){
        System.out.println(myDateFormats.size());
        for(MyDateFormat dateFormat :myDateFormats){
            try{
                Matcher matcher = dateFormat.regExpPattern.matcher(line);
                 if(matcher.find()){
                     String datePart  = matcher.group(1);
                     System.out.println(datePart);
                     return LocalDateTime.parse(datePart, dateFormat.dateTimePattern);
                 }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }




}
