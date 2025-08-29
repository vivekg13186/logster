package com.logster.config;

import java.util.ArrayList;

public class AppConfiguration {

    public static  final ArrayList<String> ignoreFileExtension =new ArrayList<>();
    public static  final ArrayList<DateFormatPattern> dateFormats =new ArrayList<>();


    private static int MAX_RESULT = 10000;

    public static int getMaxResult(){
        return MAX_RESULT;
    }

    public static void loadSystemVars(){
        try {
            String text = System.getProperty("MAX_RESULT");
            MAX_RESULT = Integer.parseInt(text);
        } catch (NumberFormatException _) {

        }
    }
}
