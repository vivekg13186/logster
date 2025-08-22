package com.logster;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LogGeneratorTestCase {


    @Test
     void genLogs() throws IOException {
        DateDetection dateDetection =new DateDetection();
        String currentDirectory = System.getProperty("user.home")+"\\Downloads\\MyLogs\\";
        for(int p=0;p<dateDetection.formatterList.size();p++){
            DateTimeFormatter formatter = dateDetection.formatterList.get(p).formatter();
            System.out.println("p - "+p);
            LocalDateTime time = LocalDateTime.now();
            File file1 = new File(currentDirectory+"log"+p+".log");
            PrintWriter writer =new PrintWriter(file1);
            int counter=0;
            for(int i=0;i<100000;i++){
                writer.println(time.format(formatter) + " - INFO - Log message " + i);
                if(counter==100){
                    counter=0;
                    time = time.plusMinutes(4);
                }
                counter++;
            }
            writer.flush();
            writer.close();
        }


    }
}
