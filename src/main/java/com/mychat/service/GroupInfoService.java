package com.mychat.service;

import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mychat.entity.po.UserContact;
import com.mychat.utils.enums.MessageTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author Administrator
* @description 针对表【group_info(群组表)】的数据库操作Service
* @createDate 2025-04-19 23:21:32
*/
public interface GroupInfoService extends IService<GroupInfo> {

    /**
     * 创建或修改群聊
     * @param groupInfo         群组信息
     * @param avatarFile     原群头像
     * @param avatarCover       群头像缩略图
     */
    void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    /**
     * 获取我创建的群组
     * @param userId    用户id
     * @return          List<GroupInfo>
     */
    List<GroupInfo> loadMyGroup(String userId);

    /**
     * 获取群聊详情
     * @param userId    当前用户id
     * @param groupId   群组id
     */
    GroupInfo getGroupInfo(String userId, String groupId);

    /**
     * 获取群人数
     * @param groupId   群组id
     * @return          群人数
     */
    Integer getGroupMemberCount(String groupId);

    /**
     * 获取群成员列表
     * @param groupId   群组id
     * @return          群成员列表
     */
    List<UserContact> getGroupUserContactList(String groupId);

    /**
     * 获取群组列表
     * @param groupId           群组id
     * @param groupNameFuzzy    群组名称（支持模糊搜索）
     * @param groupOwnerId      群主id
     * @param pageNumber        页码
     * @param pageSize          页容量
     * @return List<GroupInfo>
     */
    List<GroupInfo> loadGroupList(String groupId, String groupNameFuzzy, String groupOwnerId, Integer pageNumber, Integer pageSize);

    /**
     * 解散群组
     * @param userId            当前用户id
     * @param groupId           要紧解散的群组id
     */
    void dissolutionGroup(String userId, String groupId);

    /**
     * 群组添加或移除人员
     * @param tokenUserInfoDto  用户token对象
     * @param groupId           群组id
     * @param selectContacts    选择的要操作的人员
     * @param opType            操作类型 添加 移除
     */
    void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer opType);

    /**
     * 退群（或被踢出群组）
     * @param userId            用户id
     * @param groupId           群组id
     * @param messageTypeEnum   消息类型
     */
    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);
}
