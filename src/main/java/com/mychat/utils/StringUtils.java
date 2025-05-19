package com.mychat.utils;

import com.mychat.utils.enums.UserContactTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
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
     * @return  U + 11位随机数
     */
    public static String getUserId(){
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(11);
    }

    /**
     * 生成getGroupId
     * @return  G + 11位随机数
     */
    public static String getGroupId(){
        return UserContactTypeEnum.GROUP.getPrefix() + getRandomNumber(11);
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

    /**
     * 清除字符串中的html标签，防止恶意html注入
     * @param str   原始字符串
     * @return      清除html标签后的字符串
     */
    public static String clearHtmlTag(String str){
        if (isEmpty(str)){
            return str;
        }
        str = str.replaceAll("<", "&lt;")
                 .replaceAll(">", "&gt;")
                 .replaceAll("\r\n", "<br/>")
                 .replaceAll("\n", "<br/>");
        return str;
    }

    /**
     * 为用户会话生成会话id
     * 即：将两个用户的id排序后，相加，再MD5加密，确保会话id唯一且不变
     * @param userIds   两个用户的id
     * @return          会话id
     */
    public static final String getChatSessionId4User(String[] userIds){
        Arrays.sort(userIds);
        return encodeMD5(org.apache.commons.lang3.StringUtils.join(userIds, ""));
    }

    /**
     * 为群组会话生成会话id
     * 即：将群组id用MD5加密
     * @param groupId   群组id
     * @return          会话id
     */
    public static final String getChatSessionId4Group(String groupId){
        return encodeMD5(groupId);
    }

    public static String getFileSuffix(String fileName){
        if (isEmpty(fileName)){
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // 判断字符串是否是数字
    public static boolean isNumber(String str){
        String checkNumber = "^[0-9]+$";
        if (null == str || str.isEmpty() || !str.matches(checkNumber)){
            return false;
        }
        return true;
    }
}
