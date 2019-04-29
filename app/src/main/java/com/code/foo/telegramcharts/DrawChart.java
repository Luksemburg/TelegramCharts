package com.code.foo.telegramcharts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class DrawChart extends View {

    public static Logger myLog = Logger.getLogger(DrawChart.class.getName());

    private Paint varPaint;
    private static ArrayList<Integer> colors = new ArrayList<Integer>();
    private static ArrayList<String> legends = new ArrayList<String>();
    private static int countLines;
    private static ArrayList<ArrayList<Float>> coordinates = new ArrayList <ArrayList<Float>>();
    private static ArrayList<Float> coordX = new ArrayList<Float>();

    private static ArrayList<ArrayList<String>> rawY = new ArrayList<ArrayList<String>>();
    private static ArrayList<Long> rawX = new ArrayList<Long>();


    private int left = Main.screenWidth / 15;
    private int top = Main.screenHeight / 25;
    private int right = Main.screenWidth - left;
    private int bottom = 3 * Main.screenHeight / 5;

    private int padding = Main.screenWidth / 30;
    private int previewThickness = 3 * top / 2;

    private int topSmall = bottom + padding;
    private int bottomSmall = topSmall + previewThickness;

    private Paint rPaint = new Paint();
    private Paint bPaint = new Paint();
    private Paint prevPaint = new Paint();
    private Paint sidePaint = new Paint();
    private Paint legendPaint = new Paint();
    private Paint textLegendPaint = new Paint();
    private Paint windowPrevPaint = new Paint();
    private Paint datePaint = new Paint();
    private Paint nightPaint = new Paint();
    private Paint backgroundPaint = new Paint();

    private Rect baseRect = new Rect(left, top, right, bottom);
    private Rect smallRect = new Rect(left, topSmall, right, bottomSmall);

    Rect windowPrevRect = new Rect();
    private int windowPrevLeft = left + 3*(right - left)/5;
    private int windowPrevRight = right;

    public static Rect but = new Rect();
    public static Rect rev = new Rect();

    Rect night = new Rect();

    private Paint textPaint = new Paint();
    private Context context;
    private Paint resetPaint = new Paint();

    static int max_y;
    static int min_y;
    static int back_lines = 6;

    private float xVertLine = 0.0f;
    private boolean f = false;          //paint vertcal line?
    private boolean flagPoint;
    boolean flagShowColorData = false;

    private int textLittleSize = 30;
    private int textLegendSize = 2 * textLittleSize;

    private int textLittleGap = 3;
    private int side = (right - left) / 5;

    private boolean[] flagLegendBinClicked = new boolean[countLines];
    //rects for buttons for managing lines
    private Rect[] legendButtons = new Rect[countLines];

    private int finger = 35;
    private float x_start = 0.0f;

    private float startViewGraph;
    private float endViewGraph;
    private float sczleXGraph;

    private Path graphPath = new Path();
    private Path smallPath = new Path();

    private static ArrayList<Float> maximums = new ArrayList<Float>();
    private float relative_max_y = -1.0f;
    private float horiz_max = 1.0f;

    private static boolean isDay = true;


    public DrawChart(Context cont){
        super(cont);
        //myLog.info("constructor " + colors.size());
        this.context = cont;
        rPaint.setStrokeWidth(1);
        rPaint.setColor(Color.GRAY);
        rPaint.setAntiAlias(true);
        rPaint.setStyle(Paint.Style.STROKE);
        rPaint.setTextAlign(Paint.Align.CENTER);
        rPaint.setTextSize(textLittleSize);

        initiate();
    }

    public void initiate(){
        varPaint = new Paint();

        varPaint.setStrokeWidth(3);
        varPaint.setAntiAlias(true);
        setWillNotDraw(false);

        resetPaint.setColor(Color.WHITE);

        for(int i = 0; i < legendButtons.length; i++){
            legendButtons[i] = new Rect();
        }
    }

    @Override
    protected void onDraw (Canvas canva){
        super.onDraw(canva);


        //myLog.info(" RAW_Y_SIZE: " + rawY.get(0).size());
        backgroundPaint.setAlpha(255);
        if(isDay){
            backgroundPaint.setColor(Color.WHITE);
            textPaint.setColor(Color.DKGRAY);
            datePaint.setColor(Color.DKGRAY);
            windowPrevPaint.setColor(Color.LTGRAY);
        }else{
            backgroundPaint.setColor(Color.DKGRAY);
            textPaint.setColor(Color.WHITE);
            datePaint.setColor(Color.WHITE);
            windowPrevPaint.setColor(Color.DKGRAY);
        }

        windowPrevRect.set(windowPrevLeft, topSmall, windowPrevRight, bottomSmall);

        //preview
        prevPaint.setStyle(Paint.Style.FILL);
        prevPaint.setColor(Color.WHITE);

        //background
        canva.drawRect(0, 0, Main.screenWidth, Main.screenHeight, backgroundPaint);

        //dynamic change Y coord max value
        for(int i = 0; i < maximums.size(); i++){
            if(!flagLegendBinClicked[i] && maximums.get(i) > relative_max_y){
                relative_max_y = maximums.get(i);
                //horiz_max = maximums.get(i);
                myLog.info("Relative max Y: " + relative_max_y);
            }
        }

        //count active lines now
        int cActiveLines = 0;
        int[] whatActive = new int[countLines];

        for(int i = 0; i < countLines; i++){

            whatActive[i] = cActiveLines;

            if(!flagLegendBinClicked[i]){
                cActiveLines++;
            }
        }


        //draw web behind graph
        for(int i = 1; i <= back_lines; i++) {
            String s = "0";
            float tableValue = 0;
            Rect r = new Rect(left - padding, top + (i - 1) * (bottom - top) / back_lines + 3 * padding / 2, right, top+ i * (bottom - top) / back_lines);
            canva.drawLine(left - padding, top + i * (bottom - top) / back_lines, right, top+ i * (bottom - top) / back_lines, rPaint);

            tableValue = Math.round((max_y  - i * max_y / back_lines)* horiz_max);
            if(tableValue > 0) {
                s = String.valueOf(Math.round(tableValue));
            }
            drawRectText(s, canva, r, 2 * padding / 3, -1, textPaint, previewThickness, 80);
        }


        //gray borders
        prevPaint.setAlpha(30);
        windowPrevPaint.setStyle(Paint.Style.FILL);
        //windowPrevPaint.setColor(Color.LTGRAY);
        canva.drawRect(left, smallRect.top, windowPrevRect.left, smallRect.bottom, windowPrevPaint);
        canva.drawRect(windowPrevRect.right, smallRect.top, right, smallRect.bottom, windowPrevPaint);

        //baseRect.set(left, topSmall, right, bottomSmall);

        canva.drawRect(smallRect, prevPaint);

        //green window for preview
        windowPrevPaint.setStrokeWidth(3);
        windowPrevPaint.setColor(Color.GREEN);
        windowPrevPaint.setAlpha(40);
        canva.drawRect(windowPrevRect, windowPrevPaint);



        //buttons
        but.set(Main.screenWidth / 2, Main.screenHeight - 7 * previewThickness / 2, Main.screenWidth, Main.screenHeight);
        //myLog.info("KO :" + Main.screenHeight + "ko : " + (Main.screenHeight - previewThickness));

        bPaint.setColor(Color.BLUE);
        bPaint.setAlpha(50);
        canva.drawRect(but, bPaint);

        bPaint.setColor(Color.RED);
        bPaint.setAlpha(50);
        rev.set(0, Main.screenHeight - 7 * previewThickness / 2, Main.screenWidth / 2, Main.screenHeight);
        canva.drawRect(rev, bPaint);

        drawRectText("next >", canva, but, 2 * previewThickness / 3, 0, textPaint, previewThickness, 200);
        drawRectText("< prev", canva, rev, 2 * previewThickness / 3, 0, textPaint, previewThickness, 200);

        sidePaint.setAlpha(222);
        sidePaint.setColor(Color.LTGRAY);

        // set scale params by horizontal projaction
        startViewGraph = (float) (windowPrevRect.left) / /*(float) Main.screenWidth;*/                   (float) (right - left);
        endViewGraph = (float) (windowPrevRect.right) / /*(float) Main.screenWidth;*/                     (float) (right - left);
        sczleXGraph = (float) (windowPrevRect.right - windowPrevRect.left) / /*(float) Main.screenWidth*/(right - left);
        //startViewGraph = endViewGraph - sczleXGraph;
        if(endViewGraph > 1){
            endViewGraph = 1.0f;
        }
        if(startViewGraph < 0){
            startViewGraph = 0.0f;
        }
        //myLog.info("Sart: " + startViewGraph);
        //myLog.info("End: " + endViewGraph);
        //myLog.info("Scale: " + sczleXGraph);

        //main for
        for(int i = 0; i < countLines; i++) {
            varPaint.setColor(colors.get(i));
            varPaint.setStyle(Paint.Style.STROKE);
            varPaint.setTextSize(textLittleSize);
            varPaint.setTextAlign(Paint.Align.CENTER);

            legendPaint.setStyle(Paint.Style.FILL);
            legendPaint.setColor(colors.get(i));

            textLegendPaint.setColor(Color.WHITE);
            textLegendPaint.setTextSize(textLegendSize);

            flagPoint = true;


            graphPath.reset();
            smallPath.reset();

            //hide crossfire lines
            graphPath.moveTo(0, top + (bottom - top)/2);
            smallPath.moveTo(Main.screenWidth + 100.0f, topSmall + (bottomSmall - topSmall)/2);

            //x,y,x,y,x,y in the paths
            float[] fArr = new float[coordinates.get(i).size() + coordX.size()];
            float[] fPrew = new float[coordinates.get(i).size()];
            float[] fXPrew = new float[coordX.size()];

            //myLog.info("Compensate: " + startViewGraph * coordX.get((coordX.size() - 1)/2) / sczleXGraph);
            datePaint.setTextSize(20);
            datePaint.setTextAlign(Paint.Align.CENTER);
            datePaint.setAlpha(50);
            //for(int j = 0, k = 0; j < fArr.length; j++){
            for(int j = 0/*(int) (startViewGraph * fArr.length)*/, k = 0, l = 0; j < fArr.length/* * endViewGraph*/; j++){

                //x in the paths
                if(j % 2 == 0){
                    fArr[j] = (float) left + ((float) left / (float)sczleXGraph + (float)coordX.get(j / 2) * ((float) right - (float) left)/ (float)sczleXGraph)        //To correct(?)
                            - ((float)(right - left))*(startViewGraph * coordX.get(coordX.size() - 2) / ((float)sczleXGraph))/* + 50.0f*/;   //To correct

                    fXPrew[l] = ((float) left + coordX.get(j / 2) * ((float) right - (float) left));
                    l++;
                    //myLog.info("fArr[j]: " + fArr[j]);
                }else {
                    //add y to fArr
                    fArr[j] = ((float) bottom + ((float) top - (float) bottom) * coordinates.get(i).get(j / 2) / (horiz_max));      //(relative_max_y + relative_min_y)
                    fPrew[k] = ((float) bottomSmall + ((float) topSmall - (float) bottomSmall) * coordinates.get(i).get(j / 2) / (relative_max_y));
                    k++;
                    if(j != 1){
                        graphPath.lineTo(fArr[j - 1], fArr[j]);
                        smallPath.lineTo(fXPrew[l - 1], fPrew[k - 1]);
                    }

                    //info rect
                    RectF rL = new RectF(fArr[j - 1] - side - padding, top + padding, fArr[j - 1] - padding,
                            top + textLittleGap + (2 * cActiveLines + 1) * (textLittleSize + textLittleGap) + 3 * padding/2);
                    RectF rR = new RectF(fArr[j - 1] + padding, top + padding, fArr[j - 1] + side + padding,
                            top + textLittleGap + (2 * cActiveLines + 1) *  (textLittleSize + textLittleGap) + 3 * padding/2);

                    //vertical line and points on the graph
                    if(Math.abs(fArr[j - 1] - xVertLine) < 10 && flagPoint) {
                        if(f) {
                            String dat = toDate(rawX.get(j / 2), "dd MMM yy");
                            //line
                            canva.drawLine(fArr[j - 1], top, fArr[j - 1], bottom, rPaint);
                            //mini rect
                            if (fArr[j - 1] > (right - left) / 2) {
                                canva.drawRect(rL, sidePaint);
                                canva.drawText(dat, rL.centerX(), rL.top + textLittleGap + textLittleSize, rPaint);

                            } else {
                                canva.drawRect(rR, sidePaint);
                                canva.drawText(dat, rR.centerX(), rR.top + textLittleGap + textLittleSize, rPaint);
                            }

                            f = false;
                        }

                            if (!flagLegendBinClicked[i] && flagShowColorData) {
                                //points
                                canva.drawCircle(fArr[j - 1], fArr[j], 6, varPaint);
                                // clolur names and numbers
                                if (fArr[j - 1] > (right - left) / 2) {
                                    canva.drawText(legends.get(i), rL.centerX(), rL.top + (2 * whatActive[i] + 2) * (textLittleGap + textLittleSize), varPaint);
                                    canva.drawText(rawY.get(i).get(j / 2), rL.centerX(), rL.top + (2 * whatActive[i] + 3) * (textLittleGap + textLittleSize), varPaint);
                                } else {
                                    canva.drawText(legends.get(i), rR.centerX(), rR.top + (2 * whatActive[i] + 2) * (textLittleGap + textLittleSize), varPaint);
                                    canva.drawText(rawY.get(i).get(j / 2), rR.centerX(), rR.top + (2 * whatActive[i] + 3) * (textLittleGap + textLittleSize), varPaint);
                                }
                            }

                           // f = false;
                            flagPoint = false;
                        //}
                    }
                }

                if(j == 1 || j == fArr.length - 1){
                    graphPath.moveTo(fArr[j - 1], fArr[j]);
                    smallPath.moveTo(left - 100, bottomSmall - (bottomSmall - topSmall)/2);
                    //smallPath.lineTo(fXPrew[l - 1], fPrew[k - 1]);
                }

                //Dates on the bottom of graph
                if(j % 2 == 0) {
                    if ((j % (coordX.size()/9) == 0 && (right - left) / (windowPrevRect.right - windowPrevRect.left) >= 2) ||
                            (j % (coordX.size()/5) == 0 && (right - left) / (windowPrevRect.right - windowPrevRect.left) < 2)) {
                        canva.drawText(toDate(rawX.get(j / 2), "MMM dd"), fArr[j], 2 * textLittleGap + bottom + (topSmall - bottom) / 2, datePaint);
                    }
                }
            }

            smallPath.close();
            graphPath.close();

            //draw buttons for managing lines
            if(flagLegendBinClicked[i]){
                legendPaint.setColor(Color.LTGRAY);
                textLegendPaint.setColor(Color.BLACK);

            } else{
                //draw graphs
                canva.drawPath(graphPath , varPaint);
                canva.drawPath(smallPath, varPaint);
            }

            //support up to 6 buttons(for on\of line overview) inclusive
            if(i < 2){
                legendButtons[i].set(padding, bottomSmall + padding + i * (2*padding/3 + textLegendSize + 2*textLittleGap),
                        Main.screenWidth / 3 - padding,
                    bottomSmall + (2*padding/3 + textLegendSize + 2*textLittleGap) * (i + 1));

                canva.drawRect(legendButtons[i], legendPaint);
                drawRectText(legends.get(i), canva, legendButtons[i], textLegendSize, 0, textLegendPaint, padding/2, 255);
            }

            if(i >= 2 && i < 4){
                legendButtons[i].set(padding + Main.screenWidth / 3,
                        bottomSmall + padding + (i-2) * (2*padding/3 + textLegendSize + 2*textLittleGap),
                        2*(Main.screenWidth / 3 - padding),
                        bottomSmall + (2*padding/3 + textLegendSize + 2*textLittleGap) * (i - 1));

                canva.drawRect(legendButtons[i], legendPaint);
                drawRectText(legends.get(i), canva, legendButtons[i], textLegendSize, 0, textLegendPaint, padding/2, 255);
            }

            if(i >= 4 && i < 6) {
                legendButtons[i].set(padding + 2 * Main.screenWidth / 3,
                        bottomSmall + padding + (i - 2) * (2 * padding / 3 + textLegendSize + 2 * textLittleGap),
                        Main.screenWidth - padding,
                        bottomSmall + (2 * padding / 3 + textLegendSize + 2 * textLittleGap) * (i - 1));

                canva.drawRect(legendButtons[i], legendPaint);
                drawRectText(legends.get(i), canva, legendButtons[i], textLegendSize, 0, textLegendPaint, padding / 2, 255);
            }

        }

        //horizontal normalization Y
        float local_max = -1.0f;
        for(int i = 0; i < countLines; i++) {
            for (int n = (int) (startViewGraph * coordX.size()); n < endViewGraph * coordX.size(); n++) {
                //if (n % 2 != 0) {
                if (coordinates.get(i).get(n) > local_max && !flagLegendBinClicked[i]) {
                    local_max = coordinates.get(i).get(n);
                    //myLog.info("-----horiz_max: " + horiz_max);
                }

            }
        }

        //myLog.info("-----------");

        if(horiz_max != local_max) {
            horiz_max = local_max;
            //f = true;
            postInvalidate();
        }

        //hide preview borders
        canva.drawRect(right, topSmall, Main.screenWidth, bottomSmall, backgroundPaint);
        canva.drawRect(0, topSmall, left, bottomSmall, backgroundPaint);

        //Night mode button
        nightPaint.setTextSize(textLegendSize);
        nightPaint.setAlpha(150);
        nightPaint.setStyle(Paint.Style.STROKE);
        nightPaint.setTextAlign(Paint.Align.CENTER);
        String str = "";
        if(isDay){
            str = "Night";
            nightPaint.setColor(Color.DKGRAY);
        } else {
            str = "Day";
            nightPaint.setColor(Color.WHITE);
        }

        night.set(2*Main.screenWidth/3, 0, Main.screenWidth, top);
        canva.drawRect(night, nightPaint);

        canva.drawText(str, night.centerX(), night.centerY() + textLegendSize/3, nightPaint);
    }


    public static void setCoordinates(ArrayList<BigInteger> aX, BigInteger bInt, BigInteger minX, ArrayList<ArrayList<Integer>> arr,
                                      Integer maxY, Integer minY){

        //myLog.info("MIN :" + minX);
        //myLog.info("MAX :" + bInt);
        max_y = maxY;
        min_y = minY;



            for(int i = 0; i < aX.size(); i++){
                coordX.add((aX.get(i).floatValue() - minX.floatValue()) / (bInt.floatValue() - minX.floatValue()));
                rawX.add(aX.get(i).longValue());
                //myLog.info("KO :" + coordX.get(i));
            }

            for(int i = 0; i < arr.size(); i++){
                float temp = 0.0f;

                coordinates.add(new ArrayList<Float>());
                rawY.add(new ArrayList<String>());
                for(int j = 0; j < arr.get(i).size(); j++){
                    //normalize
                    coordinates.get(i).add((Float.valueOf(arr.get(i).get(j))) /
                            (Float.valueOf(maxY)));
                    rawY.get(i).add(String.valueOf(arr.get(i).get(j)));

                    if(coordinates.get(i).get(j) > temp){
                        temp = coordinates.get(i).get(j);
                    }
                }
                maximums.add(temp);
                myLog.info("Max " + i + " :" + maximums.get(i));
            }

    }

    public static void setLegends(ArrayList<String> s){
        legends.addAll(s);
    }

    public static void setColors(ArrayList<String> s){

        countLines = s.size();

        for(int i = 0; i < countLines; i++){
            colors.add(Color.parseColor(s.get(i)));
        }

        //myLog.info("setColors " + colors.size());
    }

    private void drawRectText(String text, Canvas canvas, Rect r, int size, int align, Paint textP, int kantik, int alpha) {

        float temp = 0;
        textP.setTextSize(size);
        textP.setAlpha(alpha);

        if(align == 0) {
            temp = r.exactCenterX();
            textP.setTextAlign(Paint.Align.CENTER);
        }

        if(align > 0){
            temp = r.right;
            textP.setTextAlign(Paint.Align.RIGHT);
        }

        if(align < 0){
            temp = r.left;
            textP.setTextAlign(Paint.Align.LEFT);
        }

        int width = r.width();

        int numOfChars = textP.breakText(text,true,width,null);
        int start = (text.length()-numOfChars)/2;
        canvas.drawText(text,start,start+numOfChars,temp,r.top + 2 * kantik / 3 + padding,textP);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(but.contains((int) event.getX(), (int) event.getY())){
            //myLog.info("NEXT!!!");
            Main.incCount();
            //clear coordinates
            coordinates.clear();
            coordX.clear();
            colors.clear();
            legends.clear();
            rawY.clear();
            rawX.clear();
            max_y = 0;
            min_y = 0;
            maximums.clear();
            countLines = 0;

            Main.recreateActivityCompat((Activity) context);
        }

        if(rev.contains((int) event.getX(), (int) event.getY())){
           // myLog.info("PREV!!!");
            Main.decCount();
            //clear coordinates
            coordinates.clear();
            coordX.clear();
            colors.clear();
            legends.clear();
            rawY.clear();
            rawX.clear();
            max_y = 0;
            min_y = 0;
            maximums.clear();
            countLines = 0;

            Main.recreateActivityCompat((Activity) context);
        }

        if(baseRect.contains((int) event.getX(), (int) event.getY())){
            //myLog.info("LINE!!!");
            xVertLine = event.getX();

            flagShowColorData = true;
            f = true;
            postInvalidate();
            return false;
            //return false;
        } else {
            flagShowColorData = false;
            postInvalidate();
        }

        if(night.contains((int) event.getX(), (int) event.getY())){
            isDay = !isDay;
            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN && smallRect.contains((int) event.getX(), (int) event.getY())) {
            x_start = event.getX();
        }


        if(event.getAction() == MotionEvent.ACTION_MOVE){

         //   if (smallRect.contains((int) event.getX(), (int) event.getY())) {
                if (Math.abs(event.getX() - windowPrevRect.right) <= 2 * finger) {
                    if (x_start > event.getX() && windowPrevRight - windowPrevLeft > 4 * finger) {
                        //myLog.info("GET_X > START = " + x_start);
                        windowPrevRight = (int) event.getX() - finger;
                        postInvalidate();
                    }
                    if (x_start < event.getX() && windowPrevRight < right - 1) {
                        //myLog.info("GET_X < START = " + x_start);
                        windowPrevRight = (int) event.getX() + finger;
                        postInvalidate();
                    }
                }

                if (Math.abs(event.getX() - windowPrevRect.left) <= 2 * finger) {
                    if (x_start < event.getX() && windowPrevRight - windowPrevLeft > 4 * finger) {
                        windowPrevLeft = (int) event.getX() + finger;
                        postInvalidate();
                    }

                    if (x_start > event.getX() && windowPrevLeft > left + 1) {
                        windowPrevLeft = (int) event.getX() - finger;
                        postInvalidate();
                    }
                }
         //   }

            if (windowPrevRect.contains((int) event.getX(), (int) event.getY())) {


                int dx = windowPrevRight - windowPrevLeft;          // To correct
                int dPointer = 0;

                if(event.getAction() == MotionEvent.ACTION_DOWN ) {
                    dPointer = (int) event.getX() - (int) windowPrevRect.exactCenterX();
                }


                if(event.getX() <= windowPrevRight - 2*finger && event.getX() >= windowPrevLeft + 2*finger ){
                //if(windowPrevRight <= right && windowPrevLeft >= left){

                    windowPrevLeft = (int) event.getX() - dx / 2 + dPointer;
                    windowPrevRight = (int) event.getX() + dx / 2 + dPointer;

                    //postInvalidate();
                }


                if(windowPrevRight > right){
                        windowPrevRight = right;
                        windowPrevLeft = right - dx;

                        return false;
                }
                if(windowPrevLeft < left){
                        windowPrevLeft = left;
                        windowPrevRight = left + dx;

                        return false;
                }

                postInvalidate();
            }
        }

        for(int i = 0; i < legendButtons.length; i++) {
            if (legendButtons[i].contains((int) event.getX(), (int) event.getY())){
                flagLegendBinClicked[i] = !flagLegendBinClicked[i];
                relative_max_y = -1.0f;
                myLog.info("-----horiz_max: " + horiz_max);

                postInvalidate();
                return false;
            }
        }

        return true;
    }

    public static String toDate(Long val, String pattern){
        Date date = new Date(val);
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return String.valueOf(dateFormat.format(date));
    }
}
