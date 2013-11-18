package com.eighthinfo.sgs.utils;


import java.util.UUID;

/**
 *  User: dam
 * Date: 13-11-18
 */
public class StringUtils {

    public static String genUUID(){
        String uuid = UUID.randomUUID().toString().replace("-","");
        return uuid;
    }
}
