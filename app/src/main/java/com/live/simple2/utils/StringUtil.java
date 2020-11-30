package com.live.simple2.utils;

public class StringUtil {
    public static boolean isEmpty(String s){
        if (s == null){
            return true;
        }
        if (s.length() == 0){
            return true;
        }
        return false;
    }
}
