package com.mychat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mychat.exception.BusinessException;
import com.mychat.utils.enums.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * projectName: com.mychat.utils
 * author:  SoulGoodman-coder
 * description: json字符串与实体类转化工具类
 */

@Slf4j
public class JsonUtils {
    // FastJSON序列化设置    （输出Map中为null的值）
    public static SerializerFeature[] FEATURES = new SerializerFeature[]{SerializerFeature.WriteMapNullValue};

    // 对象转JSON
    public static String covertObj2Json(Object obj) {
        return JSON.toJSONString(obj, FEATURES);
    }

    // JSON转对象
    public static <T> T covertJson2Obj(String json, Class<T> clazz) {
        try {
            return JSONObject.parseObject(json, clazz);
        }catch (Exception e){
            log.error("covertJson2Obj异常，JSON：{}", json);
            throw new BusinessException(ResultCodeEnum.CODE_601);
        }
    }

    //
    public static <T> List<T> covertJsonArray2List(String json, Class<T> clazz) {
        try {
            return JSONArray.parseArray(json, clazz);
        }catch (Exception e){
            log.error("covertJsonArray2List异常，JSON：{}", json);
            throw new BusinessException(ResultCodeEnum.CODE_601);
        }
    }
}
