package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.po.AppUpdate;
import com.mychat.exception.BusinessException;
import com.mychat.service.AppUpdateService;
import com.mychat.mapper.AppUpdateMapper;
import com.mychat.utils.PageUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.AppUpdateFileTypeEnum;
import com.mychat.utils.enums.AppUpdateStatusEnum;
import com.mychat.utils.enums.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【app_update(app发布表)】的数据库操作Service实现
* @createDate 2025-05-10 20:13:50
*/
@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate>
    implements AppUpdateService{

    @Resource
    private AppUpdateMapper appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    @Value("${contants.FILE_FOLDER_APP}")
    private String FILE_FOLDER_APP;

    @Value("${contants.EXE_SUFFIX}")
    private String EXE_SUFFIX;

    /**
     * 保存更新
     *
     * @param appUpdate 版本更新信息对象
     * @param file      更新文件
     */
    @Override
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
        // 判断文件类型参数是否合法
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (null == fileTypeEnum) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 不允许修改已经发布的版本
        AppUpdate dbAppUpdate = appUpdateMapper.selectById(appUpdate.getId());
        if (null != appUpdate.getId() && !AppUpdateStatusEnum.INIT.getStatus().equals(dbAppUpdate.getStatus())){
            throw new BusinessException("不允许修改已经发布的版本");
        }

        // 从数据库中查询已保存的最新的版本信息
        LambdaQueryWrapper<AppUpdate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AppUpdate::getVersion);
        List<AppUpdate> dbAppUpdateList = appUpdateMapper.selectList(queryWrapper);

        if (!dbAppUpdateList.isEmpty()) {
            // 判断新的版本号是否合法
            AppUpdate lastAppUpdate = dbAppUpdateList.get(0);
            long lastVersion = Long.parseLong(lastAppUpdate.getVersion().replace(".", ""));
            long currVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));
            // 新增
            if (null == appUpdate.getId() && currVersion <= lastVersion) {
                throw new BusinessException("新增：新版本的版本号必须大于历史版本号");
            }
            // 修改
            if ( null != appUpdate.getId() ) {
                long dbVersion = Long.parseLong(dbAppUpdate.getVersion().replace(".", ""));
                if (currVersion < dbVersion){
                    throw new BusinessException("修改：新版本的版本号不能变小");
                }
                for(AppUpdate dbUpdate : dbAppUpdateList) {
                    if(dbUpdate.getVersion().equals(appUpdate.getVersion()) && !dbUpdate.getId().equals(appUpdate.getId())) {
                        throw new BusinessException("修改：版本号已存在");
                    }
                }
            }
        }

        // 向数据库写入/修改新版本信息
        if (null == appUpdate.getId()){
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            appUpdateMapper.insert(appUpdate);
        }else {
            appUpdateMapper.updateById(appUpdate);
        }

        // 保存更新文件
        if (null != file){
            File folder = new File(appConfig.getProjectFolder() + FILE_FOLDER_APP);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file.transferTo(new File(folder.getPath() + "/" + appUpdate.getId()+ EXE_SUFFIX));
        }

    }

    /**
     * 获取更新列表
     *
     * @param startDate  日期筛选（开始时间）
     * @param endDate    日期筛选（结束时间）
     * @param pageNumber 页码
     * @param pageSize   页容量
     * @return List<AppUpdate>
     */
    @Override
    public Map<String, Object> loadUpdateList(String startDate, String endDate, Integer pageNumber, Integer pageSize) {
        // 判断页码参数是否合法
        if (null == pageNumber || pageNumber <= 0) {
            pageNumber = 1;
        }

        // 判断页容量参数是否合法
        if (null == pageSize || pageSize <= 0) {
            pageSize = 5;
        }

        // IPage接口的实现对象Page(当前页码, 页容量)
        Page<AppUpdate> page = new Page<>(pageNumber, pageSize);
        appUpdateMapper.loadUpdateList(page, startDate, endDate);

        // 获取当前页数据
        // List<AppUpdate> records = page.getRecords();
        // 封装分页数据
        Map<String, Object> pageResultData = PageUtils.getPageResultData(page);
        return pageResultData;
    }

    /**
     * 发布更新
     *
     * @param id           更新数据id
     * @param status       状态 0:未发布 1:灰度发布 2:全网发布
     * @param grayscaleUid 灰度uid
     */
    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        // 判断发布状态参数是否合法
        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByStatus(status);
        if (null == statusEnum) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 判断灰度发布参数是否合法
        if (AppUpdateStatusEnum.GRAYSCALE == statusEnum && StringUtils.isEmpty(grayscaleUid)){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 如果不是灰度发布 将灰度uid置空
        if (AppUpdateStatusEnum.GRAYSCALE != statusEnum) {
            grayscaleUid = "";
        }

        // 更新数据库数据
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setStatus(statusEnum.getStatus());
        appUpdate.setGrayscaleUid(grayscaleUid);
        appUpdateMapper.updateById(appUpdate);
    }

    /**
     * 获取最新版本数据
     *
     * @param userId     当前用户id
     * @param appVersion 当前客户端版本号
     * @return AppUpdate
     */
    @Override
    public AppUpdate getLatestUpdate(String userId, String appVersion) {
        return appUpdateMapper.selectLatestUpdate(userId, appVersion);
    }
}




