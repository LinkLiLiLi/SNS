package com.lxinet.jeesns.core.utils;

import java.util.UUID;

/**
 * ly2017/7/15.
 */
public class TokenUtil {

    public static String getToken(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
