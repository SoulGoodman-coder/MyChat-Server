package com.mychat.utils;

import com.mychat.utils.enums.UserTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * projectName: com.mychat.utils
 * author:  SoulGoodman-coder
 * description: 字符串操作工具类
 */

public class StringUtils {
    /**
     * 判断字符串是否为空（null，空格等）
     * @param str   字符串
     * @return  true|false
     */
    public static Boolean isEmpty(String str){
        if(str == null || str.isEmpty() || "null".equals(str) || "\u0000".equals(str)){
            return true;
        } else if (str.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static Boolean isEmpty(List<String> srtList){
        return srtList == null || srtList.isEmpty();
    }


    /**
     * 生成userId
     * @return
     */
    public static String getUserId(){
        return UserTypeEnum.USER.getPrefix() + getRandomNumber(11);
    }

    public static String getRandomNumber(int length) {
        return RandomStringUtils.random(length, false, true);
    }

    public static String getRandomString(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    /**
     * 密码MD5加密
     * @param str   原始密码
     * @return      加密后的密码
     */
    public static final String encodeMD5(String str) {
        return StringUtils.isEmpty(str) ? null : DigestUtils.md5Hex(str);
    }
}
