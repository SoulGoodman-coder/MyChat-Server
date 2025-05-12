package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.SysSettingDto;
import com.mychat.redis.RedisComponent;
import com.mychat.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 系统设置后台管理相关controller
 */

@RestController("adminSysSettingController")
@RequestMapping("admin")
public class AdminSysSettingController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Value("${contants.FILE_FOLDER_FILE}")
    private String FILE_FOLDER_FILE;

    @Value("${contants.FILE_FOLDER_AVATAR_NAME}")
    private String FILE_FOLDER_AVATAR_NAME;

    @Value("${contants.PNG_SUFFIX}")
    private String PNG_SUFFIX;

    @Value("${contants.COVER_PNG_SUFFIX}")
    private String COVER_PNG_SUFFIX;


    /**
     * 获取系统设置
     * @return Result
     */
    @PostMapping("getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public Result getSysSetting(){

        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();

        return Result.ok(sysSettingDto);
    }

    /**
     * 保存系统设置
     * @param sysSettingDto
     * @param robotFile
     * @param robotCover
     * @return
     */
    @PostMapping("saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public Result saveSysSetting(SysSettingDto sysSettingDto,
                                 MultipartFile robotFile,
                                 MultipartFile robotCover) throws IOException {
        // 保存机器人头像
        if (null != robotFile){
            // 构建存储路径
            String baseFolder = appConfig.getProjectFolder() + FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            SysSettingDto settingDto = new SysSettingDto();
            String filePath = targetFileFolder.getPath() + "/" + settingDto.getRobotUid() + PNG_SUFFIX;

            robotFile.transferTo(new File(filePath));
            robotCover.transferTo(new File(filePath + COVER_PNG_SUFFIX));
        }

        // 将系统设置参数对象写入redis
        redisComponent.saveSysSetting(sysSettingDto);

        return Result.ok(null);
    }

}
