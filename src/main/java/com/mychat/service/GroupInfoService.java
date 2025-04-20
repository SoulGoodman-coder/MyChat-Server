package com.mychat.service;

import com.mychat.entity.po.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}
