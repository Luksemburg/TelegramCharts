package com.code.foo.telegramcharts;

import java.io.*;
import java.util.logging.Logger;

import android.app.Activity;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;


public class Main extends AppCompatActivity {

    public static final String COLUMNS_STR = "\"columns\":";
    public static final String TYPES_STR = "\"types\":";
    public static final String X_STR = ":\"x\"";
    public static final String LINE_STR = ":\"line\"";
    public static final String COLORS_STR = "\"colors\":";
    public static final String NAMES_STR = "\"names\":";

    public static int screenHeight;
    public static int screenWidth;

    private static int count = 0;

    static String large = "";
    static String[] segments;
    static Chart[] chs;
    DrawChart dCh;
    static boolean flagCreateDrawChart = true;

    public static Logger mainLog = Logger.getLogger(Main.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;


        try{
            InputStream input = getResources().openRawResource(R.raw.chart_data);
            large = Main.readFile(input);
        } catch (Exception e){
            System.err.println("Cann't see file!");
        }

        //System.out.println(large);
        try{
            segments = large.split(COLUMNS_STR);
            if(segments.length <= 0){
                throw new RuntimeException();
            }

            chs = new Chart[segments.length - 1];

            for(int i = 1; i < segments.length; i++){
                chs[i - 1] = new Chart(segments[i]);
            }

            //dCh = new DrawChart(this);
            DrawChart.setColors(chs[count].getColor());
            DrawChart.setLegends(chs[count].getLegends());
            //mainLog.info(" : " + chs[0].getLineSize(0));
            DrawChart.setCoordinates(chs[count].getXCoord(), chs[count].getMaxX(), chs[count].getMinX(),
                    chs[count].getFloat2CoordinatesXY(), chs[count].getMaxY(), chs[count].getMinY());

            //dCh.initiate();

        } catch (RuntimeException re){
            System.err.println("Again Troubles!");
        }

        //if(flagCreateDrawChart){
            dCh = new DrawChart(this);
         //   flagCreateDrawChart = false;
        //}
        //setContentView(dCh);

        //dCh.findViewById(R.id.relativeLayout2);
        setContentView(dCh);

        mainLog.info("ON_CREATE! ");

        //
    }


    @Override
    public void onPause(){
        super.onPause();
        dCh.flagShowColorData = false;
    }


    public static String readFile(InputStream is){
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int c;

        try{
            c = is.read();

            while(c != -1){
                outStream.write(c);
                c = is.read();
            }

            is.close();
        } catch(IOException ioe){
            ioe.printStackTrace();
        }

        return outStream.toString();
    }

    public static void incCount(){
        if(count < chs.length - 1)
            count++;
    }

    public static void decCount(){
        if(count > 0)
            count--;
    }

    public static void recreateActivityCompat(final Activity a) {
        a.recreate();
    }

}


