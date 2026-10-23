package com.sdmmllc.superdupersmsmanager.utils;


public class StringUtils {
    public static String padding(String str, int repeat, char padChar) {
        StringBuilder result = new StringBuilder();

        int len = str.length();
        if (repeat > len) {
            for (int i = 0; i < (repeat - len); i++) {
                result.append(padChar);
            }
        }
        result.append(str);

        return result.toString();
    }

    public static String truncate(String str, int len) {
        if (len <= 3) {
            throw new IllegalArgumentException();
        }
        
        if (str.length() > len) {
            return str.substring(0, (len - 3)) + "...";
        } else {
            return str;
        }
    }
    
    public static boolean isNullOrEmpty(String str){
        return str == null || str.length() == 0;
    }
    
    public static boolean isNullOrEmptyOrWhitespace(String str){
        return isNullOrEmpty(str) || isNullOrEmpty(str.trim());
    }
    
    public static String convertNullToEmpty(String str){
        if(str == null){
            return "";
        }
        else {
            return str;
        }
    }
}
