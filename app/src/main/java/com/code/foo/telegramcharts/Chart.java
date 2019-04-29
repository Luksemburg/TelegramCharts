package com.code.foo.telegramcharts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Chart {

    private ArrayList<BigInteger> x = new ArrayList<BigInteger>();
    private ArrayList<ArrayList<Integer>> line = new ArrayList<ArrayList<Integer>>();
    private ArrayList<String> colors = new ArrayList<String>();
    private ArrayList<String> names = new ArrayList<String>();

    private static int c_lines = 0;
    private int maxY = 0;
    private int minY = 0;
    private BigInteger maxX = new BigInteger("0");
    private BigInteger minX = new BigInteger("0");

    public static Logger chartLog = Logger.getLogger(Chart.class.getName());

    public Chart(String str){
        String search = "";
        String searchType = "";

        int current = 0;


        try{
            search = find(current, str);
            if(search.length() < 3){
                throw new RuntimeException();
            }
        } catch (RuntimeException re){
            System.err.println("Objects not founded");
        }

        current = str.indexOf(search, current) + search.length();


        try{
            searchType = find(str.indexOf(Main.TYPES_STR) + Main.TYPES_STR.length(), str, search);
            if(searchType.equals(Main.X_STR)){
                fillArrayX(current + 1, str.indexOf(']', current), str);
                current = str.indexOf(']', current) + 1;
            } else {
                throw new RuntimeException();
            }
        }	catch(RuntimeException re){
            System.err.println("Array type of x not founded, it must be on the first place");
        }


        c_lines = countLines(str);
        //System.out.println(c_lines);

        for(int i = 0; i < c_lines; i++){
            line.add(new ArrayList<Integer>());

            try{
                search = find(current, str);
                if(search.length() < 3){
                    throw new RuntimeException();
                }
            } catch (RuntimeException re){
                System.err.println("Objects not founded");
            }

            current = str.indexOf(search, current) + search.length();


            try{
                searchType = find(str.indexOf(Main.TYPES_STR) + Main.TYPES_STR.length(), str, search);
                if(searchType.equals(Main.LINE_STR)){
                    fillArrayY(current + 1, str.indexOf(']', current), str, line.get(i));
                    current = str.indexOf(']', current) + 1;
                } else{
                    throw new RuntimeException();
                }
            } catch(RuntimeException re){
                System.err.println("Invalid type!");
            }

            try{
                searchType = find(str.indexOf(Main.NAMES_STR) + Main.NAMES_STR.length(), str, search);
                if(searchType.length() <= 0){
                    throw new RuntimeException();
                }
            }	catch(RuntimeException re){
                System.err.println("Cann't see names!");
            }

            names.add(clear(searchType));

            try{
                searchType = find(str.indexOf(Main.COLORS_STR) + Main.COLORS_STR.length(), str, search);
            }	catch(RuntimeException re){
                System.err.println("Cann't see colors!");
            }

            colors.add(clear(searchType));
        }

      /*  for(String s : names){
            System.out.println(s);
        }

        for(String s : colors){
            System.out.println(s);
        }
      */
    }


    public int getLineSize(int num){
        return line.get(num).size();
    }

    public BigInteger getMinX(){return minX;}

    public Integer getMinY(){return minY;}

    public Integer getMaxY(){
        return maxY;
    }


    public BigInteger getMaxX(){
        return maxX;
    }


    public ArrayList<BigInteger> getXCoord(){
        return x;
    }


    public ArrayList<ArrayList<Integer>> getFloat2CoordinatesXY(){
        return line;
    }


    public ArrayList<String> getColor(){
        return colors;
    }

    public ArrayList<String> getLegends(){return names;}


    public String clear (String s){
        String res = "";

        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == '#' || Character.isDigit(s.charAt(i)) || Character.isLetter(s.charAt(i))){
                res += s.charAt(i);
            }
        }

        return res;
    }


    public int countLines(String str){
        int c = 0;
        int firstIn = str.indexOf(Main.LINE_STR);
        if(firstIn == -1){
            return 0;
        }
        else{
            c++;
        }

        return c + countLines(str.substring(firstIn + Main.LINE_STR.length()));
    }


    public void fillArrayY(int start, int end, String str, ArrayList<Integer> arr) throws RuntimeException{
        String t = str.substring(start, end);
        String[] temp = t.split(",");
        //System.out.println("" + temp.length);

        for(int i = 0; i < temp.length; i++){
            temp[i].trim();
            if(Character.isDigit(temp[i].charAt(0))){
                arr.add(new Integer(temp[i]));

                if(arr.size() == 1){
                    minY = Integer.valueOf(temp[i]);
                }

                if(Integer.valueOf(temp[i]) > maxY){
                    maxY = Integer.valueOf(temp[i]);
                }

                if(Integer.valueOf(temp[i]) < minY){
                    minY = Integer.valueOf(temp[i]);
                }
            }
        }
    }


    public void fillArrayX(int start, int end, String str) throws RuntimeException{
        String t = str.substring(start, end);
        String[] temp = t.split(",");
        //System.out.println("" + temp.length);

        for(int i = 0; i < temp.length; i++){
            temp[i].trim();
            if(Character.isDigit(temp[i].charAt(0))){
                x.add(new BigInteger(temp[i]));

                if(x.size() == 1){
                    minX = x.get(x.size() - 1);
                }

                if(x.get(x.size() - 1).compareTo(maxX) > 0){
                    maxX = x.get(x.size() - 1);
                }

                if(x.get(x.size() - 1).compareTo(minX) < 0){
                    minX = x.get(x.size() - 1);
                }
            }
        }
    }


    public static String find(int from, String str, String target) throws RuntimeException{
        int t = 0;
        String temp = "";
        String res = "";

        temp = str.substring(from);
        t = temp.indexOf(target);
        temp = temp.substring(t + target.length());

        for(int i = 0, c = 0; c < 2; i++){
            res += String.valueOf(temp.charAt(i));

            if(temp.charAt(i) == '"')
                c++;
        }

        res.trim();
        return res;
    }


    public static String find(int c, String str) throws RuntimeException{

        String temp = "";

        for(int i = c, j = 1; i < str.length(); i++){
            if(str.charAt(i) == '"'){

                temp += String.valueOf(str.charAt(i));

                while(str.charAt(i + j) != '"'){
                    temp += String.valueOf(str.charAt(i + j));
                    j++;
                }

                temp += String.valueOf(str.charAt(i + j));
                break;
            }
        }

        return temp;
    }
}