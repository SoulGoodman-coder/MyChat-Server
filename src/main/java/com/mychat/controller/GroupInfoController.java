package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.GroupInfo;
import com.mychat.service.GroupInfoService;
import com.mychat.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 联系人controller
 */

@RestController("groupInfoController")
@RequestMapping("group")
@Validated
public class GroupInfoController extends BaseController{

    @Resource
    private GroupInfoService groupInfoService;

    /**
     * 创建或修改群聊
     * @param groupId       群组id
     * @param groupName     群组名称
     * @param groupNotice   群公告
     * @param joinType      加入方式
     * @param avatarFile 原群头像
     * @param avatarCover   群头像缩略图
     * @return
     */
    @PostMapping("saveGroup")
    @GlobalInterceptor
    public Result saveGroup(HttpServletRequest request,
                            String groupId,
                            @NotBlank String groupName,
                            String groupNotice,
                            @NotNull Integer joinType,
                            MultipartFile avatarFile, // 原群头像
                            MultipartFile avatarCover) throws IOException {   // 群头像缩略图

        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());

        groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);

        return Result.ok(null);
    }

}
