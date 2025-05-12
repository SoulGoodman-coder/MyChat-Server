package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.UserInfo;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.exception.BusinessException;
import com.mychat.service.UserInfoService;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.Result;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.ResultCodeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 用户信息相关controller
 */

@RestController("userInfoController")
@RequestMapping("userInfo")
@Validated         // 参数校验
public class UserInfoController extends BaseController{

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取当前用户信息
     * @param request       request
     * @return Result
     */
    @PostMapping("getUserInfo")
    @GlobalInterceptor
    public Result getUserInfo(HttpServletRequest request){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        UserInfo userInfo = userInfoService.getById(tokenUserInfoDto.getUserId());
        UserInfoVo userInfoVo = CopyUtils.copy(userInfo, UserInfoVo.class);
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());

        return Result.ok(userInfoVo);
    }

    /**
     * 修改用户信息
     * @param request           request
     * @param userInfo          新的用户信息对象
     * @param avatarFile        新用户头像
     * @param avatarCover       新用户头像缩略图
     * @return
     */
    @PostMapping("saveUserInfo")
    @GlobalInterceptor
    public Result saveUserInfo(HttpServletRequest request,
                               UserInfo userInfo,
                               MultipartFile avatarFile, // 新用户头像
                               MultipartFile avatarCover // 新用户头像缩略图
    ) throws IOException {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        if (!tokenUserInfoDto.getUserId().equals(userInfo.getUserId())){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 将用户不可修改的参数置空（防止接口攻击）
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);
        userInfo.setLastOffTime(null);

        userInfoService.updateUserInfo(userInfo, avatarFile, avatarCover);

        return Result.ok(tokenUserInfoDto);
    }

    /**
     * 修改密码
     * @param request       request
     * @param password      新密码
     * @return Result
     */
    @PostMapping("updatePassword")
    @GlobalInterceptor
    public Result updatePassword(HttpServletRequest request, @NotBlank @Pattern(regexp = AppConfig.REGEX_PASSWORD) String password){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setPassword(StringUtils.encodeMD5(password));
        userInfoService.updateById(userInfo);

        // TODO 强制退出 重新登录

        return Result.ok(null);
    }

    /**
     * 退出登录
     * @param request       request
     * @return Result
     */
    @PostMapping("logout")
    @GlobalInterceptor
    public Result logout(HttpServletRequest request){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        // TODO 退出登录 关闭ws连接

        return Result.ok(null);
    }

}
