package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.AppUpdate;
import com.mychat.entity.vo.AppUpdateVo;
import com.mychat.service.AppUpdateService;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.Result;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.AppUpdateFileTypeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 版本更新相关controller
 */

@RestController("appUpdateController")
@RequestMapping("update")
public class AppUpdateController extends BaseController{

    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private AppConfig appConfig;

    @Value("${contants.FILE_FOLDER_APP}")
    private String FILE_FOLDER_APP;

    @Value("${contants.EXE_SUFFIX}")
    private String EXE_SUFFIX;

    @Value("${contants.APP_NAME}")
    private String APP_NAME;

    /**
     * 检测更新
     * @param request       request
     * @param appVersion    当前客户端版本号
     * @return Result
     */
    @PostMapping("checkVersion")
    @GlobalInterceptor()
    public Result checkVersion(HttpServletRequest request, String appVersion){
        // 传入参数有误，则跳过更新操作
        if (StringUtils.isEmpty(appVersion)){
            return Result.ok(null);
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        AppUpdate appUpdate = appUpdateService.getLatestUpdate(tokenUserInfoDto.getUserId(), appVersion);

        // 封装返回给前端的数据对象
        AppUpdateVo appUpdateVo = CopyUtils.copy(appUpdate, AppUpdateVo.class);

        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(appUpdate.getFileType())){
            File file = new File(appConfig.getProjectFolder() + FILE_FOLDER_APP + appUpdate.getId() + EXE_SUFFIX);
            appUpdateVo.setSize(file.length());
        }else {
            appUpdateVo.setSize(0L);
        }

        appUpdateVo.setFileName(APP_NAME + "." + appUpdate.getVersion() + EXE_SUFFIX);
        return Result.ok(appUpdateVo);
    }
}
