package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.po.AppUpdate;
import com.mychat.exception.BusinessException;
import com.mychat.service.AppUpdateService;
import com.mychat.utils.Result;
import com.mychat.utils.enums.AppUpdateStatusEnum;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 版本更新后台管理相关controller
 */

@RestController("adminAppUpdateController")
@RequestMapping("admin")
@Validated
public class AdminAppUpdateController {

    @Resource
    private AppUpdateService appUpdateService;

    /**
     * 获取更新列表
     * @param startDate     日期筛选（开始时间）
     * @param endDate       日期筛选（结束时间）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return Result
     */
    @PostMapping("loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public Result loadUpdateList(String startDate, String endDate, Integer pageNumber, Integer pageSize){

        Map<String, Object> appUpdateMap = appUpdateService.loadUpdateList(startDate, endDate, pageNumber, pageSize);

        return Result.ok(appUpdateMap);
    }

    /**
     * 保存更新
     * @param id            数据库表中的自增ID
     * @param version       更新版本号
     * @param updateDesc    更新描述
     * @param fileType      文件类型 0:本都文件 1:外链
     * @param outerLink     外链地址
     * @param file          更新文件
     * @return Result
     */
    @PostMapping("saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public Result saveUpdate(Integer id,
                             @NotBlank String version,
                             @NotBlank String updateDesc,
                             @NotNull Integer fileType,
                             String outerLink,
                             MultipartFile file) throws IOException {
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdate.setOuterLink(outerLink);

        appUpdateService.saveUpdate(appUpdate, file);

        return Result.ok(null);
    }

    /**
     * 删除更新
     * @param id        要删除的更新数据id
     * @return Result
     */
    @PostMapping("delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public Result delUpdate(@NotNull Integer id){

        // 不允许直接删除已经发布的版本
        AppUpdate dbAppUpdate = appUpdateService.getById(id);
        if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbAppUpdate.getStatus())){
            throw new BusinessException("不允许删除已经发布的版本");
        }

        appUpdateService.removeById(id);

        return Result.ok(null);
    }

    /**
     * 发布更新
     * @param id                更新数据id
     * @param status            状态 0:未发布 1:灰度发布 2:全网发布
     * @param grayscaleUid      灰度uid
     * @return
     */
    @PostMapping("postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public Result postUpdate(@NotNull Integer id, @NotNull Integer status, String grayscaleUid){

        appUpdateService.postUpdate(id, status, grayscaleUid);

        return Result.ok(null);
    }
}
