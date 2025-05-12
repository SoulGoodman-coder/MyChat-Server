package com.mychat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mychat.entity.po.AppUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【app_update(app发布表)】的数据库操作Mapper
* @createDate 2025-05-10 20:13:50
* @Entity com.mychat.entity.po.AppUpdate
*/
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {

    /**
     * 获取更新列表
     * @param page          分页对象
     * @param startDate     日期筛选（开始时间）
     * @param endDate       日期筛选（结束时间）
     * @return
     */
    IPage<AppUpdate> loadUpdateList(Page<AppUpdate> page, @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 获取最新版本数据
     * @param userId        当前用户id
     * @param appVersion    当前客户端版本号
     * @return AppUpdate
     */
    AppUpdate selectLatestUpdate(@Param("userId") String userId, @Param("appVersion") String appVersion);
}




