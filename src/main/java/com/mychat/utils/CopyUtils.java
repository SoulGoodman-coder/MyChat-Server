package com.mychat.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * projectName: com.mychat.utils
 * author:  SoulGoodman-coder
 * description: 拷贝两个对象的相同属性
 */

public class CopyUtils {
    /**
     * 拷贝多个对象
     * @param source    List(被拷贝属性的对象)
     * @param clazz     目标对象class
     * @return          List(目标对象)
     */
    public static <T, S> List<T> copyList(List<S> source, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (S s : source) {
            T t = null;
            try {
                t = clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            list.add(t);
        }
        return list;
    }

    /**
     * 拷贝单个对象属性
     * @param source    被拷贝属性的对象
     * @param clazz     目标对象class
     * @return          目标对象
     */
    public static <T, S> T copy(S source, Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(source, t);
        return t;
    }
}
