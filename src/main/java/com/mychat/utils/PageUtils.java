package com.mychat.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.Map;

/**
 * projectName: com.mychat.utils
 * author:  SoulGoodman-coder
 * description: 分页数据工具类
 */

public class PageUtils {
    public static Map<String, Object> getPageResultData(Page page){
        Map<String, Object> resultMap = new HashMap();
        resultMap.put("list", page.getRecords());
        resultMap.put("pageTotal", page.getPages());
        resultMap.put("pageSize", page.getSize());
        resultMap.put("totalCount", page.getTotal());
        resultMap.put("pageNo", page.getCurrent());
        return resultMap;
    }
}
