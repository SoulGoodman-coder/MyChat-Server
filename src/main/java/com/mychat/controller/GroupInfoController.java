package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.GroupInfo;
import com.mychat.entity.po.UserContact;
import com.mychat.entity.vo.GroupInfoVo;
import com.mychat.service.GroupInfoService;
import com.mychat.utils.Result;
import com.mychat.utils.enums.MessageTypeEnum;
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
import java.util.List;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 群聊信息相关controller
 */

@RestController("groupInfoController")
@RequestMapping("group")
@Validated         // 参数校验
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
     * @return Result
     */
    @PostMapping("saveGroup")
    @GlobalInterceptor
    public Result saveGroup(HttpServletRequest request,
                            String groupId,
                            @NotBlank String groupName,
                            String groupNotice,
                            @NotNull Integer joinType,
                            MultipartFile avatarFile, // 原群头像(参数不能为空)
                            MultipartFile avatarCover) throws IOException {   // 群头像缩略图(参数不能为空)

        System.out.println("request = " + request + ", groupId = " + groupId + ", groupName = " + groupName + ", groupNotice = " + groupNotice + ", joinType = " + joinType + ", avatarFile = " + avatarFile + ", avatarCover = " + avatarCover);

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

    /**
     * 获取我创建的群组
     * @return Result
     */
    @PostMapping("loadMyGroup")
    @GlobalInterceptor
    public Result loadMyGroup(HttpServletRequest request) {

        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        List<GroupInfo> groupInfoList = groupInfoService.loadMyGroup(tokenUserInfoDto.getUserId());

        return Result.ok(groupInfoList);
    }

    /**
     * 获取群聊详情
     * @param groupId       群组id
     * @return              Result
     */
    @PostMapping("getGroupInfo")
    @GlobalInterceptor
    public Result getGroupInfo(HttpServletRequest request, @NotBlank String groupId) {
        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        // 获取群聊详情
        GroupInfo groupInfo = groupInfoService.getGroupInfo(tokenUserInfoDto.getUserId(), groupId);

        // 获取群人数
        Integer memberCount = groupInfoService.getGroupMemberCount(groupId);
        groupInfo.setMemberCount(memberCount);

        return Result.ok(groupInfo);
    }

    /**
     * 获取聊天会话群聊详情
     * @param groupId       群组id
     * @return              Result
     */
    @PostMapping("getGroupInfo4Chat")
    @GlobalInterceptor
    public Result getGroupInfo4Chat(HttpServletRequest request, @NotBlank String groupId) {
        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        // 获取群聊详情
        GroupInfo groupInfo = groupInfoService.getGroupInfo(tokenUserInfoDto.getUserId(), groupId);

        // 获取群成员列表
        List<UserContact> userContactList = groupInfoService.getGroupUserContactList(groupId);

        // 封装返回对象
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        groupInfoVo.setGroupInfo(groupInfo);
        groupInfoVo.setUserContactList(userContactList);

        return Result.ok(groupInfoVo);
    }

    /**
     * 群组添加或移除人员
     * @param request           request
     * @param groupId           群组id
     * @param selectContacts    选择的要操作的人员
     * @param opType            操作类型 0:添加 1:移除
     * @return
     */
    @PostMapping("addOrRemoveGroupUser")
    @GlobalInterceptor
    public Result addOrRemoveGroupUser(HttpServletRequest request,
                                       @NotBlank String groupId,
                                       @NotBlank String selectContacts,
                                       @NotNull Integer opType) {
        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto, groupId, selectContacts, opType);

        return Result.ok(null);
    }

    /**
     * 退出群聊
     * @param request       request
     * @param groupId       要退出的群组id
     * @return Result
     */
    @PostMapping("leaveGroup")
    @GlobalInterceptor
    public Result leaveGroup(HttpServletRequest request,
                             @NotBlank String groupId) {
        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);

        return Result.ok(null);
    }

    /**
     * 解散群聊
     * @param request       request
     * @param groupId       要解散的群组id
     * @return Result
     */
    @PostMapping("dissolutionGroup")
    @GlobalInterceptor
    public Result dissolutionGroup(HttpServletRequest request,
                                   @NotBlank String groupId) {
        // 从请求头中获取token，封装到TokenUserInfoDto对象中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        groupInfoService.dissolutionGroup(tokenUserInfoDto.getUserId(), groupId);

        return Result.ok(null);
    }

}
