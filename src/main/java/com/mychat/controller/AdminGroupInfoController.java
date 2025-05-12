package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.po.GroupInfo;
import com.mychat.exception.BusinessException;
import com.mychat.service.GroupInfoService;
import com.mychat.utils.Result;
import com.mychat.utils.enums.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 群组后台管理相关controller
 */

@RestController("adminGroupInfoController")
@RequestMapping("admin")
public class AdminGroupInfoController {
    @Resource
    private GroupInfoService groupInfoService;

    /**
     * 获取群组列表
     * @param groupId           群组id
     * @param groupNameFuzzy    群组名称（支持模糊搜索）
     * @param groupOwnerId      群主id
     * @param pageNumber        页码
     * @param pageSize          页容量
     * @return Result
     */
    @PostMapping("loadGroupList")
    @GlobalInterceptor(checkAdmin = true)
    public Result loadGroupList(String groupId, String groupNameFuzzy, String groupOwnerId, Integer pageNumber, Integer pageSize){

        List<GroupInfo> groupInfoList = groupInfoService.loadGroupList(groupId, groupNameFuzzy, groupOwnerId, pageNumber, pageSize);

        return Result.ok(groupInfoList);
    }

    /**
     * 解散群组
     * @param groupId           要解散的群组id
     * @return Result
     */
    @PostMapping("dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public Result dissolutionGroup(String groupId){
        // 判断群组是否存在
        GroupInfo groupInfo = groupInfoService.getById(groupId);
        if (null == groupInfo){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(), groupId);

        return Result.ok(null);
    }

}
