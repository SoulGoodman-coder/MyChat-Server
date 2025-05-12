package com.mychat.service;

import com.mychat.entity.po.AppUpdate;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author Administrator
* @description 针对表【app_update(app发布表)】的数据库操作Service
* @createDate 2025-05-10 20:13:50
*/
public interface AppUpdateService extends IService<AppUpdate> {

    /**
     * 保存更新
     * @param appUpdate     版本更新信息对象
     * @param file          更新文件
     */
    void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException;

    /**
     * 获取更新列表
     * @param startDate     日期筛选（开始时间）
     * @param endDate       日期筛选（结束时间）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return List<AppUpdate>
     */
    List<AppUpdate> loadUpdateList(String startDate, String endDate, Integer pageNumber, Integer pageSize);

    /**
     * 发布更新
     * @param id            更新数据id
     * @param status        状态 0:未发布 1:灰度发布 2:全网发布
     * @param grayscaleUid  灰度uid
     */
    void postUpdate(Integer id, Integer status, String grayscaleUid);

    /**
     * 获取最新版本数据
     * @param userId        当前用户id
     * @param appVersion    当前客户端版本号
     * @return AppUpdate
     */
    AppUpdate getLatestUpdate(String userId, String appVersion);
}
